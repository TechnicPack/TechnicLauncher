/*
 * This file is part of Technic Launcher Core.
 * Copyright (C) 2013 Syndicate, LLC
 *
 * Technic Launcher Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Technic Launcher Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License,
 * as well as a copy of the GNU Lesser General Public License,
 * along with Technic Launcher Core.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.spoutcraft.launcher;

import net.technicpack.launchercore.launch.MinecraftExitListener;
import net.technicpack.launchercore.launch.MinecraftProcess;
import net.technicpack.launchercore.util.LaunchAction;
import net.technicpack.launchercore.util.Settings;
import org.spoutcraft.launcher.entrypoint.SpoutcraftLauncher;

public class LauncherUnhider implements MinecraftExitListener {
	@Override
	public void onMinecraftExit(MinecraftProcess process) {
		LaunchAction action = Settings.getLaunchAction();
		if (action == null || action == LaunchAction.HIDE || action == LaunchAction.HIDE_WITH_CONSOLE) {
			Launcher.getFrame().setVisible(true);

			if (Settings.getShowConsole() && action == LaunchAction.HIDE_WITH_CONSOLE) {
				SpoutcraftLauncher.showConsole();
			}
		} else if (action == LaunchAction.CLOSE_WITH_CONSOLE) {
			System.exit(0);
		}
	}
}
