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

import net.technicpack.launchercore.exception.CacheDeleteException;
import net.technicpack.launchercore.exception.DownloadException;
import net.technicpack.launchercore.exception.PackNotAvailableOfflineException;
import net.technicpack.launchercore.install.Version;
import net.technicpack.launchercore.install.InstalledPack;
import net.technicpack.launchercore.install.ModpackInstaller;
import net.technicpack.launchercore.install.User;
import net.technicpack.launchercore.launch.LaunchOptions;
import net.technicpack.launchercore.launch.MinecraftLauncher;
import net.technicpack.launchercore.minecraft.CompleteVersion;
import net.technicpack.launchercore.util.Settings;
import org.spoutcraft.launcher.entrypoint.SpoutcraftLauncher;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import net.technicpack.launchercore.util.LaunchAction;

public class InstallThread extends Thread {
	private final User user;
	private final InstalledPack pack;
	private final ModpackInstaller modpackInstaller;
	private boolean finished = false;

	public InstallThread(User user, InstalledPack pack, String build) {
		super("InstallThread");
		this.user = user;
		this.pack = pack;
		this.modpackInstaller = new ModpackInstaller(Launcher.getFrame(), pack, build);
	}

	@Override
	public void run() {
		try {

			Launcher.getFrame().getProgressBar().setVisible(true);
			CompleteVersion version = null;
			if (!pack.isLocalOnly()) {
				version = modpackInstaller.installPack(Launcher.getFrame());
			} else {
				version = modpackInstaller.prepareOfflinePack();
			}

			int memory = Memory.getMemoryFromId(Settings.getMemory()).getMemoryMB();
			MinecraftLauncher minecraftLauncher = new MinecraftLauncher(memory, pack, version);

			StartupParameters params = SpoutcraftLauncher.params;
			LaunchOptions options = new LaunchOptions( pack.getDisplayName(), pack.getIconPath(), params.getWidth(), params.getHeight(), params.getFullscreen());
			LauncherUnhider unhider = new LauncherUnhider();
			minecraftLauncher.launch(user, options, unhider);
		} catch (PackNotAvailableOfflineException e) {
			JOptionPane.showMessageDialog(Launcher.getFrame(), e.getMessage(), "Cannot Start Modpack", JOptionPane.WARNING_MESSAGE);
		} catch (DownloadException e) {
			JOptionPane.showMessageDialog(Launcher.getFrame(), "Error downloading file for the following pack: " + pack.getDisplayName() + " \n\n" + e.getMessage() + "\n\nPlease consult the modpack author.", "Error", JOptionPane.WARNING_MESSAGE);
		} catch (ZipException e) {
			JOptionPane.showMessageDialog(Launcher.getFrame(), "Error unzipping a file for the following pack: " + pack.getDisplayName() + " \n\n" + e.getMessage() + "\n\nPlease consult the modpack author.", "Error", JOptionPane.WARNING_MESSAGE);
		} catch (CacheDeleteException e) {
			JOptionPane.showMessageDialog(Launcher.getFrame(), "Error installing the following pack: "+pack.getDisplayName() + " \n\n" + e.getMessage() + "\n\nPlease check your system settings.", "Error", JOptionPane.WARNING_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Launcher.getFrame().getProgressBar().setVisible(false);
			LaunchAction launchAction = Settings.getLaunchAction();

			if (launchAction == null || launchAction == LaunchAction.HIDE || launchAction == LaunchAction.HIDE_WITH_CONSOLE) {
					Launcher.getFrame().setVisible(false);

					if (Settings.getShowConsole() && launchAction == LaunchAction.HIDE_WITH_CONSOLE) {
						SpoutcraftLauncher.hideConsole();
					}
			} else if (launchAction == LaunchAction.CLOSE_WITH_CONSOLE) {
				System.exit(0);
			} else if (launchAction == LaunchAction.CLOSE && Settings.getShowConsole()) {
				Launcher.getFrame().setVisible(false); // quits on pack exit
			}

			finished = true;
		}
	}

	public boolean isFinished() {
		return modpackInstaller.isFinished() || finished;
	}
}
