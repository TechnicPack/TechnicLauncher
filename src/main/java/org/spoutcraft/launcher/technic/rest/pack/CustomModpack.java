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

import java.util.ArrayList;
import java.util.List;

import org.spoutcraft.launcher.technic.CustomInfo;
import org.spoutcraft.launcher.technic.rest.Mod;
import org.spoutcraft.launcher.technic.rest.Modpack;

public class CustomModpack extends Modpack {
	private final CustomInfo info;
	private final String name;
	private final String build;
	private final String minecraftVersion;

	public CustomModpack(CustomInfo info) {
		this.info = info;
		this.name = info.getName();
		this.build = info.getVersion();
		this.minecraftVersion = info.getMinecraftVersion();
	}

	@Override
	public String getMinecraftVersion() {
		return minecraftVersion;
	}

	@Override
	public String getMinecraftMd5() {
		return info.getMinecraftMd5();
	}

	@Override
	public String getBuild() {
		return build;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Mod> getMods() {
		List<Mod> mods = new ArrayList<Mod>(1);
		mods.add(new Mod(getName(), getBuild(), getInfo().getURL()));
		return mods;
	}

	public CustomInfo getInfo() {
		return info;
	}
}
