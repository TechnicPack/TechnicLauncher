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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.spoutcraft.launcher.GameUpdater;
import org.spoutcraft.launcher.LibrariesYML;
import org.spoutcraft.launcher.MD5Utils;
import org.spoutcraft.launcher.Main;
import org.spoutcraft.launcher.MinecraftUtils;
import org.spoutcraft.launcher.MinecraftYML;
import org.spoutcraft.launcher.MirrorUtils;
import org.spoutcraft.launcher.PlatformUtils;
import org.spoutcraft.launcher.SettingsUtil;
import org.spoutcraft.launcher.SpoutFocusTraversalPolicy;
import org.spoutcraft.launcher.Util;
import org.spoutcraft.launcher.async.DownloadListener;
import org.spoutcraft.launcher.exception.AccountMigratedException;
import org.spoutcraft.launcher.exception.BadLoginException;
import org.spoutcraft.launcher.exception.MCNetworkException;
import org.spoutcraft.launcher.exception.MinecraftUserNotPremiumException;
import org.spoutcraft.launcher.exception.NoMirrorsAvailableException;
import org.spoutcraft.launcher.exception.OutdatedMCLauncherException;
import org.spoutcraft.launcher.gui.widget.ComboBoxRenderer;
import org.spoutcraft.launcher.modpacks.ModLibraryYML;
import org.spoutcraft.launcher.modpacks.ModPackListYML;
import org.spoutcraft.launcher.modpacks.ModPackUpdater;
import org.spoutcraft.launcher.modpacks.ModPackYML;

public class LoginForm extends JFrame implements ActionListener, DownloadListener, KeyListener, WindowListener {

	private static final long									serialVersionUID	= 1L;
	private final JPanel											contentPane;
	private final JPasswordField							passwordField;
	private final JComboBox										usernameField			= new JComboBox();
	private final JButton											loginButton				= new JButton("Login");
	JButton																		optionsButton			= new JButton("Options");
	JButton																		modsButton				= new JButton("Mod Select");
	private final JCheckBox										rememberCheckbox	= new JCheckBox("Remember");
	final JLabel															background				= new JLabel("Loading...");
	private final JButton											offlineMode				= new JButton("Offline Mode");
	private final JButton											tryAgain					= new JButton("Try Again");
	final JTextPane														editorPane				= new JTextPane();
	private final JButton											loginSkin1;
	private final List<JButton>								loginSkin1Image;
	private final JButton											loginSkin2;
	private final List<JButton>								loginSkin2Image;
	private TumblerFeedParsingWorker					tumblerFeed;
	public final JProgressBar									progressBar;
	HashMap<String, UserPasswordInformation>	usernames					= new HashMap<String, UserPasswordInformation>();
	public boolean														mcUpdate					= false;
	public boolean														spoutUpdate				= false;
	public boolean														modpackUpdate			= false;
	public static UpdateDialog								updateDialog;
	private static String											pass							= null;
	public static String[]										values						= null;
	private int																success						= LauncherFrame.ERROR_IN_LAUNCH;
	public String															workingDir				= PlatformUtils.getWorkingDirectory().getAbsolutePath();
	public static final ModPackUpdater				gameUpdater				= new ModPackUpdater();
	OptionDialog															options						= null;
	ModsDialog																mods							= new ModsDialog(ModPackYML.getModList());
	Container																	loginPane					= new Container();
	Container																	offlinePane				= new Container();
	// private final JLabel lblLogo;
	private final JComboBox<String>						modpackList;

