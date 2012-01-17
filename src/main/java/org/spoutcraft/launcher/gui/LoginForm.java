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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.spoutcraft.launcher.*;
import org.spoutcraft.launcher.async.DownloadListener;
import org.spoutcraft.launcher.exception.*;
import org.spoutcraft.launcher.technic.TechnicUpdater;

public class LoginForm extends JFrame implements ActionListener, DownloadListener, KeyListener, WindowListener {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPasswordField passwordField;
	private JComboBox usernameField = new JComboBox();
	private JButton loginButton = new JButton("Login");
	JButton optionsButton = new JButton("Options");
	JButton modsButton = new JButton("Mods");
	private JCheckBox rememberCheckbox = new JCheckBox("Remember");
	final JLabel background = new JLabel("Loading...");
	final JTextPane editorPane = new JTextPane();
	private JButton loginSkin1;
	private List<JButton> loginSkin1Image;
	private JButton loginSkin2;
	private List<JButton> loginSkin2Image;
	private TumblerFeedParsingWorker tumblerFeed;
	public final JProgressBar progressBar;
	HashMap<String, UserPasswordInformation> usernames = new HashMap<String, UserPasswordInformation>();
	public boolean mcUpdate = false;
	public boolean spoutUpdate = false;
	public boolean technicUpdate = false;
	public static UpdateDialog updateDialog;
	private static String pass = null;
	public static String[] values = null;
	private int success = LauncherFrame.ERROR_IN_LAUNCH;
	public String modpackFilename = ModPacksYML.getModPacks().get(SettingsUtil.getModPackSelection()).get("filename");
	public String modpackName = ModPacksYML.getModPacks().get(SettingsUtil.getModPackSelection()).get("name");
	
	public String workingDir = PlatformUtils.getWorkingDirectory().getAbsolutePath();

	public static final TechnicUpdater gameUpdater = new TechnicUpdater();
	OptionDialog options = new OptionDialog();
	ModsDialog mods = new ModsDialog(ModsYML.getTechnicMods());

	
	Container loginPane = new Container();
	Container offlinePane = new Container();

