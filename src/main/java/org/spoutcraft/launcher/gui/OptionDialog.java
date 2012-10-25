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
package org.spoutcraft.launcher.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import org.spoutcraft.launcher.FileUtils;
import org.spoutcraft.launcher.GameUpdater;
import org.spoutcraft.launcher.Main;
import org.spoutcraft.launcher.MinecraftYML;
import org.spoutcraft.launcher.SettingsUtil;
import org.spoutcraft.launcher.modpacks.InstalledModsYML;
import org.spoutcraft.launcher.modpacks.ModPackYML;

public class OptionDialog extends JDialog implements ActionListener {

	private static final long		serialVersionUID		= 1L;
	private final JPanel				contentPanel				= new JPanel();
	public Map<String, String>	modPackList					= null;
	JRadioButton								devBuilds						= new JRadioButton("Always use High Resolution builds");
	JRadioButton								recBuilds						= new JRadioButton("Always use Low Graphics builds");
	JRadioButton								customBuilds				= new JRadioButton("Manual build selection");
	// JCheckBox clipboardCheckbox = new
	// JCheckBox("Allow access to your clipboard");
	JCheckBox										backupCheckbox			= new JCheckBox("Include worlds when doing automated backup");
	JCheckBox										retryLoginCheckbox	= new JCheckBox("Retry after connection timeout");
	JCheckBox										latestLWJGLCheckbox	= new JCheckBox("Use latest LWJGL binaries");
	JComboBox										memoryCombo					= new JComboBox();
	JButton											clearCache					= new JButton("Clear Cache");
	JLabel											buildInfo						= new JLabel();
	JComboBox										buildsCombo					= new JComboBox();
	int[]												memValues						= new int[] { 1, 2, 3, 4, 6, 8, 10 };

