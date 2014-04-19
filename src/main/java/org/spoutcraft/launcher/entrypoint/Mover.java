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

package org.spoutcraft.launcher.entrypoint;

import net.technicpack.launchercore.util.Directories;
import net.technicpack.launchercore.util.OperatingSystem;
import net.technicpack.launchercore.util.Utils;
import org.apache.commons.io.IOUtils;
import org.spoutcraft.launcher.settings.LauncherDirectories;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Mover {
	public static void main(String[] args) {
		main(args, false);
	}

	public static void main(String[] args, boolean exe) {
		try {
			Directories.instance = new LauncherDirectories();
			SpoutcraftLauncher.setupLogger();
			execute(args, exe);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static void execute(String[] args, boolean exe) throws Exception {
		File temp;
		if (exe) {
			temp = new File(Utils.getSettingsDirectory(), "temp.exe");
		} else {
			temp = new File(Utils.getSettingsDirectory(), "temp.jar");
		}
		File codeSource = new File(args[0]);
		codeSource.delete();
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(temp);
			fos = new FileOutputStream(codeSource);
			IOUtils.copy(fis, fos);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(fos);
		}

		codeSource.setExecutable(true, true);

		ProcessBuilder processBuilder = new ProcessBuilder();
		ArrayList<String> commands = new ArrayList<String>();
		if (!exe) {
			if (OperatingSystem.getOperatingSystem().equals(OperatingSystem.WINDOWS)) {
				commands.add("javaw");
			} else {
				commands.add("java");
			}
			commands.add("-Xmx256m");
			commands.add("-XX:+UseConcMarkSweepGC");
			commands.add("-XX:+CMSPermGenSweepingEnabled");
			commands.add("-XX:+CMSClassUnloadingEnabled");
			commands.add("-cp");
			commands.add(codeSource.getAbsolutePath());
			commands.add(SpoutcraftLauncher.class.getName());
		} else {
			commands.add(temp.getAbsolutePath());
			commands.add("-Launcher");
		}
		commands.addAll(Arrays.asList(args));
		processBuilder.command(commands);

		processBuilder.start();
	}
}
