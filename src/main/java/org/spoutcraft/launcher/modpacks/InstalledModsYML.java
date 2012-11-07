package org.spoutcraft.launcher.modpacks;

import java.io.File;
import java.util.Map;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.GameUpdater;

public class InstalledModsYML {

	private static final String		INSTALLED_MODS_YML	= "installedMods.yml";

	private static Configuration	installedModsConfig;
	private static File						installedModsLocation;

	public static File getInstalledModsYmlFile() {
		return new File(GameUpdater.modpackDir, INSTALLED_MODS_YML);
	}

	public static Configuration getInstalledModsConfig() {
		File installedModsYmlFile = getInstalledModsYmlFile();
		if (installedModsConfig == null || installedModsLocation.compareTo(installedModsYmlFile) != 0) {
			installedModsLocation = installedModsYmlFile;
			installedModsConfig = new Configuration(installedModsLocation);
			installedModsConfig.load();
		}
		return installedModsConfig;
	}

	public static boolean setInstalledModVersion(String modName, String version) {
		getInstalledModsConfig().setProperty(getModPath(modName), version);
		return getInstalledModsConfig().save();
	}

	public static String getInstalledModVersion(String modName) {
		return (String) getInstalledModsConfig().getProperty(getModPath(modName));
	}

	private static String getModPath(String modName) {
		return String.format("mods.%s", modName);
	}

	public static boolean removeMod(String modName) {
		getInstalledModsConfig().removeProperty(getModPath(modName));
		return getInstalledModsConfig().save();
	}

        @SuppressWarnings("unchecked")
	public static Map<String, String> getInstalledMods() {
		return (Map<String, String>) getInstalledModsConfig().getProperty("mods");
	}
}
