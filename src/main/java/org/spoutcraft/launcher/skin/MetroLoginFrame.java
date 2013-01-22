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
package org.spoutcraft.launcher.skin;

import static org.spoutcraft.launcher.util.ResourceUtils.getResourceAsStream;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.exceptions.RestfulAPIException;
import org.spoutcraft.launcher.skin.components.BackgroundImage;
import org.spoutcraft.launcher.skin.components.DynamicButton;
import org.spoutcraft.launcher.skin.components.HyperlinkJLabel;
import org.spoutcraft.launcher.skin.components.ImageHyperlinkButton;
import org.spoutcraft.launcher.skin.components.LiteButton;
import org.spoutcraft.launcher.skin.components.LitePasswordBox;
import org.spoutcraft.launcher.skin.components.LiteProgressBar;
import org.spoutcraft.launcher.skin.components.LiteTextBox;
import org.spoutcraft.launcher.skin.components.LoginFrame;
import org.spoutcraft.launcher.skin.components.PackSwitcher;
import org.spoutcraft.launcher.skin.components.TransparentJLabel;
import org.spoutcraft.launcher.technic.ModpackInfo;
import org.spoutcraft.launcher.technic.TechnicRestAPI;
import org.spoutcraft.launcher.technic.skin.ModpackSelector;
import org.spoutcraft.launcher.util.ImageUtils;
import org.spoutcraft.launcher.util.OperatingSystem;
import org.spoutcraft.launcher.util.ResourceUtils;

public class MetroLoginFrame extends LoginFrame implements ActionListener, KeyListener{
	private static final long serialVersionUID = 1L;
	private static final URL gearIcon = LoginFrame.class.getResource("/org/spoutcraft/launcher/resources/gear.png");
	private static final int FRAME_WIDTH = 880;
	private static final int FRAME_HEIGHT = 520;
	private static final String OPTIONS_ACTION = "options";
	private static final String PACKLEFT_ACTION = "packleft";
	private static final String PACKRIGHT_ACTION = "packright";
	private static final String LOGIN_ACTION = "login";
	private static final String IMAGE_LOGIN_ACTION = "image_login";
	private static final String REMOVE_USER = "remove";
	private final Map<JButton, DynamicButton> removeButtons = new HashMap<JButton, DynamicButton>();
	private LiteTextBox name;
	private LitePasswordBox pass;
	private LiteButton login;
	private JCheckBox remember;
	private LiteProgressBar progressBar;
	private OptionsMenu optionsMenu = null;
	private ModpackSelector packSelector;
	private BackgroundImage packBackground;
	public MetroLoginFrame() {
		initComponents();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((dim.width - FRAME_WIDTH) / 2, (dim.height - FRAME_HEIGHT) / 2, FRAME_WIDTH, FRAME_HEIGHT);
		setResizable(false);
		packBackground = new BackgroundImage(FRAME_WIDTH, FRAME_HEIGHT);
		getContentPane().add(packBackground);
		this.setTitle("Test");
	}