	public LoginForm() {
		SwingWorker<Object, Object> updateThread = new SwingWorker<Object, Object>() {
			protected Object doInBackground() throws Exception {
				MinecraftYML.updateMinecraftYMLCache();
				SpoutcraftYML.updateSpoutcraftYMLCache();
				LibrariesYML.updateLibrariesYMLCache();
				ModsYML.updateModsYMLCache();
				ModPacksYML.updateModPacksYMLCache();
				TechnicUpdater.updateTechnicModsYML();
				return null;
			}
			
			protected void done() {
				options.updateBuildsList();
				options.updateModPackList();
			}
		};		
		updateThread.execute();
		
		LoginForm.updateDialog = new UpdateDialog(this);
		gameUpdater.setListener(this);
		
		this.addWindowListener(this);

		options.setVisible(false);
		loginButton.setFont(new Font("Arial", Font.PLAIN, 11));
		loginButton.setBounds(272, 13, 86, 23);
		loginButton.setOpaque(false);
		loginButton.addActionListener(this);
		loginButton.setEnabled(false); //disable until login info is read
		optionsButton.setFont(new Font("Arial", Font.PLAIN, 11));
		optionsButton.setOpaque(false);
		optionsButton.addActionListener(this);
		modsButton.setFont(new Font("Arial", Font.PLAIN, 11));
		modsButton.setOpaque(false);
		modsButton.addActionListener(this);
		usernameField.setFont(new Font("Arial", Font.PLAIN, 11));
		usernameField.addActionListener(this);
		usernameField.setOpaque(false);
		if(modpackFilename != null)
			setIconImage(Toolkit.getDefaultToolkit().getImage(LoginForm.class.getResource("/org/spoutcraft/launcher/" + modpackFilename.toString() + "_favicon.png")));
		else
			setIconImage(Toolkit.getDefaultToolkit().getImage(LoginForm.class.getResource("/org/spoutcraft/launcher/technic_favicon.png")));
		setResizable(false);

		if(modpackName == null)
			setTitle("Technic Launcher");
		else
			setTitle(modpackName.toString() + " Launcher");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((dim.width - 860) / 2, (dim.height - 500) / 2, 860, 500);

		contentPane = new JPanel();

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblLogo = new JLabel("");
		lblLogo.setBounds(8, 0, 294, 99);
		if(modpackFilename != null)
			lblLogo.setIcon(new ImageIcon(LoginForm.class.getResource("/org/spoutcraft/launcher/" + modpackFilename.toString() + "_logo.png")));
		else
			lblLogo.setIcon(new ImageIcon(LoginForm.class.getResource("/org/spoutcraft/launcher/technic_logo.png")));
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
		progressBar.setBounds(30, 100, 400, 23);
		progressBar.setVisible(false);
		progressBar.setStringPainted(true);
		progressBar.setOpaque(true);

		

		JLabel purchaseAccount = new HyperlinkJLabel("<html><u>Need a minecraft account?</u></html>", "http://www.minecraft.net/register.jsp");
		purchaseAccount.setHorizontalAlignment(SwingConstants.RIGHT);
		purchaseAccount.setBounds(243, 70, 111, 14);

		purchaseAccount.setText("<html><u>Need an account?</u></html>");
		purchaseAccount.setFont(new Font("Arial", Font.PLAIN, 11));
		purchaseAccount.setForeground(new Color(0, 0, 255));
		
		JLabel wikiLink = new HyperlinkJLabel("<html><u>Technic Wiki</u></html>", "http://wiki.technicpack.net/index.php?title=Main_Page");
		wikiLink.setHorizontalAlignment(SwingConstants.RIGHT);
		wikiLink.setBounds(233, 85, 109, 14);
		
		wikiLink.setText("<html><u>Technic Wiki</u></html>");
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
		contentPane.add(lblLogo);
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

		JButton tryAgain = new JButton("Try Again");
		tryAgain.setOpaque(false);
		tryAgain.setFont(new Font("Arial", Font.PLAIN, 12));
		tryAgain.setBounds(257, 20, 100, 25);

		JButton offlineMode = new JButton("Offline Mode");
		offlineMode.setOpaque(false);
		offlineMode.setFont(new Font("Arial", Font.PLAIN, 12));
		offlineMode.setBounds(257, 52, 100, 25);

		offlinePane.setBounds(473, 362, 372, 99);
		offlinePane.add(tryAgain);
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
		
		//TODO: remove this after next release
		(new File(PlatformUtils.getWorkingDirectory(), "launcher_cache.jpg")).delete();
		

		Vector<Component> order = new Vector<Component>(6);
		order.add(usernameField.getEditor().getEditorComponent());
		order.add(passwordField);
		order.add(rememberCheckbox);
		order.add(loginButton);
		order.add(optionsButton);
		order.add(modsButton);

		setFocusTraversalPolicy(new SpoutFocusTraversalPolicy(order));
		
		loginButton.setEnabled(true); //enable once logins are read
		modsButton.setEnabled(false);
	}

