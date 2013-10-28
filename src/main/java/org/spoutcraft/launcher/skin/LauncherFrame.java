/*
 * This file is part of Technic Launcher.
 * Copyright (C) 2013 Syndicate, LLC
 *
 * Technic Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Technic Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Technic Launcher.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.spoutcraft.launcher.skin;

import net.technicpack.launchercore.auth.AuthResponse;
import net.technicpack.launchercore.auth.AuthenticationService;
import net.technicpack.launchercore.auth.RefreshResponse;
import net.technicpack.launchercore.install.User;
import net.technicpack.launchercore.install.Users;
import net.technicpack.launchercore.util.Download;
import net.technicpack.launchercore.util.DownloadListener;
import net.technicpack.launchercore.util.DownloadUtils;
import net.technicpack.launchercore.util.ImageUtils;
import net.technicpack.launchercore.util.ResourceUtils;
import net.technicpack.launchercore.util.Utils;
import org.spoutcraft.launcher.Launcher;
import org.spoutcraft.launcher.entrypoint.SpoutcraftLauncher;
import org.spoutcraft.launcher.skin.components.BackgroundImage;
import org.spoutcraft.launcher.skin.components.DynamicButton;
import org.spoutcraft.launcher.skin.components.ImageButton;
import org.spoutcraft.launcher.skin.components.ImageHyperlinkButton;
import org.spoutcraft.launcher.skin.components.LiteButton;
import org.spoutcraft.launcher.skin.components.LitePasswordBox;
import org.spoutcraft.launcher.skin.components.LiteProgressBar;
import org.spoutcraft.launcher.skin.components.LiteTextBox;
import org.spoutcraft.launcher.skin.components.RoundedBox;
import org.spoutcraft.launcher.skin.options.LauncherOptions;
import org.spoutcraft.launcher.skin.options.ModpackOptions;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static net.technicpack.launchercore.util.ResourceUtils.getResourceAsStream;

public class LauncherFrame extends JFrame implements ActionListener, KeyListener, MouseWheelListener, DownloadListener {
	public static final Color TRANSPARENT = new Color(45, 45, 45, 160);
	private static final long serialVersionUID = 1L;
	private static final int FRAME_WIDTH = 880;
	private static final int FRAME_HEIGHT = 520;
	private static final String OPTIONS_ACTION = "options";
	private static final String PACK_OPTIONS_ACTION = "packoptions";
	private static final String PACK_REMOVE_ACTION = "packremove";
	private static final String EXIT_ACTION = "exit";
	private static final String PACK_LEFT_ACTION = "packleft";
	private static final String PACK_RIGHT_ACTION = "packright";
	private static final String LOGIN_ACTION = "login";
	private static final String IMAGE_LOGIN_ACTION = "image_login";
	private static final String REMOVE_USER = "remove";
	private static final int SPACING = 7;
	public static URL icon = LauncherFrame.class.getResource("/org/spoutcraft/launcher/resources/icon.png");
	private final Map<JButton, DynamicButton> removeButtons = new HashMap<JButton, DynamicButton>();
	private final Map<String, DynamicButton> userButtons = new HashMap<String, DynamicButton>();
	private LiteTextBox name;
	private LitePasswordBox pass;
	private LiteButton login;
	private JCheckBox remember;
	private LiteProgressBar progressBar;
	private LauncherOptions launcherOptions = null;
	private ModpackOptions packOptions = null;
	private ModpackSelector packSelector;
	private BackgroundImage packBackground;
	private ImageButton packOptionsBtn;
	private ImageButton packRemoveBtn;
	private ImageHyperlinkButton platform;
	private JLabel customName;
	private RoundedBox barBox;
	private NewsComponent news;
	private long previous = 0L;

	public LauncherFrame() {
		initComponents();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((dim.width - FRAME_WIDTH) / 2, (dim.height - FRAME_HEIGHT) / 2, FRAME_WIDTH, FRAME_HEIGHT);
		setResizable(false);
		packBackground = new BackgroundImage(this, FRAME_WIDTH, FRAME_HEIGHT);
		this.addMouseListener(packBackground);
		this.addMouseMotionListener(packBackground);
		this.addMouseWheelListener(this);
		getContentPane().add(packBackground);
		this.setUndecorated(true);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	private void initComponents() {
		Font minecraft = getMinecraftFont(12);

		// Login background box
		RoundedBox loginArea = new RoundedBox(TRANSPARENT);
		loginArea.setBounds(605, 377, 265, 83);

		// Setup username box
		name = new LiteTextBox(this, "Username...");
		name.setBounds(loginArea.getX() + 15, loginArea.getY() + 15, 110, 24);
		name.setFont(minecraft);
		name.addKeyListener(this);

		// Setup password box
		pass = new LitePasswordBox(this, "Password...");
		pass.setBounds(loginArea.getX() + 15, loginArea.getY() + name.getHeight() + 20, 110, 24);
		pass.setFont(minecraft);
		pass.addKeyListener(this);


		// Setup login button
		login = new LiteButton("Launch");
		login.setBounds(loginArea.getX() + name.getWidth() + 30, loginArea.getY() + 15, 110, 24);
		login.setFont(minecraft);
		login.setActionCommand(LOGIN_ACTION);
		login.addActionListener(this);
		login.addKeyListener(this);

		// Setup remember checkbox
		remember = new JCheckBox("Remember");
		remember.setBounds(loginArea.getX() + name.getWidth() + 30, loginArea.getY() + name.getHeight() + 20, 110, 24);
		remember.setFont(minecraft);
		remember.setOpaque(false);
		remember.setBorderPainted(false);
		remember.setFocusPainted(false);
		remember.setContentAreaFilled(false);
		remember.setBorder(null);
		remember.setForeground(Color.WHITE);
		remember.setHorizontalTextPosition(SwingConstants.RIGHT);
		remember.setIconTextGap(10);
		remember.addKeyListener(this);

		if (Launcher.getUsers().getLastUser() != null) {
			name.setText(Launcher.getUsers().getLastUser());
			pass.setText("PASSWORD");
			remember.setSelected(true);
		}

		// Technic logo
		JLabel logo = new JLabel();
		ImageIcon logoIcon = new ImageIcon(ImageUtils.scaleWithAspectWidth(ResourceUtils.getImage("header.png"), 275));
		logo.setIcon(logoIcon);
		logo.setBounds(600, 6, logoIcon.getIconWidth(), logoIcon.getIconHeight());

		// Pack Selector Background
		JLabel selectorBackground = new JLabel();
		selectorBackground.setBounds(15, 0, 200, 520);
		selectorBackground.setBackground(TRANSPARENT);
		selectorBackground.setOpaque(true);

		// Pack Select Up
		ImageButton packUp = new ImageButton(ResourceUtils.getIcon("upButton.png", 65, 65));
		packUp.setBounds(-7, 0, 65, 65);
		packUp.setActionCommand(PACK_LEFT_ACTION);
		packUp.addActionListener(this);

		// Pack Select Down
		ImageButton packDown = new ImageButton(ResourceUtils.getIcon("downButton.png", 65, 65));
		packDown.setBounds(-7, FRAME_HEIGHT - 65, 65, 65);
		packDown.setActionCommand(PACK_RIGHT_ACTION);
		packDown.addActionListener(this);

		// Progress Bar Background box
		barBox = new RoundedBox(TRANSPARENT);
		barBox.setVisible(false);
		barBox.setBounds(605, 205, 265, 35);

		// Progress Bar
		progressBar = new LiteProgressBar(this);
		progressBar.setBounds(barBox.getX() + SPACING, barBox.getY() + SPACING, barBox.getWidth() - (SPACING * 2), barBox.getHeight() - (SPACING * 2));
		progressBar.setVisible(false);
		progressBar.setStringPainted(true);
		progressBar.setOpaque(true);
		progressBar.setFont(minecraft);

		// News Items
		news = new NewsComponent();
		news.setBounds(barBox.getX(), logo.getY() + logo.getHeight(), barBox.getWidth(), 100);

		// Link background box
		RoundedBox linkArea = new RoundedBox(TRANSPARENT);
		linkArea.setBounds(605, 250, 265, 120);

		int linkWidth = linkArea.getWidth() - (SPACING * 2);
		int linkHeight = (linkArea.getHeight() - (SPACING * 4)) / 3;

		// Browse link
		JButton browse = new ImageHyperlinkButton("http://www.technicpack.net");
		browse.setToolTipText("Get More Modpacks");
		browse.setBounds(linkArea.getX() + SPACING, linkArea.getY() + SPACING, linkWidth, linkHeight);
		browse.setIcon(ResourceUtils.getIcon("platformLinkButton.png"));
		browse.setRolloverIcon(ResourceUtils.getIcon("platformLinkButtonBright.png"));
		browse.setContentAreaFilled(false);
		browse.setBorderPainted(false);

		// Forums link
		JButton forums = new ImageHyperlinkButton("http://forums.technicpack.net/");
		forums.setToolTipText("Visit the forums");
		forums.setBounds(linkArea.getX() + SPACING, browse.getY() + browse.getHeight() + SPACING, linkWidth, linkHeight);
		forums.setIcon(ResourceUtils.getIcon("forumsLinkButton.png"));
		forums.setRolloverIcon(ResourceUtils.getIcon("forumsLinkButtonBright.png"));
		forums.setContentAreaFilled(false);
		forums.setBorderPainted(false);

		// Donate link
		JButton donate = new ImageHyperlinkButton("http://www.technicpack.net/donate/");
		donate.setToolTipText("Donate to the modders");
		donate.setBounds(linkArea.getX() + SPACING, forums.getY() + forums.getHeight() + SPACING, linkWidth, linkHeight);
		donate.setIcon(ResourceUtils.getIcon("donateLinkButton.png"));
		donate.setRolloverIcon(ResourceUtils.getIcon("donateLinkButtonBright.png"));
		donate.setContentAreaFilled(false);
		donate.setBorderPainted(false);

		// Options Button
		ImageButton options = new ImageButton(ResourceUtils.getIcon("gear.png", 28, 28), ResourceUtils.getIcon("gearInverted.png", 28, 28));
		options.setBounds(FRAME_WIDTH - 34 * 2, 6, 28, 28);
		options.setActionCommand(OPTIONS_ACTION);
		options.addActionListener(this);
		options.addKeyListener(this);

		// Pack Options Button
		packOptionsBtn = new ImageButton(ResourceUtils.getIcon("packOptions.png", 20, 20), ResourceUtils.getIcon("packOptionsInverted.png", 20, 20));
		packOptionsBtn.setBounds(25, FRAME_HEIGHT / 2 + 56, 20, 20);
		packOptionsBtn.setActionCommand(PACK_OPTIONS_ACTION);
		packOptionsBtn.addActionListener(this);

		// Platform website button
		platform = new ImageHyperlinkButton("http://www.technicpack.net/");
		platform.setIcon(ResourceUtils.getIcon("openPlatformPage.png", 20, 20));
		platform.setBounds(50, FRAME_HEIGHT / 2 + 56, 20, 20);

		// Pack Remove Button
		packRemoveBtn = new ImageButton(ResourceUtils.getIcon("packDelete.png", 20, 20), ResourceUtils.getIcon("packDeleteInverted.png", 20, 20));
		packRemoveBtn.setBounds(185, FRAME_HEIGHT / 2 + 56, 20, 20);
		packRemoveBtn.setActionCommand(PACK_REMOVE_ACTION);
		packRemoveBtn.addActionListener(this);

		// Exit Button
		ImageButton exit = new ImageButton(ResourceUtils.getIcon("quit.png", 28, 28), ResourceUtils.getIcon("quitHover.png", 28, 28));
		exit.setBounds(FRAME_WIDTH - 34, 6, 28, 28);
		exit.setActionCommand(EXIT_ACTION);
		exit.addActionListener(this);

		// Steam button
		JButton steam = new ImageHyperlinkButton("http://steamcommunity.com/groups/technic-pack");
		steam.setRolloverIcon(ResourceUtils.getIcon("steamInverted.png", 28, 28));
		steam.setToolTipText("Game with us on Steam");
		steam.setBounds(215 + 6, 6, 28, 28);
		setIcon(steam, "steam.png", 28);

		// Twitter button
		JButton twitter = new ImageHyperlinkButton("https://twitter.com/TechnicPack");
		twitter.setRolloverIcon(ResourceUtils.getIcon("twitterInverted.png", 28, 28));
		twitter.setToolTipText("Follow us on Twitter");
		twitter.setBounds(215 + 6 + 34 * 3, 6, 28, 28);
		setIcon(twitter, "twitter.png", 28);

		// Facebook button
		JButton facebook = new ImageHyperlinkButton("https://www.facebook.com/TechnicPack");
		facebook.setRolloverIcon(ResourceUtils.getIcon("facebookInverted.png", 28, 28));
		facebook.setToolTipText("Like us on Facebook");
		facebook.setBounds(215 + 6 + 34 * 2, 6, 28, 28);
		setIcon(facebook, "facebook.png", 28);

		// YouTube button
		JButton youtube = new ImageHyperlinkButton("http://www.youtube.com/user/kakermix");
		youtube.setRolloverIcon(ResourceUtils.getIcon("youtubeInverted.png", 28, 28));
		youtube.setToolTipText("Subscribe to our videos");
		youtube.setBounds(215 + 6 + 34, 6, 28, 28);
		setIcon(youtube, "youtube.png", 28);

		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		// Pack Selector
		packSelector = new ModpackSelector(this);
		packSelector.setBounds(15, 0, 200, 520);

		// Custom Pack Name Label
		customName = new JLabel("", JLabel.CENTER);
		customName.setBounds(FRAME_WIDTH / 2 - (192 / 2), FRAME_HEIGHT / 2 + (110 / 2) - 30, 192, 30);
		customName.setFont(minecraft.deriveFont(14F));
		customName.setVisible(false);
		customName.setForeground(Color.white);

		// User Faces
		Collection<String> savedUsers = Launcher.getUsers().getUsers();
		int users = Math.min(5, savedUsers.size());
		int index = 0;

		for (String userName : savedUsers) {
			if (index >= users) {
				break;
			}

			BufferedImage image = ResourceUtils.getImage("face.png", 45, 45);
			File assets = new File(Utils.getAssetsDirectory(), "avatars");
			File face = new File(assets, userName + ".png");
			if (face.exists()) {
				try {
					image = ImageIO.read(face);
					System.out.println("Loaded face");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			DynamicButton userButton = new DynamicButton(this, image, 1, Launcher.getUsers().getUser(userName));
			userButton.setFont(minecraft.deriveFont(12F));

			userButton.setBounds(FRAME_WIDTH - ((index + 1) * 70), FRAME_HEIGHT - 57, 45, 45);
			contentPane.add(userButton);
			userButton.setActionCommand(IMAGE_LOGIN_ACTION);
			userButton.addActionListener(this);
			setIcon(userButton.getRemoveIcon(), "remove.png", 16);
			userButton.getRemoveIcon().addActionListener(this);
			userButton.getRemoveIcon().setActionCommand(REMOVE_USER);
			removeButtons.put(userButton.getRemoveIcon(), userButton);
			userButtons.put(userName, userButton);
			index++;
		}

		contentPane.add(progressBar);
		contentPane.add(barBox);
		contentPane.add(packUp);
		contentPane.add(packDown);
		contentPane.add(customName);
		contentPane.add(packOptionsBtn);
		contentPane.add(packRemoveBtn);
		contentPane.add(platform);
		contentPane.add(packSelector);
		contentPane.add(selectorBackground);
		contentPane.add(name);
		contentPane.add(pass);
		contentPane.add(remember);
		contentPane.add(login);
		contentPane.add(steam);
		contentPane.add(twitter);
		contentPane.add(facebook);
		contentPane.add(youtube);
		contentPane.add(browse);
		contentPane.add(forums);
		contentPane.add(donate);
		contentPane.add(linkArea);
		contentPane.add(logo);
		contentPane.add(news);
		contentPane.add(loginArea);
		contentPane.add(options);
		contentPane.add(exit);


		setFocusTraversalPolicy(new LoginFocusTraversalPolicy());
	}

	private void setIcon(JButton button, String iconName, int size) {
		try {
			button.setIcon(new ImageIcon(ImageUtils.scaleImage(ImageIO.read(ResourceUtils.getResourceAsStream("/org/spoutcraft/launcher/resources/" + iconName)), size, size)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Font getMinecraftFont(int size) {
		Font minecraft;
		try {
			minecraft = Font.createFont(Font.TRUETYPE_FONT, getResourceAsStream("/org/spoutcraft/launcher/resources/minecraft.ttf")).deriveFont((float) size);
		} catch (Exception e) {
			e.printStackTrace();
			// Fallback
			minecraft = new Font("Arial", Font.PLAIN, 12);
		}
		return minecraft;
	}

	public static void setIcon(JLabel label, String iconName, int w, int h) {
		try {
			label.setIcon(new ImageIcon(ImageUtils.scaleImage(ImageIO.read(ResourceUtils.getResourceAsStream("/org/spoutcraft/launcher/resources/" + iconName)), w, h)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setUser(String name) {
		if (name != null) {
			DynamicButton user = userButtons.get(name);
			if (user != null) {
				user.doClick();
			}
		}
	}

	public NewsComponent getNews() {
		return news;
	}

	public BackgroundImage getBackgroundImage() {
		return packBackground;
	}

	public RoundedBox getBarBox() {
		return barBox;
	}

	public void updateFaces() {
		for (String user : userButtons.keySet()) {
			String userDisplayName = Launcher.getUsers().getUser(user).getDisplayName();
			BufferedImage image = getUserImage(userDisplayName);
			if (image != null) {
				userButtons.get(user).updateIcon(image);
			}
		}
	}

	private BufferedImage getUserImage(String user) {
		File assets = new File(Utils.getAssetsDirectory(), "avatars");
		assets.mkdirs();
		File file = new File(assets, user + ".png");
		try {
			Download download = DownloadUtils.downloadFile("http://skins.technicpack.net/helm/" + user + "/100", file.getName(), file.getAbsolutePath());
			if (download.getResult().equals(Download.Result.SUCCESS)) {
				return ImageIO.read(download.getOutFile());
			}
		} catch (IOException e) {
			if (SpoutcraftLauncher.params.isDebugMode()) {
				Utils.getLogger().log(Level.INFO, "Error downloading user face image: " + user, e);
			} else {
				Utils.getLogger().log(Level.INFO, "Error downloading user face image: " + user);
			}
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComponent) {
			action(e.getActionCommand(), (JComponent) e.getSource());
		}
	}

	private void action(String action, JComponent c) {
		if (action.equals(OPTIONS_ACTION)) {
			if (launcherOptions == null || !launcherOptions.isVisible()) {
				launcherOptions = new LauncherOptions();
				launcherOptions.setModal(true);
				launcherOptions.setVisible(true);
			}
		} else if (action.equals(PACK_REMOVE_ACTION)) {
			int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this pack?\n This will delete all files in: " + getSelector().getSelectedPack().getInstalledDirectory(), "Remove Pack", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				getSelector().removePack();
			}
		} else if (action.equals(PACK_OPTIONS_ACTION)) {
			if (packOptions == null || !packOptions.isVisible()) {
				System.out.println("Opening options for " + getSelector().getSelectedPack());
				packOptions = new ModpackOptions(getSelector().getSelectedPack());
				packOptions.setModal(true);
				packOptions.setVisible(true);
			}
		} else if (action.equals(EXIT_ACTION)) {
			System.exit(0);
		} else if (action.equals(PACK_LEFT_ACTION)) {
			getSelector().selectPreviousPack();
		} else if (action.equals(PACK_RIGHT_ACTION)) {
			getSelector().selectNextPack();
		} else if (action.equals(LOGIN_ACTION)) {
			if (Launcher.isLaunching()) {
				return;
			}
			User user = Launcher.getUsers().getUser(this.name.getText());
			if (user != null) {
				
				boolean valid = AuthenticationService.validate(user);

				if (!valid && !refresh(user)) {
					if (!AuthenticationService.wasOnline()) {
						offlineDialog(User.createOfflineUser(this.name.getText()));
						return;
					}
					
					JOptionPane.showMessageDialog(this, "Invalid token, please login again", "Invalid Token", JOptionPane.WARNING_MESSAGE);
					Launcher.getUsers().removeUser(this.name.getText());
					return;
				}
			} else {
				if (this.pass.getPassword().length == 0) {
					return;
				}
				user = login(Launcher.getUsers(), this.name.getText(), new String(this.pass.getPassword()));
				
				if (!AuthenticationService.wasOnline()) {
					offlineDialog(User.createOfflineUser(this.name.getText()));
					return;
				}
				else if (user == null) {
					return;
				}
			}

			if (remember.isSelected()) {
				Launcher.getUsers().addUser(user);
				Launcher.getUsers().setLastUser(user.getUsername());
			}

			Launcher.launch(user, packSelector.getSelectedPack(), packSelector.getSelectedPack().getBuild());
		} else if (action.equals(IMAGE_LOGIN_ACTION)) {
			DynamicButton userButton = (DynamicButton) c;
			this.name.setText(userButton.getUser().getUsername());
		} else if (action.equals(REMOVE_USER)) {
			DynamicButton userButton = removeButtons.get((JButton) c);
			Launcher.getUsers().removeUser(userButton.getUser().getUsername());
		}
	}

	public boolean refresh(User user) {
		RefreshResponse response = AuthenticationService.requestRefresh(user);
		
		if(response == null) {
			return false;
		}
		
		return response.getError() == null;
	}

	public User login(Users users, String username, String password) {
		AuthResponse response = AuthenticationService.requestLogin(username, password, users.getClientToken());
		
		if(response == null) {
			return null;
		}
		
		if (response.getError() != null) {
			JOptionPane.showMessageDialog(this, response.getErrorMessage(), response.getError(), JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		return new User(username, response);
	}

	public ModpackSelector getSelector() {
		return packSelector;
	}
	
	public void offlineDialog(User user) {
		int answer = JOptionPane.showOptionDialog(this,
												  "Minecraft's auth server appears to be offline, start in offline mode?",
												  "Offline Mode",
												  JOptionPane.YES_NO_OPTION,
												  JOptionPane.QUESTION_MESSAGE,
												  null,
												  new Object[] {"Yes", "Cancel"},
												  "Yes");
		if(answer == 0) {
			Launcher.launch(user, packSelector.getSelectedPack(), packSelector.getSelectedPack().getBuild());
		}
		else {
			return;
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

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public void disableForm() {
	}

	public void enableForm() {
		progressBar.setVisible(false);
		lockLoginButton(true);
	}

	public void lockLoginButton(boolean unlock) {
		if (unlock) {
			login.setText("Login");
		} else {
			login.setText("Launching...");
		}
		login.setEnabled(unlock);
		packRemoveBtn.setEnabled(unlock);
		packOptionsBtn.setEnabled(unlock);
	}

	public ImageButton getPackOptionsBtn() {
		return packOptionsBtn;
	}

	public ImageButton getPackRemoveBtn() {
		return packRemoveBtn;
	}

	public ImageHyperlinkButton getPlatform() {
		return platform;
	}

	public void enableComponent(JComponent component, boolean enable) {
		component.setVisible(enable);
		component.setEnabled(enable);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			// Allows the user to press enter and log in from the login box focus, username box focus, or password box focus
			if (e.getComponent() == login || e.getComponent() == name || e.getComponent() == pass) {
				action(LOGIN_ACTION, (JComponent) e.getComponent());
			} else if (e.getComponent() == remember) {
				remember.setSelected(!remember.isSelected());
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWhen() != previous) {
			if (e.getUnitsToScroll() > 0) {
				getSelector().selectNextPack();
			} else if (e.getUnitsToScroll() < 0) {
				getSelector().selectPreviousPack();
			}
			this.previous = e.getWhen();
		}

	}

	// Emulates tab focus policy of name -> pass -> remember -> login
	private class LoginFocusTraversalPolicy extends FocusTraversalPolicy {
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
}
