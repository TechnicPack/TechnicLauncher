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
package org.spoutcraft.launcher.technic.skin;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.spoutcraft.launcher.Memory;
import org.spoutcraft.launcher.Settings;
import org.spoutcraft.launcher.entrypoint.SpoutcraftLauncher;
import org.spoutcraft.launcher.exceptions.RestfulAPIException;
import org.spoutcraft.launcher.skin.MetroLoginFrame;
import org.spoutcraft.launcher.skin.components.LiteButton;
import org.spoutcraft.launcher.skin.components.LiteTextBox;
import org.spoutcraft.launcher.technic.rest.RestAPI;
import org.spoutcraft.launcher.util.Compatibility;
import org.spoutcraft.launcher.util.Utils;

public class LauncherOptions extends JDialog implements ActionListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;

	private static final int FRAME_WIDTH = 300;
	private static final int FRAME_HEIGHT = 300;
	private static final String LAUNCHER_PREPEND = "Launcher Build:    ";
	private static final String QUIT_ACTION = "quit";
	private static final String SAVE_ACTION = "save";
	private static final String LOGS_ACTION = "logs";
	private static final String CONSOLE_ACTION = "console";
	private static final String CHANGEFOLDER_ACTION = "changefolder";
	private static final String BETA_ACTION = "beta";
	private static final String STABLE_ACTION = "stable";
	private static final String CLOSEDIALOG_KEY = "ESCAPE";

	private JLabel background;
	private JLabel build;
	private LiteButton logs;
	private JComboBox memory;
	private JCheckBox permgen;
	private JRadioButton beta;
	private JRadioButton stable;
	private JFileChooser fileChooser;
	private int mouseX = 0, mouseY = 0;
	private String installedDirectory;
	private LiteTextBox packLocation;
	private boolean directoryChanged = false;
	private String buildStream = "stable";

	public LauncherOptions() {
		setTitle("Launcher Options");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		addMouseListener(this);
		addMouseMotionListener(this);
		setResizable(false);
		setUndecorated(true);
		initComponents();
	}

	private void initComponents() {
		Font minecraft = MetroLoginFrame.getMinecraftFont(12);
		
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction()
		{ public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, CLOSEDIALOG_KEY);
		getRootPane().getActionMap().put(CLOSEDIALOG_KEY, escapeAction);

		background = new JLabel();
		background.setBounds(0,0, FRAME_WIDTH, FRAME_HEIGHT);
		MetroLoginFrame.setIcon(background, "optionsBackground.png", background.getWidth(), background.getHeight());

		ImageButton optionsQuit = new ImageButton(MetroLoginFrame.getIcon("quit.png", 28, 28), MetroLoginFrame.getIcon("quit.png", 28, 28));
		optionsQuit.setRolloverIcon(MetroLoginFrame.getIcon("quitHover.png", 28, 28));
		optionsQuit.setBounds(FRAME_WIDTH - 38, 10, 28, 28);
		optionsQuit.setActionCommand(QUIT_ACTION);
		optionsQuit.addActionListener(this);

		JLabel title = new JLabel("Launcher Options");
		title.setFont(minecraft.deriveFont(14F));
		title.setBounds(50, 10, 200, 20);
		title.setForeground(Color.WHITE);
		title.setHorizontalAlignment(SwingConstants.CENTER);

		build = new JLabel(LAUNCHER_PREPEND + Settings.getLauncherBuild());
		build.setBounds(15, title.getY() + title.getHeight() + 10, FRAME_WIDTH - 20, 20);
		build.setFont(minecraft);
		build.setForeground(Color.WHITE);
		
		ButtonGroup group = new ButtonGroup();
		
		stable = new JRadioButton("Always use Stable Launcher Builds");
		stable.setBounds(10, build.getY() + build.getHeight() + 10, FRAME_WIDTH - 20, 20);
		stable.setFont(minecraft);
		stable.setForeground(Color.WHITE);
		stable.setContentAreaFilled(false);
		stable.setFocusPainted(false);
		stable.setBorderPainted(false);
		stable.setSelected(true);
		stable.setActionCommand(STABLE_ACTION);
		stable.addActionListener(this);
		group.add(stable);

		beta = new JRadioButton("Always use Beta Launcher Builds");
		beta.setBounds(10, stable.getY() + stable.getHeight() + 10, FRAME_WIDTH - 20, 20);
		beta.setFont(minecraft);
		beta.setForeground(Color.WHITE);
		beta.setContentAreaFilled(false);
		beta.setFocusPainted(false);
		beta.setBorderPainted(false);
		beta.setActionCommand(BETA_ACTION);
		beta.addActionListener(this);
		group.add(beta);
		
		buildStream = Settings.getBuildStream();
		if (buildStream.equals("stable")) {
			stable.setSelected(true);
		} else if (buildStream.equals("beta")) {
			beta.setSelected(true);
		}

		JLabel memoryLabel = new JLabel("Memory: ");
		memoryLabel.setFont(minecraft);
		memoryLabel.setBounds(10, beta.getY() + beta.getHeight() + 10, 65, 20);
		memoryLabel.setForeground(Color.WHITE);
		memoryLabel.setHorizontalAlignment(SwingConstants.CENTER);

		memory = new JComboBox();
		memory.setBounds(memoryLabel.getX() + memoryLabel.getWidth() + 10, memoryLabel.getY(), 100, 20);
		populateMemory(memory);

		permgen = new JCheckBox("Increase PermGen Size");
		permgen.setFont(minecraft);
		permgen.setBounds(10, memoryLabel.getY() + memoryLabel.getHeight() + 10, FRAME_WIDTH - 20, 25);
		permgen.setSelected(Settings.getPermGen());
		permgen.setBorderPainted(false);
		permgen.setFocusPainted(false);
		permgen.setContentAreaFilled(false);
		permgen.setForeground(Color.WHITE);
		permgen.setIconTextGap(15);

		installedDirectory = Settings.getLauncherDir();

		packLocation = new LiteTextBox(this, "");
		packLocation.setBounds(10, permgen.getY() + permgen.getHeight() + 10, FRAME_WIDTH - 20, 25);
		packLocation.setFont(minecraft.deriveFont(10F));
		packLocation.setText(installedDirectory);
		packLocation.setEnabled(false);

		LiteButton changeFolder = new LiteButton("Change Folder");
		changeFolder.setBounds(FRAME_WIDTH / 2 + 5, packLocation.getY() + packLocation.getHeight() + 10, FRAME_WIDTH / 2 - 15, 25);
		changeFolder.setFont(minecraft);
		changeFolder.setActionCommand(CHANGEFOLDER_ACTION);
		changeFolder.addActionListener(this);
		changeFolder.setEnabled(!Utils.getStartupParameters().isPortable());

		logs = new LiteButton("Logs");
		logs.setFont(minecraft.deriveFont(14F));
		logs.setBounds(10, packLocation.getY() + packLocation.getHeight() + 10, FRAME_WIDTH / 2 - 15, 25);
		logs.setForeground(Color.WHITE);
		logs.setActionCommand(LOGS_ACTION);
		logs.addActionListener(this);

		LiteButton save = new LiteButton("Save");
		save.setFont(minecraft.deriveFont(14F));
		save.setBounds(FRAME_WIDTH / 2 + 5, logs.getY() + logs.getHeight() + 10, FRAME_WIDTH / 2 - 15, 25);
		save.setActionCommand(SAVE_ACTION);
		save.addActionListener(this);

		LiteButton console = new LiteButton("Console");
		console.setFont(minecraft.deriveFont(14F));
		console.setBounds(10, logs.getY() + logs.getHeight() + 10, FRAME_WIDTH / 2 - 15, 25);
		console.setForeground(Color.WHITE);
		console.setActionCommand(CONSOLE_ACTION);
		console.addActionListener(this);

		fileChooser = new JFileChooser(Utils.getLauncherDirectory());
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		Container contentPane = getContentPane();
		contentPane.add(permgen);
		contentPane.add(build);
		contentPane.add(beta);
		contentPane.add(stable);
		contentPane.add(changeFolder);
		contentPane.add(packLocation);
		contentPane.add(logs);
		contentPane.add(console);
		contentPane.add(optionsQuit);
		contentPane.add(title);
		contentPane.add(memory);
		contentPane.add(memoryLabel);
		contentPane.add(save);
		contentPane.add(background);

		setLocationRelativeTo(this.getOwner());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComponent) {
			action(e.getActionCommand(), (JComponent) e.getSource());
		}
	}

	public void action(String action, JComponent c) {
		if (action.equals(QUIT_ACTION)) {
			dispose();
		} else if (action.equals(SAVE_ACTION)) {
			int oldMem = Settings.getMemory();
			int mem = Memory.memoryOptions[memory.getSelectedIndex()].getSettingsId();
			Settings.setMemory(mem);
			boolean oldperm = Settings.getPermGen();
			boolean perm = permgen.isSelected();
			Settings.setPermGen(perm);
			Settings.setBuildStream(buildStream);
			if (directoryChanged) {
				Settings.setMigrate(true);
				Settings.setMigrateDir(installedDirectory);
			}
			Settings.getYAML().save();
			
			if (mem != oldMem || oldperm != perm || directoryChanged) {
				int result = JOptionPane.showConfirmDialog(c, "Restart required for settings to take effect. Would you like to restart?", "Restart Required", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					SpoutcraftLauncher.relaunch(true);
				}
			}
			dispose();
		} else if (action.equals(LOGS_ACTION)) {
			File logDirectory = new File(Utils.getLauncherDirectory(), "logs");
			Compatibility.open(logDirectory);
		} else if (action.equals(CONSOLE_ACTION)) {
			SpoutcraftLauncher.setupConsole();
			dispose();
		} else if (action.equals(CHANGEFOLDER_ACTION)) {
			int result = fileChooser.showOpenDialog(this);
			
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				packLocation.setText(file.getPath());
				installedDirectory = file.getAbsolutePath();
				directoryChanged = true;
			}
		} else if (action.equals(BETA_ACTION)) {
			buildStream = "beta";
			build.setText(LAUNCHER_PREPEND + getLatestLauncherBuild(buildStream));
		} else if (action.equals(STABLE_ACTION)) {
			buildStream = "stable";
			build.setText(LAUNCHER_PREPEND + getLatestLauncherBuild(buildStream));
		}
		
	}
	
	private int getLatestLauncherBuild(String buildStream) {
		int build = Settings.getLauncherBuild();
		try {
			build = RestAPI.getLatestLauncherBuild(buildStream);
			return build;
		} catch (RestfulAPIException e) {
			e.printStackTrace();
		}
		
		return build;
	}

	@SuppressWarnings("restriction")
	private void populateMemory(JComboBox memory) {
		long maxMemory = 1024;
		String architecture = System.getProperty("sun.arch.data.model", "32");
		boolean bit64 = architecture.equals("64");

		try {
			OperatingSystemMXBean osInfo = ManagementFactory.getOperatingSystemMXBean();
			if (osInfo instanceof com.sun.management.OperatingSystemMXBean) {
				maxMemory = ((com.sun.management.OperatingSystemMXBean) osInfo).getTotalPhysicalMemorySize() / 1024 / 1024;
			}
		} catch (Throwable t) {
		}
		maxMemory = Math.max(512, maxMemory);

		if (maxMemory >= Memory.MAX_32_BIT_MEMORY && !bit64) {
			memory.setToolTipText("<html>Sets the amount of memory assigned to Minecraft<br/>" + "You have more than 1.5GB of memory available, but<br/>"
					+ "you must have 64bit java installed to use it.</html>");
		} else {
			memory.setToolTipText("<html>Sets the amount of memory assigned to Minecraft<br/>" + "More memory is not always better.<br/>"
					+ "More memory will also cause your CPU to work more.</html>");
		}

		if (!bit64) {
			maxMemory = Math.min(Memory.MAX_32_BIT_MEMORY, maxMemory);
		}
		System.out.println("Maximum usable memory detected: " + maxMemory + " mb");

		for (Memory mem : Memory.memoryOptions) {
			if (maxMemory >= mem.getMemoryMB()) {
				memory.addItem(mem.getDescription());
			}
		}

		int memoryOption = Settings.getMemory();
		try {
			Settings.setMemory(memoryOption);
			memory.setSelectedIndex(Memory.getMemoryIndexFromId(memoryOption));
		} catch (IllegalArgumentException e) {
			memory.removeAllItems();
			memory.addItem(String.valueOf(Memory.memoryOptions[0]));
			Settings.setMemory(1); // 512 == 1
			memory.setSelectedIndex(0); // 1st element
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		this.setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
