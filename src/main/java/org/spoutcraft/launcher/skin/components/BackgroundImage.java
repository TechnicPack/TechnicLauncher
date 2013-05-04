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

package org.spoutcraft.launcher.skin.components;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.spoutcraft.launcher.skin.MetroLoginFrame;

public class BackgroundImage extends JLabel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;

	private final MetroLoginFrame frame;
	private int mouseX = 0, mouseY = 0;
	private AnimatedBackground background;
	private AnimatedImage tekkit;

	public BackgroundImage(MetroLoginFrame frame, int width, int height) {
		this.frame = frame;
		setVerticalAlignment(SwingConstants.CENTER);
		setHorizontalAlignment(SwingConstants.CENTER);
		setBounds(0, 0, width, height);

		setVerticalAlignment(SwingConstants.TOP);
		setHorizontalAlignment(SwingConstants.LEFT);
		setIcon(MetroLoginFrame.getIcon("background.jpg", width, height));
		background = new AnimatedBackground(this);
		background.setIcon(MetroLoginFrame.getIcon("background.jpg", width, height));
		background.setBounds(0, 0, width, height);
		

		tekkit = new AnimatedImage(650, 100, MetroLoginFrame.getIcon("creeper.png", 107, 69));
		tekkit.setBounds(500, 100, 107, 69);
		tekkit.setVisible(false);

		this.add(tekkit);
		this.add(background);
	}

	public void changeBackground(String name, Icon icon) {
		background.changeIcon(name, icon);
		if (name.equals("tekkitmain")) {
			tekkit.setVisible(true);
			tekkit.setAnimating(true);
		} else {
			tekkit.setVisible(false);
			tekkit.setAnimating(false);
		}
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

	@Override
	public void mouseDragged(MouseEvent e) {
		frame.setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
