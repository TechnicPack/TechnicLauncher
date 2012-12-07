package org.spoutcraft.launcher;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.util.config.Configuration;

public class MinecraftYML {
	private static final String			MINECRAFT_YML	= "minecraft.yml";
	private static volatile boolean	updated				= false;
	private static String						latest				= null;
	private static String						recommended		= null;
	private static final Object			key						= new Object();
	private static Configuration		config				= null;
	private static File							configFile		= null;

	public static Configuration getMinecraftYML() {
		updateMinecraftYMLCache();
		return getConfig();
	}

	public static File getConfigFile() {
		return new File(GameUpdater.modpackDir, MINECRAFT_YML);
	}

	public static Configuration getConfig() {
		File currentConfigFile = getConfigFile();
		if (config == null || configFile.compareTo(currentConfigFile) != 0) {
			configFile = currentConfigFile;
			config = new Configuration(configFile);
			config.load();
		}
		return config;
	}

	public static String getLatestMinecraftVersion() {
		updateMinecraftYMLCache();
		return latest;
	}

	public static String getRecommendedMinecraftVersion() {
		updateMinecraftYMLCache();
		return recommended;
	}

	public static void setInstalledVersion(String version) {
		Configuration config = getMinecraftYML();
		config.setProperty("current", version);
		config.save();
	}

	public static String getInstalledVersion() {
		Configuration config = getMinecraftYML();
		return config.getString("current");
	}

	public static Set<String> getMinecraftVersions() {
		return ((Map<String, Map<String, String>>) config.getProperty("versions")).keySet();
	}

	public static Set<String> getCachedMinecraftVersions() {
		Set<String> minecraftVersions = new HashSet<String>();
		for (String filename : GameUpdater.cacheDir.list()) {
			if (!filename.startsWith("minecraft_"))
				continue;
			minecraftVersions.add(filename.split("_|.jar")[1]);
		}
		return minecraftVersions;
	}

	public static String getLatestCachedMinecraft() {
		String latestVersion = "0";
		for (String nextVersion : getCachedMinecraftVersions()) {
			if (compareVersions(latestVersion, nextVersion) < 0)
				latestVersion = nextVersion;
		}
		return latestVersion == "0" ? null : latestVersion;
	}

	public static int compareVersions(String version1, String version2) {
		String[] vals1 = version1.split("\\.");
		String[] vals2 = version2.split("\\.");
		int i=0;
		while(i<vals1.length&&i<vals2.length&&vals1[i].equals(vals2[i])) {
		  i++;
		}

		if (i<vals1.length&&i<vals2.length) {
		    int diff = new Integer(vals1[i]).compareTo(new Integer(vals2[i]));
		    return diff<0?-1:diff==0?0:1;
		}

		return vals1.length<vals2.length?-1:vals1.length==vals2.length?0:1;
	}

	public static void updateMinecraftYMLCache() {
		if (!updated || !getConfigFile().exists()) {
			synchronized (key) {
				String current = null;
				if (getConfigFile().exists()) {
					try {
						current = getConfig().getString("current");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				if (YmlUtils.downloadYmlFile(MINECRAFT_YML, "http://technic.freeworldsgaming.com/minecraft.yml", getConfigFile())) {
					// GameUpdater.copy(getConfigFile(), output)
					config = null;
					Configuration config = getConfig();
					latest = config.getString("latest");
					recommended = config.getString("recommended");
					if (current != null) {
						config.setProperty("current", current);
						config.save();
					}
				}
				updated = true;
			}
		}
	}
}
