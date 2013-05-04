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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.Timer;

public class AnimatedImage extends JLabel implements ActionListener {
	private static final long serialVersionUID = 1;

	private final Timer timer;
	private final int x;
	private final int y;
	private final int distance = 30;
	private int modX = 0;
	private boolean xReverse = false;
	private int modY = 0;
	private boolean yReverse = false;
	private int delay = 50;

	public AnimatedImage(int x, int y, Icon image) {
		this.setIcon(image);
		this.x = x;
		this.y = y;
		timer = new Timer(delay, this);
	}

	public void setAnimating(boolean animate) {
		if (animate) {
			timer.start();
		} else {
			timer.stop();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (modX == distance) {
			xReverse = true;
		}
		if (modX == 0) {
			xReverse = false;
		}
		if (modY == distance) {
			yReverse = true;
		}
		if (modY == 0) {
			yReverse = false;
		}

		if (xReverse) {
			modX--;
		} else {
			modX++;
		}

		if (yReverse) {
			modY--;
		} else {
			modY++;
		}

		int delayChange = 0;
		if (modX < distance / 2) {
			delayChange = distance - modX - (distance / 2);
		} else {
			delayChange = modX - (distance / 2);
		}
		timer.setDelay(delay + (delayChange * 10));

		this.setBounds(x + modX, y + modY, getWidth(), getHeight());
		this.repaint();
	}
}
