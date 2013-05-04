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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.spoutcraft.launcher.technic.rest.RestAPI;
import org.spoutcraft.launcher.technic.rest.pack.CustomModpack;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomInfo extends PackInfo {
	@JsonProperty("name")
	private String displayName;
	@JsonProperty("user")
	private String user;
	@JsonProperty("friendly_name")
	private String name;
	@JsonProperty("version")
	private String version;
	@JsonProperty("url")
	private String url;
	@JsonProperty("logo")
	private String logoUrl;
	@JsonProperty("background")
	private String backgroundUrl;
	@JsonProperty("mirror")
	private boolean hasMirror;
	@JsonProperty("mirror_url")
	private String mirrorUrl;
	@JsonProperty("minecraft")
	private String minecraftVersion;
	@JsonProperty("minecraft_md5")
	private String minecraftMd5;
	@JsonProperty("use_patch")
	boolean usePatch;
	@JsonProperty("logo_md5")
	private String logoMD5;
	@JsonProperty("background_md5")
	private String backgroundMD5;
	@JsonProperty("force_directory")
	private boolean forceDir;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getRecommended() {
		return version;
	}

	@Override
	public String getLatest() {
		return version;
	}

	@Override
	public List<String> getBuilds() {
		List<String> builds = new ArrayList<String>(1);
		builds.add(getLatest());
		return builds;
	}

	@Override
	public boolean isLoading() {
		return false;
	}

	@Override
	public String getLogoURL() {
		return logoUrl;
	}

	@Override
	public String getBackgroundURL() {
		return backgroundUrl;
	}

	@Override
	public String getIconURL() {
		return logoUrl;
	}

	@Override
	public String getLogoMD5() {
		return logoMD5;
	}

	@Override
	public String getBackgroundMD5() {
		return backgroundMD5;
	}

	@Override
	public String getIconMD5() {
		return logoMD5;
	}

	@Override
	public CustomModpack getModpack() {
		return new CustomModpack(this);
	}

	@Override
	public boolean isForceDir() {
		return forceDir;
	}

	public String getVersion() {
		return version;
	}

	public String getMinecraftVersion() {
		return minecraftVersion;
	}

	public String getMinecraftMd5() {
		return minecraftMd5;
	}

	public String getUser() {
		return user;
	}

	public String getURL() {
		return url;
	}

	public boolean hasMirror() {
		return hasMirror;
	}

	public String getMirrorURL() {
		return mirrorUrl;
	}

	public PackInfo getPack() throws IOException {
		if (this.hasMirror()) {
			RestAPI rest = new RestAPI(getMirrorURL());
			RestInfo restInfo = rest.getModpackInfo(getName());
			return restInfo;
		} else {
			return this;
		}
	}
}
