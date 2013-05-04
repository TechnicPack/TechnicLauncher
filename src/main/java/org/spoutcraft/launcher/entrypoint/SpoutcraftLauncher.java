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

package org.spoutcraft.launcher.entrypoint;

import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import javax.swing.UIManager;

import com.beust.jcommander.JCommander;
import org.apache.commons.io.IOUtils;

import org.spoutcraft.launcher.GameUpdater;
import org.spoutcraft.launcher.Proxy;
import org.spoutcraft.launcher.Settings;
import org.spoutcraft.launcher.GameLauncher;
import org.spoutcraft.launcher.StartupParameters;
import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.skin.ConsoleFrame;
import org.spoutcraft.launcher.skin.MetroLoginFrame;
import org.spoutcraft.launcher.technic.PackManager;
import org.spoutcraft.launcher.technic.skin.ModpackSelector;
import org.spoutcraft.launcher.util.OperatingSystem;
import org.spoutcraft.launcher.util.Utils;
import org.spoutcraft.launcher.yml.YAMLFormat;
import org.spoutcraft.launcher.yml.YAMLProcessor;

public class SpoutcraftLauncher {
	private static Logger logger = null;
	protected static RotatingFileHandler handler = null;
	protected static ConsoleFrame console;
	private static StartupParameters params;
	public SpoutcraftLauncher() {
		main(new String[0]);
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		final long startupTime = start;

		// Prefer IPv4
		System.setProperty("java.net.preferIPv4Stack" , "true");

		params = setupParameters(args);

		cleanup();

		SplashScreen splash = new SplashScreen(Toolkit.getDefaultToolkit().getImage(SplashScreen.class.getResource("/org/spoutcraft/launcher/resources/splash.png")));
		splash.setVisible(true);
		Utils.setSplashScreen(splash);
		setLookAndFeel();

		SpoutcraftLauncher.logger = setupLogger();

		int launcherBuild = parseInt(getLauncherBuild(), -1);
		logger.info("------------------------------------------");
		logger.info("Technic Launcher is starting....");
		logger.info("Launcher Build: " + launcherBuild);

		params.logParameters(logger);

		// Setup directories
		GameUpdater updater = new GameUpdater();
		Utils.getAssetsDirectory().mkdirs();

		Settings.setLauncherBuild(launcherBuild);
		setupProxy();

		if (params.isDebugMode()) {
			logger.info("Initial launcher organization and look and feel time took " + (System.currentTimeMillis() - start)	 + " ms");
			start = System.currentTimeMillis();
		}

		if (params.isDebugMode()) {
			logger.info("Launcher settings took " + (System.currentTimeMillis() - start) + " ms");
			start = System.currentTimeMillis();
		}

		relaunch(false);

		if (params.isConsole() || Settings.getLauncherShowConsole()) {
			setupConsole();
			logger.info("Console Mode Activated");
		}

		Runtime.getRuntime().addShutdownHook(new ShutdownThread());
		Thread logThread = new LogFlushThread();
		logThread.start();

		// Set up the launcher and load login frame
		MetroLoginFrame frame = new MetroLoginFrame();
		ModpackSelector selector = frame.getSelector();

		new Launcher(updater, new GameLauncher(), frame);
		PackManager.initPacks(selector);

		frame.setUser(Settings.getLastUser());
//		frame.getNews().loadArticles();

		if (params.isDebugMode()) {
			logger.info("Launcher skin manager took " + (System.currentTimeMillis() - start) + " ms");
			start = System.currentTimeMillis();
		}

		splash.dispose();
		frame.setVisible(true);

		String lastPack = Settings.getLastModpack();
		
		if (!Settings.getInstalledPacks().contains(lastPack)) {
			lastPack = ModpackSelector.DEFAULT_PACK;
		}
		selector.selectPack(lastPack);
		PackManager.loadPack(selector.getPackMap(), lastPack);

		PackManager.addRestPacks(selector);
		PackManager.addCustomPacks(selector);
		
		frame.updateFaces();

		if (params.hasAccount()) {
			frame.disableForm();
			frame.doLogin(params.getUser(), params.getPass());
		}

		if (params.isDebugMode()) {
			logger.info("Launcher default skin loading took " + (System.currentTimeMillis() - start) + " ms");
			start = System.currentTimeMillis();
		}

		logger.info("Launcher took: " + (System.currentTimeMillis() - startupTime) + "ms to start");
	}

