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

import org.codehaus.jackson.annotate.JsonProperty;

public class Article extends RestObject {

	@JsonProperty("title")
	private String title;
	@JsonProperty("image")
	private String image;
	@JsonProperty("display_title")
	private String displayTitle;
	@JsonProperty("category")
	private String category;
	@JsonProperty("user")
	private String user;
	@JsonProperty("summary")
	private String summary;
	@JsonProperty("created_at")
	private String date;

	public String getCategory() {
		return category;
	}

	public String getDate() {
		return date;
	}

	public String getDisplayTitle() {
		return displayTitle;
	}

	public String getSummary() {
		return summary;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return RestAPI.getPlatformURL() + "article/view/" + title;
	}

	public String getImageUrl() {
		return RestAPI.getPlatformURL() + "something" + image;
	}

	public String getUser() {
		return user;
	}
}
