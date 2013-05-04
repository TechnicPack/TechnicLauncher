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

package org.spoutcraft.launcher.technic.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.spoutcraft.launcher.Settings;
import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.exceptions.RestfulAPIException;
import org.spoutcraft.launcher.rest.Versions;
import org.spoutcraft.launcher.technic.CustomInfo;
import org.spoutcraft.launcher.technic.RestInfo;
import org.spoutcraft.launcher.technic.rest.pack.RestModpack;
import org.spoutcraft.launcher.util.MirrorUtils;

public class RestAPI {
	private static RestAPI TECHNIC;
	private static Map<String, Minecraft> mcVersions;
	private static FullModpacks DEFAULT;

	private static final String PLATFORM = "http://www.technicpack.net/";
	private static final String PATCH_VERSION = "1.4.7";
	private static final ObjectMapper mapper = new ObjectMapper();

	private final String restURL;
	private final String restInfoURL;
	private final String modURL;

	private String mirrorURL;
	private static Modpacks modpacks;

	public RestAPI(String url) {
		restURL = url;
		restInfoURL = restURL + "modpack/";
		modURL = restURL + "mod/";
		try {
			modpacks = setupModpacks();
			modpacks.setRest(this);
			mirrorURL = modpacks.getMirrorURL();

		} catch (RestfulAPIException e) {
			Launcher.getLogger().log(Level.SEVERE, "Unable to connect to the Rest API at " + url + " Running Offline instead.");
			mirrorURL = "";
		}

		if (mcVersions == null) {
			try {
				mcVersions = getMinecraftVersions();
			} catch (RestfulAPIException e) {
				Launcher.getLogger().log(Level.SEVERE, "Unable to load minecraft versions from " + getMinecraftVersionURL(), e);
				mcVersions = Collections.emptyMap();
			}
		}
	}

	public static void setupDefault() {
		if (DEFAULT != null) {
			return;
		}
		try {
			DEFAULT = getRestObject(FullModpacks.class, getDefault().getRestInfoURL() + "?include=full");
		} catch (RestfulAPIException e) {
			Launcher.getLogger().log(Level.SEVERE, "Unable to connect to the Rest API at " + TECHNIC.getRestInfoURL() + "?include=full" + " Running Offline instead.", e);
		}
	}

	public static Collection<RestInfo> getDefaults() {
		getDefault();
		if (DEFAULT != null) {
			return DEFAULT.getModpacks();
		} else {
			return Collections.emptyList();
		}
	}

	public static RestAPI getDefault() {
		if (TECHNIC == null) {
			TECHNIC = new RestAPI("http://solder.technicpack.net/api/");
			setupDefault();
		}

		return TECHNIC;
	}

	public static String getPlatformAPI() {
		return PLATFORM + "api/";
	}

	public static String getPlatformURL() {
		return PLATFORM;
	}

	public String getRestURL() {
		return restURL;
	}

	public String getRestInfoURL() {
		return restInfoURL;
	}

	public String getModURL() {
		return modURL;
	}

	public String getModDownloadURL(String mod, String build) {
		return getMirrorURL() + "mods/" + mod + "/" + mod + "-" + build + ".zip";
	}

	public String getModMD5URL(String mod, String build) {
		return getModURL() + mod + "/" + build;
	}

	public String getModpackURL(String modpack, String build) {
		return getRestInfoURL() + modpack + "/" + build;
	}

	public String getModpackInfoURL(String modpack) {
		return getRestInfoURL() + modpack;
	}

	public String getModpackImgURL(String modpack) {
		return getMirrorURL() + modpack + "/resources/logo_180.png";
	}

	public String getModpackBackgroundURL(String modpack) {
		return getMirrorURL() + modpack + "/resources/background.jpg";
	}

	public String getModpackIconURL(String modpack) {
		return getMirrorURL() + modpack + "/resources/icon.png";
	}

	public static String getCustomPackURL(String modpack) {
		return getPlatformAPI() + "modpack/" + modpack;
	}

