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

package org.spoutcraft.launcher.technic;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.spoutcraft.launcher.Settings;
import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.technic.rest.Modpack;
import org.spoutcraft.launcher.technic.rest.RestObject;
import org.spoutcraft.launcher.technic.skin.ModpackOptions;
import org.spoutcraft.launcher.util.Download;
import org.spoutcraft.launcher.util.DownloadUtils;
import org.spoutcraft.launcher.util.MD5Utils;
import org.spoutcraft.launcher.util.ResourceUtils;
import org.spoutcraft.launcher.util.Utils;

public abstract class PackInfo extends RestObject {
	private Image logo;
	private Image background;
	private Image icon;

	// Directories
	private File installedDirectory;
	private File binDir;
	private File cacheDir;
	private File configDir;
	private File savesDir;
	private File tempDir;
	private File resourceDir;
	private File coremodsDir;

	public abstract String getName();

	public abstract String getDisplayName();

	public abstract String getRecommended();

	public abstract String getLatest();

	public abstract List<String> getBuilds();

	public abstract Modpack getModpack();

	public boolean isLoading() {
		return true;
	}

	public boolean isForceDir() {
		return false;
	}

	public String getLogoURL() {
		return "";
	}

	public String getBackgroundURL() {
		return "";
	}

	public String getIconURL() {
		return "";
	}

	public String getLogoMD5() {
		return "";
	}

	public String getBackgroundMD5() {
		return "";
	}

	public String getIconMD5() {
		return "";
	}

	public void init() {
		String location = Settings.getPackDirectory(getName());
		
		if (location != null) {
			installedDirectory = new File(location);
			initDirectories();
		}
		
		String build = Settings.getModpackBuild(getName());
		
		if (build == null) {
			Settings.setModpackBuild(getName(), ModpackOptions.RECOMMENDED);
			Settings.getYAML().save();
		}
	}

	public String getBuild() {
		String build = Settings.getModpackBuild(getName());
		String saveBuild = build;
		if (ModpackOptions.LATEST.equals(build)) {
			build = getLatest();
			saveBuild = ModpackOptions.LATEST;
		} else if (ModpackOptions.RECOMMENDED.equals(build) || build == null) {
			build = getRecommended();
			saveBuild = ModpackOptions.RECOMMENDED;
		}
		
		Settings.setModpackBuild(getName(), saveBuild);
		Settings.getYAML().save();
		return build;
	}

	public void initDirectories() {
		binDir = new File(installedDirectory, "bin");
		cacheDir = new File(installedDirectory, "cache");
		configDir = new File(installedDirectory, "config");
		savesDir = new File(installedDirectory, "saves");
		tempDir = new File(installedDirectory, "temp");
		resourceDir = new File(installedDirectory, "resources");
		coremodsDir = new File(installedDirectory, "coremods");
		
		binDir.mkdirs();
		cacheDir.mkdirs();
		configDir.mkdirs();
		savesDir.mkdirs();
		tempDir.mkdirs();
		resourceDir.mkdirs();
		coremodsDir.mkdirs();
	}
	
	public void setPackDirectory(File packPath) {
		if (installedDirectory != null) {
			try {
				org.apache.commons.io.FileUtils.copyDirectory(installedDirectory, packPath);
				org.apache.commons.io.FileUtils.cleanDirectory(installedDirectory);
			} catch (IOException e) {
				Launcher.getLogger().log(Level.SEVERE, "Unable to move modpack directory at " + installedDirectory.getAbsolutePath() + " to " + packPath.getAbsolutePath(), e);
				return;
			}
		}
		Settings.setPackDirectory(getName(), packPath);
		installedDirectory = packPath;
		initDirectories();
	}
	
	public File getPackDirectory() {
		if (installedDirectory == null) {
			setPackDirectory(new File(Utils.getLauncherDirectory(), getName()));
		}
		return installedDirectory;
	}
	
	public File getBinDir() {
		return binDir;
	}
	
	public File getCacheDir() {
		return cacheDir;
	}
	
	public File getConfigDir() {
		return configDir;
	}
	
	public File getSavesDir() {
		return savesDir;
	}
	
	public File getTempDir() {
		return tempDir;
	}
	
	public File getresourceDir() {
		return resourceDir;
	}

	public Image getLogo() {
		if (logo == null) {
			try {
				logo = buildLogo();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return logo;
	}

	public Image getBackground() {
		if (background == null) {
			try {
				background = buildBackground().getScaledInstance(880, 520, Image.SCALE_SMOOTH);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return background;
	}

	public Image getIcon() {
		if (icon == null) {
			try {
				icon = buildIcon();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return icon;
	}

	private Image buildLogo() throws IOException {
		return buildImage("logo.png", "/org/spoutcraft/launcher/resources/noLogo.png", getLogoURL(), getLogoMD5());
	}
	
	private Image buildBackground() throws IOException {
		return buildImage("background.jpg", "/org/spoutcraft/launcher/resources/background.jpg", getBackgroundURL(), getBackgroundMD5());
	}

	private Image buildIcon() throws IOException { 
		return buildImage("icon.png", "/org/spoutcraft/launcher/resources/icon.png", getIconURL(), getIconMD5());
	}

	private Image buildImage(String name, String backup, String url, String md5) throws IOException {
		BufferedImage image;
		File assets = new File(Utils.getAssetsDirectory(), getName());
		assets.mkdirs();
		File temp = new File(assets, name);
		if (url.isEmpty()) {
			if (temp.exists()) {
				image = ImageIO.read(temp);
			} else {
				image = ImageIO.read(ResourceUtils.getResourceAsStream(backup));
			}
		} else {
			if (temp.exists() && MD5Utils.getMD5(temp).equalsIgnoreCase(md5)) {
				image = ImageIO.read(temp);
			} else {
				Download download = DownloadUtils.downloadFile(url, temp.getAbsolutePath());
				image = ImageIO.read(download.getOutFile());
			}
		}
		return image;
	}
}
