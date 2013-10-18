/*
 * This file is part of Technic Launcher.
 * Copyright (C) 2013 Syndicate, LLC
 *
 * Technic Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Technic Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Technic Launcher.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.spoutcraft.launcher;

import net.technicpack.launchercore.install.Version;
import net.technicpack.launchercore.install.InstalledPack;
import net.technicpack.launchercore.install.ModpackInstaller;
import net.technicpack.launchercore.install.User;
import net.technicpack.launchercore.launch.MinecraftLauncher;
import net.technicpack.launchercore.minecraft.CompleteVersion;
import net.technicpack.launchercore.util.Settings;

import java.io.File;
import java.io.IOException;
import net.technicpack.launchercore.launch.MinecraftExitListener;

public class InstallThread extends Thread {
	private final User user;
	private final InstalledPack pack;
	private final ModpackInstaller modpackInstaller;
	private final MinecraftExitListener exitListener;
	private boolean finished = false;

	public InstallThread(User user, InstalledPack pack, String build, MinecraftExitListener launcherHider) {
		super("InstallThread");
		this.user = user;
		this.pack = pack;
		this.modpackInstaller = new ModpackInstaller(Launcher.getFrame(), pack, build);
		this.exitListener = launcherHider;
	}

	@Override
	public void run() {
		try {

			Launcher.getFrame().getProgressBar().setVisible(true);
			CompleteVersion version = modpackInstaller.installPack(Launcher.getFrame());

			int memory = Memory.getMemoryFromId(Settings.getMemory()).getMemoryMB();
			MinecraftLauncher minecraftLauncher = new MinecraftLauncher(memory, pack, version);
			minecraftLauncher.launch(user).setExitListener(exitListener);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Launcher.getFrame().getProgressBar().setVisible(false);
			Launcher.getFrame().setVisible(false);
			finished = true;
		}
	}

	public boolean isFinished() {
		return modpackInstaller.isFinished() || finished;
	}
}