	/**
	 * Create the dialog.
	 */
	@SuppressWarnings("unchecked")
	public OptionDialog() {
		setTitle("Technic Launcher Settings");

		ButtonGroup group = new ButtonGroup();
		group.add(devBuilds);
		group.add(recBuilds);
		group.add(customBuilds);

		buildInfo.setText("Technic Launcher Build " + Main.build);
		buildInfo.setOpaque(true);
		buildInfo.setForeground(Color.DARK_GRAY);
		buildInfo.setToolTipText("Created by the Spout Development Team and Modified by the Technic Team. Licensed under the LGPL. Source code is available at www.github.com/SpoutDev");

		customBuilds.setToolTipText("Only use if you know what you are doing!");
		devBuilds.setToolTipText("Development builds are often unstable and buggy. Use at your own risk!");
		recBuilds.setToolTipText("Recommended builds are (nearly) bug-free and well-tested.");
		// clipboardCheckbox.setToolTipText("Allows server mods to see the contents of your clipboard.");
		backupCheckbox.setToolTipText("Backs up your Single Player worlds after each Modpack update");
		retryLoginCheckbox.setToolTipText("Retries logging into minecraft.net up to 3 times after a failure");
		latestLWJGLCheckbox.setToolTipText("Minecraft normally uses older, more compatible versions of LWJGL, but the latest may improve performance or fix audio issues");
		clearCache.setToolTipText("Clears the cached minecraft and Modpack files, forcing a redownload on your next login");
		memoryCombo.setToolTipText("Allows you to adjust the memory assigned to Minecraft. Assigning more memory than you have may cause crashes.");

		customBuilds.addActionListener(this);
		recBuilds.addActionListener(this);
		devBuilds.addActionListener(this);
		buildsCombo.addActionListener(this);

		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		memoryCombo.addItem("512 MB");
		memoryCombo.addItem("1 GB");
		memoryCombo.addItem("1536 MB");

		String vmType = System.getProperty("sun.arch.data.model");
		if (vmType != null && vmType.equals("64")) {
			memoryCombo.addItem("2 GB");
			memoryCombo.addItem("4 GB");
			memoryCombo.addItem("8 GB");
			memoryCombo.addItem("16 GB");
		}

		JLabel lblMemoryToAllocate = new JLabel("Memory to allocate: ");
		JLabel lblPack = new JLabel("Select Mod Pack: ");

		JLabel selectBuild = new JLabel("Select Modpack build: ");

		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPanel.createSequentialGroup().addContainerGap().addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPanel.createSequentialGroup().addComponent(selectBuild).addPreferredGap(ComponentPlacement.RELATED).addComponent(buildsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addComponent(devBuilds).addComponent(recBuilds).addComponent(customBuilds).addGroup(gl_contentPanel.createSequentialGroup()) // .addComponent(clipboardCheckbox)
		.addComponent(backupCheckbox).addComponent(retryLoginCheckbox).addComponent(latestLWJGLCheckbox).addComponent(clearCache).addComponent(buildInfo).addGroup(gl_contentPanel.createSequentialGroup().addComponent(lblMemoryToAllocate).addPreferredGap(ComponentPlacement.RELATED).addComponent(memoryCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))).addContainerGap(27, Short.MAX_VALUE)));

		Font font = new Font("Arial", Font.PLAIN, 11);
		backupCheckbox.setFont(font);
		// clipboardCheckbox.setFont(font);
		devBuilds.setFont(font);
		recBuilds.setFont(font);
		retryLoginCheckbox.setFont(font);
		clearCache.setFont(font);
		clearCache.setActionCommand("Clear Cache");
		clearCache.addActionListener(this);
		gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPanel.createSequentialGroup().addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE).addComponent(selectBuild).addComponent(buildsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addComponent(devBuilds).addComponent(recBuilds).addComponent(customBuilds).addComponent(retryLoginCheckbox).addPreferredGap(ComponentPlacement.RELATED) // .addComponent(clipboardCheckbox)
		.addComponent(backupCheckbox).addComponent(latestLWJGLCheckbox).addPreferredGap(ComponentPlacement.RELATED).addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE).addComponent(memoryCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblMemoryToAllocate)).addPreferredGap(ComponentPlacement.RELATED).addComponent(clearCache).addPreferredGap(ComponentPlacement.RELATED).addComponent(buildInfo).addContainerGap(316, Short.MAX_VALUE)));

		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setFont(font);
				okButton.setActionCommand("OK");
				okButton.addActionListener(this);
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setFont(font);
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(this);
				buttonPane.add(cancelButton);
			}
		}
		latestLWJGLCheckbox.setEnabled(false);
	}

	public void reloadSettings() {
		if (SettingsUtil.isRecommendedBuild()) {
			devBuilds.setSelected(false);
			recBuilds.setSelected(true);
			customBuilds.setSelected(false);
			SettingsUtil.setDevelopmentBuild(false);
		} else if (SettingsUtil.isDevelopmentBuild()) {
			devBuilds.setSelected(true);
			recBuilds.setSelected(false);
			customBuilds.setSelected(false);
		} else {
			devBuilds.setSelected(false);
			recBuilds.setSelected(false);
			customBuilds.setSelected(true);
		}

		updateBuildsList();

		// clipboardCheckbox.setSelected(SettingsUtil.isClipboardAccess());
		backupCheckbox.setSelected(SettingsUtil.isWorldBackup());
		retryLoginCheckbox.setSelected(SettingsUtil.getLoginTries() > 1);
		latestLWJGLCheckbox.setSelected(SettingsUtil.isLatestLWJGL());
		latestLWJGLCheckbox.setSelected(false);

		int memIndex = Arrays.binarySearch(memValues, SettingsUtil.getMemorySelection() / 512);
		if (memIndex < 0 || memIndex > memoryCombo.getItemCount()) {
			memIndex = 1;
		}
		memoryCombo.setSelectedIndex(memIndex);
	}

	public void updateBuildsList() {
		buildsCombo.removeAllItems();
		String[] buildList = ModPackYML.getModpackBuilds();
		if (buildList != null) {
			for (String item : buildList) {
				buildsCombo.addItem(item);
			}
		} else {
			buildsCombo.addItem("No builds found");
		}
		updateBuildsCombo();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		String id = evt.getActionCommand();
		if (id.equals("OK")) {
			if ((devBuilds.isSelected() && !SettingsUtil.isDevelopmentBuild()) || (recBuilds.isSelected() && !SettingsUtil.isRecommendedBuild())) {
				cleanMods();
			}

			SettingsUtil.setDevelopmentBuild(devBuilds.isSelected());
			SettingsUtil.setRecommendedBuild(recBuilds.isSelected());
			// SettingsUtil.setClipboardAccess(clipboardCheckbox.isSelected());
			SettingsUtil.setWorldBackup(backupCheckbox.isSelected());
			SettingsUtil.setLoginTries(retryLoginCheckbox.isSelected());

			if (SettingsUtil.getMemorySelection() > (memValues[memValues.length - 1] * 512)) {
				SettingsUtil.setMemorySelection(1024);
			}

			if (latestLWJGLCheckbox.isSelected() != SettingsUtil.isLatestLWJGL()) {
				SettingsUtil.setLatestLWJGL(latestLWJGLCheckbox.isSelected());
				clearCache();
			}

			if (buildsCombo.isEnabled()) {
				String build = getSelectedBuildFromCombo();
				if (build != null) {
					if (!build.equalsIgnoreCase(SettingsUtil.getSelectedBuild())) {
						cleanMods();
					}
					SettingsUtil.setSelectedBuild(build);
				}
			}

			File propFile = new File(GameUpdater.modpackDir, "launcher.properties");
			GameUpdater.copy(SettingsUtil.settingsFile, propFile);

			if (SettingsUtil.getMemorySelection() < 128) {
				SettingsUtil.setMemorySelection(1024);
			}

			int selectedIndex = memoryCombo.getSelectedIndex();
			int selectedMemory = memValues[selectedIndex] * 512;
			if (selectedMemory != SettingsUtil.getMemorySelection()) {
				SettingsUtil.setMemorySelection(selectedMemory);
				GameUpdater.copy(SettingsUtil.settingsFile, propFile);
				// int mem = 1 << 9 + memoryCombo.getSelectedIndex();
				Main.reboot("-Xmx" + selectedMemory + "m");
			}

			this.setVisible(false);
			this.dispose();
		} else if (id.equals("Cancel")) {
			this.setVisible(false);
			this.dispose();
		} else if (id.equals("Clear Cache")) {
			if (clearCache()) {
				JOptionPane.showMessageDialog(getParent(), "Successfully cleared the cache.");
			} else {
				JOptionPane.showMessageDialog(getParent(), "Failed to clear the cache! Ensure Modpack files are open.\nIf all else fails, close the launcher, restart it, and try again.");
			}
		} else if (id.equals(customBuilds.getText()) || id.equals(devBuilds.getText()) || id.equals(recBuilds.getText())) {
			updateBuildsCombo();
		}
	}

	private String getSelectedBuildFromCombo() {
		String build = null;
		try {
			String item = ((String) buildsCombo.getSelectedItem());
			if (item.contains("|")) {
				item = item.split("\\|")[0];
			}
			build = item.trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return build;
	}

	private void cleanMods() {
		File modsConfig = InstalledModsYML.getInstalledModsYmlFile();
		if (modsConfig.exists()) modsConfig.delete();
		FileUtils.cleanDirectory(GameUpdater.modsDir);
	}

	public void updateBuildsCombo() {
		buildsCombo.setEnabled(customBuilds.isSelected());

		if (customBuilds.isSelected()) {
			if (SettingsUtil.getSelectedBuild() != null) {
				String build = SettingsUtil.getSelectedBuild();
				for (int i = 0; i < buildsCombo.getItemCount(); i++) {
					String item = (String) buildsCombo.getItemAt(i);
					if (item.contains(String.valueOf(build))) {
						buildsCombo.setSelectedIndex(i);
						break;
					}
				}
			}
		} else if (devBuilds.isSelected()) {
			buildsCombo.setSelectedIndex(0);
		} else if (recBuilds.isSelected()) {
			for (int i = 0; i < buildsCombo.getItemCount(); i++) {
				String item = (String) buildsCombo.getItemAt(i);
				if (item.contains("Rec. Build")) {
					buildsCombo.setSelectedIndex(i);
					break;
				}
			}
		}
	}

	public static boolean clearCache() {
		try {
			FileUtils.deleteDirectory(GameUpdater.binDir);
			FileUtils.deleteDirectory(GameUpdater.tempDir);
			FileUtils.deleteDirectory(GameUpdater.cacheDir);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			ModPackYML.getModPackYML().setProperty("current", null);
			MinecraftYML.setInstalledVersion("");
		}
	}
}
