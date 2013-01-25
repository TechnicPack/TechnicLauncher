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
package org.spoutcraft.launcher.technic.skin;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.spoutcraft.launcher.Settings;
import org.spoutcraft.launcher.skin.MetroLoginFrame;
import org.spoutcraft.launcher.skin.components.LiteButton;
import org.spoutcraft.launcher.technic.InstalledPack;

public class ModpackOptions extends JDialog implements ActionListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private static final int FRAME_WIDTH = 300;
	private static final int FRAME_HEIGHT = 300;
	private static final String QUIT_ACTION = "quit";
	private static final String SAVE_ACTION = "save";
	private static final String BUILD_ACTION = "build";
	private JLabel buildLabel;
	private JLabel background;
	private InstalledPack installedPack;
	private int mouseX = 0, mouseY = 0;
	
	public ModpackOptions(InstalledPack installedPack) {
		this.installedPack = installedPack;
		setTitle("Modpack Options");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		addMouseListener(this);
		addMouseMotionListener(this);
		setResizable(false);
		setUndecorated(true);
		initComponents();
	}
	
	private void initComponents() {
		Font minecraft = MetroLoginFrame.getMinecraftFont(12);
		
		background = new JLabel();
		background.setBounds(0,0, FRAME_WIDTH, FRAME_HEIGHT);
		MetroLoginFrame.setIcon(background, "optionsBackground.png", background.getWidth(), background.getHeight());
		
		Container contentPane = getContentPane();
		contentPane.setLayout(null);
		
		JLabel optionsTitle = new JLabel();
		optionsTitle.setBounds(10, 10, FRAME_WIDTH, 25);
		optionsTitle.setText(installedPack.getDisplayName() + " Options");
		optionsTitle.setForeground(Color.white);
		optionsTitle.setFont(minecraft.deriveFont(14F));
		
		ImageButton optionsQuit = new ImageButton(MetroLoginFrame.getIcon("quit.png", 28, 28), MetroLoginFrame.getIcon("quit.png", 28, 28));
		optionsQuit.setRolloverIcon(MetroLoginFrame.getIcon("quitHover.png", 28, 28));
		optionsQuit.setBounds(FRAME_WIDTH - 38, 10, 28, 28);
		optionsQuit.setActionCommand(QUIT_ACTION);
		optionsQuit.addActionListener(this);
		
		buildLabel = new JLabel();
		buildLabel.setBounds(10, 50, 140, 25);
		buildLabel.setText("Select Build");
		buildLabel.setForeground(Color.white);
		buildLabel.setFont(minecraft);
		
		JComboBox buildSelector = new JComboBox(installedPack.getInfo().getBuilds());
		buildSelector.setBounds(FRAME_WIDTH / 2, 50, 140, 25);
		buildSelector.setActionCommand(BUILD_ACTION);
		buildSelector.addActionListener(this);
		
		String build = Settings.getModpackBuild(installedPack.getInfo().getName());
		buildSelector.setSelectedItem((String) build);
		
		LiteButton save = new LiteButton("Save and Close");
		save.setFont(minecraft.deriveFont(14F));
		save.setBounds(10, FRAME_HEIGHT - 40, 280, 30);
		save.setActionCommand(SAVE_ACTION);
		save.addActionListener(this);
		
		contentPane.add(optionsTitle);
		contentPane.add(optionsQuit);
		contentPane.add(buildLabel);
		contentPane.add(buildSelector);
		contentPane.add(save);
		contentPane.add(background);
		
		setLocationRelativeTo(this.getOwner());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComponent) {
			action(e.getActionCommand(), (JComponent)e.getSource());
		}
	}
	
	private void action(String action, JComponent c) {
		if (action.equals(QUIT_ACTION)) {
			dispose();
		} else if (action.equals(SAVE_ACTION)) {
			Settings.getYAML().save();
			dispose();
		} else if (action.equals(BUILD_ACTION) && c instanceof JComboBox) {
			String build = (String) ((JComboBox) c).getSelectedItem();
			Settings.setModpackBuild(installedPack.getInfo().getName(), build);
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
	public void mousePressed(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
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

}
