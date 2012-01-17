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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.UIManager;

import org.spoutcraft.launcher.gui.LoadingScreen;

import org.spoutcraft.launcher.gui.LoginForm;
import org.spoutcraft.launcher.logs.SystemConsoleListener;

import com.beust.jcommander.JCommander;

public class Main {
	
	static String[] args_temp;
	public static String build = "0.5.0";
	public static String currentPack;
	static File recursion;
	
	
	public Main() throws Exception {
		main(new String[0]);
	}

	public static void reboot(String memory) {
		try {
			String modpackFilename = ModPacksYML.getModPacks().get(SettingsUtil.getModPackSelection()).get("filenames");
			int mem = 1 << 9 + SettingsUtil.getMemorySelection();
			String pathToJar = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			ArrayList<String> params = new ArrayList<String>();
			if (PlatformUtils.getPlatform() == PlatformUtils.OS.windows) {
				params.add("javaw"); // Windows-specific
			} else {
				params.add("java"); // Linux/Mac/whatever
			}
			if(memory == ("-Xmx" + mem + "m"))
			{
				params.add(memory);
			}
			else
			{
				params.add("-Xmx" + mem + "m");
				params.add(memory);
			}
			params.add("-classpath");
			params.add(pathToJar);
			params.add("org.spoutcraft.launcher.Main");
			for (String arg : args_temp) {
				params.add(arg);
			}
			
			
			if (PlatformUtils.getPlatform() == PlatformUtils.OS.macos) {
				params.add("-Xdock:name=\"Technic\"");
				
				try {
					if(modpackFilename != null)
					{
						File icon = new File(PlatformUtils.getWorkingDirectory(), modpackFilename.toString() + "_icon.icns");
						GameUpdater.copy(Main.class.getResourceAsStream("/org/spoutcraft/launcher/" + modpackFilename.toString() + "_icon.icns"), new FileOutputStream(icon));
						params.add("-Xdock:icon=" + icon.getCanonicalPath());
					}
					else
					{
						File icon = new File(PlatformUtils.getWorkingDirectory(), "technic_icon.icns");
						GameUpdater.copy(Main.class.getResourceAsStream("/org/spoutcraft/launcher/technic_icon.icns"), new FileOutputStream(icon));
						params.add("-Xdock:icon=" + icon.getCanonicalPath());
					}
				}
				catch (Exception ignore) { }
			}
			ProcessBuilder pb = new ProcessBuilder(params);
			Process process = pb.start();
			if(process == null)
				throw new Exception("!");
			System.exit(0);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void reboot(String memory, String modpack)
	{
		reboot(memory);
//		SettingsUtil.setModPackSelection(Integer.parseInt(modpack));
	}
	
//	public static void reboot(String memory, String modpack)
//	{
//		try {
//			String pathToJar = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
//			ArrayList<String> params = new ArrayList<String>();
//			SettingsUtil.setModPackSelection(Integer.parseInt(modpack));
//			if (PlatformUtils.getPlatform() == PlatformUtils.OS.windows) {
//				params.add("javaw"); // Windows-specific
//			} else {
//				params.add("java"); // Linux/Mac/whatever
//			}
//			params.add(memory);
////			params.add(modpack);
//			params.add("-classpath");
//			params.add(pathToJar);
//			params.add("org.spoutcraft.launcher.Main");
//			for (String arg : args_temp) {
//				params.add(arg);
//			}
//			if (PlatformUtils.getPlatform() == PlatformUtils.OS.macos) {
//				params.add("-Xdock:name=\"" + ModPacksYML.getModPacks().get(SettingsUtil.getModPackSelection()).get("name").toString() + "\"");
//				
//				try {
//					String modpackFilename = ModPacksYML.getModPacks().get(SettingsUtil.getModPackSelection()).get("filename");
//					File icon = new File(PlatformUtils.getWorkingDirectory(), modpackFilename.toString() + "_icon.icns");
//					GameUpdater.copy(Main.class.getResourceAsStream("/org/spoutcraft/launcher/" + modpackFilename.toString() + "icon.icns"), new FileOutputStream(icon));
//					params.add("-Xdock:icon=" + icon.getCanonicalPath());
//				}
//				catch (Exception ignore) { }
//			}
//			ProcessBuilder pb = new ProcessBuilder(params);
//			Process process = pb.start();
//			if(process == null)
//				throw new Exception("!");
//			System.exit(0);
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	
	public static void main(String[] args) throws Exception {
		LoadingScreen ls = new LoadingScreen();
		ls.setVisible(true);
		System.out.println("Loading at " + new Date(System.currentTimeMillis()).toString());
		//int i = 1;
		//System.out.println(i++ + " - " + new Date(System.currentTimeMillis()).toString());
		Options options = new Options();
		try {
			new JCommander(options, args);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		//System.out.println(i++ + " - " + new Date(System.currentTimeMillis()).toString());
		MinecraftUtils.setOptions(options);
		recursion = new File(PlatformUtils.getWorkingDirectory(), "rtemp");

		//System.out.println(i++ + " - " + new Date(System.currentTimeMillis()).toString());
		args_temp = args;
		boolean relaunch = false;
		try {
			if (!recursion.exists()) {
				relaunch = true;
			} else {
				recursion.delete();
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}

		//System.out.println(i++ + " - " + new Date(System.currentTimeMillis()).toString());
		if (relaunch) {
			ls.close();
			if (SettingsUtil.getMemorySelection() < 6) {
				int mem = 1 << (9 + SettingsUtil.getMemorySelection());
				recursion.createNewFile();
				reboot("-Xmx" + mem + "m");
			}
		}
		//System.out.println(i++ + " - " + new Date(System.currentTimeMillis()).toString());
		if (PlatformUtils.getPlatform() == PlatformUtils.OS.macos) {
			try{
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Technic");
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ignore) { }
		}
		//System.out.println(i++ + " - " + new Date(System.currentTimeMillis()).toString());
		PlatformUtils.getWorkingDirectory().mkdirs();
		new File(PlatformUtils.getWorkingDirectory(), "launcher").mkdir();

		//System.out.println(i++ + " - " + new Date(System.currentTimeMillis()).toString());
		SystemConsoleListener listener = new SystemConsoleListener();

		listener.initialize();
		
		System.out.println("------------------------------------------");
		System.out.println("Launcher is starting....");
		System.out.println("Launcher Build: " + getBuild());


		//System.out.println(i++ + " - " + new Date(System.currentTimeMillis()).toString());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Warning: Can't get system LnF: " + e);
		}

		//System.out.println(i++ + " - " + new Date(System.currentTimeMillis()).toString());
		LoginForm login = new LoginForm();

		//System.out.println(i++ + " - " + new Date(System.currentTimeMillis()).toString());
		System.out.println("Showing GUI at " + new Date(System.currentTimeMillis()).toString());
		ls.close();
		login.setVisible(true);
		String modpackFilename = ModPacksYML.getModPacks().get(SettingsUtil.getModPackSelection()).get("filenames");
		if(modpackFilename != null)
		{
			new File(PlatformUtils.getWorkingDirectory(), modpackFilename.toString()).mkdir();
		}
	}

	private static String getBuild() {
		if (build == null) {
			File buildInfo = new File(PlatformUtils.getWorkingDirectory(), "launcherVersion");
			if (buildInfo.exists()) {
				try {
					BufferedReader bf = new BufferedReader(new FileReader(buildInfo));
					build = bf.readLine();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return build;
	}

}