	public LoginForm() {
		loadLauncherData();

		LoginForm.updateDialog = new UpdateDialog(this);
		gameUpdater.setListener(this);

		this.addWindowListener(this);

		loginButton.setFont(new Font("Arial", Font.PLAIN, 11));
		loginButton.setBounds(272, 13, 86, 23);
		loginButton.setOpaque(false);
		loginButton.addActionListener(this);
		loginButton.setEnabled(false); // disable until login info is read
		optionsButton.setFont(new Font("Arial", Font.PLAIN, 11));
		optionsButton.setOpaque(false);
		optionsButton.addActionListener(this);
		optionsButton.setEnabled(false);
		modsButton.setFont(new Font("Arial", Font.PLAIN, 11));
		modsButton.setOpaque(false);
		modsButton.addActionListener(this);
		usernameField.setFont(new Font("Arial", Font.PLAIN, 11));
		usernameField.addActionListener(this);
		usernameField.setOpaque(false);
		offlineMode.setFont(new Font("Arial", Font.PLAIN, 11));
		offlineMode.setOpaque(false);
		offlineMode.addActionListener(this);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((dim.width - 860) / 2, (dim.height - 500) / 2, 860, 500);

		contentPane = new JPanel();

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		// lblLogo = new JLabel("");
		// lblLogo.setBounds(8, 0, 294, 99);
		List<String> items = new ArrayList<String>();
		int i = 0;
		for (String item : ModPackListYML.modpackMap.keySet()) {
			if (!Main.isOffline || GameUpdater.canPlayOffline(item)) {
				items.add(item);
				i += 1;
			}
		}
		String[] itemArray = new String[i];
		modpackList = new JComboBox<String>(items.toArray(itemArray));
		modpackList.setBounds(10, 10, 328, 100);
		ComboBoxRenderer renderer = new ComboBoxRenderer();
		renderer.setPreferredSize(new Dimension(200, 110));
		modpackList.setRenderer(renderer);
		modpackList.setMaximumRowCount(3);
		modpackList.setSelectedItem(SettingsUtil.getModPackSelection());
		modpackList.addActionListener(this);

		JLabel lblMinecraftUsername = new JLabel("Minecraft Username: ");
		lblMinecraftUsername.setFont(new Font("Arial", Font.PLAIN, 11));
		lblMinecraftUsername.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMinecraftUsername.setBounds(-17, 17, 150, 14);

		JLabel lblPassword = new JLabel("Password: ");
		lblPassword.setFont(new Font("Arial", Font.PLAIN, 11));
		lblPassword.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPassword.setBounds(33, 42, 100, 20);

		passwordField = new JPasswordField();
		passwordField.setFont(new Font("Arial", Font.PLAIN, 11));
		passwordField.setBounds(143, 42, 119, 22);

		loginSkin1 = new JButton("Login as Player");
		loginSkin1.setFont(new Font("Arial", Font.PLAIN, 11));
		loginSkin1.setBounds(72, 428, 119, 23);
		loginSkin1.setOpaque(false);
		loginSkin1.addActionListener(this);
		loginSkin1.setVisible(false);
		loginSkin1Image = new ArrayList<JButton>();

		loginSkin2 = new JButton("Login as Player");
		loginSkin2.setFont(new Font("Arial", Font.PLAIN, 11));
		loginSkin2.setBounds(261, 428, 119, 23);
		loginSkin2.setOpaque(false);
		loginSkin2.addActionListener(this);
		loginSkin2.setVisible(false);
		loginSkin2Image = new ArrayList<JButton>();

		progressBar = new JProgressBar();
		progressBar.setBounds(30, 120, 400, 23);
		progressBar.setVisible(false);
		progressBar.setStringPainted(true);
		progressBar.setOpaque(true);

		JLabel purchaseAccount = new HyperlinkJLabel("<html><u>Need a minecraft account?</u></html>", "http://www.minecraft.net/register.jsp");
		purchaseAccount.setHorizontalAlignment(SwingConstants.RIGHT);
		purchaseAccount.setBounds(243, 70, 111, 14);

		purchaseAccount.setText("<html><u>Need an account?</u></html>");
		purchaseAccount.setFont(new Font("Arial", Font.PLAIN, 11));
		purchaseAccount.setForeground(new Color(0, 0, 255));

		JLabel wikiLink = new HyperlinkJLabel("<html><u>Technic WebSite</u></html>", "http://technicpack.net/");
		wikiLink.setHorizontalAlignment(SwingConstants.RIGHT);
		wikiLink.setBounds(233, 85, 109, 14);

		// wikiLink.setText();
		wikiLink.setFont(new Font("Arial", Font.PLAIN, 11));
		wikiLink.setForeground(new Color(0, 0, 255));

		usernameField.setBounds(143, 14, 119, 25);
		rememberCheckbox.setFont(new Font("Arial", Font.PLAIN, 11));

		rememberCheckbox.setOpaque(false);

		editorPane.setContentType("text/html");

		readUsedUsernames();

		editorPane.setEditable(false);
		editorPane.setOpaque(false);

		JLabel trans2;

		JScrollPane scrollPane = new JScrollPane(editorPane);
		scrollPane.setBounds(473, 11, 372, 340);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);

