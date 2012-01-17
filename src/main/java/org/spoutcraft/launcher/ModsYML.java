package org.spoutcraft.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.util.config.Configuration;

public class ModsYML {
	private static volatile boolean updated = false;
	private static File modsYML = new File(PlatformUtils.getWorkingDirectory(), "technic" + File.separator + "mods.yml");
	private static Object key = new Object();

	public static Configuration getModsYML() {
		updateModsYMLCache();
		Configuration config = new Configuration(modsYML);
		config.load();
		return config;
	}
	
	public static void updateModsYMLCache() {
		if (!updated) {
			synchronized(key) {
				String urlName = MirrorUtils.getMirrorUrl("mods.yml", "http://technic.freeworldsgaming.com/mods.yml", null);
				if (urlName != null) {
	
					try {
						URL url = new URL(urlName);
						URLConnection con = (url.openConnection());
						System.setProperty("http.agent", "");
						con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
						GameUpdater.copy(con.getInputStream(), new FileOutputStream(modsYML));
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
				updated = true;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> getTechnicMods()
	{
		Configuration config = getModsYML();
		Map<Integer, Object> mods = (Map<Integer, Object>) config.getProperty("mods");
		
		List<Map<String, String>> modList = new ArrayList<Map<String, String>>();
		
		Map<String, String> modDetails = null;
//		int index = 0;
		for (Integer i : mods.keySet())
		{
			Map<String, String> map = (Map<String, String>) mods.get(i);
			
			modDetails = new HashMap<String, String>();
			modDetails.put("name", String.valueOf(map.get("name")));
			modDetails.put("description", String.valueOf(map.get("description")));
			modDetails.put("installtype", String.valueOf(map.get("installtype")));
			modDetails.put("filenames", String.valueOf(map.get("filenames")));
			if (map.containsKey("groupid"))
				modDetails.put("groupid", String.valueOf(map.get("groupid")));
			
			modList.add(modDetails);
		}
		
		return modList;		
	}
}
