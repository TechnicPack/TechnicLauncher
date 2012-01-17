package org.spoutcraft.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import org.spoutcraft.launcher.async.Download;
import org.spoutcraft.launcher.async.DownloadListener;

public class DownloadUtils {
	public static Download downloadFile(String url, String output, String cacheName, String md5, DownloadListener listener) throws IOException {
		int tries = SettingsUtil.getLoginTries();
		File outputFile = null;
		Download download = null;
		while (tries > 0) {
			System.out.println("Starting download of " + url + ", with " + tries + " tries remaining");
			tries--;
			download = new Download(url, output);
			download.setListener(listener);
			download.run();
			if (!download.isSuccess()) {
				if (download.getOutFile() != null) {
					download.getOutFile().delete();
				}
				System.err.println("Download of " + url + " Failed!");
				if (listener != null)
					listener.stateChanged("Download Failed, retries remaining: " + tries, 0F);
			}
			else {
				if (md5 != null) {
					String resultMD5 = MD5Utils.getMD5(download.getOutFile());
					System.out.println("Expected MD5: " + md5 + " Calculated MD5: " + resultMD5);
					if (resultMD5.equals(md5)) {
						outputFile = download.getOutFile();
						break;
					}
				}
				else {
					outputFile = download.getOutFile();
					break;
				}
			}
		}
		if (outputFile == null) {
			throw new IOException("Failed to download " + url);
		}
		if (cacheName != null) {
			GameUpdater.copy(outputFile, new File(GameUpdater.binCacheDir, cacheName));
		}
		return download;
	}
	
	public static Download downloadFile(String url, String output, String cacheName) throws IOException {
		return downloadFile(url, output, cacheName, null, null);
	}

	public static Download downloadFile(String url, String output) throws IOException {
		return downloadFile(url, output, null);
	}
}
