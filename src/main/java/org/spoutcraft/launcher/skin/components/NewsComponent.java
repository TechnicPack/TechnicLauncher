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
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JComponent;

import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.exceptions.RestfulAPIException;
import org.spoutcraft.launcher.skin.MetroLoginFrame;
import org.spoutcraft.launcher.technic.rest.Article;
import org.spoutcraft.launcher.technic.rest.RestAPI;
import org.spoutcraft.launcher.technic.skin.RoundedBox;

public class NewsComponent extends JComponent {
	private static final long serialVersionUID = 1L;

	public NewsComponent() {
		GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(MetroLoginFrame.getMinecraftFont(12));
	}

	public void loadArticles() {
		try {
			List<Article> articles = RestAPI.getNews();
			setupArticles(articles);
		} catch (RestfulAPIException e) {
			Launcher.getLogger().log(Level.WARNING, "Unable to load news, hiding news section", e);
			this.setVisible(false);
			this.setEnabled(false);
		}
	}

	private void setupArticles(List<Article> articles) {
		int i = 0;
		Font minecraft = MetroLoginFrame.getMinecraftFont(12);
		for (Article article : articles) {
			String date = article.getDate();
			String title = article.getDisplayTitle();

			HyperlinkJTextPane link = new HyperlinkJTextPane(date + " " + title, article.getUrl());
			link.setFont(minecraft);
			link.setForeground(Color.WHITE);
			link.setBackground(new Color(255, 255, 255, 0));
			link.setBounds(8, i * 50 + 5, getWidth() - 8, 42);
			if (i == 0) {
				this.add(link);
			}
			
			i++;
		}
		RoundedBox background = new RoundedBox(MetroLoginFrame.TRANSPARENT);
		background.setBounds(0, 0, getWidth(), getHeight());
		this.add(background);
		this.repaint();
	}
}
