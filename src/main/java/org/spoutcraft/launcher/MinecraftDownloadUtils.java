package org.spoutcraft.launcher;

import java.io.File;
import java.io.IOException;
import org.spoutcraft.diff.JBPatch;
import org.spoutcraft.launcher.async.Download;
import org.spoutcraft.launcher.async.DownloadListener;

public class MinecraftDownloadUtils {

	public static void downloadMinecraft(String user, String output, ModpackBuild build, DownloadListener listener) throws IOException {
		String requiredMinecraftVersion = build.getMinecraftVersion();

		if (downloadMinecraftVersion(requiredMinecraftVersion, listener))
			return;

		int tries = 3;
		File outputFile = null;
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
				String resultMD5 = MD5Utils.getMD5(download.getOutFile());

				String minecraftVersion = MD5Utils.getMinecraftMD5(resultMD5);
				GameUpdater.copy(download.getOutFile(), new File(GameUpdater.cacheDir, "minecraft_" + minecraftVersion + ".jar"));
				if (minecraftVersion != null) {
					Util.log("Downloaded 'minecraft.jar' matches MD5 of version '%s'.", minecraftVersion);
				} else {
					Util.log("Downloaded 'minecraft.jar' does not matche MD5 of any known minecraft version!");
					continue;
				}

				if (!minecraftVersion.equals(requiredMinecraftVersion)) {
					if (downloadMinecraftVersion(requiredMinecraftVersion, listener))
						return;
				} else {
					outputFile = download.getOutFile();
					break;
				}
			}
		}
		if (outputFile == null) { throw new IOException("Failed to download minecraft"); }
		GameUpdater.copy(outputFile, new File(GameUpdater.cacheDir, "minecraft_" + requiredMinecraftVersion + ".jar"));
	}

	public static boolean downloadMinecraftVersion(String requiredMinecraftVersion, DownloadListener listener) {
		String latestCached = MinecraftYML.getLatestCachedMinecraft();
		if (latestCached == null || MinecraftYML.compareVersions(requiredMinecraftVersion, latestCached) > 0)
			return false;

		for (String cachedVersion : MinecraftYML.getCachedMinecraftVersions()) {
			if (MD5Utils.getMD5FromList("Patches/Minecraft/minecraft_"+cachedVersion+"-"+requiredMinecraftVersion+".patch") != null)
				return downgradeFrom(cachedVersion, requiredMinecraftVersion, listener);
		}
		return false;
	}

	private static boolean downgradeFrom(String cachedVersion, String requiredMinecraftVersion, DownloadListener listener) {
		File patch = new File(GameUpdater.tempDir, "mc.patch");
		String patchURL = MirrorUtils.getMirrorUrl("Patches/Minecraft/minecraft_"+cachedVersion+"-"+requiredMinecraftVersion+".patch", null);
		File cachedFile = new File(GameUpdater.cacheDir, "minecraft_" + cachedVersion + ".jar");
		File requiredFile = new File(GameUpdater.cacheDir, "minecraft_" + requiredMinecraftVersion + ".jar");
		Download patchDownload;
		try {
			patchDownload = DownloadUtils.downloadFile(patchURL, patch.getPath(), null, null, listener);
			if (patchDownload.isSuccess()) {
				File patchedMinecraft = new File(GameUpdater.tempDir, "patched_minecraft.jar");
				patchedMinecraft.delete();
				listener.stateChanged(String.format("Patching Minecraft to '%s'.", requiredMinecraftVersion), 0F);
				JBPatch.bspatch(cachedFile, patchedMinecraft, patch);
				listener.stateChanged(String.format("Patched Minecraft to '%s'.", requiredMinecraftVersion), 100F);
				String currentMinecraftMD5 = MD5Utils.getMD5(FileType.minecraft, requiredMinecraftVersion);
				String resultMD5 = MD5Utils.getMD5(patchedMinecraft);
				Util.log("Comapring new jar md5 '%s' to stored md5 '%s'.", resultMD5, currentMinecraftMD5);

				if (currentMinecraftMD5.equals(resultMD5)) {
					GameUpdater.copy(patchedMinecraft, requiredFile);
					patchedMinecraft.delete();
					patch.deleteOnExit();
					patch.delete();
					return true;
				}
			}
		} catch (IOException e) {
		}
		return false;
	}
}
