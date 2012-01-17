package org.spoutcraft.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.async.DownloadListener;

public class MirrorUtils {
	private static boolean updated = false;
	private static File mirrorsYML = new File(PlatformUtils.getWorkingDirectory(), "technic" + File.separator + "mirrors.yml");
	private static final Random rand = new Random();
	
	public static String getMirrorUrl(String mirrorURI, String fallbackUrl, DownloadListener listener) {
		try {
			Map<String, Integer> mirrors = getMirrors();
			Set<Entry<String, Integer>> set = mirrors.entrySet();
			
			ArrayList<String> goodMirrors = new ArrayList<String>(mirrors.size());
			Iterator<Entry<String, Integer>> iterator = set.iterator();
			while (iterator.hasNext()) {
				Entry<String, Integer> e = iterator.next();
				String url = e.getKey();
				String mirror = (!url.contains("github.com")) ? "http://" + e.getKey() + "/" + mirrorURI : "https://" + e.getKey() + "/" + mirrorURI;
				if (isAddressReachable(mirror)) {
					goodMirrors.add(e.getKey());
				}
			}
			//safe fast return
			if (goodMirrors.size() == 1) {
				return "http://" + goodMirrors.get(0) + "/" + mirrorURI;
			}
			
			//the for loop may fail if random numbers are unlucky, in which case we want to try again
			while (goodMirrors.size() > 0) {
				int random = rand.nextInt(10 * mirrors.size());
				int index = random / 10;
				float progress = 0F;
				for (int i = index; i < goodMirrors.size() + index; i++) {
					int j = i;
					if (j >= goodMirrors.size()) j-= goodMirrors.size();
						int roll = rand.nextInt(100);
						String url = goodMirrors.get(j);
						int chance = mirrors.get(url);
						if (roll < chance) {
							String mirror = (!url.contains("github.com")) ? "http://" + url + "/" + mirrorURI : "https://" + url + "/" + mirrorURI;
							System.out.println("Using mirror: " + mirror);
							if (listener != null) {
								listener.stateChanged("Contacting Mirrors...", 100F);
							}
							return mirror;
						}
					else {
						progress += 100F / mirrors.size();
						if (listener != null) {
							listener.stateChanged("Contacting Mirrors...", progress);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("All mirrors failed, reverting to default");
		return fallbackUrl;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Integer> getMirrors() {
		Configuration config = getMirrorsYML();
		return (Map<String, Integer>) config.getProperty("mirrors");
	}
	
	public static boolean isAddressReachable(String url) {
		try {
			if (url.contains("https")) {
				HttpsURLConnection urlConnect = (HttpsURLConnection)(new URL(url).openConnection());
				urlConnect.setInstanceFollowRedirects(true);
				
				urlConnect.setRequestMethod("HEAD");
				int responseCode = urlConnect.getResponseCode();
				return (responseCode == HttpURLConnection.HTTP_OK);
			} else {			
				HttpURLConnection urlConnect = (HttpURLConnection)(new URL(url).openConnection());
				urlConnect.setInstanceFollowRedirects(true);
				
				urlConnect.setRequestMethod("HEAD");
				int responseCode = urlConnect.getResponseCode();
				return (responseCode == HttpURLConnection.HTTP_OK);
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	public static Configuration getMirrorsYML() {
		updateMirrorsYMLCache();
		Configuration config = new Configuration(mirrorsYML);
		config.load();
		return config;
	}
	
	public static void updateMirrorsYMLCache() {
		if (!updated) {
			try {
				if (isAddressReachable("http://technic.freeworldsgaming.com/mirrors.yml")) {
				URL url = new URL("http://technic.freeworldsgaming.com/mirrors.yml");
					URLConnection con = (url.openConnection());
					System.setProperty("http.agent", "");
					con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
					GameUpdater.copy(con.getInputStream(), new FileOutputStream(mirrorsYML));
				}
				else
				{
					return;
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			updated = true;
		}
	}
}
