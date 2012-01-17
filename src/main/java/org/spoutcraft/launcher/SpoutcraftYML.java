package org.spoutcraft.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.util.config.Configuration;

public class SpoutcraftYML {
	private static volatile boolean updated = false;
	private static File spoutcraftYML = new File(PlatformUtils.getWorkingDirectory(), "technic" + File.separator + "technic.yml");
	private static Object key = new Object();

	public static Configuration getSpoutcraftYML() {
		updateSpoutcraftYMLCache();
		Configuration config = new Configuration(spoutcraftYML);
		config.load();
		return config;
	}
	
	public static void updateSpoutcraftYMLCache() {
		if (!updated) {
			synchronized(key) {
				String urlName = MirrorUtils.getMirrorUrl("technic.yml", "http://technic.freeworldsgaming.com/technic.yml", null);
				if (urlName != null) {
	
					try {
						String selected = null;
						if (spoutcraftYML.exists()) {
							try {
								Configuration config = new Configuration(spoutcraftYML);
								config.load();
								selected = config.getString("current");
								if (selected == "-1") selected = config.getString("recommended");
							}
							catch (Exception ex){
								ex.printStackTrace();
							}
						}
	
						URL url = new URL(urlName);
						URLConnection con = (url.openConnection());
						System.setProperty("http.agent", "");
						con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
						GameUpdater.copy(con.getInputStream(), new FileOutputStream(spoutcraftYML));
	
						Configuration config = new Configuration(spoutcraftYML);
						config.load();
						config.setProperty("current", selected);
						config.setProperty("launcher", Main.build);
						config.save();
						
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
				updated = true;
			}
		}
	}
}
