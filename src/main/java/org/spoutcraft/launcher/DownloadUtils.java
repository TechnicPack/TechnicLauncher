package org.spoutcraft.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.spoutcraft.launcher.async.Download;
import org.spoutcraft.launcher.async.DownloadListener;

public class DownloadUtils {

	public static Download downloadFile(String url, String output, String cacheName, String md5, DownloadListener listener) throws IOException {
		if (Main.isOffline) return null;
		int tries = SettingsUtil.getLoginTries();
		File outputFile = new File(output);
		File tempfile = File.createTempFile("file", null, GameUpdater.tempDir);
		tempfile.mkdirs();
		Download download = null;
		boolean areFilesIdentical = tempfile.getPath().equalsIgnoreCase(outputFile.getPath());
		while (tries > 0) {
			Util.logi("Starting download of '%s', with %s trie(s) remaining", url, tries);
			tries--;
			download = new Download(url, tempfile.getPath());
			download.setListener(listener);
			download.run();
			if (!download.isSuccess()) {
				if (download.getOutFile() != null) {
					download.getOutFile().delete();
				}
				Util.log("Download of " + url + " Failed!");
				if (listener != null) {
					listener.stateChanged("Download Failed, retries remaining: " + tries, 0F);
				}
			} else {
				String fileMD5 = MD5Utils.getMD5(download.getOutFile());
				Util.logi("Copying: %s to: %s", tempfile, outputFile);
				if (!areFilesIdentical) {
					GameUpdater.copy(tempfile, outputFile);
				}
			}
		}

		if (cacheName != null) {
			if (tempfile.exists()) {
				GameUpdater.copy(tempfile, new File(GameUpdater.cacheDir, cacheName));
			} else {
				Util.log("Could not copy file to cache: %s", tempfile);
			}
		}

		if (!areFilesIdentical) {
			tempfile.delete();
		}
		return download;
	}

	public static Download downloadFile(String url, String output, String cacheName) throws IOException {
		return downloadFile(url, output, cacheName, null, null);
	}

	public static Download downloadFile(String url, String output) throws IOException {
		return downloadFile(url, output, null, null, null);
	}

	private static int	filesToDownload	= 0;
	private static int	filesDownloaded	= 0;

	public static int downloadFiles(Map<String, String> downloadFileList, long timeout, TimeUnit unit) {
		if (Main.isOffline) return 0;
		filesToDownload = downloadFileList.size();
		filesDownloaded = 0;

		ExecutorService es = Executors.newCachedThreadPool();
		for (final Map.Entry<String, String> file : downloadFileList.entrySet()) {
			es.execute(new Runnable() {

				@Override
				public void run() {
					Download downloadFile = null;
					try {
						downloadFile = downloadFile(file.getKey(), file.getValue());
						if (downloadFile != null && downloadFile.isSuccess()) {
							filesDownloaded++;
							return;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					Util.log("File '%s' failed to download.", downloadFile.getOutFile());
				}
			});
		}
		es.shutdown();
		try {
			if (es.awaitTermination(timeout, unit)) { return filesDownloaded; }
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static boolean downloadFile(String relativePath) {
		if (Main.isOffline) return false;

		URL url = null;
		File tempFile = null;
		try {
			String mirrorUrl = MirrorUtils.getMirrorUrl(relativePath, null);
			url = new URL(mirrorUrl);
			URLConnection con = (url.openConnection());

			System.setProperty("http.agent", "");
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");

			tempFile = File.createTempFile("Modpack", null);

			// Download to temporary file
			OutputStream baos = new FileOutputStream(tempFile);
			// new FileOutputStream(tempFile)
			if (GameUpdater.copy(con.getInputStream(), baos) <= 0) {
				Util.log("Download URL was empty: '%s'", url);
				return false;
			}

			// If no Exception then file loaded fine, copy to output file
			GameUpdater.copy(tempFile, new File(relativePath));
			tempFile.delete();

			return true;
		} catch (MalformedURLException e) {
			Util.log("Download URL badly formed: '%s'", url);
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			Util.log("Could not write to temp file: '%s'", tempFile);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