	public void stateChanged(String fileName, float progress) {
		int intProgress = Math.round(progress);

		progressBar.setValue(intProgress);
		
		fileName = fileName.replace(workingDir, ".technic");
		
		if (fileName.length() > 60) {
			fileName = fileName.substring(0, 60) + "...";
		}
		progressBar.setString(intProgress + "% " + fileName);
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		if (loginButton.isEnabled() && e.getKeyCode() == KeyEvent.VK_ENTER) {
			doLogin();
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	private void readUsedUsernames() {
		int i = 0;
		try {
			File lastLogin = new File(PlatformUtils.getWorkingDirectory(), "lastlogin");
			if (!lastLogin.exists())
				return;
			Cipher cipher = getCipher(2, "passwordfile");

			DataInputStream dis;
			if (cipher != null)
				dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLogin), cipher));
			else {
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
						String pass = dis.readUTF();
						if (!pass.isEmpty()) {
							i++;
							if (i == 1) {
								if (tumblerFeed != null) tumblerFeed.setUser(user);
								loginSkin1.setText(user);
								loginSkin1.setVisible(true);
								ImageUtils.drawCharacter(contentPane, this, "http://s3.amazonaws.com/MinecraftSkins/" + user + ".png", 103, 170, loginSkin1Image);
							} else if (i == 2) {
								loginSkin2.setText(user);
								loginSkin2.setVisible(true);
								ImageUtils.drawCharacter(contentPane, this, "http://s3.amazonaws.com/MinecraftSkins/" + user + ".png", 293, 170, loginSkin2Image);
							}
						}
						usernames.put(user, new UserPasswordInformation(pass));
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
			if (cipher != null)
				dos = new DataOutputStream(new CipherOutputStream(new FileOutputStream(lastLogin), cipher));
			else {
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

	public void actionPerformed(ActionEvent event) {
		String eventId = event.getActionCommand();
		if (event.getSource() == loginSkin1 || event.getSource() == loginSkin2) {
			eventId = "Login";
			this.usernameField.setSelectedItem(((JButton) event.getSource()).getText());
		} else if(loginSkin1Image.contains(event.getSource())) {
			eventId = "Login";
			this.usernameField.setSelectedItem(loginSkin1.getText());
		} else if(loginSkin2Image.contains(event.getSource())) {
			eventId = "Login";
			this.usernameField.setSelectedItem(loginSkin2.getText());
		}
		if ((eventId.equals("Login") || eventId.equals(usernameField.getSelectedItem())) && loginButton.isEnabled()) {
			doLogin();
		} else if (eventId.equals("Options")) {
			options.setVisible(true);
			options.setBounds((int) getBounds().getCenterX() - 250, (int) getBounds().getCenterY() - 75, 300, 325);
		} else if (eventId.equals("Mods")) {
			mods.setVisible(true);
		} else if (eventId.equals("comboBoxChanged")) {
			updatePasswordField();
		}
	}

	private void updatePasswordField() {
		if (this.usernameField.getSelectedItem() != null){
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
		if (user == null || pass == null) {
			return;
		}
		this.loginButton.setEnabled(false);
		this.optionsButton.setEnabled(false);
		this.modsButton.setEnabled(false);
		this.loginSkin1.setEnabled(false);
		this.loginSkin2.setEnabled(false);
		options.setVisible(false);
		SwingWorker<Boolean, Boolean> loginThread = new SwingWorker<Boolean, Boolean>() {

			protected Boolean doInBackground() throws Exception {
				progressBar.setVisible(true);
				progressBar.setString("Connecting to www.minecraft.net...");
				try {
					values = MinecraftUtils.doLogin(user, pass, progressBar);
					return true;
				} catch (BadLoginException e) {
					JOptionPane.showMessageDialog(getParent(), "Incorrect usernameField/passwordField combination");
					this.cancel(true);
					progressBar.setVisible(false);
				} catch (MinecraftUserNotPremiumException e){
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
							}
							catch (NoSuchAlgorithmException ex) {
								authFailed = true;
							}
						} else {
							authFailed = !(pass.equals(info.password));
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
				loginButton.setEnabled(true);
				optionsButton.setEnabled(true);
				modsButton.setEnabled(true);
				loginSkin1.setEnabled(true);
				loginSkin2.setEnabled(true);
				this.cancel(true);
				return false;
			}

			protected void done() {
				if (values == null || values.length < 4)
					return;
				LoginForm.pass = pass;

				MessageDigest digest = null;

				try {
					digest = MessageDigest.getInstance("SHA-256");
				} catch (NoSuchAlgorithmException e) {
				}

				gameUpdater.user = values[2].trim();
				gameUpdater.downloadTicket = values[1].trim();
				if (cmdLine == false) {
					String password = new String(passwordField.getPassword());
					if (rememberCheckbox.isSelected()) {
						usernames.put(gameUpdater.user, new UserPasswordInformation(password));
					}
					else {
						if (digest == null) {
							usernames.put(gameUpdater.user, new UserPasswordInformation(""));
						}
						else {
							usernames.put(gameUpdater.user, new UserPasswordInformation(digest.digest(password.getBytes())));
						}
					}
					writeUsernameList();
				}

				SwingWorker<Boolean, String> updateThread = new SwingWorker<Boolean, String>() {

					protected Boolean doInBackground() throws Exception {
						publish("Checking for Minecraft Update...\n");
						try {
							mcUpdate = gameUpdater.checkMCUpdate();
						} catch (Exception e) {
							mcUpdate = false;
						}

						publish("Checking for Spoutcraft update...\n");
						try {
							spoutUpdate = mcUpdate || gameUpdater.isSpoutcraftUpdateAvailable();
						} catch (Exception e) {
							spoutUpdate = false;
						}
						
						publish("Checking for Technic update...\n");
						try {
							technicUpdate = spoutUpdate || gameUpdater.isTechnicUpdateAvailable();
						} catch (Exception e) {
							technicUpdate = false;
						}
						return true;
					}
					
					protected void done() {
						if (mcUpdate) {
							updateDialog.setToUpdate("Minecraft");
						} else if (spoutUpdate) {
							updateDialog.setToUpdate("Spoutcraft");
						} else if (technicUpdate) {
							updateDialog.setToUpdate("Technic");
						}
						if (mcUpdate || spoutUpdate || technicUpdate) {
							if (!GameUpdater.binDir.exists())
								updateThread();
							else
								LoginForm.updateDialog.setVisible(true);
						} else {
							runGame();
						}
						this.cancel(true);
					}

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
			boolean error = false;
			protected void done() {
				progressBar.setVisible(false);
				if (!error){
					runGame();
				}
				this.cancel(true);
			}

			protected Boolean doInBackground() throws Exception {
				try {
					if (mcUpdate) {
						gameUpdater.updateMC();
					}

					if (spoutUpdate) {
						gameUpdater.updateSpoutcraft();
					}
					if (technicUpdate) {
//						gameUpdater.updateTechnic();
						gameUpdater.updateTechnicMods();
					}
				}
				catch (NoMirrorsAvailableException e) {
					JOptionPane.showMessageDialog(getParent(), "No Mirrors Are Available to download from!\nTry again later.");
				}
				catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(getParent(), "Download Interrupted!");
					error = true;
					loginButton.setEnabled(true);
					optionsButton.setEnabled(true);
					modsButton.setEnabled(true);
					loginSkin1.setEnabled(true);
					loginSkin2.setEnabled(true);
					this.cancel(true);
					return false;
				}
				return true;
			}

			protected void process(List<String> chunks) {
				progressBar.setString(chunks.get(0));
			}

		};
		updateThread.execute();
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
		LauncherFrame launcher = new LauncherFrame();
		launcher.setLoginForm(this);
		int result = launcher.runGame(values[2].trim(), values[3].trim(), values[1].trim(), pass);
		if (result == LauncherFrame.SUCCESSFUL_LAUNCH) {
			LoginForm.updateDialog.dispose();
			LoginForm.updateDialog = null;
			setVisible(false);
			dispose();
		}
		else if (result == LauncherFrame.ERROR_IN_LAUNCH){
			loginButton.setEnabled(true);
			optionsButton.setEnabled(true);
			modsButton.setEnabled(true);
			loginSkin1.setEnabled(true);
			loginSkin2.setEnabled(true);
			progressBar.setVisible(false);
		}
		
		this.success = result;
		//Do nothing for retrying launch
	}
	
	public void windowOpened(WindowEvent e) {
		tumblerFeed = new TumblerFeedParsingWorker(editorPane);
		tumblerFeed.execute();

		File cacheDir = new File(PlatformUtils.getWorkingDirectory(), "cache");
		cacheDir.mkdir();
		File backgroundImage = new File(cacheDir, "launcher_background.jpg");
		(new BackgroundImageWorker(backgroundImage, background)).execute();
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
		if (success == LauncherFrame.ERROR_IN_LAUNCH) {
			System.out.println("Exiting the Technic Launcher");
			System.exit(0);
		}
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}
	
	private static final class UserPasswordInformation {
		public boolean isHash;
		public byte[] passwordHash = null;
		public String password = null;

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

