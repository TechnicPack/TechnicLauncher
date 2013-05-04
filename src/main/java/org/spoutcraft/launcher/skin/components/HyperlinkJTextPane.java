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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

import javax.swing.JTextPane;

import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.util.Compatibility;

public class HyperlinkJTextPane extends JTextPane implements MouseListener {
	private static final long CLICK_DELAY = 250L;
	private static final long serialVersionUID = 1L;
	private String url;
	private long lastClick = System.currentTimeMillis();

	public HyperlinkJTextPane(String text, String url) {
		this.setText(text);
		this.url = url;
		super.addMouseListener(this);
		this.setHighlighter(null);
		this.setCaretColor(new Color(255, 255, 255, 0));
		this.setCursor(Launcher.getFrame().getCursor());
		setOpaque(false);
//		setBorder(new EmptyBorder(10, 10, 10, 10));
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(getBackground());
		Insets insets = getInsets();
		int x = insets.left;
		int y = insets.top;
		int width = getWidth() - (insets.left + insets.right);
		int height = getHeight() - (insets.top + insets.bottom);
		g.fillRect(x, y, width, height);
		super.paintComponent(g);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (lastClick + CLICK_DELAY > System.currentTimeMillis()) {
			return;
		}
		lastClick = System.currentTimeMillis();
		try {
			URI uri = new java.net.URI(url);
			HyperlinkJLabel.browse(uri);
		} catch (Exception ex) {
			System.err.println("Unable to open browser to " + url);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	public static void browse(URI uri) {
		Compatibility.browse(uri);
	}
}
