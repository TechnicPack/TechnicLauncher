package org.spoutcraft.launcher.modpacks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.spoutcraft.launcher.DownloadUtils;
import org.spoutcraft.launcher.GameUpdater;
import org.spoutcraft.launcher.MD5Utils;
import org.spoutcraft.launcher.MirrorUtils;
import org.spoutcraft.launcher.ModpackBuild;
import org.spoutcraft.launcher.Util;
import org.spoutcraft.launcher.async.Download;

public class ModPackUpdater extends GameUpdater {

	public static final String	defaultModPackName	= "atex";

	private static final String	baseFallbackURL			= "https://raw.github.com/triforce1/technic/master/";
	private static final String	fallbackModsURL			= baseFallbackURL + "mods/";

	public void updateModPackMods() {
		try {

			Map<String, Object> modLibrary = (Map<String, Object>) ModLibraryYML.getModLibraryYML().getProperty("mods");
			Map<String, Object> currentModList = ModpackBuild.getSpoutcraftBuild().getMods();

			// Remove Mods no longer in previous version
			removeOldMods(currentModList.keySet());

			for (Map.Entry<String, Object> modEntry2 : currentModList.entrySet()) {
				String modName = modEntry2.getKey();

				if (!modLibrary.containsKey(modName)) { throw new IOException(String.format("Mod '%s' is missing from the mod library", modName)); }

				Map<String, Object> modProperties = (Map<String, Object>) modLibrary.get(modName);
				Map<String, Object> modVersions = (Map<String, Object>) modProperties.get("versions");

				String version = modEntry2.getValue().toString();

				if (!modVersions.containsKey(version)) { throw new IOException(String.format("Mod '%s' version '%s' is missing from the mod library", modName, version)); }

				String installType = modProperties.containsKey("installtype") ? (String) modProperties.get("installtype") : "zip";
				String fullFilename = modName + "-" + version + "." + installType;
				Boolean isOptional = modProperties.containsKey("optional") ? (Boolean) modProperties.get("optional") : false;

				String installedModVersion = InstalledModsYML.getInstalledModVersion(modName);

				// If installed mods md5 hash is the same as server's version
				// then go to next mod.
				if (installedModVersion != null && installedModVersion.equals(version)) {
					String md5ModPath = String.format("mods/%s/%s", modName, fullFilename);
					if (MD5Utils.checksumCachePath(fullFilename, md5ModPath)) {
						continue;
					}
				}

				File modFile = new File(tempDir, fullFilename);

				// If have the mod file then update
				if (downloadModPackage(modName, fullFilename, modFile)) {
					updateMod(modFile, modName, version);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void removeOldMods(Set<String> modsToInstall) {
		Map<String, String> installedMods = InstalledModsYML.getInstalledMods();

		if (installedMods == null || modsToInstall == null || modsToInstall.size() <= 0) { return; }

		Set<String> modsToRemove = installedMods.keySet();
		modsToRemove.removeAll(modsToInstall);

		String[] array = new String[modsToRemove.size()];
		modsToRemove.toArray(array);
		for (String modName : array) {
			removePreviousModVersion(modName, installedMods.get(modName));
		}

	}

	public boolean downloadModPackage(String name, String filename, File downloadedFile) {
		try {
			// Install from cache if md5 matches otherwise download and insert
			// to cache
			File modCache = new File(cacheDir, filename);
			String md5Name = "mods\\" + name + "\\" + filename;
			if (modCache.exists() && MD5Utils.checksumCachePath(filename, md5Name)) {
				stateChanged("Copying " + filename + " from cache", 0);
				copy(modCache, downloadedFile);
				stateChanged("Copied " + filename + " from cache", 100);
				return true;
			} else {
				String mirrorURL = "mods/" + name + "/" + filename;
				String fallbackURL = fallbackModsURL + name + "/" + filename;
				String url = MirrorUtils.getMirrorUrl(mirrorURL, fallbackURL, this);
				String fileMD5 = MD5Utils.getMD5FromList(mirrorURL);
				Download download = DownloadUtils.downloadFile(url, downloadedFile.getPath(), filename, fileMD5, this);
				return download.isSuccess();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean createJar(File jarFilename, File... filesToAdd) {
		try {
			FileOutputStream stream = new FileOutputStream(jarFilename);
			JarOutputStream out = new JarOutputStream(stream, new Manifest());
			BufferedOutputStream bos = new BufferedOutputStream(out);
			for (File fileToAdd : filesToAdd) {
				if (fileToAdd == null || !fileToAdd.exists() || fileToAdd.isDirectory()) {
					continue; // Just in case...
				}
				JarEntry jarAdd = new JarEntry(fileToAdd.getName());
				jarAdd.setTime(fileToAdd.lastModified());
				out.putNextEntry(jarAdd);
				FileInputStream in = new FileInputStream(fileToAdd);
				BufferedInputStream bis = new BufferedInputStream(in);
				int data;
				while ((data = bis.read()) != -1) {
					bos.write(data);
				}
				bis.close();
				in.close();
			}
			bos.close();
			out.close();
			stream.close();
		} catch (FileNotFoundException e) {
			// skip not found file
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void updateMod(File modFile, String modName, String modVersion) {
		// Check if previous version of mod is installed
		String installedVersion = InstalledModsYML.getInstalledModVersion(modName);

		if (installedVersion != null) {
			removePreviousModVersion(modName, installedVersion);
		}

		stateChanged("Extracting Files ...", 0);
		// Extract Natives
		extractNatives2(GameUpdater.modpackDir, modFile);

		InstalledModsYML.setInstalledModVersion(modName, modVersion);

		modFile.delete();
	}

	private void removePreviousModVersion(String modName, String installedVersion) {
		try {
			// Mod has been previously installed uninstall previous version
			File previousModZip = new File(cacheDir, modName + "-" + installedVersion + ".zip");
			ZipFile zf = new ZipFile(previousModZip);
			Enumeration<? extends ZipEntry> entries = zf.entries();
			// Go through zipfile of previous version and delete all file from
			// Modpack that exist in the zip
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				File file = new File(GameUpdater.modpackDir, entry.getName());
				Util.log("Deleting '%s'", entry.getName());
				if (file.exists()) {
					// File from mod exists.. delete it
					file.delete();
				}
			}

			InstalledModsYML.removeMod(modName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isModpackUpdateAvailable() throws IOException {

		Map<String, Object> modLibrary = (Map<String, Object>) ModLibraryYML.getModLibraryYML().getProperty("mods");
		Map<String, Object> currentModList = ModpackBuild.getSpoutcraftBuild().getMods();

		for (Map.Entry<String, Object> modEntry2 : currentModList.entrySet()) {
			String modName = modEntry2.getKey();

			if (!modLibrary.containsKey(modName)) { throw new IOException("Mod is missing from the mod library"); }

			Map<String, Object> modProperties = (Map<String, Object>) modLibrary.get(modName);
			Map<String, Object> modVersions = (Map<String, Object>) modProperties.get("versions");

			String version = modEntry2.getValue().toString();

			if (!modVersions.containsKey(version)) { throw new IOException("Mod version is missing from the mod library"); }

			String installType = modProperties.get("installtype").toString();
			String fullFilename = modName + "-" + version + "." + installType;

			String md5Name = "mods\\" + modName + "\\" + fullFilename;
			//if (!MD5Utils.checksumCachePath(fullFilename, md5Name)) { return true; }
			String installedModVersion = InstalledModsYML.getInstalledModVersion(modName);
			if (installedModVersion == null || !installedModVersion.equals(version)) { return true; }
		}

		return false;
	}
}
