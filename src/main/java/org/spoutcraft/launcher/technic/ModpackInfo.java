package org.spoutcraft.launcher.technic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.codehaus.jackson.annotate.JsonProperty;
import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.util.Download;
import org.spoutcraft.launcher.util.DownloadUtils;

public class ModpackInfo {
	@JsonProperty("name")
	private String name;
	@JsonProperty("recommended")
	private String recommended;
	@JsonProperty("latest")
	private String latest;
	@JsonProperty("builds")
	private String[] builds;

	private String displayName = "Technic";

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}
	public String getRecommended() {
		return recommended;
	}

	public String getLatest() {
		return latest;
	}

	public List<String> getBuilds() {
		return Arrays.asList(builds);
	}

	public String getImgURL() {
		return TechnicRestAPI.getModpackImgURL(name);
	}
	
	public String getBackgroundURL() {
		return TechnicRestAPI.getModpackBackgroundURL(name);
	}

	public BufferedImage getImg() throws IOException {
		BufferedImage image;
		File temp = new File(Launcher.getGameUpdater().getTempDir(), "logo.png");
		Download download = DownloadUtils.downloadFile(getImgURL(), temp.getAbsolutePath());
		image = ImageIO.read(download.getOutFile());
		return image;
	}
	
	public BufferedImage getBackground() throws IOException {
		BufferedImage image;
		File temp = new File(Launcher.getGameUpdater().getTempDir(), "background.jpg");
		Download download = DownloadUtils.downloadFile(getBackgroundURL(), temp.getAbsolutePath());
		image = ImageIO.read(download.getOutFile());
		return image;
	}
	@Override
	public String toString() {
		return "{ ModpackBuilds [name: " + name + ", recommended: " + recommended + ", latest: " + latest + ", builds: " + builds + "] }";
	}
}
