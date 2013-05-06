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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.spoutcraft.launcher.util.Utils;
import org.spoutcraft.launcher.yml.YAMLNode;
import org.spoutcraft.launcher.yml.YAMLProcessor;

public class Settings {
	public static final String DEFAULT_MINECRAFT_VERSION = "Latest";
	private static YAMLProcessor yaml;

	public static synchronized void setYAML(YAMLProcessor settings) {
		if (Settings.yaml != null) {
			throw new IllegalArgumentException("settings is already set!");
		}
		Settings.yaml = settings;
		try {
			Settings.yaml.load();
		} catch (FileNotFoundException ignore) {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static synchronized YAMLProcessor getYAML() {
		return yaml;
	}

	public static synchronized int getLauncherBuild() {
		return yaml.getInt("launcher.launcher.buildNumber", -1);
	}

	public static synchronized void setLauncherBuild(int build) {
		yaml.setProperty("launcher.launcher.buildNumber", build);
	}

	public static synchronized Channel getLauncherChannel() {
		return Channel.getType(yaml.getInt("launcher.launcher.type", 0));
	}

	public static synchronized void setLauncherChannel(Channel build) {
		yaml.setProperty("launcher.launcher.type", build.type());
	}

	public static synchronized Channel getSpoutcraftChannel() {
		return Channel.getType(yaml.getInt("launcher.client.type", 0));
	}

	public static synchronized void setSpoutcraftChannel(Channel build) {
		yaml.setProperty("launcher.client.type", build.type());
	}

	public static synchronized String getSpoutcraftSelectedBuild() {
		return yaml.getString("launcher.client.buildNumber", "-1");
	}

	public static synchronized void setSpoutcraftSelectedBuild(String build) {
		yaml.setProperty("launcher.client.buildNumber", build);
	}

	public static synchronized int getMemory() {
		return yaml.getInt("launcher.memory", 0);
	}

	public static synchronized void setMemory(int memory) {
		yaml.setProperty("launcher.memory", memory);
	}

	public static synchronized void setPermGen(boolean permgen) {
		yaml.setProperty("launcher.permgen", permgen);
	}

	public static synchronized boolean getPermGen() {
		return yaml.getBoolean("launcher.permgen", false);
	}

	public static synchronized String getDeveloperCode() {
		return yaml.getString("launcher.devcode", "");
	}

	public static synchronized void setDeveloperCode(String code) {
		yaml.setProperty("launcher.devcode", code);
	}

	public static synchronized boolean isIgnoreMD5() {
		return yaml.getBoolean("launcher.md5", false);
	}

	public static synchronized void setIgnoreMD5(boolean ignore) {
		yaml.setProperty("launcher.md5", ignore);
	}

	public static synchronized String getProxyHost() {
		return yaml.getString("launcher.proxy_host", null);
	}

	public static synchronized void setProxyHost(String host) {
		yaml.setProperty("launcher.proxy_host", host);
	}

	public static synchronized String getProxyPort() {
		return yaml.getString("launcher.proxy_port", null);
	}

	public static synchronized void setProxyPort(String port) {
		yaml.setProperty("launcher.proxy_port", port);
	}

	public static synchronized String getProxyUsername() {
		return yaml.getString("launcher.proxy_user", null);
	}

	public static synchronized void setProxyUsername(String user) {
		yaml.setProperty("launcher.proxy_user", user);
	}

	public static synchronized String getProxyPassword() {
		return yaml.getString("launcher.proxy_pass", null);
	}

	public static synchronized void setProxyPassword(char[] pass) {
		StringBuilder b = new StringBuilder();
		for (char c : pass) {
			b.append(c);
		}
		yaml.setProperty("launcher.proxy_pass", b.toString());
	}

	public static synchronized int getWindowModeId() {
		return yaml.getInt("launcher.windowmode", WindowMode.WINDOWED.getId());
	}

	public static synchronized void setWindowModeId(int id) {
		yaml.setProperty("launcher.windowmode", id);
	}

	public static synchronized String getMinecraftVersion() {
		return yaml.getString("launcher.mc", DEFAULT_MINECRAFT_VERSION);
	}

	public static synchronized void setMinecraftVersion(String version) {
		yaml.setProperty("launcher.mc", version);
	}

	public static synchronized String getDirectJoin() {
		return yaml.getString("launcher.client.server", null);
	}

	public static synchronized void setDirectJoin(String server) {
		yaml.setProperty("launcher.client.server", server);
	}

	public static synchronized String getInstalledMC(String modpack) {
		return yaml.getString("modpacks." + modpack + ".minecraft", null);
	}

	public static synchronized void setInstalledMC(String modpack, String version) {
		yaml.setProperty("modpacks." + modpack + ".minecraft", version);
	}

	public static synchronized void setModpackBuild(String modpack, String build) {
		yaml.setProperty("modpacks." + modpack + ".build", build);
	}

	public static synchronized String getModpackBuild(String modpack) {
		return yaml.getString("modpacks." + modpack + ".build");
	}

	public static synchronized void setLastModpack(String modpack) {
		yaml.setProperty("launcher.lastmodpack", modpack);
	}

	public static synchronized String getLastModpack() {
		return yaml.getString("launcher.lastmodpack");
	}
	
	public static synchronized void setPackDirectory(String modpack, File dir) {
		String directory = dir.getAbsolutePath();
		if (!Utils.getStartupParameters().isPortable() && dir.getParent().contains(Settings.getLauncherDir())) {
			directory = "launcher\\" + dir.getName();
		}
		yaml.setProperty("modpacks." + modpack + ".directory", directory);
	}

	public static synchronized String getPackDirectory(String modpack) {
		String directory = yaml.getString("modpacks." + modpack + ".directory");
		if (directory != null && directory.startsWith("launcher\\")) {
			directory = new File(Settings.getLauncherDir(), directory.substring(9)).getAbsolutePath();
		}
		return directory;
	}

	public static synchronized boolean isPackCustom(String modpack) {
		return yaml.getBoolean("modpacks." + modpack + ".custom", false);
	}

	public static synchronized void setPackCustom(String modpack, boolean custom) {
		yaml.setProperty("modpacks." + modpack + ".custom", custom);
	}

	public static synchronized void removePack(String modpack) {
		yaml.removeProperty("modpacks." + modpack);
	}

	public static synchronized List<String> getInstalledPacks() {
		YAMLNode node = yaml.getNode("modpacks");
		if (node == null) {
			return Collections.emptyList();
		} else {
			return new ArrayList<String>(node.getMap().keySet());
		}
	}

	public static synchronized void setLastUser(String name) {
		yaml.setProperty("launcher.lastuser", name);
	}

	public static synchronized String getLastUser() {
		return yaml.getString("launcher.lastuser");
	}

	public static synchronized void setLauncherDir(String path) {
		yaml.setProperty("launcher.directory", path);
	}

	public static synchronized String getLauncherDir() {
		return yaml.getString("launcher.directory");
	}

	public static synchronized void setMigrate(boolean migrate) {
		yaml.setProperty("launcher.migrate", migrate);
	}

	public static synchronized boolean getMigrate() {
		return yaml.getBoolean("launcher.migrate", false);
	}

	public static synchronized void setMigrateDir(String path) {
		yaml.setProperty("launcher.migratedir", path);
	}

	public static synchronized String getMigrateDir() {
		return yaml.getString("launcher.migratedir");
	}

	public static synchronized void removeMigrate() {
		yaml.removeProperty("launcher.migrate");
		yaml.removeProperty("launcher.migratedir");
	}
	
	public static synchronized void setBuildStream(String stream) {
		yaml.setProperty("launcher.buildstream", stream);
	}
	
	public static synchronized String getBuildStream() {
		if (yaml.getString("launcher.buildstream") != null) {
			return yaml.getString("launcher.buildstream");
		}
		return "stable";
	}

	public static boolean getShowLauncherConsole() {
		return yaml.getBoolean("launcher.showConsole", false);
	}

	public static void setShowLauncherConsole(boolean value) {
		yaml.setProperty("launcher.showConsole",value);
	}
}
