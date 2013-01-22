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
package org.spoutcraft.launcher.technic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.codehaus.jackson.annotate.JsonProperty;
import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.util.Download;
import org.spoutcraft.launcher.util.DownloadUtils;

public class ModpackInfo {
	@JsonProperty("name")
	private String name;
	@JsonProperty("recommended")
	private String recommended;
	@JsonProperty("latest")
	private String latest;
	@JsonProperty("builds")
	private String[] builds;

	private String displayName = "Technic";

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}
	public String getRecommended() {
		return recommended;
	}

	public String getLatest() {
		return latest;
	}

	public List<String> getBuilds() {
		return Arrays.asList(builds);
	}

	public String getImgURL() {
		return TechnicRestAPI.getModpackImgURL(name);
	}
	
	public String getBackgroundURL() {
		return TechnicRestAPI.getModpackBackgroundURL(name);
	}

	public BufferedImage getImg() throws IOException {
		BufferedImage image;
		File temp = new File(Launcher.getGameUpdater().getTempDir(), "logo.png");
		Download download = DownloadUtils.downloadFile(getImgURL(), temp.getAbsolutePath());
		image = ImageIO.read(download.getOutFile());
		return image;
	}
	
	public BufferedImage getBackground() throws IOException {
		BufferedImage image;
		File temp = new File(Launcher.getGameUpdater().getTempDir(), "background.jpg");
		Download download = DownloadUtils.downloadFile(getBackgroundURL(), temp.getAbsolutePath());
		image = ImageIO.read(download.getOutFile());
		return image;
	}
	@Override
	public String toString() {
		return "{ ModpackBuilds [name: " + name + ", recommended: " + recommended + ", latest: " + latest + ", builds: " + builds + "] }";
	}
}