	public static void relaunch(boolean force) {
		if (params.relaunch(logger, force)) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) { }
			System.exit(0);
			return;
		}
	}

	private static void setupProxy() {
		Proxy proxy = new Proxy();
		proxy.setHost(Settings.getProxyHost());
		proxy.setPort(Settings.getProxyPort());
		proxy.setUser(Settings.getProxyUsername());
		String pass = Settings.getProxyPassword();
		proxy.setPass(pass != null ? pass.toCharArray() : null);
		proxy.setup();
	}


	private static void cleanup() {
		File temp = new File(Utils.getLauncherDirectory(), "temp.jar");
		temp.delete();
		temp = new File(Utils.getLauncherDirectory(), "temp.exe");
		temp.delete();
		temp = new File(Utils.getLauncherDirectory(), "Spoutcraft-Launcher.jar");
		temp.delete();
		temp = new File(Utils.getLauncherDirectory(), "mc.patch");
		temp.delete();
		temp = new File(Utils.getLauncherDirectory(), "config/libraries.yml");
		temp.delete();
		temp = new File(Utils.getLauncherDirectory(), "config/spoutcraft.yml");
		temp.delete();
		temp = new File(Utils.getLauncherDirectory(), "config/minecraft.yml");
		temp.delete();
	}

	private static void setLookAndFeel() {
		OperatingSystem os = OperatingSystem.getOS();
		if (os.isMac()) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Technic Launcher");
		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to setup look and feel", e);
		}
	}

	private static int parseInt(String s, int def) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	protected static Logger setupLogger() {
		final Logger logger = Logger.getLogger("launcher");
		File logDirectory = new File(Utils.getLauncherDirectory(), "logs");
		if (!logDirectory.exists()) {
			logDirectory.mkdir();
		}
		File logs = new File(logDirectory, "techniclauncher_%D.log");
		RotatingFileHandler fileHandler = new RotatingFileHandler(logs.getPath());

		fileHandler.setFormatter(new DateOutputFormatter(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")));

		for (Handler h : logger.getHandlers()) {
			logger.removeHandler(h);
		}
		logger.addHandler(fileHandler);

		SpoutcraftLauncher.handler = fileHandler;

		if (params != null && !params.isDebugMode()) {
			logger.setUseParentHandlers(false);

			System.setOut(new PrintStream(new LoggerOutputStream(Level.INFO, logger), true));
			System.setErr(new PrintStream(new LoggerOutputStream(Level.SEVERE, logger), true));
		}

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
		    	logger.log(Level.SEVERE, "Unhandled Exception in " + t, e);
			}
		});

		return logger;
	}

	private static StartupParameters setupParameters(String[] args) {
		StartupParameters params = new StartupParameters(args);
		try {
			new JCommander(params, args);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Utils.setStartupParameters(params);

		params.setupProxy();

		return params;
	}

	public static String getLauncherBuild() {
		String build = "0";
		try {
			build = IOUtils.toString(SpoutcraftLauncher.class.getResource("/org/spoutcraft/launcher/resources/version").openStream(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return build;
	}

	public static void setupSettings(File file) {
		File settingsFile = new File(file, "settings.yml");
		YAMLProcessor settings = new YAMLProcessor(settingsFile, false, YAMLFormat.EXTENDED);
		Settings.setYAML(settings);
	}

	public static void setupConsole() {
		if (console != null) {
			console.dispose();
		}
		console = new ConsoleFrame(2500, true);
		console.setVisible(true);
	}

	public static void destroyConsole() {
		if (console != null) {
			console.setVisible(false);
			console.dispose();
		}
	}
}

class LogFlushThread extends Thread {
	public LogFlushThread() {
		super("Log Flush Thread");
		this.setDaemon(true);
	}

	@Override
	public void run() {
		while (!this.isInterrupted()) {
			if (SpoutcraftLauncher.handler != null) {
				SpoutcraftLauncher.handler.flush();
			}
			try {
				sleep(60000);
			} catch (InterruptedException e) { }
		}
	}
}

class ShutdownThread extends Thread {
	public ShutdownThread() {
		super("Shutdown Thread");
		this.setDaemon(true);
	}

	@Override
	public void run() {
		if (SpoutcraftLauncher.handler != null) {
			SpoutcraftLauncher.handler.flush();
		}
	}
}

class LoggerOutputStream extends ByteArrayOutputStream {
	private final String separator = System.getProperty("line.separator");
	private final Level level;
	private final Logger log;

	public LoggerOutputStream(Level level, Logger log) {
		super();
		this.level = level;
		this.log = log;
	}

	@Override
	public synchronized void flush() throws IOException {
		super.flush();
		String record = this.toString();
		super.reset();

		if (record.length() > 0 && !record.equals(separator)) {
			log.logp(level, "LoggerOutputStream", "log" + level, record);
			if (SpoutcraftLauncher.console != null) {
				SpoutcraftLauncher.console.log(record + "\n");
			}
		}
	}
}

class RotatingFileHandler extends StreamHandler {
	private final SimpleDateFormat date;
	private final String logFile;
	private String filename;

	public RotatingFileHandler(String logFile) {
		this.logFile = logFile;
		date = new SimpleDateFormat("yyyy-MM-dd");
		filename = calculateFilename();
		try {
			setOutputStream(new FileOutputStream(filename, true));
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public synchronized void flush() {
		if (!filename.equals(calculateFilename())) {
			filename = calculateFilename();
			try {
				setOutputStream(new FileOutputStream(filename, true));
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			}
		}
		super.flush();
	}

	private String calculateFilename() {
		return logFile.replace("%D", date.format(new Date()));
	}
}

class DateOutputFormatter extends Formatter {
	private final SimpleDateFormat date;

	public DateOutputFormatter(SimpleDateFormat date) {
		this.date = date;
	}

	@Override
	public String format(LogRecord record) {
		StringBuilder builder = new StringBuilder();

		builder.append(date.format(record.getMillis()));
		builder.append(" [");
		builder.append(record.getLevel().getLocalizedName().toUpperCase());
		builder.append("] ");
		builder.append(formatMessage(record));
		builder.append('\n');

		if (record.getThrown() != null) {
			StringWriter writer = new StringWriter();
			record.getThrown().printStackTrace(new PrintWriter(writer));
			builder.append(writer.toString());
		}

		return builder.toString();
	}
}
