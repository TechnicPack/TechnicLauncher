package org.spoutcraft.launcher.modpacks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.spoutcraft.launcher.DownloadUtils;
import org.spoutcraft.launcher.GameUpdater;
import org.spoutcraft.launcher.MD5Utils;
import org.spoutcraft.launcher.MirrorUtils;
import org.spoutcraft.launcher.ModpackBuild;
import org.spoutcraft.launcher.SettingsUtil;
import org.spoutcraft.launcher.Util;
import org.spoutcraft.launcher.async.Download;

@SuppressWarnings("unchecked")
public class ModPackUpdater extends GameUpdater {

  public static final String  defaultModPackName = "technicssp";

  private static final String baseFallbackURL    = "http://mirror.technicpack.net/Technic/";
  private static final String fallbackModsURL    = baseFallbackURL + "mods/";

  public void updateModPackMods() {
    try {

      Map<String, Object> modLibrary = (Map<String, Object>) ModLibraryYML.getModLibraryYML().getProperty("mods");
      Map<String, Object> currentModList = ModpackBuild.getSpoutcraftBuild().getMods();

      // Remove Mods no longer in previous version
      removeOldMods(currentModList.keySet());

      for (Map.Entry<String, Object> modEntry2 : currentModList.entrySet()) {
        String modName = modEntry2.getKey();

        if (!modLibrary.containsKey(modName)) {
          throw new IOException(String.format("Mod '%s' is missing from the mod library", modName));
        }

        Map<String, Object> modProperties = (Map<String, Object>) modLibrary.get(modName);
        Map<String, Object> modVersions = (Map<String, Object>) modProperties.get("versions");

        String version = modEntry2.getValue().toString();

        if (!modVersions.containsKey(version)) {
          throw new IOException(String.format("Mod '%s' version '%s' is missing from the mod library", modName, version));
        }

        String installType = modProperties.containsKey("installtype") ? (String) modProperties.get("installtype") : "zip";
        String fullFilename = modName + "-" + version + "." + installType;

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

      extractCustomZip();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void extractCustomZip() {
    try {
      String customZipUrl = SettingsUtil.getCustomZipUrl();
      if (customZipUrl == null || customZipUrl.isEmpty()) {
        return;
      }
      File customZipFile = new File(tempDir, "custom.zip");
      Download download = DownloadUtils.downloadFile(SettingsUtil.getCustomZipUrl(), customZipFile.getPath(), null, null, this);
      if (download.isSuccess()) {
        stateChanged("Extracting Custom Zip Files ...", 0);
        // Extract Natives
        extractCompressedFile(GameUpdater.modpackDir, customZipFile, true);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void removeOldMods(Set<String> modsToInstall) {
    Map<String, String> installedMods = InstalledModsYML.getInstalledMods();

    if (installedMods == null || modsToInstall == null || modsToInstall.size() <= 0) {
      return;
    }

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
    } catch (MalformedURLException e) {
      Util.log("Cannot download the mod '%s'. Does the exact filename exist on the mirror?", "mods/" + name + "/" + filename);
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
    // Extract Mod zip
    extractCompressedFile(GameUpdater.modpackDir, modFile, true);

    InstalledModsYML.setInstalledModVersion(modName, modVersion);

    modFile.delete();
  }

  private void removePreviousModVersion(String modName, String installedVersion) {
    File previousModZip = new File(cacheDir, modName + "-" + installedVersion + ".zip");
    if (!previousModZip.exists()) {
      Util.log("[File not Found] Could not delete '%s'.", previousModZip.getPath());
      return;
    }
    try {
      // Mod has been previously installed uninstall previous version
      ZipFile zf = new ZipFile(previousModZip);
      List<FileHeader> entries = zf.getFileHeaders();
      // Go through zipfile of previous version and delete all file from
      // Modpack that exist in the zip
      for (FileHeader fileHeader : entries) {
        if (fileHeader.isDirectory()) {
          continue;
        }
        File file = new File(GameUpdater.modpackDir, fileHeader.getFileName());
        Util.log("Deleting '%s'", file.getPath());
        if (file.exists()) {
          // File from mod exists.. delete it
          file.delete();
        }
      }
      InstalledModsYML.removeMod(modName);
    } catch (ZipException e) {
      e.printStackTrace();
    }
  }

  public boolean isModpackUpdateAvailable() throws IOException {

    Map<String, Object> modLibrary = (Map<String, Object>) ModLibraryYML.getModLibraryYML().getProperty("mods");
    Map<String, Object> currentModList = ModpackBuild.getSpoutcraftBuild().getMods();

    for (Map.Entry<String, Object> modEntry2 : currentModList.entrySet()) {
      String modName = modEntry2.getKey();

      if (!modLibrary.containsKey(modName)) {
        throw new IOException("Mod is missing from the mod library");
      }

      Map<String, Object> modProperties = (Map<String, Object>) modLibrary.get(modName);
      Map<String, Object> modVersions = (Map<String, Object>) modProperties.get("versions");

      String version = modEntry2.getValue().toString();

      if (!modVersions.containsKey(version)) {
        throw new IOException("Mod version is missing from the mod library");
      }

      String installType = modProperties.get("installtype").toString();
      String fullFilename = modName + "-" + version + "." + installType;

      String md5Name = "mods\\" + modName + "\\" + fullFilename;
      if (!MD5Utils.checksumCachePath(fullFilename, md5Name)) {
        Util.log("'%s' has MD5 mismatch! Updating..", fullFilename);
        return true;
      }

      String installedModVersion = InstalledModsYML.getInstalledModVersion(modName);
      if (installedModVersion == null) {
        Util.log("No 'installedMods.yml'! Updating..", fullFilename);
        return true;
      }

      if (!installedModVersion.equals(version)) {
        Util.log("'%s' has version '%s' installed instead of '%s'! Updating..", fullFilename, installedModVersion, version);
        return true;
      }
    }

    return false;
  }
}
