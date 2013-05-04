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

package org.spoutcraft.launcher.technic.rest.pack;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import org.spoutcraft.launcher.technic.RestInfo;
import org.spoutcraft.launcher.technic.rest.Mod;
import org.spoutcraft.launcher.technic.rest.Modpack;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestModpack extends Modpack {

	@JsonProperty("libraries")
	private String libraries;
	@JsonProperty("minecraft")
	private String minecraftVersion;
	@JsonProperty("minecraft_md5")
	private String minecraftMd5;
	@JsonProperty("use_patch")
	boolean usePatch;
	@JsonProperty("forge")
	private String forgeVersion;
	@JsonProperty("mods")
	private List<Mod> mods;

	private String name;
	private String displayName;
	private String build;

	private RestInfo info;

	public RestModpack setInfo(RestInfo info, String build) {
		this.info = info;
		this.name = info.getName();
		this.displayName = info.getDisplayName();
		this.build = build;
		return this;
	}

	public RestInfo getInfo() {
		return info;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getBuild() {
		return build;
	}

	@Override
	public String getMinecraftVersion() {
		return minecraftVersion;
	}

	@Override
	public String getMinecraftMd5() {
		return minecraftMd5;
	}

	public String getForgeVersion() {
		return forgeVersion;
	}

	@Override
	public List<Mod> getMods() {
		return mods;
	}

	@Override
	public String toString() {
		return "{ Modpack [name: " + name + ", build: " + build + ", libraries: " + libraries + ", minecraft: " + minecraftVersion + ", forge: " + forgeVersion + ", mods: " + mods + "] }";
	}
}
