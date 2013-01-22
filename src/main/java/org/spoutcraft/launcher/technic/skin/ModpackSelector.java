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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.spoutcraft.launcher.skin.components.LoginFrame;
import org.spoutcraft.launcher.technic.ModpackInfo;
import org.spoutcraft.launcher.technic.TechnicRestAPI;

public class ModpackSelector extends JComponent {
	private static final long serialVersionUID = 1L;

	private final LoginFrame frame;
	private List<PackButton> buttons = new ArrayList<PackButton>();

	private final int bigWidth = 180;
	private final int bigHeight = 110;
	private final float smallScale = 0.7F;
	private final int spacing = 15;
	private final int smallWidth = (int) (bigWidth * smallScale);
	private final int smallHeight = (int) (bigHeight * smallScale);

	private int index;

	public ModpackSelector(LoginFrame frame) {
		this.frame = frame;
		this.index = 0;
	}


	public void setupModpackButtons() throws IOException {
		List<ModpackInfo> modpacks = TechnicRestAPI.getModpacks();
		for (ModpackInfo info : modpacks) {
			buttons.add(new PackButton(info));
		}
		selectPack(0);
	}

	private int getIndex() {
		return this.index;
	}

	public void selectPack(int index) {
		if (index >= buttons.size()) {
			this.index = 0;
		} else if (index < 0) {
			this.index = buttons.size();
		} else {
			this.index = index;
		}
		this.removeAll();
		// Set the big button in the middle
		int bigX = (getWidth() / 2) - (bigWidth / 2);
		int bigY = (getHeight() / 2) - (bigHeight / 2);
		this.add(buttons.get(getIndex()).createButton(bigX, bigY, bigWidth, bigHeight));

		// Label the pack by name
		JLabel label = new JLabel(buttons.get(getIndex()).getModpackInfo().getDisplayName());
		label.setFont(frame.getMinecraftFont(18));
		label.setForeground(Color.white);
		label.setBounds(bigX, bigY - 25, bigWidth, 20);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(label);

		int smallY = getHeight() / 2 - smallHeight / 2;

		// Start the iterator just after the selected pack
		ListIterator<PackButton> iterator = buttons.listIterator(getIndex() + 1);
		// Add the first 3 buttons to the right
		for (int i = 0; i < 3; i++) {
			// If you run out of packs, start the iterator back at 0
			if (!iterator.hasNext()) {
				iterator = buttons.listIterator(0);
			}
			PackButton button = iterator.next();
			int smallX = bigX + bigWidth + spacing + (i * (smallWidth + spacing));
			this.add(button.createButton(smallX, smallY, smallWidth, smallHeight));

		}

		// Start the iterator at the selected pack
		iterator = buttons.listIterator(getIndex());
		// Add the last 3 buttons to the left
		for (int i = 1; i < 4; i++) {
			// If you run out of packs, start the iterator back at the last element
			if (!iterator.hasPrevious()) {
				iterator = buttons.listIterator(buttons.size());
			}
			PackButton button = iterator.previous();
			int smallX = bigX - (i * (smallWidth + spacing));
			this.add(button.createButton(smallX, smallY, smallWidth, smallHeight));
		}

		frame.repaint();
	}

	public void selectNextPack() {
		selectPack(getIndex() + 1);
	}

	public void selectPreviousPack() {
		selectPack(getIndex() - 1);
	}

	public ModpackInfo getSelectedPack() {
		return buttons.get(index).getModpackInfo();
	}
	
}
