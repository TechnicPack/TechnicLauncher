/*
 * This file is part of Technic Launcher.
 *
 * Copyright (c) 2013-2013, Technic <http://www.technicpack.net/>
 * Technic Launcher is licensed under the Spout License Version 1.
 *
 * Technic Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Technic Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */

package org.spoutcraft.launcher.util;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;

import org.spoutcraft.launcher.exceptions.DownloadException;
import org.spoutcraft.launcher.exceptions.PermissionDeniedException;

public class Download implements Runnable {
	private static final long TIMEOUT = 30000;

	private URL url;
	private long size = -1;
	private long downloaded = 0;
	private String outPath;
	private DownloadListener listener;
	private Result result = Result.FAILURE;
	private File outFile = null;
	private Exception exception = null;;

	public Download(String url, String outPath) throws MalformedURLException {
		this.url = new URL(url);
		this.outPath = outPath;
	}

	public float getProgress() {
		return ((float) downloaded / size) * 100;
	}

	public Exception getException() {
		return exception;
	}

	@Override
	@SuppressWarnings("unused")
	public void run(){
		ReadableByteChannel rbc = null;
		FileOutputStream fos = null;
		try {
			URLConnection conn = url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");
			HttpURLConnection.setFollowRedirects(true);
			conn.setUseCaches(false);
			((HttpURLConnection)conn).setInstanceFollowRedirects(true);
			int response = ((HttpURLConnection)conn).getResponseCode();
			InputStream in = getConnectionInputStream(conn);

			size = conn.getContentLength();
			outFile = new File(outPath);
			outFile.delete();

			rbc = Channels.newChannel(in);
			fos = new FileOutputStream(outFile);

			stateChanged();

			Thread progress = new MonitorThread(Thread.currentThread(), rbc);
			progress.start();

			fos.getChannel().transferFrom(rbc, 0, size > 0 ? size : Integer.MAX_VALUE);
			in.close();
			rbc.close();
			progress.interrupt();
			if (size > 0) {
				if (size == outFile.length()) {
					result = Result.SUCCESS;
				}
			} else {
				result = Result.SUCCESS;
			}
		} catch (PermissionDeniedException e) {
			exception = e;
			result = Result.PERMISSION_DENIED;
		} catch (DownloadException e) {
			exception = e;
			result = Result.FAILURE;
		} catch (Exception e) {
			exception = e;
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(rbc);
		}
	}

	protected InputStream getConnectionInputStream(final URLConnection urlconnection) throws DownloadException {
		final AtomicReference<InputStream> is = new AtomicReference<InputStream>();

		for (int j = 0; (j < 3) && (is.get() == null); j++) {
			StreamThread stream = new StreamThread(urlconnection, is);
			stream.start();
			int iterationCount = 0;
			while ((is.get() == null) && (iterationCount++ < 5)) {
				try {
					stream.join(1000L);
				} catch (InterruptedException ignore) {
				}
			}

			if (stream.permDenied.get()) {
				throw new PermissionDeniedException("Permission denied!");
			}

			if (is.get() != null) {
				break;
			}
			try {
				stream.interrupt();
				stream.join();
			} catch (InterruptedException ignore) {
			}
		}

		if (is.get() == null) {
			throw new DownloadException("Unable to download file from " + urlconnection.getURL());
		}
		return new BufferedInputStream(is.get());
	}

	private void stateChanged() {
		if (listener != null) listener.stateChanged(outPath, getProgress());
	}

	public void setListener(DownloadListener listener) {
		this.listener = listener;
	}

	public Result getResult() {
		return result;
	}

	public File getOutFile() {
		return outFile;
	}

	private static class StreamThread extends Thread {
		private final URLConnection urlconnection;
		private final AtomicReference<InputStream> is;
		public final AtomicBoolean permDenied = new AtomicBoolean(false);
		public StreamThread(URLConnection urlconnection, AtomicReference<InputStream> is) {
			this.urlconnection = urlconnection;
			this.is = is;
		}

		@Override
		public void run() {
			try {
				is.set(urlconnection.getInputStream());
			} catch (SocketException e) {
				if (e.getMessage().equalsIgnoreCase("Permission denied: connect")) {
					permDenied.set(true);
				}
			} catch (IOException ignore) { }
		}
	}

	private class MonitorThread extends Thread {
		private final ReadableByteChannel rbc;
		private final Thread downloadThread;
		private long last = System.currentTimeMillis();
		public MonitorThread(Thread downloadThread, ReadableByteChannel rbc) {
			super("Download Monitor Thread");
			this.setDaemon(true);
			this.rbc = rbc;
			this.downloadThread = downloadThread;
		}

		@Override
		public void run() {
			while (!this.isInterrupted()) {
				long diff = outFile.length() - downloaded;
				downloaded = outFile.length();
				if (diff == 0) {
					if ((System.currentTimeMillis() - last) > TIMEOUT) {
						if (listener != null) {
							listener.stateChanged("Download Failed", getProgress());
						}
						try {
							rbc.close();
							downloadThread.interrupt();
						} catch (IOException ignore) { }
						return;
					}
				} else {
					last = System.currentTimeMillis();
				}

				stateChanged();
				try {
					sleep(50);
				} catch (InterruptedException ignore) {
					return;
				}
			}
		}
	}

	public enum Result {
		SUCCESS,
		FAILURE,
		PERMISSION_DENIED,
	}
}
