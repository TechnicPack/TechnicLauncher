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
package org.spoutcraft.launcher.technic;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.spoutcraft.launcher.exceptions.RestfulAPIException;

public class TechnicRestAPI {

	private static final ObjectMapper mapper = new ObjectMapper();

	public static final String REST_URL = "http://www.sctgaming.com/Technic/API/";
	public static final String MODPACKS_URL = REST_URL + "modpack/";
	public static final String CACHE_URL = REST_URL + "cache/";
	public static final String MOD_URL = CACHE_URL + "mod/";
	public static final String MIRROR_URL = "http://mirror.technicpack.net/Technic/";

	public static String getModDownloadURL(String mod, String build) {
		return MIRROR_URL + "mods/" + mod + "/" + mod + "-" + build + ".zip";
	}

	public static String getModMD5URL(String mod, String build) {
		return CACHE_URL + "mod/" + mod + "/" + build + "/MD5";
	}

	public static String getModpackURL(String modpack, String build) {
		return MODPACKS_URL + modpack + "/build/" + build;
	}

	public static String getModpackMD5URL(String modpack) {
		return MODPACKS_URL + modpack + "/MD5";
	}

	public static String getModpackYMLURL(String modpack) {
		return MIRROR_URL + modpack + "/modpack.yml";
	}

	public static String getModpackInfoURL(String modpack) {
		return MODPACKS_URL + modpack;
	}

	public static String getModpackImgURL(String modpack) {
		return MIRROR_URL + modpack + "/resources/logo_180.png";
	}
	
	public static String getModpackBackgroundURL(String modpack) {
		return MIRROR_URL + modpack + "/resources/background.jpg";
	}

	public static List<ModpackInfo> getModpacks() throws RestfulAPIException {
		InputStream stream = null;
		String url = MODPACKS_URL;
		try {
			URL conn = new URL(url);
			stream = conn.openConnection().getInputStream();
			Modpacks result = mapper.readValue(stream, Modpacks.class);
			return result.getModpacks();
		} catch (IOException e) {
			throw new RestfulAPIException("Error accessing URL [" + url + "]", e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public static String getModMD5(String mod, String build) throws RestfulAPIException {
		InputStream stream = null;
		String url = getModMD5URL(mod, build);
		try {
			URL conn = new URL(url);
			stream = conn.openConnection().getInputStream();
			TechnicMD5 md5Result = mapper.readValue(stream, TechnicMD5.class);
			return md5Result.getMD5();
		} catch (IOException e) {
			throw new RestfulAPIException("Error accessing URL [" + url + "]", e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public static Modpack getModpack(String modpack, String build) throws RestfulAPIException {
		InputStream stream = null;
		String url = getModpackURL(modpack, build);
		try {
			URL conn = new URL(url);
			stream = conn.openConnection().getInputStream();
			Modpack result = mapper.readValue(stream, Modpack.class);
			return result.setInfo(modpack, build);
		} catch (IOException e) {
			throw new RestfulAPIException("Error accessing URL [" + url + "]", e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public static ModpackInfo getModpackInfo(String modpack) throws RestfulAPIException {
		InputStream stream = null;
		String url = getModpackInfoURL(modpack);
		try {
			URL conn = new URL(url);
			stream = conn.openStream();
			ModpackInfo result = mapper.readValue(stream, ModpackInfo.class);
			return result;
		} catch (IOException e) {
			throw new RestfulAPIException("Error accessing URL [" + url + "]", e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public static String getLatestBuild(String modpack) throws RestfulAPIException {
		return getModpackInfo(modpack).getLatest();
	}

	public static String getRecommendedBuild(String modpack) throws RestfulAPIException {
		return getModpackInfo(modpack).getRecommended();
	}

	public static String getModpackMD5(String modpack) throws RestfulAPIException {
		InputStream stream = null;
		String url = getModpackMD5URL(modpack);
		try {
			URL conn = new URL(url);
			stream = conn.openConnection().getInputStream();
			TechnicMD5 md5Result = mapper.readValue(stream, TechnicMD5.class);
			return md5Result.getMD5();
		} catch (IOException e) {
			throw new RestfulAPIException("Error accessing URL [" + url + "]", e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	private static class TechnicMD5 {
		@JsonProperty("MD5")
		String md5;

		public String getMD5() {
			return md5;
		}
	}
}
