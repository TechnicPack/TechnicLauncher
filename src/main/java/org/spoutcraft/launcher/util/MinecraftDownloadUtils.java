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
import java.io.IOException;

import org.spoutcraft.diff.JBPatch;
import org.spoutcraft.launcher.technic.PackInfo;
import org.spoutcraft.launcher.technic.rest.Modpack;
import org.spoutcraft.launcher.technic.rest.RestAPI;
import org.spoutcraft.launcher.util.Download.Result;

public class MinecraftDownloadUtils {

	public static void downloadMinecraft(String user, String output, PackInfo pack, Modpack build, DownloadListener listener) throws IOException {
		File outFile = null;
		String version = build.getMinecraftVersion();
		String url = RestAPI.getMinecraftURL(version);
		String md5 = RestAPI.getMinecraftMD5(version);
		pack.init();
		Download download = DownloadUtils.downloadFile(url, output, null, md5, listener);
		outFile = download.getOutFile();

		if (RestAPI.shouldUsePatch(version)) {
			patchMinecraft(pack, version, build.getMinecraftMd5(), outFile, listener);
		}

		Utils.copy(outFile, new File(Utils.getCacheDirectory(), "minecraft_" + build.getMinecraftVersion() + ".jar"));
	}

	private static void patchMinecraft(PackInfo pack, String version, String md5, File outFile, DownloadListener listener) throws IOException {
		File patch = new File(pack.getPackDirectory(), "mc.patch");
		Download patchDownload = DownloadUtils.downloadFile(RestAPI.getPatchURL(version), patch.getPath(), null, null, listener);
		if (patchDownload.getResult() == Result.SUCCESS) {
			File patchedMinecraft = new File(pack.getTempDir(), "patched_minecraft.jar");
			patchedMinecraft.delete();
			JBPatch.bspatch(outFile, patchedMinecraft, patch);

			String resultMD5 = MD5Utils.getMD5(patchedMinecraft);

			if (md5.equalsIgnoreCase(resultMD5)) {
				outFile.delete();
				Utils.copy(patchedMinecraft, outFile);
				patchedMinecraft.delete();
				patch.delete();
			}
		}
	}
}
