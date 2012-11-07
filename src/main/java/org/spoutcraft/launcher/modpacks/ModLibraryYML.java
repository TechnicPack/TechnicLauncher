package org.spoutcraft.launcher.modpacks;

import java.io.File;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.GameUpdater;
import org.spoutcraft.launcher.YmlUtils;

public class ModLibraryYML {
    
	public static final String MODLIBRARY_YML = "modlibrary.yml";
	public static final File modLibraryYML = new File(GameUpdater.workDir, MODLIBRARY_YML);

	private static volatile boolean	updated	= false;
	private static final Object key	= new Object();

	public static void updateModLibraryYML() {
		if (updated) { return; }
		synchronized (key) {
			YmlUtils.downloadRelativeYmlFile(ModLibraryYML.MODLIBRARY_YML);
			updated = true;
		}
	}

	public static Configuration getModLibraryYML() {
		updateModLibraryYML();
		Configuration config = new Configuration(modLibraryYML);
		config.load();
		return config;
	}
}
