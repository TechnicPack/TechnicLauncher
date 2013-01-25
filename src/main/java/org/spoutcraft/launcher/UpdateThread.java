/*
 * This file is part of Spoutcraft.
 *
 * Copyright (c) 2011-2012, Spout LLC <http://www.spout.org/>
 * Spoutcraft is licensed under the Spout License Version 1.
 *
 * Spoutcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Spoutcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spoutcraft.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;

import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.exceptions.RestfulAPIException;
import org.spoutcraft.launcher.exceptions.UnsupportedOSException;
import org.spoutcraft.launcher.launch.MinecraftClassLoader;
import org.spoutcraft.launcher.launch.MinecraftLauncher;
import org.spoutcraft.launcher.rest.Library;
import org.spoutcraft.launcher.rest.Versions;
import org.spoutcraft.launcher.technic.Mod;
import org.spoutcraft.launcher.technic.Modpack;
import org.spoutcraft.launcher.technic.TechnicRestAPI;
import org.spoutcraft.launcher.util.Download;
import org.spoutcraft.launcher.util.DownloadListener;
import org.spoutcraft.launcher.util.DownloadUtils;
import org.spoutcraft.launcher.util.FileType;
import org.spoutcraft.launcher.util.FileUtils;
import org.spoutcraft.launcher.util.MD5Utils;
import org.spoutcraft.launcher.util.MinecraftDownloadUtils;
import org.spoutcraft.launcher.util.OperatingSystem;
import org.spoutcraft.launcher.util.Utils;
import org.spoutcraft.launcher.util.Download.Result;
import org.spoutcraft.launcher.yml.Resources;
import org.spoutcraft.launcher.yml.YAMLFormat;
import org.spoutcraft.launcher.yml.YAMLProcessor;

public class UpdateThread extends Thread {
	/**
	 * We only want to clean the old logs, temp folders once per startup
	 */
	private static final AtomicBoolean cleaned = new AtomicBoolean(false);
	private static final int PRELOAD_CLASSES = 100;

	// Temporarily hardcoded
	private static final String WINDOWS_NATIVES_URL = "http://s3.amazonaws.com/MinecraftDownload/windows_natives.jar";
	private static final String WINDOWS_NATIVES_MD5 = "9406d7d376b131d20c5717ee9fd89a7f";

	private static final String OSX_NATIVES_URL = "http://s3.amazonaws.com/MinecraftDownload/macosx_natives.jar";
	private static final String OSX_NATIVES_MD5 = "2f60f009723553622af280c920bb7431";

	private static final String LINUX_NATIVES_URL = "http://s3.amazonaws.com/MinecraftDownload/linux_natives.jar";
	private static final String LINUX_NATIVES_MD5 = "3b4435ec85e63faa041b4c080b815b22";

	private final Logger logger = Logger.getLogger("launcher");
	private final AtomicBoolean waiting = new AtomicBoolean(false);
	private final AtomicBoolean valid = new AtomicBoolean(false);
	private final AtomicBoolean finished = new AtomicBoolean(false);
	private final StartupParameters params = Utils.getStartupParameters();
	private final DownloadListener listener = new DownloadListenerWrapper();
	private final Modpack build;
	public UpdateThread(Modpack build, DownloadListener listener) {
		super("Update Thread");
		setDaemon(true);
		this.build = build;
		setDownloadListener(listener);
	}

	@Override
	public void run() {
		while (true) {
			try {
				runTasks();
				break;
			} catch (Exception e) {
				Launcher.getLoginFrame().handleException(e);
				return;
			}
		}
	}

	private void runTasks() throws IOException{
		while (!valid.get()) {
			boolean minecraftUpdate = isMinecraftUpdateAvailable(build);

			if (minecraftUpdate) {
				updateMinecraft(build);
			}

			if (build instanceof SpoutcraftData) {
				boolean spoutcraftUpdate = minecraftUpdate || isSpoutcraftUpdateAvailable((SpoutcraftData) build);
				if (spoutcraftUpdate) {
					updateSpoutcraft((SpoutcraftData) build);
				}
				updateAssets();
			} else {
				boolean modpackUpdate = minecraftUpdate || isModpackUpdateAvailable(build);
				if (modpackUpdate) {
					updateModpack(build);
				}
			}

			// Download assets
			if (cleaned.compareAndSet(false, true)) {
				if (build instanceof SpoutcraftData) {
					Resources.VIP.getYAML();
					Resources.Special.getYAML();
				}
				Versions.getMinecraftVersions();

				cleanLogs();
				cleanTemp();
			}

//			Validator validate = new Validator();
//			if (!(params.isIgnoreMD5() || Settings.isIgnoreMD5())) {
//				validate.run(build);
//				valid.set(validate.isValid());
//			} else {
//				valid.set(true);
//			}
			valid.set(true);
		}

		MinecraftClassLoader loader;
		loader = MinecraftLauncher.getClassLoader(build.getLibraries());

		int loaded = 0;
		while (!waiting.get()) {
			int pass = loader.preloadClasses(PRELOAD_CLASSES);
			loaded += pass;
			// Less than the preload amount, so we are finished
			if (pass != PRELOAD_CLASSES) {
				break;
			}
		}
		logger.info("Preloaded " + loaded + " classes in advance");
		finished.set(true);
	}

	private void updateAssets() {
		YAMLProcessor assets = Resources.Assets.getYAML();
		updateAssets(assets.getMap(), Utils.getAssetsDirectory());
	}

	@SuppressWarnings("unchecked")
	private void updateAssets(Map<String, Object> assets, File directory) {
		directory.mkdirs();
		for (Entry<String, Object> entry : assets.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Map<?, ?>) {
				updateAssets((Map<String, Object>)value, new File(directory, key));
			} else if (value instanceof String) {
				String url = (String)value;
				String name = getFileName(url);
				File asset = new File(directory, name);

				stateChanged("Verifying Asset: " + name, 0);

				boolean needDownload = true;
				if (asset.exists() && !(params.isIgnoreMD5() || Settings.isIgnoreMD5())) {
					String md5 = MD5Utils.getMD5(asset);
					logger.info("Checking MD5 of " + asset.getName() + ". Expected MD5: " + key + " | Actual MD5: " + md5);
					needDownload = md5 == null || !md5.equals(key);
				} else if (asset.exists() && (params.isIgnoreMD5() || Settings.isIgnoreMD5())) {
					needDownload = false;
				}

				if (needDownload) {
					try {
						DownloadUtils.downloadFile(url, asset.getPath(), null, key, listener);
					} catch (IOException e) {
						logger.log(Level.SEVERE, "Failed to download asset [" + url + "]", e);
					}
				}
				stateChanged("Verified Asset: " + name, 100);
			} else {
				logger.warning("Unknown asset type for " + key + ". Type is " + value);
			}
		}
	}

	private String getFileName(String url) {
		String[] split = url.split("/");
		return split[split.length - 1];
	}

	private void cleanTemp() {
		File binDir = Launcher.getGameUpdater().getBinDir();
		for (File f : binDir.listFiles()) {
			if (f.isDirectory()) {
				if (f.getName().startsWith("temp_")) {
					FileUtils.deleteQuietly(f);
				}
			}
		}
	}

	private void cleanLogs() {
		File logDirectory = new File(Utils.getLauncherDirectory(), "logs");
		if (logDirectory.exists() && logDirectory.isDirectory()) {
			for (File log : logDirectory.listFiles()) {
				if (!log.getName().endsWith(".log")) {
					log.delete();
					continue;
				}

				if (!log.getName().startsWith("techniclauncher")) {
					log.delete();
					continue;
				}

				String[] split = log.getName().split("_");
				if (split.length != 2) {
					log.delete();
					continue;
				}

				String[] date = split[1].split("-");
				if (date.length != 3) {
					log.delete();
					continue;
				}
				date[2] = date[2].substring(0, date[2].length() - 4); // Trim .log extension
				try {
					int logYear = Integer.parseInt(date[0]);
					int logMonth = Integer.parseInt(date[1]);
					int logDay = Integer.parseInt(date[2]);

					Calendar logDate = Calendar.getInstance();
					// Add a month to the calendar (clear logs older than 1 month)
					if (logMonth < 12) {
						logMonth++;
					} else {
						logMonth = 1;
						logYear++;
					}
					logDate.set(logYear, logMonth, logDay);

					if (Calendar.getInstance().after(logDate)) {
						log.delete();
					}
				} catch (NumberFormatException ignore) {
					log.delete();
					continue;
				}
			}
		}
	}

	public void setWaiting(boolean waiting) {
		this.waiting.set(waiting);
	}

	public boolean isFinished() {
		return finished.get();
	}

	public boolean isValidInstall() {
		return valid.get();
	}

	public boolean isSpoutcraftUpdateAvailable(SpoutcraftData build) throws RestfulAPIException {
		if (!Utils.getLauncherDirectory().exists()) {
			return true;
		}
		if (!Launcher.getGameUpdater().getConfigDir().exists()) {
			return true;
		}

		List<Library> libraries = build.getLibraries();
		int steps = libraries.size() + 2;
		float progress = 100F;

		stateChanged("Checking for mod pack update...", progress / steps);
		progress += 100F;
		File spoutcraft = new File(Launcher.getGameUpdater().getBinDir(), "modpack.jar");
		if (!spoutcraft.exists() || !build.getMD5().equalsIgnoreCase(MD5Utils.getMD5(spoutcraft))) {
			return true;
		}
		stateChanged("Checking for mod pack update...", progress / steps);
		progress += 100F;
		File libDir = new File(Launcher.getGameUpdater().getBinDir(), "lib");
		libDir.mkdir();

		for (Library lib : libraries) {
			File libraryFile = new File(libDir, lib.name() + ".jar");
			if (!libraryFile.exists()) {
				return true;
			}
			stateChanged("Checking for mod pack update...", progress / steps);
			progress += 100F;
		}

		return false;
	}

	public boolean isModpackUpdateAvailable(Modpack build) throws RestfulAPIException {
		if (!Utils.getLauncherDirectory().exists()) {
			return true;
		}
		if (!Launcher.getGameUpdater().getConfigDir().exists()) {
			return true;
		}
		
		float progress = 100F;
		int steps = 2;
		stateChanged("Checking for " + build.getName() + " update...", progress / steps);
		progress += 100F;

		File installed = new File(Launcher.getGameUpdater().getBinDir(), "installed");
		if (!installed.exists()) {
			return true;
		}
		YAMLProcessor yaml = new YAMLProcessor(installed, false, YAMLFormat.EXTENDED);
		try {
			yaml.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!build.getBuild().equals(yaml.getProperty("build"))) {
			return true;
		}
		stateChanged("No update for " + build.getName() + " found.", progress / steps);
		return false;
	}

	public boolean isMinecraftUpdateAvailable(Modpack build) {
		int steps = 7;
		if (!Launcher.getGameUpdater().getBinDir().exists()) {
			return true;
		}
		stateChanged("Checking for Minecraft update...", 100F / steps);
		File nativesDir = new File(Launcher.getGameUpdater().getBinDir(), "natives");
		if (!nativesDir.exists()) {
			return true;
		}
		//Empty dir
		if (nativesDir.listFiles().length == 0) {
			return true;
		}
		stateChanged("Checking for Minecraft update...", 200F / steps);
		File minecraft = new File(Launcher.getGameUpdater().getBinDir(), "minecraft.jar");
		if (!minecraft.exists()) {
			return true;
		}
		stateChanged("Checking for Minecraft update...", 300F / steps);
		File lib = new File(Launcher.getGameUpdater().getBinDir(), "jinput.jar");
		if (!lib.exists()) {
			return true;
		}
		stateChanged("Checking for Minecraft update...", 400F / steps);
		lib = new File(Launcher.getGameUpdater().getBinDir(), "lwjgl.jar");
		if (!lib.exists()) {
			return true;
		}
		stateChanged("Checking for Minecraft update...", 500F / steps);
		lib = new File(Launcher.getGameUpdater().getBinDir(), "lwjgl_util.jar");
		if (!lib.exists()) {
			return true;
		}
		stateChanged("Checking for Minecraft update...", 600F / steps);
		String installed = Settings.getInstalledMC();
		stateChanged("Checking for Minecraft update...", 700F / steps);
		String required = build.getMinecraftVersion();
		return installed == null || !installed.equals(required);
	}

	public void updateMinecraft(Modpack build) throws IOException {
		Launcher.getGameUpdater().getBinDir().mkdir();
		Launcher.getGameUpdater().getCacheDir().mkdir();
		if (Launcher.getGameUpdater().getTempDir().exists()) {
			FileUtils.deleteDirectory(Launcher.getGameUpdater().getTempDir());
		}
		Launcher.getGameUpdater().getTempDir().mkdir();

		String minecraftMD5 = FileType.MINECRAFT.getMD5();
		String jinputMD5 = FileType.JINPUT.getMD5();
		String lwjglMD5 = FileType.LWJGL.getMD5();
		String lwjgl_utilMD5 = FileType.LWJGL_UTIL.getMD5();

		// Processs minecraft.jar
		logger.info("Mod pack Build: " + build.getBuild() + " Minecraft Version: " + build.getMinecraftVersion());
		File mcCache = new File(Launcher.getGameUpdater().getCacheDir(), "minecraft_" + build.getMinecraftVersion() + ".jar");
		if (!mcCache.exists() || (minecraftMD5 == null || !minecraftMD5.equals(MD5Utils.getMD5(mcCache)))) {
			String output = Launcher.getGameUpdater().getTempDir() + File.separator + "minecraft.jar";
			MinecraftDownloadUtils.downloadMinecraft(Launcher.getGameUpdater().getMinecraftUser(), output, build, listener);
		}
		Utils.copy(mcCache, new File(Launcher.getGameUpdater().getBinDir(), "minecraft.jar"));

		File nativesDir = new File(Launcher.getGameUpdater().getBinDir(), "natives");
		nativesDir.mkdir();

		// Process other downloads
		mcCache = new File(Launcher.getGameUpdater().getCacheDir(), "jinput.jar");
		if (!mcCache.exists() || !jinputMD5.equals(MD5Utils.getMD5(mcCache))) {
			DownloadUtils.downloadFile(getNativesUrl() + "jinput.jar", Launcher.getGameUpdater().getBinDir().getPath() + File.separator + "jinput.jar", "jinput.jar");
		} else {
			Utils.copy(mcCache, new File(Launcher.getGameUpdater().getBinDir(), "jinput.jar"));
		}

		mcCache = new File(Launcher.getGameUpdater().getCacheDir(), "lwjgl.jar");
		if (!mcCache.exists() || !lwjglMD5.equals(MD5Utils.getMD5(mcCache))) {
			DownloadUtils.downloadFile(getNativesUrl() + "lwjgl.jar", Launcher.getGameUpdater().getBinDir().getPath() + File.separator + "lwjgl.jar", "lwjgl.jar");
		} else {
			Utils.copy(mcCache, new File(Launcher.getGameUpdater().getBinDir(), "lwjgl.jar"));
		}

		mcCache = new File(Launcher.getGameUpdater().getCacheDir(), "lwjgl_util.jar");
		if (!mcCache.exists() || !lwjgl_utilMD5.equals(MD5Utils.getMD5(mcCache))) {
			DownloadUtils.downloadFile(getNativesUrl() + "lwjgl_util.jar", Launcher.getGameUpdater().getBinDir().getPath() + File.separator + "lwjgl_util.jar", "lwjgl_util.jar");
		} else {
			Utils.copy(mcCache, new File(Launcher.getGameUpdater().getBinDir(), "lwjgl_util.jar"));
		}

		try {
			getNatives();
		} catch (Exception e) {
			e.printStackTrace();
		}

		stateChanged("Extracting Files...", 0);

		Settings.setInstalledMC(build.getMinecraftVersion());
		Settings.getYAML().save();
	}

	public String getNativesUrl() {
		return GameUpdater.baseURL;
	}

	public void getNatives() throws IOException, UnsupportedOSException {
		String url, md5;

		OperatingSystem os = OperatingSystem.getOS();
		if (os.isUnix()) {
			url = LINUX_NATIVES_URL;
			md5 = LINUX_NATIVES_MD5;
		} else if (os.isMac()) {
			url = OSX_NATIVES_URL;
			md5 = OSX_NATIVES_MD5;
		} else if (os.isWindows()) {
			url = WINDOWS_NATIVES_URL;
			md5 = WINDOWS_NATIVES_MD5;
		} else {
			throw new UnsupportedOperationException("Unknown OS: " + os);
		}

		// Download natives
		File nativesJar = new File(Launcher.getGameUpdater().getTempDir(), "natives.jar");
		DownloadUtils.downloadFile(url, nativesJar.getPath(), null, md5, listener);

		// Extract natives
		List<String> ignores = new ArrayList<String>();
		ignores.add("META-INF");
		File tempNatives = new File(Launcher.getGameUpdater().getTempDir(), "natives");
		Utils.extractJar(new JarFile(nativesJar), tempNatives, ignores);
		FileUtils.moveDirectory(tempNatives, new File(Launcher.getGameUpdater().getBinDir(), "natives"));
	}

	public void updateModpack(Modpack modpack) throws IOException {
		cleanupBinFolders();
		cleanupModsFolders();

		Launcher.getGameUpdater().getTempDir().mkdirs();
		Launcher.getGameUpdater().getCacheDir().mkdirs();
		Launcher.getGameUpdater().getConfigDir().mkdirs();

		File temp = Launcher.getGameUpdater().getTempDir();

		File mcCache = new File(Launcher.getGameUpdater().getCacheDir(), "minecraft_" + build.getMinecraftVersion() + ".jar");
		File updateMC = new File(Launcher.getGameUpdater().getTempDir().getPath() + File.separator + "minecraft.jar");
		if (mcCache.exists()) {
			Utils.copy(mcCache, updateMC);
		}

		File workingDir = Launcher.getGameUpdater().getWorkingDir();

		List<Mod> mods = build.getMods();
		for (Mod mod : mods) {
			String name = mod.getName();
			String build = mod.getVersion();

			File modFile = new File(temp, name + "-" + build + ".zip");

			Download download = DownloadUtils.downloadFile(TechnicRestAPI.getModDownloadURL(name, build), modFile.getAbsolutePath(), null, mod.getMD5(), listener);
			if (download.getResult() == Result.SUCCESS) {
				try {
					ZipFile zipFile = new ZipFile(download.getOutFile());
					zipFile.setRunInThread(true);
					zipFile.extractAll(workingDir.getAbsolutePath());

					ProgressMonitor monitor = zipFile.getProgressMonitor();
					while (monitor.getState() == ProgressMonitor.STATE_BUSY) {
						long totalProgress = monitor.getWorkCompleted() / (monitor.getTotalWork() + 1);
						stateChanged("Extracting " + monitor.getFileName() + "...", totalProgress);
					}
				} catch (ZipException e) {
					Launcher.getLogger().log(Level.SEVERE, "An error occurred while extracting file: " + modFile.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}

		File installed = new File(Launcher.getGameUpdater().getBinDir(), "installed");
		if (!installed.exists()) {
			installed.createNewFile();
		}
		YAMLProcessor yaml = new YAMLProcessor(installed, false, YAMLFormat.EXTENDED);
		yaml.setProperty("build", modpack.getBuild());
		yaml.save();
	}
	public void updateSpoutcraft(SpoutcraftData build) throws IOException {
		cleanupBinFolders();

		Launcher.getGameUpdater().getTempDir().mkdirs();
		Launcher.getGameUpdater().getCacheDir().mkdirs();
		Launcher.getGameUpdater().getConfigDir().mkdirs();
		File cacheDir = new File(Launcher.getGameUpdater().getBinDir(), "cache");
		cacheDir.mkdir();

		File mcCache = new File(Launcher.getGameUpdater().getCacheDir(), "minecraft_" + build.getMinecraftVersion() + ".jar");
		File updateMC = new File(Launcher.getGameUpdater().getTempDir().getPath() + File.separator + "minecraft.jar");
		if (mcCache.exists()) {
			Utils.copy(mcCache, updateMC);
		}

		File spoutcraft = new File(Launcher.getGameUpdater().getBinDir(), "modpack.jar");
		if (spoutcraft.exists() && Integer.parseInt(build.getInstalledBuild()) > 0) {
			// Save our installed copy
			File spoutcraftCache = new File(cacheDir, "spoutcraft_" + build.getInstalledBuild() + ".jar");
			if (!spoutcraftCache.exists()) {
				Utils.copy(spoutcraft, spoutcraftCache);
			}
			spoutcraft.delete();
			// Check for an old copy of this build if it is already saved
			spoutcraftCache = new File(cacheDir, "spoutcraft_" + build.getBuild() + ".jar");
			if (spoutcraftCache.exists()) {
				Utils.copy(spoutcraftCache, spoutcraft);
			}
		}

		stateChanged("Looking Up Mirrors...", 0F);

		String url = build.getSpoutcraftURL();

		if (!spoutcraft.exists()) {
			Download download = DownloadUtils.downloadFile(url, Launcher.getGameUpdater().getTempDir() + File.separator + "modpack.jar", null, build.getMD5(), listener);
			if (download.getResult() == Result.SUCCESS) {
				Utils.copy(download.getOutFile(), spoutcraft);
			}
		}

		File libDir = new File(Launcher.getGameUpdater().getBinDir(), "lib");
		libDir.mkdir();

		List<Library> libraries = build.getLibraries();
		for (Library lib : libraries) {
			File libraryFile = new File(libDir, lib.name() + ".jar");
			if (libraryFile.exists()) {
				String computedMD5 = MD5Utils.getMD5(libraryFile);
				if (!lib.valid(computedMD5)) {
					logger.warning("MD5 check of " + libraryFile.getName() + " failed. Deleting and Redownloading.");
					libraryFile.delete();
				}
			}
			File cachedLibraryFile = new File(cacheDir, lib.name() + ".jar");
			if (cachedLibraryFile.exists()) {
				String computedMD5 = MD5Utils.getMD5(cachedLibraryFile);
				if (lib.valid(computedMD5)) {
					Utils.copy(cachedLibraryFile, libraryFile);
				}
			}
			if (!libraryFile.exists()) {
				lib.download(libraryFile, listener);
			}
		}
	}

	public void cleanupBinFolders() {
		try {
			if (!Launcher.getGameUpdater().getBinDir().exists()) {
				return;
			}

			HashSet<String> neededBinFiles = new HashSet<String>(Arrays.asList(new String[]{"modpack.jar", "minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar"}));
			for (File file : Launcher.getGameUpdater().getBinDir().listFiles()) {
				if (!file.isFile()) {
					continue;
				}
				if (neededBinFiles.contains(file.getName())) {
					continue;
				}
				file.delete();
			}
		} catch (Exception e) {
			System.err.println("Error while cleaning files: ");
			e.printStackTrace();
		}
	}

	public void cleanupModsFolders() {
		try {
			File working = Launcher.getGameUpdater().getWorkingDir();
			File mods = new File(working, "mods");
			if (mods.exists()) {
				FileUtils.cleanDirectory(mods);
			}

			File coremods = new File(working, "coremods");
			if (coremods.exists()) {
				FileUtils.cleanDirectory(coremods);
			}

			File config = new File(working, "config");
			if (config.exists()) {
				FileUtils.cleanDirectory(config);
			}

			File resources = new File(working, "resources");
			if (resources.exists()) {
				FileUtils.cleanDirectory(resources);
			}
		} catch (IOException e) {
			System.err.println("Error while cleaning mods folders: ");
			e.printStackTrace();
		}
	}
	public DownloadListener getDownloadListener() {
		return listener;
	}

	public void setDownloadListener(DownloadListener listener) {
		((DownloadListenerWrapper)this.listener).setDownloadListener(listener);
	}

	public void stateChanged(String message, float progress) {
		if (listener != null) {
			listener.stateChanged(message, progress);
		}
	}

	private class DownloadListenerWrapper implements DownloadListener {
		private final AtomicReference<DownloadListener> wrapped = new AtomicReference<DownloadListener>(null);
		@Override
		public void stateChanged(String fileName, float progress) {
			fileName = (new File(fileName)).getName();
			DownloadListener listener = wrapped.get();
			if (listener != null) {
				listener.stateChanged(fileName, progress);
			}
		}

		public void setDownloadListener(DownloadListener listener) {
			wrapped.set(listener);
		}
	}
}
