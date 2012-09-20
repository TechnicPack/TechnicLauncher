package org.spoutcraft.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.util.config.Configuration;

public class MD5Utils {

	private static final String								CHECKSUM_MD5	= "CHECKSUM.md5";
	private static final File									CHECKSUM_FILE	= new File(GameUpdater.workDir, CHECKSUM_MD5);
	private static boolean										updated;
	private static final Map<String, String>	md5Map				= new HashMap<String, String>();

	public static String getMD5(File file) {
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			String md5Hex = DigestUtils.md5Hex(stream);
			stream.close();
			return md5Hex;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getMD5(FileType type) {
		return getMD5(type, MinecraftYML.getLatestMinecraftVersion());
	}

	@SuppressWarnings("unchecked")
	public static String getMD5(FileType type, String version) {
		Configuration config = MinecraftYML.getMinecraftYML();
		Map<String, Map<String, String>> builds = (Map<String, Map<String, String>>) config.getProperty("versions");
		if (builds.containsKey(version)) {
			Map<String, String> files = builds.get(version);
			return files.get(type.name());
		}
		return null;
	}

	public static String getMinecraftMD5(String md5Hash) {
		Configuration config = MinecraftYML.getMinecraftYML();
		Map<String, Map<String, String>> builds = (Map<String, Map<String, String>>) config.getProperty("versions");
		for (String version : builds.keySet()) {
			String minecraftMD5 = builds.get(version).get("minecraft");
			if (minecraftMD5.equalsIgnoreCase(md5Hash)) { return version; }
		}
		return null;
	}

	public static void updateMD5Cache() {
		if (!updated && !Main.isOffline) {
			updated = true;
			try {
				String url = MirrorUtils.getMirrorUrl(CHECKSUM_MD5, null);

				if (url == null) {
					if (GameUpdater.canPlayOffline()) {
						Main.isOffline = true;
						parseChecksumFile();
					}
					return;
				}

				if (DownloadUtils.downloadFile(url, CHECKSUM_FILE.getPath()).isSuccess()) {
					parseChecksumFile();
				}
			} catch (FileNotFoundException e) {
				Util.log("Checksum file '%s' not found.", CHECKSUM_FILE.getAbsoluteFile());
				e.printStackTrace();
			} catch (IOException e) {
				Util.log("Checksum file '%s' threw error.", CHECKSUM_FILE.getAbsoluteFile());
				e.printStackTrace();
			}
		}
	}

	private static void parseChecksumFile() throws FileNotFoundException {
		md5Map.clear();
		Scanner scanner = new Scanner(CHECKSUM_FILE).useDelimiter("\\||\n");
		while (scanner.hasNext()) {
			String md5 = scanner.next().toLowerCase();
			String path = scanner.next().replace("\r", "").replace('/', '\\');
			md5Map.put(path, md5);
			scanner.nextLine();
		}
	}

	public static boolean checksumPath(String relativePath) {
		return checksumPath(relativePath, relativePath);
	}

	public static boolean checksumPath(String filePath, String md5Path) {
		return checksumPath(new File(GameUpdater.workDir, filePath), md5Path);
	}

	public static boolean checksumCachePath(String filePath, String md5Path) {
		return checksumPath(new File(GameUpdater.cacheDir, filePath), md5Path);
	}

	public static boolean checksumPath(File file, String md5Path) {
		return false;
	}

	public static String getMD5FromList(String md5Path) {
		md5Path = md5Path.replace('/', '\\');
		return (!md5Map.containsKey(md5Path)) ? null : md5Map.get(md5Path);
	}
}
