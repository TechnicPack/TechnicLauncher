/*
 * This file is part of Technic Launcher.
 *
 * Copyright (c) 2013-2013, Technic <http://www.technicpack.net/>
 * Technic Launcher is licensed under the Spout License Version 1.
 *
 * Technic Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Technic Launcher is distributed in the hope that it will be useful,
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;

import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.exceptions.DownloadException;
import org.spoutcraft.launcher.exceptions.RestfulAPIException;
import org.spoutcraft.launcher.exceptions.UnsupportedOSException;
import org.spoutcraft.launcher.launch.MinecraftClassLoader;
import org.spoutcraft.launcher.launch.MinecraftLauncher;
import org.spoutcraft.launcher.rest.Versions;
import org.spoutcraft.launcher.technic.OfflineInfo;
import org.spoutcraft.launcher.technic.PackInfo;
import org.spoutcraft.launcher.technic.rest.Mod;
import org.spoutcraft.launcher.technic.rest.Modpack;
import org.spoutcraft.launcher.technic.rest.RestAPI;
import org.spoutcraft.launcher.util.DownloadListener;
import org.spoutcraft.launcher.util.DownloadUtils;
import org.spoutcraft.launcher.util.FileType;
import org.spoutcraft.launcher.util.FileUtils;
import org.spoutcraft.launcher.util.MD5Utils;
import org.spoutcraft.launcher.util.MinecraftDownloadUtils;
import org.spoutcraft.launcher.util.OperatingSystem;
import org.spoutcraft.launcher.util.Utils;
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
	private final DownloadListener listener = new DownloadListenerWrapper();
	private final Modpack build;
	private final PackInfo pack;

	public UpdateThread(PackInfo pack, DownloadListener listener) throws RestfulAPIException {
		super("Update Thread");
		setDaemon(true);
		this.pack = pack;
		this.build = pack.getModpack();
		setDownloadListener(listener);
	}

	@Override
	public void run() {
		while (true) {
			try {
				runTasks();
				break;
			} catch (DownloadException e) {
				JOptionPane.showMessageDialog(Launcher.getFrame(), "Error downloading file for the following pack: " + pack.getDisplayName() + " \n\n" + e.getMessage() + "\n\nPlease consult the modpack author.", "Error", JOptionPane.WARNING_MESSAGE);
				Launcher.getFrame().enableForm();
				Launcher.getGameUpdater().resetUpdateThread();
				return;
			} catch (Exception e) {
				Launcher.getFrame().handleException(e);
				return;
			}
		}
	}

	private void runTasks() throws IOException {
		while (!valid.get()) {
			if (!pack.isLoading() && build.getMinecraftVersion() != null && !(pack instanceof OfflineInfo)) {
				boolean minecraftUpdate = isMinecraftUpdateAvailable(build);

				if (minecraftUpdate) {
					updateMinecraft(build);
				}

				boolean modpackUpdate = minecraftUpdate || isModpackUpdateAvailable(build);
				if (modpackUpdate) {
					File installed = new File(pack.getBinDir(), "installed");
					if(!installed.exists()) {
						updateModpack(build);
					}
					else {
						int result = JOptionPane.showConfirmDialog(Launcher.getFrame(), "Would you like to update this pack?", "Update Found", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
						if (result == JOptionPane.YES_OPTION) {
							updateModpack(build);
						}
					}
				}
			}

			// Download assets
			if (cleaned.compareAndSet(false, true)) {
				Versions.getMinecraftVersions();
			}

			cleanLogs();

			valid.set(true);
		}

		MinecraftClassLoader loader;
		loader = MinecraftLauncher.getClassLoader(pack);
		
		Utils.pingURL(RestAPI.getRunCountURL(build.getName()));

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

	public boolean isModpackUpdateAvailable(Modpack build) throws RestfulAPIException {
		if (!Utils.getLauncherDirectory().exists()) {
			return true;
		}
		if (!pack.getConfigDir().exists()) {
			return true;
		}

		float progress = 100F;
		int steps = 2;
		stateChanged("Checking for " + build.getName() + " update...", progress / steps);
		progress += 100F;

		File installed = new File(pack.getBinDir(), "installed");
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
		if (!pack.getBinDir().exists()) {
			return true;
		}
		stateChanged("Checking for Minecraft update...", 100F / steps);
		File nativesDir = new File(pack.getBinDir(), "natives");
		if (!nativesDir.exists()) {
			return true;
		}
		//Empty dir
		if (nativesDir.listFiles().length == 0) {
			return true;
		}
		stateChanged("Checking for Minecraft update...", 200F / steps);
		File minecraft = new File(pack.getBinDir(), "minecraft.jar");
		if (!minecraft.exists()) {
			return true;
		}
		stateChanged("Checking for Minecraft update...", 300F / steps);
		File lib = new File(pack.getBinDir(), "jinput.jar");
		if (!lib.exists()) {
			return true;
		}
		stateChanged("Checking for Minecraft update...", 400F / steps);
		lib = new File(pack.getBinDir(), "lwjgl.jar");
		if (!lib.exists()) {
			return true;
		}
		stateChanged("Checking for Minecraft update...", 500F / steps);
		lib = new File(pack.getBinDir(), "lwjgl_util.jar");
		if (!lib.exists()) {
			return true;
		}
		stateChanged("Checking for Minecraft update...", 600F / steps);
		String installed = Settings.getInstalledMC(build.getName());
		stateChanged("Checking for Minecraft update...", 700F / steps);
		String required = build.getMinecraftVersion();
		return installed == null || !installed.equals(required);
	}

	public void updateMinecraft(Modpack build) throws IOException {
		pack.init();

		String minecraftMD5 = build.getMinecraftMd5();
		String jinputMD5 = FileType.JINPUT.getMD5();
		String lwjglMD5 = FileType.LWJGL.getMD5();
		String lwjgl_utilMD5 = FileType.LWJGL_UTIL.getMD5();

		// Processs minecraft.jar
		logger.info("Mod pack Build: " + build.getBuild() + " Minecraft Version: " + build.getMinecraftVersion());
		File mcCache = new File(Utils.getCacheDirectory(), "minecraft_" + build.getMinecraftVersion() + ".jar");
		if (!mcCache.exists() || (minecraftMD5 == null || !minecraftMD5.equals(MD5Utils.getMD5(mcCache)))) {
			String output = pack.getTempDir() + File.separator + "minecraft.jar";
			MinecraftDownloadUtils.downloadMinecraft(Launcher.getGameUpdater().getMinecraftUser(), output, pack, build, listener);
		}
		Utils.copy(mcCache, new File(pack.getBinDir(), "minecraft.jar"));

		File nativesDir = new File(pack.getBinDir(), "natives");
		nativesDir.mkdir();

		// Process other downloads
		mcCache = new File(Utils.getCacheDirectory(), "jinput.jar");
		if (!mcCache.exists() || !jinputMD5.equals(MD5Utils.getMD5(mcCache))) {
			DownloadUtils.downloadFile(getNativesUrl() + "jinput.jar", pack.getBinDir().getPath() + File.separator + "jinput.jar", "jinput.jar");
		} else {
			Utils.copy(mcCache, new File(pack.getBinDir(), "jinput.jar"));
		}

		mcCache = new File(Utils.getCacheDirectory(), "lwjgl.jar");
		if (!mcCache.exists() || !lwjglMD5.equals(MD5Utils.getMD5(mcCache))) {
			DownloadUtils.downloadFile(getNativesUrl() + "lwjgl.jar", pack.getBinDir().getPath() + File.separator + "lwjgl.jar", "lwjgl.jar");
		} else {
			Utils.copy(mcCache, new File(pack.getBinDir(), "lwjgl.jar"));
		}

		mcCache = new File(Utils.getCacheDirectory(), "lwjgl_util.jar");
		if (!mcCache.exists() || !lwjgl_utilMD5.equals(MD5Utils.getMD5(mcCache))) {
			DownloadUtils.downloadFile(getNativesUrl() + "lwjgl_util.jar", pack.getBinDir().getPath() + File.separator + "lwjgl_util.jar", "lwjgl_util.jar");
		} else {
			Utils.copy(mcCache, new File(pack.getBinDir(), "lwjgl_util.jar"));
		}

		try {
			getNatives();
		} catch (Exception e) {
			e.printStackTrace();
		}

		stateChanged("Extracting Files...", 0);

		Settings.setInstalledMC(build.getName(), build.getMinecraftVersion());
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
		File nativesJar = new File(pack.getTempDir(), "natives.jar");
		DownloadUtils.downloadFile(url, nativesJar.getPath(), null, md5, listener);

		// Extract natives
		List<String> ignores = new ArrayList<String>();
		ignores.add("META-INF");
		File tempNatives = new File(pack.getTempDir(), "natives");
		Utils.extractJar(new JarFile(nativesJar), tempNatives, ignores);
		FileUtils.moveDirectory(tempNatives, new File(pack.getBinDir(), "natives"));
	}

	public void updateModpack(Modpack modpack) throws IOException {
		cleanupBinFolders(pack);
		cleanupModsFolders(pack);
		File workingDir = pack.getPackDirectory();

		pack.getTempDir().mkdirs();
		pack.getCacheDir().mkdirs();
		pack.getConfigDir().mkdirs();

		File temp = pack.getTempDir();

		File mcCache = new File(pack.getCacheDir(), "minecraft_" + build.getMinecraftVersion() + ".jar");
		File updateMC = new File(pack.getTempDir().getPath() + File.separator + "minecraft.jar");
		if (mcCache.exists()) {
			Utils.copy(mcCache, updateMC);
		}

		List<Mod> mods = build.getMods();
		for (Mod mod : mods) {
			String name = mod.getName();
			String build = mod.getVersion();

			File modFile = new File(temp, name + "-" + build + ".zip");

			boolean shouldDownload = true;
			if (modFile.exists()) {
				System.out.println("Mod " + modFile.getName() + " already found in the cache.");
				String resultMD5 = MD5Utils.getMD5(modFile);
				System.out.println("Expected MD5: " + mod.getMD5() + " Calculated MD5: " + resultMD5);
				if (MD5Utils.getMD5(modFile).equalsIgnoreCase(mod.getMD5())) {
					shouldDownload = false;
					System.out.println("Succesfully skipped download of: " + mod.getName() + " " + mod.getVersion());
				}
			}

			if (shouldDownload) {
				DownloadUtils.downloadFile(mod.getURL(), modFile.getAbsolutePath(), null, mod.getMD5(), listener);
			}

			try {
				ZipFile zipFile = new ZipFile(modFile);
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
		cleanupPackTemp(modpack);

		File installed = new File(pack.getBinDir(), "installed");
		if (!installed.exists()) {
			installed.createNewFile();
			Utils.pingURL(RestAPI.getDownloadCountURL(modpack.getName()));
		}
		YAMLProcessor yaml = new YAMLProcessor(installed, false, YAMLFormat.EXTENDED);
		yaml.setProperty("build", modpack.getBuild());
		yaml.save();
	}

	public void cleanupPackTemp(Modpack modpack) {
		if (!pack.getTempDir().isDirectory()) {
			return;
		}

		File[] files = pack.getTempDir().listFiles();
		List<String> keepFiles = new ArrayList<String>(modpack.getMods().size());
		for (Mod mod : modpack.getMods()) {
			keepFiles.add(mod.getName() + "-" + mod.getVersion() + ".zip");
		}
		keepFiles.add("minecraft.jar");
		keepFiles.add("natives.jar");

		for (File file : files) {
			String fileName = file.getName();
			if (keepFiles.contains(fileName)) {
				continue;
			}
			FileUtils.deleteQuietly(file);
		}
	}

	public static void cleanupBinFolders(PackInfo pack) {
		try {
			if (!pack.getBinDir().exists()) {
				return;
			}

			HashSet<String> neededBinFiles = new HashSet<String>(Arrays.asList(new String[]{"modpack.jar", "minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar"}));
			for (File file : pack.getBinDir().listFiles()) {
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

	public static void cleanupModsFolders(PackInfo pack) {
		try {
			File working = pack.getPackDirectory();
			File mods = new File(working, "mods");
			if (mods.exists()) {
				FileUtils.cleanDirectory(mods, false);
			}

			File coremods = new File(working, "coremods");
			if (coremods.exists()) {
				FileUtils.cleanDirectory(coremods);
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
