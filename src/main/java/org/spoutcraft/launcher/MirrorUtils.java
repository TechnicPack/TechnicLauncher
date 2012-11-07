package org.spoutcraft.launcher;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.async.DownloadListener;

public class MirrorUtils {

	public static final String[]	MIRRORS_URL	= { "https://raw.github.com/Triforce1/Technic/master/mirrors.yml" };
	public static File						mirrorsYML	= new File(GameUpdater.workDir, "mirrors.yml");
	private static boolean				updated			= false;
	private static final Random		rand				= new Random();

	public static String getMirrorUrl(String mirrorURI, String fallbackUrl, DownloadListener listener) {
		try { 
			if (Main.isOffline) return null;

			Map<String, Integer> mirrors = getMirrors();
			Set<Entry<String, Integer>> set = mirrors.entrySet();

			int total = 0;
			Iterator<Entry<String, Integer>> iterator = set.iterator();
			while (iterator.hasNext()) {
				total += iterator.next().getValue();
			}

			int random = rand.nextInt(total);

			int count = 0;
			boolean isFinished = false;
			iterator = set.iterator();
			Entry<String, Integer> current = null;
			while (!isFinished) {
				while (iterator.hasNext()) {
					current = iterator.next();
					count += current.getValue();
					String url = current.getKey();
					if (count > random) {
						String mirror = (!url.contains("github.com")) ? "http://" + url + "/" + mirrorURI : "https://" + url + "/" + mirrorURI;
						if (isAddressReachable(mirror)) {
							return mirror;
						} else {
							break;
						}
					}
				}

				if (set.size() == 1) {
					return null;
				} else {
					total -= current.getValue();
					random = rand.nextInt(total);
					set.remove(current);
					iterator = set.iterator();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("All mirrors failed, reverting to default");
		return fallbackUrl;
	}

	public static String getMirrorUrl(String mirrorURI, String fallbackUrl) {
		return getMirrorUrl(mirrorURI, fallbackUrl, null);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Integer> getMirrors() {
		Configuration config = getMirrorsYML();
		return (Map<String, Integer>) config.getProperty("mirrors");
	}

	public static boolean isAddressReachable(String url) {
		URLConnection urlConnection = null;
		try {
			urlConnection = new URL(url).openConnection();
			if (url.contains("https")) {
				HttpsURLConnection urlConnect = (HttpsURLConnection) urlConnection;
				urlConnect.setConnectTimeout(5000);
				urlConnect.setReadTimeout(30000);
				urlConnect.setInstanceFollowRedirects(false);
				urlConnect.setRequestMethod("HEAD");
				int responseCode = urlConnect.getResponseCode();
				urlConnect.disconnect();
				urlConnect = null;
				return (responseCode == HttpURLConnection.HTTP_OK);
			} else {
				HttpURLConnection urlConnect = (HttpURLConnection) urlConnection;
				urlConnect.setConnectTimeout(5000);
				urlConnect.setReadTimeout(30000);
				urlConnect.setInstanceFollowRedirects(false);
				urlConnect.setRequestMethod("HEAD");
				int responseCode = urlConnect.getResponseCode();
				urlConnect.disconnect();
				urlConnect = null;
				return (responseCode == HttpURLConnection.HTTP_OK);
			}
		} catch (Exception e) {
		} finally {
			if (urlConnection != null) {
				urlConnection = null;
			}
		}
		return false;
	}

	public static Configuration getMirrorsYML() {
		updateMirrorsYMLCache();
		Configuration config = new Configuration(mirrorsYML);
		config.load();
		return config;
	}

	public static void updateMirrorsYMLCache() {
		if (updated) { return; }
		updated = true;
		for (String urlentry : MIRRORS_URL) {
			if (YmlUtils.downloadMirrorsYmlFile(urlentry)) { return; }
		}
	}
}