	public static String getMinecraftVersionURL() {
		return getPlatformAPI() + "minecraft";
	}

	public static String getDownloadCountURL(String modpack) {
		if (Settings.isPackCustom(modpack)) {
			return getCustomPackURL(modpack) + "/download";
		}
		return getDefault().getRestURL() + "";
	}

	public static String getRunCountURL(String modpack) {
		if (Settings.isPackCustom(modpack)) {
			return getCustomPackURL(modpack) + "/run";
		}
		return getDefault().getRestURL() + "";
	}

	private Modpacks setupModpacks() throws RestfulAPIException {
		Modpacks result = getRestObject(Modpacks.class, restInfoURL);
		result.setRest(this);
		return result;
	}

	public Modpacks getModpacks() {
		return modpacks;
	}

	public String getMirrorURL() {
		return mirrorURL;
	}

	public Collection<RestInfo> getRestInfos() throws RestfulAPIException {
		return modpacks.getModpacks();
	}

	public String getModMD5(String mod, String build) throws RestfulAPIException {
		TechnicMD5 result = getRestObject(TechnicMD5.class, getModMD5URL(mod, build));
		return result.getMD5();
	}

	public RestModpack getModpack(RestInfo modpack, String build) throws RestfulAPIException {
		RestModpack result = getRestObject(RestModpack.class, getModpackURL(modpack.getName(), build));
		result.setRest(this);
		return result.setInfo(modpack, build);
	}

	public RestInfo getModpackInfo(String modpack) throws RestfulAPIException {
		RestInfo result;
		if (this.equals(getDefault())) {
			result = DEFAULT.getMap().get(modpack);
		} else {
			result = getRestObject(RestInfo.class, getModpackInfoURL(modpack));
		}
		
		result.setRest(this);
		result.init();

		return result;
	}

	public static CustomInfo getCustomModpack(String packURL) throws RestfulAPIException {
		CustomInfo info = getRestObject(CustomInfo.class, packURL);
		return info;
	}

	public String getLatestBuild(String modpack) throws RestfulAPIException {
		return getModpackInfo(modpack).getLatest();
	}

	public String getRecommendedBuild(String modpack) throws RestfulAPIException {
		return getModpackInfo(modpack).getRecommended();
	}

	public static int getLatestLauncherBuild(String stream) throws RestfulAPIException {
		LauncherBuild result = getRestObject(LauncherBuild.class, getPlatformAPI() + "launcher/version/" + stream);
		return result.getLatestBuild();
	}
	
	public static String getLauncherDownloadURL(int version, Boolean isJar) throws RestfulAPIException {
		String ext = null;
		if (isJar) {
			ext = "jar";
		} else {
			ext = "exe";
		}
		
		String url = getPlatformAPI() + "launcher/url/" + version + "/" + ext;
		LauncherURL result = getRestObject(LauncherURL.class, url);
		return result.getLauncherURL();
	}

