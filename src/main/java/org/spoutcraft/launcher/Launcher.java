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

import java.applet.Applet;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.spoutcraft.launcher.exception.CorruptedMinecraftJarException;
import org.spoutcraft.launcher.exception.MinecraftVerifyException;
import org.spoutcraft.launcher.exception.UnknownMinecraftException;

public class Launcher {

	public static Class<?>	mcClass	= null, appletClass = null;
	public static Field			mcField	= null;

	@SuppressWarnings("rawtypes")
	public static Applet getMinecraftApplet() throws CorruptedMinecraftJarException, MinecraftVerifyException {

		File mcBinFolder = GameUpdater.binDir;

		File spoutcraftJar = new File(mcBinFolder, "modpack.jar");
		File minecraftJar = new File(mcBinFolder, "minecraft.jar");
		File jinputJar = new File(mcBinFolder, "jinput.jar");
		File lwglJar = new File(mcBinFolder, "lwjgl.jar");
		File lwjgl_utilJar = new File(mcBinFolder, "lwjgl_util.jar");

		ModpackBuild build = ModpackBuild.getSpoutcraftBuild();
		Map<String, Object> libraries = build.getLibraries();

		int librarycount = 4;
		if (libraries != null) {
			librarycount += libraries.size();
		}
		File[] files = new File[librarycount];

		int index = 0;
		if (libraries != null) {
			Iterator<Entry<String, Object>> i = libraries.entrySet().iterator();
			while (i.hasNext()) {
				Entry<String, Object> lib = i.next();
				File libraryFile = new File(mcBinFolder, "lib" + File.separator + lib.getKey() + ".jar");
				files[index] = libraryFile;
				index++;
			}
		}

		URL urls[] = new URL[5];

		try {
			urls[0] = minecraftJar.toURI().toURL();
			files[index + 0] = minecraftJar;
			urls[1] = jinputJar.toURI().toURL();
			files[index + 1] = jinputJar;
			urls[2] = lwglJar.toURI().toURL();
			files[index + 2] = lwglJar;
			urls[3] = lwjgl_utilJar.toURI().toURL();
			files[index + 3] = lwjgl_utilJar;
			urls[4] = spoutcraftJar.toURI().toURL();

			ClassLoader classLoader = new MinecraftClassLoader(urls, ClassLoader.getSystemClassLoader(), spoutcraftJar, files);

			setMinecraftDirectory(classLoader, GameUpdater.modpackDir);
			int a = 1;
			String nativesPath = new File(mcBinFolder, "natives").getAbsolutePath();
			System.setProperty("org.lwjgl.librarypath", nativesPath);
			System.setProperty("net.java.games.input.librarypath", nativesPath);

			appletClass = classLoader.loadClass("net.minecraft.client.MinecraftApplet");
			mcClass = classLoader.loadClass("net.minecraft.client.Minecraft");
			mcField = appletClass.getDeclaredFields()[1];

			return (Applet) appletClass.newInstance();
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			return null;
		} catch (ClassNotFoundException ex) {
			throw new CorruptedMinecraftJarException(ex);
		} catch (IllegalAccessException ex) {
			throw new CorruptedMinecraftJarException(ex);
		} catch (InstantiationException ex) {
			throw new CorruptedMinecraftJarException(ex);
		} catch (VerifyError ex) {
			throw new MinecraftVerifyException(ex);
		} catch (Throwable t) {
			throw new UnknownMinecraftException(t);
		} 
	}

	/*
	 * This method works based on the assumption that there is only one field in
	 * Minecraft.class that is a private static File, this may change in the
	 * future and so should be tested with new minecraft versions.
	 */
	private static void setMinecraftDirectory(ClassLoader loader, File directory) throws MinecraftVerifyException {
		try {
			Class<?> clazz = loader.loadClass("net.minecraft.client.Minecraft");
			Field[] fields = clazz.getDeclaredFields();

			int fieldCount = 0;
			Field mineDirField = null;
			for (Field field : fields) {
				if (field.getType() == File.class) {
					int mods = field.getModifiers();
					if (Modifier.isStatic(mods) && Modifier.isPrivate(mods)) {
						mineDirField = field;
						fieldCount++;
					}
				}
			}
			if (fieldCount != 1) { throw new MinecraftVerifyException("Cannot find directory field in minecraft"); }

			mineDirField.setAccessible(true);
			mineDirField.set(null, directory);

		} catch (Exception e) {
			throw new MinecraftVerifyException(e, "Cannot set directory in Minecraft class");
		}

	}
}