		editorPane.setCaretPosition(0);
		trans2 = new JLabel();
		trans2.setBackground(new Color(229, 246, 255, 100));
		trans2.setOpaque(true);
		trans2.setBounds(473, 11, 372, 340);

		JLabel login = new JLabel();
		login.setBackground(new Color(255, 255, 255, 120));
		login.setOpaque(true);
		login.setBounds(473, 362, 372, 99);

		JLabel trans;
		trans = new JLabel();
		trans.setBackground(new Color(229, 246, 255, 60));
		trans.setOpaque(true);
		trans.setBounds(0, 0, 854, 480);

		usernameField.getEditor().addActionListener(this);
		passwordField.addKeyListener(this);
		rememberCheckbox.addKeyListener(this);

		usernameField.setEditable(true);
		contentPane.setLayout(null);
		rememberCheckbox.setBounds(144, 66, 93, 23);
		// contentPane.add(lblLogo);
		contentPane.add(modpackList);
		optionsButton.setBounds(272, 41, 86, 23);
		modsButton.setBounds(15, 66, 93, 23);
		contentPane.add(loginSkin1);
		contentPane.add(loginSkin2);

		loginPane.setBounds(473, 362, 372, 106);
		loginPane.add(lblPassword);
		loginPane.add(lblMinecraftUsername);
		loginPane.add(passwordField);
		loginPane.add(usernameField);
		loginPane.add(loginButton);
		loginPane.add(rememberCheckbox);
		loginPane.add(purchaseAccount);
		loginPane.add(wikiLink);
		loginPane.add(optionsButton);
		loginPane.add(modsButton);
		contentPane.add(loginPane);

		JLabel offlineMessage = new JLabel("Could not connect to minecraft.net");
		offlineMessage.setFont(new Font("Arial", Font.PLAIN, 14));
		offlineMessage.setBounds(25, 40, 217, 17);

		tryAgain.setOpaque(false);
		tryAgain.setFont(new Font("Arial", Font.PLAIN, 12));
		tryAgain.setBounds(257, 20, 100, 25);

		offlineMode.setOpaque(false);
		offlineMode.setFont(new Font("Arial", Font.PLAIN, 12));
		offlineMode.setBounds(257, 52, 100, 25);

		offlinePane.setBounds(473, 362, 372, 99);
		// offlinePane.add(tryAgain);
		offlinePane.add(offlineMode);
		offlinePane.add(offlineMessage);
		offlinePane.setVisible(false);
		contentPane.add(offlinePane);

		contentPane.add(scrollPane);
		contentPane.add(trans2);
		contentPane.add(login);
		contentPane.add(trans);
		contentPane.add(progressBar);

		background.setVerticalAlignment(SwingConstants.CENTER);
		background.setHorizontalAlignment(SwingConstants.CENTER);
		background.setBounds(0, 0, 854, 480);
		contentPane.add(background);

		// TODO: remove this after next release
		(new File(PlatformUtils.getWorkingDirectory(), "launcher_cache.jpg")).delete();

		List<Component> order = new ArrayList<Component>(6);
		order.add(usernameField.getEditor().getEditorComponent());
		order.add(passwordField);
		order.add(rememberCheckbox);
		order.add(loginButton);
		order.add(optionsButton);
		order.add(modsButton);

		setFocusTraversalPolicy(new SpoutFocusTraversalPolicy(order));

		// loginButton.setEnabled(true); // enable once logins are read
		modsButton.setEnabled(false);
		setResizable(false);