	public static <T extends RestObject> T getRestObject(Class<T> restObject, String url) throws RestfulAPIException {
		InputStream stream = null;
		try {
			URLConnection conn = new URL(url).openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(15000);

			stream = conn.getInputStream();
			T result = mapper.readValue(stream, restObject);
			if (result.hasError()) {
				throw new RestfulAPIException("Error in json response: " + result.getError());
			}

			return result;
		} catch (SocketTimeoutException e) {
			throw new RestfulAPIException("Timed out accessing URL [" + url + "]", e);
		} catch (IOException e) {
			throw new RestfulAPIException("Error accessing URL [" + url + "]", e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public static List<Article> getNews() throws RestfulAPIException {
		InputStream stream = null;
		try {
			URLConnection conn = new URL(getPlatformAPI() + "news/").openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(15000);

			stream = conn.getInputStream();
			List<Article> versions = mapper.readValue(stream, new TypeReference<List<Article>>() {});

			for (Article result : versions) {
				if (result.hasError()) {
					throw new RestfulAPIException("Error in json response: " + result.getError());
				}
			}

			return versions;
		} catch (SocketTimeoutException e) {
			throw new RestfulAPIException("Timed out accessing URL [" + getMinecraftVersionURL() + "]", e);
		} catch (IOException e) {
			throw new RestfulAPIException("Error accessing URL [" + getMinecraftVersionURL() + "]", e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public static HashMap<String, Minecraft> getMinecraftVersions() throws RestfulAPIException {
		InputStream stream = null;
		try {
			URLConnection conn = new URL(getMinecraftVersionURL()).openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(15000);

			stream = conn.getInputStream();
			HashMap<String, Minecraft> versions = mapper.readValue(stream, new TypeReference<Map<String, Minecraft>>() {});

			for (Minecraft result : versions.values()) {
				if (result.hasError()) {
					throw new RestfulAPIException("Error in json response: " + result.getError());
				}
			}

			return versions;
		} catch (SocketTimeoutException e) {
			throw new RestfulAPIException("Timed out accessing URL [" + getMinecraftVersionURL() + "]", e);
		} catch (IOException e) {
			throw new RestfulAPIException("Error accessing URL [" + getMinecraftVersionURL() + "]", e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public static String getMinecraftURL(String version) {
		Minecraft minecraft = mcVersions.get(version);
		System.out.println(minecraft);
		if (minecraft == null || minecraft.shouldUsePatch()) {
			version = PATCH_VERSION;
		}

		if (Arrays.asList(Versions.OLD_ASSETS).contains(version)) {
			version = version.replace('.', '_');
			return "http://assets.minecraft.net/" + version + "/minecraft.jar";
		}
		return "https://s3.amazonaws.com/Minecraft.Download/versions/" + version +"/" + version + ".jar";
	}

	public static String getMinecraftMD5(String version) {
		Minecraft minecraft = mcVersions.get(version);
		if (shouldUsePatch(version)) {
			version = PATCH_VERSION;
		}
		minecraft = mcVersions.get(version);

		if (minecraft == null) {
			return null;
		}
		return minecraft.getMd5();
	}

	public static boolean shouldUsePatch(String version) {
		Minecraft minecraft = mcVersions.get(version);
		boolean shouldPatch = false;
		if (minecraft == null && (version.equals("1.2.3") || version.equals("1.2.5"))) {
			shouldPatch = true;
		} else if (minecraft != null) {
			shouldPatch = minecraft.shouldUsePatch();
		}
		return shouldPatch;
	}

	public static String getPatchURL(String version) {
		String mirrorURL = "Patches/Minecraft/minecraft_";
		mirrorURL += Versions.getLatestMinecraftVersion();
		mirrorURL += "-" + version + ".patch";
		String fallbackURL = "http://get.spout.org/patch/minecraft_";
		fallbackURL += Versions.getLatestMinecraftVersion();
		fallbackURL += "-" + version + ".patch";
		return MirrorUtils.getMirrorUrl(mirrorURL, fallbackURL);
	}

	private static class TechnicMD5 extends RestObject {
		@JsonProperty("md5")
		private String md5;

		public String getMD5() {
			return md5;
		}
	}
	
	private static class LauncherBuild extends RestObject {
		@JsonProperty("LatestBuild")
		private int latestBuild;
		
		public int getLatestBuild() {
			return latestBuild;
		}
	}
	
	private static class LauncherURL extends RestObject {
		@JsonProperty("URL")
		private String launcherURL;
		
		public String getLauncherURL() {
			return launcherURL;
		}
	}

	private static class Minecraft extends RestObject {
		@JsonProperty("version")
		private String version;
		@JsonProperty("md5")
		private String md5;
		@JsonProperty("use_patch")
		private boolean usePatch;

		@SuppressWarnings("unused")
		public String getVersion() {
			return version;
		}

		public String getMd5() {
			return md5;
		}

		public boolean shouldUsePatch() {
			return usePatch;
		}

		@Override
		public String toString() {
			return "Minecraft: " + version + " md5: " + md5 + " use_patch: " + usePatch;
		}
	}
}