	private void initComponents() {
		Font minecraft = getMinecraftFont(12);

		int xShift = 0;
		int yShift = 0;
		if (this.isUndecorated()) {
			yShift += 30;
		}

		// Setup username box
		name = new LiteTextBox(this, "Username...");
		name.setBounds(602 + xShift, 379 + yShift, 140, 24);
		name.setFont(minecraft);
		name.addKeyListener(this);

		// Setup password box
		pass = new LitePasswordBox(this, "Password...");
		pass.setBounds(602 + xShift, 408 + yShift, 140, 24);
		pass.setFont(minecraft);
		pass.addKeyListener(this);

		// Setup remember checkbox
		remember = new JCheckBox("Remember");
		remember.setBounds(755 + xShift, 408 + yShift, 110, 24);
		remember.setFont(minecraft);
		remember.setOpaque(false);
		remember.setBorderPainted(false);
		remember.setContentAreaFilled(false);
		remember.setBorder(null);
		remember.setForeground(Color.WHITE);
		remember.addKeyListener(this);

		// Setup login button
		login = new LiteButton("Login");
		login.setBounds(755 + xShift, 379 + yShift, 92, 24);
		login.setFont(minecraft);
		login.setActionCommand(LOGIN_ACTION);
		login.addActionListener(this);
		login.addKeyListener(this);

		// Technic logo
		JLabel logo = new JLabel();
		logo.setBounds(FRAME_WIDTH / 2 - 200, 15, 400, 109);
		setIcon(logo, "techniclauncher.png", logo.getWidth(), logo.getHeight());
		
		// Pack Selector Background
		JLabel selectorBackground = new JLabel();
		selectorBackground.setBounds(0, FRAME_HEIGHT / 2 - 100, FRAME_WIDTH, 168);
		setIcon(selectorBackground, "selectorBackground.png", selectorBackground.getWidth(), selectorBackground.getHeight());
		
		// Pack Select Left
		PackSwitcher switchLeft = new PackSwitcher();
		switchLeft.setBounds(0, FRAME_HEIGHT / 2 - 100, 22, 168);
		switchLeft.setActionCommand(PACKLEFT_ACTION);
		switchLeft.addActionListener(this);
		setIcon(switchLeft, "selectLeft.png", switchLeft.getWidth(), switchLeft.getHeight());
		
		// Pack Select Right
		PackSwitcher switchRight = new PackSwitcher();
		switchRight.setBounds(FRAME_WIDTH - 28, FRAME_HEIGHT / 2 - 100, 22, 168);
		switchRight.setActionCommand(PACKRIGHT_ACTION);
		switchRight.addActionListener(this);
		setIcon(switchRight, "selectRight.png", switchRight.getWidth(), switchRight.getHeight());
		
		// Login Strip
		TransparentJLabel loginStrip = new TransparentJLabel();
		loginStrip.setBounds(0, FRAME_HEIGHT - 107 - 60, FRAME_WIDTH, 107);
		loginStrip.setTransparency(0.95F);
		loginStrip.setHoverTransparency(0.95F);
		setIcon(loginStrip, "loginstrip.png", loginStrip.getWidth(), loginStrip.getHeight());

		// Progress Bar
		progressBar = new LiteProgressBar();
		progressBar.setBounds(8, 130, 395, 23);
		progressBar.setVisible(false);
		progressBar.setStringPainted(true);
		progressBar.setOpaque(true);
		progressBar.setTransparency(0.70F);
		progressBar.setHoverTransparency(0.70F);
		progressBar.setFont(minecraft);

		// Home Link
		Font largerMinecraft;
		if (OperatingSystem.getOS().isUnix()) {
			largerMinecraft = minecraft.deriveFont((float)18);
		} else {
			largerMinecraft = minecraft.deriveFont((float)20);
		}

		HyperlinkJLabel home = new HyperlinkJLabel("Home", "http://www.technicpack.net");
		home.setFont(largerMinecraft);
		home.setBounds(545, 35, 65, 20);
		home.setForeground(Color.WHITE);
		home.setOpaque(false);
		home.setTransparency(0.70F);
		home.setHoverTransparency(1F);

		/*
		// Forums link
		HyperlinkJLabel forums = new HyperlinkJLabel("Forums", "http://forums.technicpack.net/");
		forums.setFont(largerMinecraft);
		forums.setBounds(625, 35, 90, 20);
		forums.setForeground(Color.WHITE);
		forums.setOpaque(false);
		forums.setTransparency(0.70F);
		forums.setHoverTransparency(1F);
		*/
		
		// Forums link
		JButton forums = new ImageHyperlinkButton("http://forums.technicpack.net/");
		forums.setToolTipText("Visit the forums");
		forums.setBounds(FRAME_WIDTH - 190, 20, 170, 95);
		setIcon(forums, "forums.png", forums.getWidth(), forums.getHeight());
		
		// Donate link
		JButton donate = new ImageHyperlinkButton("http://www.technicpack.net/donate/");
		donate.setToolTipText("Donate to the modders");
		donate.setBounds(forums.getX() - 180,  forums.getY(), 170, 95);
		setIcon(donate, "donate.png", forums.getWidth(), forums.getHeight());

		// Issues link
		HyperlinkJLabel issues = new HyperlinkJLabel("Issues", "http://forums.technicpack.net/forums/bug-reports.81/");
		issues.setFont(largerMinecraft);
		issues.setBounds(733, 35, 85, 20);
		issues.setForeground(Color.WHITE);
		issues.setOpaque(false);
		issues.setTransparency(0.70F);
		issues.setHoverTransparency(1F);

		// Options Button
		JButton options = new JButton();
		options.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(gearIcon)));
		options.setBounds(FRAME_WIDTH - 40, 6, 28, 28);
		options.setActionCommand(OPTIONS_ACTION);
		options.addActionListener(this);

		// Steam button
		JButton steam = new ImageHyperlinkButton("http://steamcommunity.com/groups/technic-pack");
		steam.setToolTipText("Game with us on Steam");
		steam.setBounds(6, 6 + yShift, 28, 28);
		setIcon(steam, "steam.png", 28);

		// Twitter button
		JButton twitter = new ImageHyperlinkButton("https://twitter.com/TechnicPack");
		twitter.setToolTipText("Follow us on Twitter");
		twitter.setBounds(6 + 34 * 3 + xShift, 6 + yShift, 28, 28);
		setIcon(twitter, "twitter.png", 28);

		// Facebook button
		JButton facebook = new ImageHyperlinkButton("https://www.facebook.com/TechnicPack");
		facebook.setToolTipText("Like us on Facebook");
		facebook.setBounds(6 + 34 * 2 + xShift, 6 + yShift, 28, 28);
		setIcon(facebook, "facebook.png", 28);

		// Google+ button
		/*
		JButton gplus = new ImageHyperlinkButton("http://spout.in/gplus");
		gplus.setToolTipText("Follow us on Google+");
		gplus.setBounds(6 + 34 * 2 + xShift, FRAME_HEIGHT - 62 + yShift, 28, 28);
		setIcon(gplus, "gplus.png", 28);
		*/

		// YouTube button
		JButton youtube = new ImageHyperlinkButton("http://www.youtube.com/user/kakermix");
		youtube.setToolTipText("Subscribe to our videos");
		youtube.setBounds(6 + 34 + xShift, 6 + yShift, 28, 28);
		setIcon(youtube, "youtube.png", 28);

		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		// Pack Selector
		packSelector = new ModpackSelector(this);
		packSelector.setBounds(0, (FRAME_HEIGHT / 2) - 85, FRAME_WIDTH, 170);

		// User Faces
		java.util.List<String> savedUsers = getSavedUsernames();
		int users = Math.min(5, this.getSavedUsernames().size());
		for (int i = 0; i < users; i++) {
			String accountName = savedUsers.get(i);
			String userName = this.getUsername(accountName);

			DynamicButton userButton = new DynamicButton(this, getImage(userName), 44, accountName, userName);
			userButton.setFont(minecraft.deriveFont(14F));

			userButton.setBounds((FRAME_WIDTH - 75) * (i + 1) / (users + 1), FRAME_HEIGHT - 75 , 75, 75);
			contentPane.add(userButton);
			userButton.setActionCommand(IMAGE_LOGIN_ACTION);
			userButton.addActionListener(this);
			setIcon(userButton.getRemoveIcon(), "remove.png", 16);
			userButton.getRemoveIcon().addActionListener(this);
			userButton.getRemoveIcon().setActionCommand(REMOVE_USER);
			removeButtons.put(userButton.getRemoveIcon(), userButton);
		}

		contentPane.add(switchLeft);
		contentPane.add(switchRight);
		contentPane.add(packSelector);
		contentPane.add(selectorBackground);
		contentPane.add(name);
		contentPane.add(pass);
		contentPane.add(remember);
		contentPane.add(login);
		contentPane.add(steam);
		contentPane.add(twitter);
		contentPane.add(facebook);
		//contentPane.add(gplus);
		contentPane.add(youtube);
		//contentPane.add(home);
		//contentPane.add(forums);
		//contentPane.add(donate);
		//contentPane.add(issues);
		contentPane.add(logo);
		contentPane.add(loginStrip);
		contentPane.add(options);
		contentPane.add(progressBar);
		
		
		setFocusTraversalPolicy(new LoginFocusTraversalPolicy());
	}

	public ModpackSelector getModpackSelector() {
		return packSelector;
	}
	
	public BackgroundImage getBackgroundImage() {
		return packBackground;
	}

	private void setIcon(JButton button, String iconName, int size) {
		try {
			button.setIcon(new ImageIcon(ImageUtils.scaleImage(ImageIO.read(ResourceUtils.getResourceAsStream("/org/spoutcraft/launcher/resources/" + iconName)), size, size)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setIcon(JButton label, String iconName, int w, int h) {
		try {
			label.setIcon(new ImageIcon(ImageUtils.scaleImage(ImageIO.read(ResourceUtils.getResourceAsStream("/org/spoutcraft/launcher/resources/" + iconName)), w, h)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setIcon(JLabel label, String iconName, int w, int h) {
		try {
			label.setIcon(new ImageIcon(ImageUtils.scaleImage(ImageIO.read(ResourceUtils.getResourceAsStream("/org/spoutcraft/launcher/resources/" + iconName)), w, h)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BufferedImage getImage(String user){
		try {
			URLConnection conn = (new URL("https://minotar.net/helm/" + user + "/100")).openConnection();
			InputStream stream = conn.getInputStream();
			BufferedImage image = ImageIO.read(stream);
			if (image != null) {
				return image;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			return ImageIO.read(getResourceAsStream("/org/spoutcraft/launcher/resources/face.png"));
		} catch (IOException e1) {
			throw new RuntimeException("Error reading backup image", e1);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComponent) {
			action(e.getActionCommand(), (JComponent)e.getSource());
		}
	}

	private void action(String action, JComponent c) {
		if (action.equals(OPTIONS_ACTION)) {
			if (optionsMenu == null || !optionsMenu.isVisible()) {
				optionsMenu = new OptionsMenu();
				optionsMenu.setModal(true);
				optionsMenu.setVisible(true);
			}
		} else if (action.equals(PACKLEFT_ACTION)) {
			getModpackSelector().selectPreviousPack();
			this.setTitle(packSelector.getSelectedPack().getDisplayName());
			setBackgroundImage(packBackground);
		} else if (action.equals(PACKRIGHT_ACTION)) {
			getModpackSelector().selectNextPack();
			this.setTitle(packSelector.getSelectedPack().getDisplayName());
			setBackgroundImage(packBackground);
		} else if (action.equals(LOGIN_ACTION)) {
			String modpack = getModpackSelector().getSelectedPack().getName();
			String build;
			try {
				build = TechnicRestAPI.getRecommendedBuild(modpack);
				Launcher.getGameUpdater().onModpackBuildChange(TechnicRestAPI.getModpack(modpack, build));
			} catch (RestfulAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String pass = new String(this.pass.getPassword());
			if (getSelectedUser().length() > 0 && pass.length() > 0) {
				this.doLogin(getSelectedUser(), pass);
				if (remember.isSelected()) {
					saveUsername(getSelectedUser(), pass);
				}
			}
		} else if (action.equals(IMAGE_LOGIN_ACTION)) {
			DynamicButton userButton = (DynamicButton)c;
			this.name.setText(userButton.getAccount());
			this.pass.setText(this.getSavedPassword(userButton.getAccount()));
			this.remember.setSelected(true);
			action(LOGIN_ACTION, userButton);
		}  else if (action.equals(REMOVE_USER)) {
			DynamicButton userButton = removeButtons.get((JButton)c);
			this.removeAccount(userButton.getAccount());
			userButton.setVisible(false);
			userButton.setEnabled(false);
			getContentPane().remove(userButton);
			c.setVisible(false);
			c.setEnabled(false);
			getContentPane().remove(c);
			removeButtons.remove(c);
			writeUsernameList();
		}
	}

	@Override
	public void stateChanged(final String status, final float progress) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				int intProgress = Math.round(progress);
				progressBar.setValue(intProgress);
				String text = status;
				if (text.length() > 60) {
					text = text.substring(0, 60) + "...";
				}
				progressBar.setString(intProgress + "% " + text);
			}
		});
	}

	@Override
	public JProgressBar getProgressBar() {
		return progressBar;
	}

	@Override
	public void disableForm() {
	}

	@Override
	public void enableForm() {
	}

	@Override
	public String getSelectedUser() {
		return this.name.getText();
	}
	
	public void setBackgroundImage(BackgroundImage packBackground) {
		packBackground.setIcon(new ImageIcon(newBackgroundImage(packSelector.getSelectedPack())));
	}
	
	public Image newBackgroundImage(ModpackInfo modpack) {
		try {
			Image image = modpack.getBackground().getScaledInstance(FRAME_WIDTH, FRAME_HEIGHT, Image.SCALE_SMOOTH);
			return image;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// Emulates tab focus policy of name -> pass -> remember -> login
	private class LoginFocusTraversalPolicy extends FocusTraversalPolicy{
		@Override
		public Component getComponentAfter(Container con, Component c) {
			if (c == name) {
				return pass;
			} else if (c == pass) {
				return remember;
			} else if (c == remember) {
				return login;
			} else if (c == login) {
				return name;
			}
			return getFirstComponent(con);
		}

		@Override
		public Component getComponentBefore(Container con, Component c) {
			if (c == name) {
				return login;
			} else if (c == pass) {
				return name;
			} else if (c == remember) {
				return pass;
			} else if (c == login) {
				return remember;
			}
			return getFirstComponent(con);
		}

		@Override
		public Component getFirstComponent(Container c) {
			return name;
		}

		@Override
		public Component getLastComponent(Container c) {
			return login;
		}

		@Override
		public Component getDefaultComponent(Container c) {
			return name;
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER){
			// Allows the user to press enter and log in from the login box focus, username box focus, or password box focus
			if (e.getComponent() == login || e.getComponent() == name || e.getComponent() == pass) {
				action(LOGIN_ACTION, (JComponent) e.getComponent());
			} else if (e.getComponent() == remember) {
				remember.setSelected(!remember.isSelected());
			}
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			getModpackSelector().selectPreviousPack();
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			getModpackSelector().selectNextPack();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
}