		if (Main.isOffline) {
			offlinePane.setVisible(true);
			loginPane.setVisible(false);
		}
	}

	public void loadLauncherData() {
		MirrorUtils.updateMirrorsYMLCache();
		MD5Utils.updateMD5Cache();
		ModPackListYML.updateModPacksYMLCache();

		ModPackListYML.getAllModPackResources();
		ModPackListYML.loadModpackLogos();

		LibrariesYML.updateLibrariesYMLCache();
		ModLibraryYML.updateModLibraryYML();

		if (SettingsUtil.getModPackSelection() != null) {
			updateBranding();
		} else {
			setTitle("Technic Launcher - No Modpack Selected");
		}
	}

	public void updateBranding() {
		loginButton.setEnabled(false);
		optionsButton.setEnabled(false);
		setTitle("Loading Modpack Data...");
		SwingWorker<Object, Object> updateThread = new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				ModPackListYML.setCurrentModpack();
				return null;
			}

			@Override
			protected void done() {
				if (options == null) {
					options = new OptionDialog();
					options.modPackList = ModPackListYML.modpackMap;
					options.setVisible(false);
				}

				loginButton.setEnabled(true);
				optionsButton.setEnabled(true);
				setIconImage(Toolkit.getDefaultToolkit().getImage(ModPackYML.getModPackIcon()));
				setTitle(String.format("Technic Launcher - %s - (%s)", Main.build, ModPackListYML.currentModPackLabel));
				options.reloadSettings();
				MinecraftYML.updateMinecraftYMLCache();
				setModLoaderEnabled();
			}
		};
		updateThread.execute();
	}

	public void setModLoaderEnabled() {
		File modLoaderConfig = new File(GameUpdater.modconfigsDir, "ModLoader.cfg");
		boolean modLoaderExists = modLoaderConfig.exists();
		modsButton.setEnabled(modLoaderExists);
	}

	@Override
	public void stateChanged(String fileName, float progress) {
		int intProgress = Math.round(progress);

		if (intProgress >= 0) {
			progressBar.setValue(intProgress);
			progressBar.setIndeterminate(false);
		} else {
			progressBar.setIndeterminate(true);
		}

		fileName = fileName.replace(workingDir, "");
		if (fileName.contains("?")) {
			fileName = fileName.substring(0, fileName.indexOf("?"));
		}

		if (fileName.length() > 60) {
			fileName = fileName.substring(0, 60) + "...";
		}
		String progressText = intProgress + "% " + fileName;
		if (intProgress < 0) progressText = fileName;
		progressBar.setString(progressText);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (loginButton.isEnabled() && e.getKeyCode() == KeyEvent.VK_ENTER) {
			doLogin();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	private void tryConvertLastLogin() {
		try {
			File lastLogin = new File(PlatformUtils.getWorkingDirectory(), "lastlogin");
			Cipher cipher = getCipher(2, "passwordfile");
			DataInputStream dis;
			if (cipher != null) dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLogin), cipher));
			else dis = new DataInputStream(new FileInputStream(lastLogin));
			String userName = dis.readUTF();
			String password = dis.readUTF();

			dis.close();

			cipher = getCipher(1, "passwordfile");

			DataOutputStream dos;
			if (cipher != null) {
				dos = new DataOutputStream(new CipherOutputStream(new FileOutputStream(lastLogin), cipher));
			} else {
				dos = new DataOutputStream(new FileOutputStream(lastLogin, true));
			}
			dos.writeUTF(userName);
			dos.writeBoolean(false);
			dos.writeUTF(password);
			dos.close();
		} catch (Exception ignore) {

		}
	}

	private void readUsedUsernames() {
		int i = 0;
		try {
			File lastLogin = new File(PlatformUtils.getWorkingDirectory(), "lastlogin");
			if (!lastLogin.exists()) { return; }
			Cipher cipher = getCipher(2, "passwordfile");

			DataInputStream dis;
			if (cipher != null) {
				dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLogin), cipher));
			} else {
				dis = new DataInputStream(new FileInputStream(lastLogin));
			}

			try {
				// noinspection InfiniteLoopStatement
				while (true) {
					String user = dis.readUTF();
					boolean isHash = dis.readBoolean();
					if (isHash) {
						byte[] hash = new byte[32];
						dis.read(hash);

						usernames.put(user, new UserPasswordInformation(hash));
					} else {
						String password = dis.readUTF();
						if (!password.isEmpty()) {
							i++;
							if (i == 1) {
								// if (tumblerFeed != null) {
								TumblerFeedParsingWorker.setUser(user);
								// }
								if (!Main.isOffline) {
									loginSkin1.setText(user);
									loginSkin1.setVisible(true);
									ImageUtils.drawCharacter(contentPane, this, "http://s3.amazonaws.com/MinecraftSkins/" + user + ".png", 103, 170, loginSkin1Image);
								}
							} else if (i == 2) {
								if (!Main.isOffline) {
									loginSkin2.setText(user);
									loginSkin2.setVisible(true);
									ImageUtils.drawCharacter(contentPane, this, "http://s3.amazonaws.com/MinecraftSkins/" + user + ".png", 293, 170, loginSkin2Image);
								}
							}
						}
						usernames.put(user, new UserPasswordInformation(password));
					}
					this.usernameField.addItem(user);
				}
			} catch (EOFException ignored) {
			}
			dis.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		updatePasswordField();
	}

	private void writeUsernameList() {
		try {
			File lastLogin = new File(PlatformUtils.getWorkingDirectory(), "lastlogin");

			Cipher cipher = getCipher(1, "passwordfile");

			DataOutputStream dos;
			if (cipher != null) {
				dos = new DataOutputStream(new CipherOutputStream(new FileOutputStream(lastLogin), cipher));
			} else {
				dos = new DataOutputStream(new FileOutputStream(lastLogin, true));
			}
			for (String user : usernames.keySet()) {
				dos.writeUTF(user);
				UserPasswordInformation info = usernames.get(user);
				dos.writeBoolean(info.isHash);
				if (info.isHash) {
					dos.write(info.passwordHash);
				} else {
					dos.writeUTF(info.password);
				}
			}
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String eventId = event.getActionCommand();
		Object source = event.getSource();
		if (source == loginSkin1 || source == loginSkin2) {
			eventId = "Login";
			this.usernameField.setSelectedItem(((JButton) source).getText());
		} else if (loginSkin1Image.contains(source)) {
			eventId = "Login";
			this.usernameField.setSelectedItem(loginSkin1.getText());
		} else if (loginSkin2Image.contains(source)) {
			eventId = "Login";
			this.usernameField.setSelectedItem(loginSkin2.getText());
		}
		if ((source == modpackList)) {
			if (ModPackListYML.currentModPack == null) {
				SettingsUtil.init();
				GameUpdater.copy(SettingsUtil.settingsFile, ModPackListYML.ORIGINAL_PROPERTIES);
			} else {
				GameUpdater.copy(SettingsUtil.settingsFile, new File(GameUpdater.modpackDir, "launcher.properties"));
			}
			String selectedItem = (String) ((JComboBox) source).getSelectedItem();
			SettingsUtil.setModPack(selectedItem);
			updateBranding();
		}
		if ((eventId.equals("Login") || eventId.equals(usernameField.getSelectedItem())) && loginButton.isEnabled()) {
			doLogin();
		} else if (eventId.equals("Options")) {
			options.setVisible(true);
			options.setBounds((int) getBounds().getCenterX() - 250, (int) getBounds().getCenterY() - 75, 300, 325);
		} else if (eventId.equals(modsButton.getText())) {
			if (ModPackListYML.currentModPack != null) {
				open(new File(GameUpdater.modconfigsDir, "ModLoader.cfg"));
			}
		} else if (eventId.equals("comboBoxChanged")) {
			updatePasswordField();
		}

		if (source == offlineMode) {
			gameUpdater.user = "user";
			gameUpdater.downloadTicket = "0";
			offlineMode.setEnabled(false);
			tryAgain.setEnabled(false);
			runGame();
		}
	}

	public static void open(File document) {
		if (!document.exists()) return;
		try {
			Desktop dt = Desktop.getDesktop();
			dt.open(document);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updatePasswordField() {
		if (this.usernameField.getSelectedItem() != null) {
			UserPasswordInformation info = usernames.get(this.usernameField.getSelectedItem().toString());
			if (info != null) {
				if (info.isHash) {
					this.passwordField.setText("");
					this.rememberCheckbox.setSelected(false);
				} else {
					this.passwordField.setText(info.password);
					this.rememberCheckbox.setSelected(true);
				}
			}
		}
	}

	public void doLogin() {
		doLogin(usernameField.getSelectedItem().toString(), new String(passwordField.getPassword()), false);
	}

	public void doLogin(final String user, final String pass) {
		doLogin(user, pass, true);
	}

	public void doLogin(final String user, final String pass, final boolean cmdLine) {
		if (user == null || user.isEmpty() || pass == null || pass.isEmpty()) { return; }
		this.loginButton.setEnabled(false);
		this.optionsButton.setEnabled(false);
		this.modsButton.setEnabled(false);
		this.loginSkin1.setEnabled(false);
		this.loginSkin2.setEnabled(false);
		options.setVisible(false);
		SwingWorker<Boolean, Boolean> loginThread = new SwingWorker<Boolean, Boolean>() {

			@Override
			protected Boolean doInBackground() {
				progressBar.setVisible(true);
				progressBar.setString("Connecting to www.minecraft.net...");
				String password = pass.toString();
				try {
					values = MinecraftUtils.doLogin(user, pass, progressBar);
					return true;
				} catch (AccountMigratedException e) {
					JOptionPane.showMessageDialog(getParent(), "Account migrated, use e-mail as username");
					this.cancel(true);
					progressBar.setVisible(false);
				} catch (BadLoginException e) {
					JOptionPane.showMessageDialog(getParent(), "Incorrect usernameField/passwordField combination");
					this.cancel(true);
					progressBar.setVisible(false);
				} catch (MinecraftUserNotPremiumException e) {
					JOptionPane.showMessageDialog(getParent(), "You purchase a minecraft account to play");
					this.cancel(true);
					progressBar.setVisible(false);
				} catch (MCNetworkException e) {
					UserPasswordInformation info = null;

					for (String username : usernames.keySet()) {
						if (username.equalsIgnoreCase(user)) {
							info = usernames.get(username);
							break;
						}
					}

					boolean authFailed = (info == null);

					if (!authFailed) {
						if (info.isHash) {
							try {
								MessageDigest digest = MessageDigest.getInstance("SHA-256");
								byte[] hash = digest.digest(pass.getBytes());
								for (int i = 0; i < hash.length; i++) {
									if (hash[i] != info.passwordHash[i]) {
										authFailed = true;
										break;
									}
								}
							} catch (NoSuchAlgorithmException ex) {
								authFailed = true;
							}
						} else {
							authFailed = !(password.equals(info.password));
						}
					}

					if (authFailed) {
						JOptionPane.showMessageDialog(getParent(), "Unable to authenticate account with minecraft.net");
					} else {
						int result = JOptionPane.showConfirmDialog(getParent(), "Would you like to run in offline mode?", "Unable to Connect to Minecraft.net", JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.YES_OPTION) {
							values = new String[] { "0", "0", user, "0" };
							return true;
						}
					}
					this.cancel(true);
					progressBar.setVisible(false);
				} catch (OutdatedMCLauncherException e) {
					JOptionPane.showMessageDialog(getParent(), "Incompatible Login Version.");
					progressBar.setVisible(false);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					this.cancel(true);
					progressBar.setVisible(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
				enableUI();
				this.cancel(true);
				return false;
			}

			@Override
			protected void done() {
				if (values == null || values.length < 4) { return; }
				LoginForm.pass = pass;

				MessageDigest digest = null;

				try {
					digest = MessageDigest.getInstance("SHA-256");
				} catch (NoSuchAlgorithmException e) {
				}

				gameUpdater.user = usernameField.getSelectedItem().toString(); //values[2].trim();
				gameUpdater.downloadTicket = values[1].trim();
				if (!cmdLine) {
					String password = new String(passwordField.getPassword());
					if (rememberCheckbox.isSelected()) {
						usernames.put(gameUpdater.user, new UserPasswordInformation(password));
					} else {
						if (digest == null) {
							usernames.put(gameUpdater.user, new UserPasswordInformation(""));
						} else {
							usernames.put(gameUpdater.user, new UserPasswordInformation(digest.digest(password.getBytes())));
						}
					}
					writeUsernameList();
				}

				SwingWorker<Boolean, String> updateThread = new SwingWorker<Boolean, String>() {

					@Override
					protected Boolean doInBackground() throws Exception {
						publish("Checking for Minecraft Update...\n");
						try {
							mcUpdate = gameUpdater.checkMCUpdate();
						} catch (Exception e) {
							e.printStackTrace();
						}

						publish("Checking for Spoutcraft update...\n");
						try {
							spoutUpdate = gameUpdater.isSpoutcraftUpdateAvailable();
						} catch (Exception e) {
							e.printStackTrace();
						}

						publish(String.format("Checking for %s update...\n", ModPackListYML.currentModPackLabel));
						try {
							modpackUpdate = gameUpdater.isModpackUpdateAvailable();
						} catch (Exception e) {
							e.printStackTrace();
						}
						return true;
					}

					@Override
					protected void done() {
						if (modpackUpdate) {
							updateDialog.setToUpdate(ModPackListYML.currentModPackLabel);
						} else if (spoutUpdate) {
							updateDialog.setToUpdate("Spoutcraft");
						} else if (mcUpdate) {
							updateDialog.setToUpdate("Minecraft");
						}
						if (mcUpdate || spoutUpdate || modpackUpdate) {
							if (!GameUpdater.binDir.exists() || mcUpdate) {
								updateThread();
							} else {
								LoginForm.updateDialog.setVisible(true);
							}
						} else {
							runGame();
						}
						this.cancel(true);
					}

					@Override
					protected void process(List<String> chunks) {
						progressBar.setString(chunks.get(0));
					}
				};
				updateThread.execute();
				this.cancel(true);
			}
		};
		loginThread.execute();
	}

	public void updateThread() {
		SwingWorker<Boolean, String> updateThread = new SwingWorker<Boolean, String>() {

			boolean	error	= false;

			@Override
			protected void done() {
				progressBar.setVisible(false);
				// FileUtils.cleanDirectory(GameUpdater.tempDir);
				if (!error) {
					runGame();
				}
				this.cancel(true);
			}

			@Override
			protected Boolean doInBackground() throws Exception {
				try {
					if (mcUpdate) {
						gameUpdater.updateMC();
					}
					if (spoutUpdate) {
						gameUpdater.updateSpoutcraft();
					}
					if (modpackUpdate) {
						gameUpdater.updateModPackMods();
					}
				} catch (NoMirrorsAvailableException e) {
					JOptionPane.showMessageDialog(getParent(), "No Mirrors Are Available to download from!\nTry again later.");
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(getParent(), "Update Failed!");
					error = true;
					enableUI();
					this.cancel(true);
					return false;
				}
				return true;
			}

			@Override
			protected void process(List<String> chunks) {
				progressBar.setString(chunks.get(0));
			}
		};
		updateThread.execute();
	}

	public void enableUI() {
		loginButton.setEnabled(true);
		optionsButton.setEnabled(true);
		modsButton.setEnabled(true);
		loginSkin1.setEnabled(true);
		loginSkin2.setEnabled(true);
	}

	private Cipher getCipher(int mode, String password) throws Exception {
		Random random = new Random(43287234L);
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);

		SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(mode, pbeKey, pbeParamSpec);
		return cipher;
	}

	public void runGame() {
		if (ModPackListYML.currentModPack.equals("technicssp")) {
			File temp = new File(GameUpdater.modsDir, "industrialcraft-2-client_1.64.jar");
			if (temp.exists()) temp.delete();
		}

		LauncherFrame launcher = new LauncherFrame();
		launcher.setLoginForm(this);
		int result = (Main.isOffline) ? launcher.runGame(null, null, null, null) : launcher.runGame(values[2].trim(), values[3].trim(), values[1].trim(), pass);
		if (result == LauncherFrame.SUCCESSFUL_LAUNCH) {
			LoginForm.updateDialog.dispose();
			LoginForm.updateDialog = null;
			setVisible(false);
			Main.loginForm = null;

			dispose();
		} else if (result == LauncherFrame.ERROR_IN_LAUNCH) {
			loginButton.setEnabled(true);
			optionsButton.setEnabled(true);
			modsButton.setEnabled(true);
			loginSkin1.setEnabled(true);
			loginSkin2.setEnabled(true);
			progressBar.setVisible(false);
		}

		this.success = result;
		// Do nothing for retrying launch
	}

	@Override
	public void windowOpened(WindowEvent e) {
		tumblerFeed = new TumblerFeedParsingWorker(editorPane);
		tumblerFeed.execute();

		File cacheDir = new File(PlatformUtils.getWorkingDirectory(), "cache");
		cacheDir.mkdir();
		File backgroundImage = new File(cacheDir, "launcher_background.jpg");
		(new BackgroundImageWorker(backgroundImage, background)).execute();
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		if (success == LauncherFrame.ERROR_IN_LAUNCH) {
			Util.log("Exiting the Technic Launcher");
			System.exit(0);
		}
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	private static final class UserPasswordInformation {

		public boolean	isHash;
		public byte[]		passwordHash	= null;
		public String		password			= null;

		public UserPasswordInformation(String pass) {
			isHash = false;
			password = pass;
		}

		public UserPasswordInformation(byte[] hash) {
			isHash = true;
			passwordHash = hash;
		}
	}
}
