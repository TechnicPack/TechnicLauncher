package org.spoutcraft.launcher.technic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.DownloadUtils;
import org.spoutcraft.launcher.GameUpdater;
import org.spoutcraft.launcher.MD5Utils;
import org.spoutcraft.launcher.MirrorUtils;
import org.spoutcraft.launcher.PlatformUtils;
import org.spoutcraft.launcher.SpoutcraftBuild;
import org.spoutcraft.launcher.async.Download;

public class TechnicUpdater extends GameUpdater {
	private static volatile boolean updated = false;

	private static File baseTechnicDirectory = new File(PlatformUtils.getWorkingDirectory(), "technic");
	private static File technicModsDirectory = new File(baseTechnicDirectory, "mods");
	private static File technicModsYML = new File(baseTechnicDirectory, "modlist.yml");	

	private static String baseTechnicURL = "http://technic.freeworldsgaming.com/";
	private static String technicModsURL = baseTechnicURL + "mods/";

	private static Object key = new Object();
	
	private static File installedTechnicModsYML = new File(baseTechnicDirectory, "installedMods.yml");	
	
	public static void updateTechnicModsYML() {
		if (!updated) {
			synchronized(key) {
				String urlName = MirrorUtils.getMirrorUrl("modlist.yml", baseTechnicURL + "modlist.yml", null);
				if (urlName != null) {
	
					try {
						if (technicModsYML.exists()) {
							try {
								Configuration config = new Configuration(technicModsYML);
								config.load();
							}
							catch (Exception ex){
								ex.printStackTrace();
							}
						}
	
						URL url = new URL(urlName);
						URLConnection con = (url.openConnection());
						System.setProperty("http.agent", "");
						con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
						GameUpdater.copy(con.getInputStream(), new FileOutputStream(technicModsYML));
	
						Configuration config = new Configuration(technicModsYML);
						config.load();
						
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
				updated = true;
			}
		}
	}

	public static Configuration getTechnicModsYML() {
		updateTechnicModsYML();
		Configuration config = new Configuration(technicModsYML);
		config.load();
		return config;
	}

	public void updateTechnicMods() throws Exception {
		Download download;
		
		Configuration modsConfig = new Configuration(installedTechnicModsYML);
		modsConfig.load();
		
		
		technicModsDirectory.mkdirs();
		SecurityManager security = System.getSecurityManager();
		
		Map<String, Object> modLibrary = (Map<String, Object>) getTechnicModsYML().getProperty("mods");

		Map<String, Object> currentModList = SpoutcraftBuild.getSpoutcraftBuild().getMods();
		for (Map.Entry<String, Object> modEntry2 : currentModList.entrySet()) {
			String modName = modEntry2.getKey();
			
			if (!modLibrary.containsKey(modName))
				throw new IOException(String.format("Mod '%s' is missing from the mod library", modName));

			Map<String, Object> modProperties = (Map<String, Object>) modLibrary.get(modName);
			Map<String, Object> modVersions = (Map<String, Object>) modProperties.get("versions");
			
			String version = modEntry2.getValue().toString();
						
			if (!modVersions.containsKey(version))
				throw new IOException(String.format("Mod '%s' version '%s' is missing from the mod library", modName, version));
					

			File modDirectory = new File(technicModsDirectory, modName);
			modDirectory.mkdirs();

			String installType = modProperties.get("installtype").toString();
			String fullFilename = modName + "-" + version + "." + installType;
			Boolean isOptional = modProperties.containsKey("optional") ? (Boolean) modProperties.get("optional") : false;
			
			Map<String, Object> versionProperties = (Map<String, Object>) modVersions.get(version);
			String md5 = (String)versionProperties.get("md5");
			md5 = md5.toLowerCase();
			
			//If local mods md5 hash is not the same as server version then delete to update.
			File modFile = new File(modDirectory, fullFilename);
			boolean isDeleted = false;
			if (modFile.exists()) {
				String computedMD5  = MD5Utils.getMD5(modFile);
				if (!computedMD5.equalsIgnoreCase(md5)) {
					//security.checkDelete(modFile.getAbsolutePath());
					isDeleted = modFile.delete();
				}
			}
			
			if (!modFile.exists() || isDeleted) {
				boolean isFileDownloaded = false;
				
				//Install from cache if md5 matches otherwise download and insert to cache
				File modCache = new File(binCacheDir, fullFilename);
				if (modCache.exists() && md5.equalsIgnoreCase(MD5Utils.getMD5(modCache))) {
					copy(modCache, modFile);
					isFileDownloaded = true;
				} else {			
				
					String mirrorURL = "mods/" + modName + "/" + fullFilename;
					String fallbackURL = technicModsURL + modName + "/" + fullFilename;
					String url = MirrorUtils.getMirrorUrl(mirrorURL, fallbackURL, this);
					download = DownloadUtils.downloadFile(url, modFile.getPath(), fullFilename, md5, this);
					isFileDownloaded = download.isSuccess();
				}
				
				//If have the mod file then update
				if (isFileDownloaded)
					updateMod(modFile, modName, version, modsConfig);
			}
		}
		
		modsConfig.save();
	}
	
	public void regenerateJarFile() {
		
		
	}
	
	public boolean createJar(File jarFilename, File... filesToAdd) {
		FileOutputStream stream = null;
		JarOutputStream out = null;
		BufferedOutputStream bos = null;
		FileInputStream in = null;
		BufferedInputStream bis = null;
		try {
			stream = new FileOutputStream(jarFilename);
		    out = new JarOutputStream(stream, new Manifest());
		    bos = new BufferedOutputStream(out);
		    for (File fileToAdd : filesToAdd) {
		      if (fileToAdd == null || !fileToAdd.exists() || fileToAdd.isDirectory())
		        continue; // Just in case...
		      JarEntry jarAdd = new JarEntry(fileToAdd.getName());
		      jarAdd.setTime(fileToAdd.lastModified());
		      out.putNextEntry(jarAdd);
		      in = new FileInputStream(fileToAdd);
		      bis = new BufferedInputStream(in);
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
		} finally {
			
		}
	    return true;
	}
	
	public void updateMod(File modFile, String modName, String modVersion, Configuration modsConfig) {
		try {
			//Check if previous version of mod is installed
			String modPath = String.format("mods.%s", modName);
			String installedVersion = (String)modsConfig.getProperty(modPath);
			
			if (installedVersion != null) {
				//Mod has been previously installed uninstall previous version
				File previousModZip = new File(new File(technicModsDirectory, modName), modName + "-" + installedVersion + ".zip");
				//String previousModVersionFileName = String.format("%s\\%s\\%s-%v.zip", technicModsDirectory.getAbsolutePath(), modName, modName, modVersion);
				ZipFile zf = new ZipFile(previousModZip);
				Enumeration<? extends ZipEntry> entries = zf.entries();
				//Go through zipfile of previous version and delete all file from technic that exist in the zip
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if (entry.isDirectory()) continue;
					File file = new File(PlatformUtils.getWorkingDirectory(), entry.getName());					
					if (file.exists()) {
						//File from mod exists.. delete it
						file.delete();
					}
				}
					
			}			
			 
			stateChanged("Extracting Files ...", 0);
			// Extract Natives
			extractNatives2(PlatformUtils.getWorkingDirectory(), modFile);	
			
			modsConfig.setProperty(modPath, modVersion);
			
			modFile.delete();
		}
		catch (FileNotFoundException inUse) {
			//If we previously loaded this dll with a failed launch, we will be unable to access the files
			//This is because the previous classloader opened them with the parent classloader, and while the mc classloader
			//has been gc'd, the parent classloader is still around, holding the file open. In that case, we have to assume
			//the files are good, since they got loaded last time...
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isTechnicUpdateAvailable() throws IOException {
		//If no technic mods directory then update
		if (!technicModsDirectory.exists())
			return true;
		
		Map<String, Object> modLibrary = (Map<String, Object>) getTechnicModsYML().getProperty("mods");

		Map<String, Object> currentModList = SpoutcraftBuild.getSpoutcraftBuild().getMods();
		for (Map.Entry<String, Object> modEntry2 : currentModList.entrySet()) {
			String modName = modEntry2.getKey();
			
			if (!modLibrary.containsKey(modName))
				throw new IOException("Mod is missing from the mod library");
			
			Map<String, Object> modProperties = (Map<String, Object>) modLibrary.get(modName);
			Map<String, Object> modVersions = (Map<String, Object>) modProperties.get("versions");
			
			String version = modEntry2.getValue().toString();
						
			if (!modVersions.containsKey(version))
				throw new IOException("Mod version is missing from the mod library");
					

			File modDirectory = new File(technicModsDirectory, modName);

			String installType = modProperties.get("installtype").toString();
			String fullFilename = modName + "-" + version + "." + installType;
			
			Map<String, Object> versionProperties = (Map<String, Object>) modVersions.get(version);
			String md5 = (String)versionProperties.get("md5");
			
			File modCache = new File(binCacheDir, fullFilename);
			if (!modCache.exists() || !md5.equalsIgnoreCase(MD5Utils.getMD5(modCache))) {
				return true;
			}
		}
		
		return false;
	}

}
