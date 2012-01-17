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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.spoutcraft.launcher.FileUtils;
import org.spoutcraft.launcher.GameUpdater;
import org.spoutcraft.launcher.Main;
import org.spoutcraft.launcher.MinecraftDownloadUtils;
import org.spoutcraft.launcher.MinecraftYML;
import org.spoutcraft.launcher.ModPacksYML;
import org.spoutcraft.launcher.SettingsUtil;
import org.spoutcraft.launcher.SpoutcraftYML;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.JLabel;

public class OptionDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	JRadioButton devBuilds = new JRadioButton("Always use development builds");
	
	JRadioButton recBuilds = new JRadioButton("Always use recommended builds");
	
	JRadioButton customBuilds = new JRadioButton("Manual build selection");
	
//	JCheckBox clipboardCheckbox = new JCheckBox("Allow access to your clipboard");
	
	JCheckBox backupCheckbox = new JCheckBox("Include worlds when doing automated backup");
	
	JCheckBox retryLoginCheckbox = new JCheckBox("Retry after connection timeout");
	
	JCheckBox latestLWJGLCheckbox = new JCheckBox("Use latest LWJGL binaries");
	
	JComboBox memoryCombo = new JComboBox();
	
	JComboBox packCombo = new JComboBox();
	
	JButton clearCache = new JButton("Clear Cache");
	
	JLabel buildInfo = new JLabel();
		
	JComboBox buildsCombo = new JComboBox();

	/**
	 * Create the dialog.
	 */
	@SuppressWarnings("unchecked")
	public OptionDialog() {
		setTitle("Technic Settings");
		
		ButtonGroup group = new ButtonGroup();
		group.add(devBuilds);
		group.add(recBuilds);
		group.add(customBuilds);
		
		buildInfo.setText("Technic Launcher Build " + Main.build);
		buildInfo.setOpaque(true);
		buildInfo.setForeground(Color.DARK_GRAY);
		buildInfo.setToolTipText("Created by the Spout Development Team. Licensed under the LGPL. Source code is available at www.github.com/SpoutDev" );
		
		customBuilds.setToolTipText("Only use if you know what you are doing!");
		devBuilds.setToolTipText("Development builds are often unstable and buggy. Use at your own risk!");
		recBuilds.setToolTipText("Recommended builds are (nearly) bug-free and well-tested.");
//		clipboardCheckbox.setToolTipText("Allows server mods to see the contents of your clipboard.");
		backupCheckbox.setToolTipText("Backs up your Technic SP worlds after each Technic update");
		retryLoginCheckbox.setToolTipText("Retries logging into minecraft.net up to 3 times after a failure");
		latestLWJGLCheckbox.setToolTipText("Minecraft normally uses older, more compatible versions of LWJGL, but the latest may improve performance or fix audio issues");
		clearCache.setToolTipText("Clears the cached minecraft and Technic files, forcing a redownload on your next login");
		memoryCombo.setToolTipText("Allows you to adjust the memory assigned to Technic. Assigning more memory than you have may cause crashes.");
		packCombo.setToolTipText("Select which mod pack to use with the launcher.");
		
		if (SettingsUtil.isRecommendedBuild()) {
			devBuilds.setSelected(false);
			recBuilds.setSelected(true);
			customBuilds.setSelected(false);
			SettingsUtil.setDevelopmentBuild(false);
		}
		else if (SettingsUtil.isDevelopmentBuild()) {
			devBuilds.setSelected(true);
			recBuilds.setSelected(false);
			customBuilds.setSelected(false);
		}
		else {
			devBuilds.setSelected(false);
			recBuilds.setSelected(false);
			customBuilds.setSelected(true);
		}
		customBuilds.addActionListener(this);
		recBuilds.addActionListener(this);
		devBuilds.addActionListener(this);
		buildsCombo.addActionListener(this);
		packCombo.addActionListener(this);
		
//		clipboardCheckbox.setSelected(SettingsUtil.isClipboardAccess());
		backupCheckbox.setSelected(SettingsUtil.isWorldBackup());
		retryLoginCheckbox.setSelected(SettingsUtil.getLoginTries() > 1);
		latestLWJGLCheckbox.setSelected(SettingsUtil.isLatestLWJGL());
		
		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		memoryCombo.addItem("512 MB");
		memoryCombo.addItem("1 GB");
		memoryCombo.addItem("2 GB");
		memoryCombo.addItem("4 GB");
		memoryCombo.addItem("8 GB");
		memoryCombo.addItem("16 GB");
		
		memoryCombo.setSelectedIndex(SettingsUtil.getMemorySelection());
		
		JLabel lblMemoryToAllocate = new JLabel("Memory to allocate: ");
		JLabel lblPack = new JLabel("Select Mod Pack: ");
		
		JLabel selectBuild = new JLabel("Select Technic build: ");
		
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPanel.createSequentialGroup()
								.addComponent(selectBuild)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(buildsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(devBuilds)
						.addComponent(recBuilds)
						.addComponent(customBuilds)
						.addGroup(gl_contentPanel.createSequentialGroup()
								.addComponent(lblPack)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(packCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//						.addComponent(clipboardCheckbox)
						.addComponent(backupCheckbox)
						.addComponent(retryLoginCheckbox)
						.addComponent(latestLWJGLCheckbox)
						.addComponent(clearCache)
						.addComponent(buildInfo)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addComponent(lblMemoryToAllocate)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(memoryCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(27, Short.MAX_VALUE))
		);
		
		Font font = new Font("Arial", Font.PLAIN, 11);
		backupCheckbox.setFont(font);
//		clipboardCheckbox.setFont(font);
		devBuilds.setFont(font);
		recBuilds.setFont(font);
		retryLoginCheckbox.setFont(font);
		clearCache.setFont(font);
		clearCache.setActionCommand("Clear Cache");
		clearCache.addActionListener(this);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(selectBuild)
							.addComponent(buildsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addComponent(devBuilds)
					.addComponent(recBuilds)
					.addComponent(customBuilds)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(packCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblPack))
					.addComponent(retryLoginCheckbox)
					.addPreferredGap(ComponentPlacement.RELATED)
//					.addComponent(clipboardCheckbox)
					.addComponent(backupCheckbox)
					.addComponent(latestLWJGLCheckbox)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(memoryCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblMemoryToAllocate))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(clearCache)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(buildInfo)
					.addContainerGap(316, Short.MAX_VALUE))
		);
		
		//TODO remove once implemented 
		packCombo.setEnabled(false);
		
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
	}
	
	public void updateBuildsList() {
		if (buildsCombo.getItemCount() == 0) {
			String[] buildList = MinecraftDownloadUtils.getSpoutcraftBuilds();
			if (buildList != null) {
				for (String item : buildList) {
					buildsCombo.addItem(item);
				}
			}
			else {
				buildsCombo.addItem("No builds found");
			}
			updateBuildsCombo();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void updateModPackList()
	{
		if(packCombo.getItemCount() == 0)
		{
//			List<Map<String, String>> modpackList = ModPacksYML.getModPacks();
			if (ModPacksYML.getModPacks() != null)
			{
				for(int i=0; i < ModPacksYML.getModPacks().size(); i++)
				{
//					Map<String, String> map = (Map<String, String>) modpackList.get(i);
//					packCombo.addItem(map.get("name").toString());
					if(ModPacksYML.getModPacks().get(i).get("name") != null)
						packCombo.addItem(ModPacksYML.getModPacks().get(i).get("name").toString());
				}
			}
			else
			{
				if(ModPacksYML.getModPacks().get(0).get("name") != null)
					packCombo.addItem(ModPacksYML.getModPacks().get(0).get("name").toString());
				else
					packCombo.addItem("Technic");
			}
			updateModPacksCombo();
		}
	}
	
	public void updateModPacksCombo()
	{
		if(ModPacksYML.getModPacks().size() > 1)
			packCombo.setEnabled(true);
		
//		if(!SettingsUtil.isModPack())
//		{
//			packCombo.setSelectedIndex(0);
//			SettingsUtil.setModPackSelection(0);
//		}
//		else
		if(SettingsUtil.isModPack())
		{
			int id = SettingsUtil.getModPackSelection();
			packCombo.setSelectedIndex(id);
		}
		else
		{
			packCombo.setSelectedIndex(0);
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}

	public void actionPerformed(ActionEvent evt) {
		String id = evt.getActionCommand(); 
		if (id.equals("OK")) {
			SettingsUtil.setDevelopmentBuild(devBuilds.isSelected());
			SettingsUtil.setRecommendedBuild(recBuilds.isSelected());
//			SettingsUtil.setClipboardAccess(clipboardCheckbox.isSelected());
			SettingsUtil.setWorldBackup(backupCheckbox.isSelected());
			SettingsUtil.setLoginTries(retryLoginCheckbox.isSelected());
//			SettingsUtil.setModPackSelection(packCombo.getSelectedIndex());
			if (SettingsUtil.getMemorySelection() > 5) {
				SettingsUtil.setMemorySelection(0);
			}
//			if ((memoryCombo.getSelectedIndex() != SettingsUtil.getMemorySelection()) && (packCombo.getSelectedIndex() != SettingsUtil.getModPackSelection()))
//			{
//				SettingsUtil.setMemorySelection(memoryCombo.getSelectedIndex());
//				SettingsUtil.setModPackSelection(packCombo.getSelectedIndex());
//				int mem = 1 << 9 + memoryCombo.getSelectedIndex();
//				Main.reboot("-Xmx" + mem + "m", "-modpack " + packCombo.getSelectedIndex());
////				packCombo.setSelectedIndex(SettingsUtil.getModPackSelection());
////				SettingsUtil.setModPackSelection(packCombo.getSelectedIndex());
//			}
			if (memoryCombo.getSelectedIndex() != SettingsUtil.getMemorySelection()) {
				SettingsUtil.setMemorySelection(memoryCombo.getSelectedIndex());
				int mem = 1 << 9 + memoryCombo.getSelectedIndex();
				Main.reboot("-Xmx" + mem + "m");
			}
			if (latestLWJGLCheckbox.isSelected() != SettingsUtil.isLatestLWJGL()) {
				SettingsUtil.setLatestLWJGL(latestLWJGLCheckbox.isSelected());
				clearCache();
			}
			
			if (packCombo.getSelectedIndex() != SettingsUtil.getModPackSelection())
			{
				int mem = 1 << 9 + memoryCombo.getSelectedIndex();
				SettingsUtil.setModPackSelection(packCombo.getSelectedIndex());
//				packCombo.setSelectedIndex(packCombo.getSelectedIndex());
//				Main.reboot("-Xmx" + mem + "m", "-modpack " + packCombo.getSelectedIndex());
				Main.reboot("-Xmx" + mem + "m");
			}
			
			
			if (buildsCombo.isEnabled()) {
				String build = null;
				try {
					String item = ((String)buildsCombo.getSelectedItem());
					if (item.contains("|")) {
						item = item.split("\\|")[0];
					}
					build = item.trim();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				if (build != null) {
					SettingsUtil.setSelectedBuild(build);
				}
			}
			
			this.setVisible(false);
			this.dispose();
		} else if (id.equals("Cancel")) {
			this.setVisible(false);
			this.dispose();
		}
		else if (id.equals("Clear Cache")) {
			if (clearCache()) {
				JOptionPane.showMessageDialog(getParent(), "Successfully cleared the cache.");
			}
			else {
				JOptionPane.showMessageDialog(getParent(), "Failed to clear the cache! Ensure Technic files are open.\nIf all else fails, close the launcher, restart it, and try again.");
			}
		}
		else if (id.equals(customBuilds.getText()) || id.equals(devBuilds.getText()) || id.equals(recBuilds.getText())) {
			updateBuildsCombo();
		}
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
		}
		else if (devBuilds.isSelected()) {
			buildsCombo.setSelectedIndex(0);
		}
		else if (recBuilds.isSelected()) {
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
			FileUtils.deleteDirectory(GameUpdater.updateDir);
			FileUtils.deleteDirectory(GameUpdater.cacheDir);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		finally {
			SpoutcraftYML.getSpoutcraftYML().setProperty("current", null);
			MinecraftYML.setInstalledVersion("");
		}
	}
}
