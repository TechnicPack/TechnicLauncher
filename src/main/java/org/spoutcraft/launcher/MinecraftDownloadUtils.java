package org.spoutcraft.launcher;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.diff.JBPatch;
import org.spoutcraft.launcher.async.Download;
import org.spoutcraft.launcher.async.DownloadListener;

public class MinecraftDownloadUtils {
	public static void downloadMinecraft(String user, String output, SpoutcraftBuild build, DownloadListener listener) throws IOException{
		int tries = 3;
		File outputFile = null;
		while (tries > 0) {
			System.out.println("Starting download of minecraft, with " + tries + " tries remaining");
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
			}
			else {
				String minecraftMD5 = MD5Utils.getMD5(FileType.minecraft, build.getLatestMinecraftVersion());
				String resultMD5 = MD5Utils.getMD5(download.getOutFile());
				System.out.println("Expected MD5: " + minecraftMD5 + " Result MD5: " + resultMD5);
				if (resultMD5.equals(minecraftMD5)) {
					if (!build.getLatestMinecraftVersion().equals(build.getMinecraftVersion())) {
						
						File patch = new File(PlatformUtils.getWorkingDirectory(), "mc.patch");
						Download patchDownload = DownloadUtils.downloadFile(build.getPatchURL(), patch.getPath(), null, null, listener);
						if (patchDownload.isSuccess()) {
							File patchedMinecraft = new File(GameUpdater.updateDir, "patched_minecraft.jar");
							patchedMinecraft.delete();
							JBPatch.bspatch(download.getOutFile(), patchedMinecraft, patch);
							String minecraft181MD5 = MD5Utils.getMD5(FileType.minecraft, build.getMinecraftVersion());
							resultMD5 = MD5Utils.getMD5(patchedMinecraft);
							
							if (minecraft181MD5.equals(resultMD5)) {
								outputFile = download.getOutFile();
								download.getOutFile().delete();
								GameUpdater.copy(patchedMinecraft, download.getOutFile());
								patchedMinecraft.delete();
								patch.deleteOnExit();
								patch.delete();
								break;
							}
						}
					}
					else {
						outputFile = download.getOutFile();
						break;
					}
				}
			}
		}
		if (outputFile == null) {
			throw new IOException("Failed to download minecraft");
		}
		GameUpdater.copy(outputFile, new File(GameUpdater.binCacheDir, "minecraft_" + build.getMinecraftVersion() + ".jar"));
	}

	@SuppressWarnings("unchecked")
	public static String[] getSpoutcraftBuilds() {
		Configuration config = SpoutcraftYML.getSpoutcraftYML();
		Map<String, Object> builds = (Map<String, Object>) config.getProperty("builds");
		String latest = config.getString("latest", null);
		String recommended = config.getString("recommended", null);
		
		if (builds != null) {
			String[] results = new String[builds.size()];
			int index = 0;
			for (String i : builds.keySet()) {
				results[index] = i.toString();
				Map<String, Object> map = (Map<String, Object>) builds.get(i);
				String version = String.valueOf(map.get("minecraft"));
				results[index] += "| " + version;
				if (i == latest) {
					results[index] += " | Latest";
				}
				if (i == recommended) {
					results[index] += " | Rec. Build";
				}
				index++;
			}
			return results;
		}
		return null;
	}
}
