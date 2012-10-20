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
package net.minecraft;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.spoutcraft.launcher.FileUtils;
import org.spoutcraft.launcher.GameUpdater;
import org.spoutcraft.launcher.LauncherController;


/**
 * 
 * @author creadri
 */
public class Launcher extends Applet implements AppletStub {

	/**
   *
   */
	private static final long					serialVersionUID	= -4815977474500388254L;
	private Applet										minecraftApplet;
	private URL												minecraftDocumentBase;
	private final Map<String, String>	customParameters;
	private boolean										active						= false;

	public Launcher() throws HeadlessException {
		this.customParameters = new HashMap<String, String>();
		this.setLayout(new GridBagLayout());
	}

	public Launcher(Applet minecraftApplet) throws HeadlessException {
		this();
		this.minecraftApplet = minecraftApplet;
		java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
		this.add(minecraftApplet, gridBagConstraints);
	}

	public Applet getMinecraftApplet() {
		return minecraftApplet;
	}

	public void setMinecraftApplet(Applet minecraftApplet) {
		if (this.minecraftApplet != null) {
			remove(minecraftApplet);
		}
		java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);

		this.add(minecraftApplet, gridBagConstraints);
		this.minecraftApplet = minecraftApplet;
	}

	public void addParameter(String name, String value) {
		customParameters.put(name, value);
	}

	@Override
	public String getParameter(String name) {
		String custom = this.customParameters.get(name);
		if (custom != null) { return custom; }
		try {
			return super.getParameter(name);
		} catch (Exception e) {
			this.customParameters.put(name, null);
		}
		return null;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void appletResize(int width, int height) {
		minecraftApplet.resize(width, height);
	}

	@Override
	public void init() {
		if (minecraftApplet != null) {
			minecraftApplet.init();

		}
	}

	@Override
	public void start() {
		if (minecraftApplet != null) {
			try {
				LauncherController.mcField.setAccessible(true);
				Object mcInstance = LauncherController.mcField.get(minecraftApplet);
				Field quitField = LauncherController.mcClass.getDeclaredField("n");
				Object quitInstance = quitField.get(mcInstance);
				quitField.setBoolean(mcInstance, Boolean.FALSE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			minecraftApplet.start();
			FileUtils.cleanDirectory(GameUpdater.tempDir);
			active = true;
		}
	}

	@Override
	public void stop() {
		if (minecraftApplet != null) {
			minecraftApplet.stop();
			active = false;
		}
	}
	
	public void replace(Applet applet)
	{
		this.minecraftApplet = applet;

		applet.setStub(this);
		applet.setSize(getWidth(), getHeight());

		this.setLayout(new BorderLayout());
		this.add(applet, "Center");
		applet.init();
		active = true;
		applet.start();
		validate();
	}

	@Override
	public URL getCodeBase() {
		return minecraftApplet.getCodeBase();
	}

	@Override
	public URL getDocumentBase() {
		if (minecraftDocumentBase == null) {
			try {
				minecraftDocumentBase = new URL("http://www.minecraft.net/game");
			} catch (MalformedURLException ignored) {
			}
		}
		return minecraftDocumentBase;
	}

	@Override
	public void resize(int width, int height) {
		minecraftApplet.resize(width, height);
	}

	@Override
	public void resize(Dimension d) {
		minecraftApplet.resize(d);
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		minecraftApplet.setVisible(b);
	}
}