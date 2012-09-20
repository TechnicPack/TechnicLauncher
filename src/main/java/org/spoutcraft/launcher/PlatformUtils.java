/*
 * This file is part of Spoutcraft Launcher (http://wiki.getspout.org/).
 * 
 * Spoutcraft Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spoutcraft Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spoutcraft.launcher;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JProgressBar;

public class PlatformUtils {

	public static final String	LAUNCHER_DIR	= "atex";
	private static File					workDir				= null;

	public static File getWorkingDirectory() {
		if (workDir == null) {
			workDir = getWorkingDirectory(LAUNCHER_DIR);
		}
		return workDir;
	}

	public static File getWorkingDirectory(String applicationName) {
		boolean isPortable = MinecraftUtils.getOptions().isPortable();
		if (isPortable) { return new File("." + LAUNCHER_DIR); }
		String userHome = System.getProperty("user.home", ".");
		File workingDirectory;
		switch (getPlatform()) {
		case linux:
		case solaris:
			workingDirectory = new File(userHome, '.' + applicationName + '/');
			break;
		case windows:
			String applicationData = System.getenv("APPDATA");
			if (applicationData != null) {
				workingDirectory = new File(applicationData, "." + applicationName + '/');
			} else {
				workingDirectory = new File(userHome, '.' + applicationName + '/');
			}
			break;
		case macos:
			workingDirectory = new File(userHome, "Library/Application Support/" + applicationName);
			break;
		default:
			workingDirectory = new File(userHome, applicationName + '/');
		}
		if ((!workingDirectory.exists()) && (!workingDirectory.mkdirs())) { throw new RuntimeException("The working directory could not be created: " + workingDirectory); }
		return workingDirectory;
	}

	public static OS getPlatform() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) { return OS.windows; }
		if (osName.contains("mac")) { return OS.macos; }
		if (osName.contains("solaris")) { return OS.solaris; }
		if (osName.contains("sunos")) { return OS.solaris; }
		if (osName.contains("linux")) { return OS.linux; }
		if (osName.contains("unix")) { return OS.linux; }
		return OS.unknown;
	}

	public enum OS {

		linux, solaris, windows, macos, unknown
	}

	public static String excutePost(String targetURL, String urlParameters, JProgressBar progress) {
		HttpsURLConnection connection = null;
		try {
			URL url = new URL(targetURL);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			connection.setConnectTimeout(10000);

			connection.connect();
			Certificate[] certs = connection.getServerCertificates();

			byte[] bytes = new byte[294];
			DataInputStream dis = new DataInputStream(PlatformUtils.class.getResourceAsStream("minecraft.key"));
			dis.readFully(bytes);
			dis.close();

			Certificate c = certs[0];
			PublicKey pk = c.getPublicKey();
			byte[] data = pk.getEncoded();

			for (int j = 0; j < data.length; j++) {
				if (data[j] == bytes[j]) {
					continue;
				}
				throw new RuntimeException("Public key mismatch");
			}

			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));

			StringBuilder response = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();

			return response.toString();
		} catch (Exception e) {
			String message = "Login failed...";
			progress.setString(message);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}
}
