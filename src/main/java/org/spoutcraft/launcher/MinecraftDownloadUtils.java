package org.spoutcraft.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.spoutcraft.diff.JBPatch;
import org.spoutcraft.launcher.VersionTree.VersionPair;
import org.spoutcraft.launcher.async.Download;
import org.spoutcraft.launcher.async.DownloadListener;

public class MinecraftDownloadUtils {

	public static void downloadMinecraft(String user, String output, ModpackBuild build, DownloadListener listener) throws IOException {
		int tries = 3;
		File outputFile = null;
		String requiredMinecraftVersion = build.getMinecraftVersion();
		while (tries > 0) {
			Util.logi("Starting download of minecraft, with %s trie(s) remaining", tries);
			tries--;
			Download download = new Download(build.getMinecraftURL(user), output);
			download.setListener(listener);
			download.run();
			if (!download.isSuccess()) {
				if (download.getOutFile() != null) {
					download.getOutFile().delete();
				}
				System.err.println("Download of minecraft failed!");
				listener.stateChanged("Download Failed, retries remaining: " + tries, 0F);
			} else {
				// String minecraftMD5 = MD5Utils.getMD5(FileType.minecraft,
				// build.getLatestMinecraftVersion());
				String resultMD5 = MD5Utils.getMD5(download.getOutFile());

				String minecraftVersion = MD5Utils.getMinecraftMD5(resultMD5);
				if (minecraftVersion != null) {
					Util.log("Downloaded 'minecraft.jar' matches MD5 of version '%s'.", minecraftVersion);
				} else {
					Util.log("Downloaded 'minecraft.jar' does not matche MD5 of any known minecraft version!");
					continue;
				}

				// Util.log("Expected MD5: " + minecraftMD5 + " Result MD5: " +
				// resultMD5);
				//
				// if (!resultMD5.equals(minecraftMD5)) {
				// continue;
				// }

				if (!minecraftVersion.equals(requiredMinecraftVersion)) {
					VersionTree versions = new VersionTree(GameUpdater.workDir + "/minecraft_diffs.txt", minecraftVersion);
					List<VersionPair> ver_path = versions.getVersionPath(requiredMinecraftVersion);
					if(ver_path == null)
					{
						// If no path was found, fake it and hope there's a diff file directly between
						// these two versions!
						ver_path = new ArrayList<VersionPair>();
						VersionPair vp = versions.new VersionPair();
						vp.ver_first = minecraftVersion;
						vp.ver_next = requiredMinecraftVersion;
						ver_path.add(vp);
					}

					for(VersionPair vp : ver_path)
					{					
						File patch = new File(GameUpdater.tempDir, "mc.patch");
						String patchURL = build.getPatchURL(vp.ver_first, vp.ver_next);
						Download patchDownload = DownloadUtils.downloadFile(patchURL, patch.getPath(), null, null, listener);
						if (patchDownload.isSuccess()) {
							File patchedMinecraft = new File(GameUpdater.tempDir, "patched_minecraft.jar");
							patchedMinecraft.delete();
							listener.stateChanged(String.format("Patching Minecraft to '%s'.", vp.ver_next), 0F);
							JBPatch.bspatch(download.getOutFile(), patchedMinecraft, patch);
							listener.stateChanged(String.format("Patched Minecraft to '%s'.", vp.ver_next), 100F);
							String currentMinecraftMD5 = MD5Utils.getMD5(FileType.minecraft, vp.ver_next);
							resultMD5 = MD5Utils.getMD5(patchedMinecraft);
	
							if (currentMinecraftMD5.equals(resultMD5)) {
								outputFile = download.getOutFile();
								download.getOutFile().delete();
								GameUpdater.copy(patchedMinecraft, download.getOutFile());
								patchedMinecraft.delete();
								patch.deleteOnExit();
								patch.delete();
								continue;
							}
						}
					}
					String currentMinecraftMD5 = MD5Utils.getMD5(FileType.minecraft, requiredMinecraftVersion);
					resultMD5 = MD5Utils.getMD5(download.getOutFile());

					if (currentMinecraftMD5.equals(resultMD5)) {
						break;
					}

				} else {
					outputFile = download.getOutFile();
					break;
				}
			}
		}
		if (outputFile == null) { throw new IOException("Failed to download minecraft"); }
		GameUpdater.copy(outputFile, new File(GameUpdater.cacheDir, "minecraft_" + requiredMinecraftVersion + ".jar"));
	}
	
}
