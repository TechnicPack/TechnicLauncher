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

package org.spoutcraft.launcher.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Apache Commons IO
 */
public class FileUtils {
	/**
	 * Deletes a directory recursively.
	 *
	 * @param directory directory to delete
	 * @throws IOException in case deletion is unsuccessful
	 */
	public static void deleteDirectory(File directory) throws IOException {
		if (directory == null || !directory.exists()) {
			return;
		}

		if (!isSymlink(directory)) {
			cleanDirectory(directory);
		}

		if (!directory.delete()) {
			String message = "Unable to delete directory " + directory + ".";
			throw new IOException(message);
		}
	}

	/**
	 * Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories.
	 * <p/>
	 * The difference between File.delete() and this method are:
	 * <ul>
	 * <li>A directory to be deleted does not have to be empty.</li>
	 * <li>No exceptions are thrown when a file or directory cannot be deleted.</li>
	 * </ul>
	 *
	 * @param file file or directory to delete, can be <code>null</code>
	 * @return <code>true</code> if the file or directory was deleted, otherwise
	 *         <code>false</code>
	 * @since Commons IO 1.4
	 */
	public static boolean deleteQuietly(File file) {
		if (file == null) {
			return false;
		}
		try {
			if (file.isDirectory()) {
				cleanDirectory(file);
			}
		} catch (Exception ignored) {
		}

		try {
			return file.delete();
		} catch (Exception ignored) {
			return false;
		}
	}

	/**
	 * Cleans a directory without deleting it.
	 *
	 * @param directory directory to clean
	 * @throws IOException in case cleaning is unsuccessful
	 */
	public static void cleanDirectory(File directory) throws IOException {
		cleanDirectory(directory, true);
	}

	/**
	 * Cleans a directory without deleting it.
	 *
	 * @param directory directory to clean
	 * @param directories true to delete sub directories as well
	 * @throws IOException in case cleaning is unsuccessful
	 */
	public static void cleanDirectory(File directory, boolean directories) throws IOException {
		if (!directory.exists()) {
			String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
		}

		if (!directory.isDirectory()) {
			String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
		}

		File[] files = directory.listFiles();
		if (files == null) { // null if security restricted
			throw new IOException("Failed to list contents of " + directory);
		}

		IOException exception = null;
		for (File file : files) {
			try {
				if (file.isDirectory() && !directories) {
					continue;
				}
				forceDelete(file);
			} catch (IOException ioe) {
				exception = ioe;
			}
		}

		if (null != exception) {
			throw exception;
		}
	}

	/**
	 * Deletes a file. If file is a directory, delete it and all sub-directories.
	 * <p/>
	 * The difference between File.delete() and this method are:
	 * <ul>
	 * <li>A directory to be deleted does not have to be empty.</li>
	 * <li>You get exceptions when a file or directory cannot be deleted.
	 * (java.io.File methods returns a boolean)</li>
	 * </ul>
	 *
	 * @param file file or directory to delete, must not be <code>null</code>
	 * @throws NullPointerException  if the directory is <code>null</code>
	 * @throws FileNotFoundException if the file was not found
	 * @throws IOException           in case deletion is unsuccessful
	 */
	public static void forceDelete(File file) throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(file);
		} else {
			boolean filePresent = file.exists();
			if (!file.delete()) {
				if (!filePresent) {
					throw new FileNotFoundException("File does not exist: " + file);
				}
				String message = "Unable to delete file: " + file;
				throw new IOException(message);
			}
		}
	}

	/**
	 * Determines whether the specified file is a Symbolic Link rather than an actual file.
	 * <p/>
	 * Will not return true if there is a Symbolic Link anywhere in the path,
	 * only if the specific file is.
	 *
	 * @param file the file to check
	 * @return true if the file is a Symbolic Link
	 * @throws IOException if an IO error occurs while checking the file
	 * @since Commons IO 2.0
	 */
	public static boolean isSymlink(File file) throws IOException {
		if (file == null) {
			throw new NullPointerException("File must not be null");
		}
		File fileInCanonicalDir = null;
		if (file.getParent() == null) {
			fileInCanonicalDir = file;
		} else {
			File canonicalDir = file.getParentFile().getCanonicalFile();
			fileInCanonicalDir = new File(canonicalDir, file.getName());
		}

		if (fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile())) {
			return false;
		} else {
			return true;
		}
	}

	public static void moveDirectory(File dir, File dest) {
		if (!dest.exists()) {
			dest.mkdirs();
		}
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				File newDir = new File(dest, file.getName());
				newDir.mkdir();
				moveDirectory(file, newDir);
				continue;
			}
			file.renameTo(new File(dest, file.getName()));
		}
		deleteQuietly(dir);
	}

	public static boolean checkLaunchDirectory(File dir) {
		if (!dir.isDirectory()) {
			return false;
		}

		if (dir.list().length == 0) {
			return true;
		}

		for (File file: dir.listFiles()) {
			if (file.getName().equals("settings.yml")) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if a directory is empty
	 * 
	 * @param dir to check
	 * @return true if the directory is empty
	 */
	public static boolean checkEmpty(File dir) {
		if (!dir.isDirectory()) {
			return false;
		}

		return dir.list().length == 0;
	}
}
