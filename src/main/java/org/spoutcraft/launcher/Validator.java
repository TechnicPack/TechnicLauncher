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
package org.spoutcraft.launcher;

import java.io.File;
import java.util.List;
import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.exceptions.RestfulAPIException;
import org.spoutcraft.launcher.rest.Library;
import org.spoutcraft.launcher.util.FileType;
import org.spoutcraft.launcher.util.MD5Utils;

public class Validator{
	private boolean passed = false;
	private boolean errors = false;

	public void run(SpoutcraftData build) {
		Launcher.getGameUpdater().setStartValidationTime(System.currentTimeMillis());
		try {
			errors = !validate(build);
		} catch (RestfulAPIException e) {
			e.printStackTrace();
		}
		Launcher.getGameUpdater().validationFinished(passed);
	}

	/**
	 * Returns true if validation completed without errors, false if something went wrong deleting files
	 * 
	 * @return true on validation completion, false on failure
	 * @throws RestfulAPIException 
	 */
	private boolean validate(SpoutcraftData build) throws RestfulAPIException {
		File minecraftJar = new File(Launcher.getGameUpdater().getBinDir(), "minecraft.jar");
		if (minecraftJar.exists()) {
			if (!compareMD5s(build, FileType.MINECRAFT, minecraftJar)) {
				err("Invalid minecraft.jar");
				return minecraftJar.delete();
			}
		} else {
			err("There is no minecraft.jar!");
			return true;
		}

		File spoutcraft = new File(Launcher.getGameUpdater().getBinDir(), "modpack.jar");
		if (spoutcraft.exists()) {
			if (!compareSpoutcraftMD5s(build, spoutcraft)) {
				err("Invalid modpack.jar");
				return spoutcraft.delete();
			}
		} else {
			err("There is no modpack.jar");
			return true;
		}

		File jinputJar = new File(Launcher.getGameUpdater().getBinDir(), "jinput.jar");
		if (jinputJar.exists()) {
			if (!compareMD5s(build, FileType.JINPUT, jinputJar)) {
				err("Invalid jinput.jar");
				return jinputJar.delete();
			}
		} else {
			err("There is no jinput.jar");
			return true;
		}

		File lwjglJar = new File(Launcher.getGameUpdater().getBinDir(), "lwjgl.jar");
		if (lwjglJar.exists()) {
			if (!compareMD5s(build, FileType.LWJGL, lwjglJar)) {
				err("Invalid lwjgl.jar");
				return lwjglJar.delete();
			}
		} else {
			err("There is no lwjgl.jar");
			return true;
		}

		File lwjgl_utilJar = new File(Launcher.getGameUpdater().getBinDir(), "lwjgl_util.jar");
		if (lwjgl_utilJar.exists()) {
			if (!compareMD5s(build, FileType.LWJGL_UTIL, lwjgl_utilJar)) {
				err("Invalid lwjgl_util.jar");
				return lwjgl_utilJar.delete();
			}
		} else {
			err("There is no lwjgl_util.jar");
			return true;
		}

		File libDir = new File(Launcher.getGameUpdater().getBinDir(), "lib");
		List<Library> libraries = build.getLibraries();
		for (Library lib : libraries) {
			File libraryFile = new File(libDir, lib.name() + ".jar");

			if (libraryFile.exists()) {
				String md5 = MD5Utils.getMD5(libraryFile);
				if (!lib.valid(md5)) {
					err("Invalid " + libraryFile.getName());
					return libraryFile.delete();
				}
			} else {
				err("There is no " + libraryFile.getName());
				return true;
			}
		}
		passed = true;
		return true;
	}

	/**
	 * Returns true if the validator confirmed that all the files were correct
	 * 
	 * @return passed validation
	 */
	public boolean isValid() {
		return passed;
	}

	/**
	 * Returns true if the validator encountered an error while validating
	 * 
	 * @return true if an error occured
	 */
	public boolean hasErrors() {
		return errors;
	}

	private boolean compareMD5s(SpoutcraftData build, FileType type, File file) {
		return compareMD5s(type, build.getMinecraftVersion(), file);
	}

	private boolean compareMD5s(FileType type, String version, File file) {
		String expected = type.getMD5(version);
		String actual = MD5Utils.getMD5(file);
		debug("Checking MD5 of " + type.name() + ". Expected MD5: " + expected + " | Actual MD5: " + actual);
		if (expected == null || actual == null) {
			return false;
		}
		return expected.equals(actual);
	}

	private boolean compareSpoutcraftMD5s(SpoutcraftData build, File file) {
		String expected = build.getMD5();
		String actual = MD5Utils.getMD5(file);
		debug("Checking MD5 of mod pack. Expected MD5: " + expected + " | Actual MD5: " + actual);
		if (expected == null || actual == null) {
			return false;
		}
		return expected.equals(actual);
	}

	private void debug(Object obj) {
		if (Settings.isDebugMode()) {
			System.out.println(obj);
		}
	}

	private void err(Object obj) {
		System.err.println(obj);
	}
}
