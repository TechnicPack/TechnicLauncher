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
package org.spoutcraft.launcher.skin.options;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

import net.technicpack.launchercore.install.InstalledPack;
import net.technicpack.launchercore.restful.PackInfo;
import net.technicpack.launchercore.restful.PlatformConstants;
import net.technicpack.launchercore.restful.RestObject;
import net.technicpack.launchercore.restful.platform.PlatformPackInfo;
import net.technicpack.launchercore.restful.solder.SolderPackInfo;
import net.technicpack.launchercore.util.ResourceUtils;
import org.spoutcraft.launcher.Launcher;
import org.spoutcraft.launcher.skin.LauncherFrame;
import org.spoutcraft.launcher.skin.components.ImageButton;
import org.spoutcraft.launcher.skin.components.LiteButton;
import org.spoutcraft.launcher.skin.components.LiteTextBox;

import net.technicpack.launchercore.util.ZipUtils;
import net.technicpack.launchercore.util.Utils;

public class ImportOptions extends JDialog implements ActionListener, MouseListener, MouseMotionListener, DocumentListener {
	private static final long serialVersionUID = 1L;
	private static final String QUIT_ACTION = "quit";
	private static final String IMPORT_ACTION = "import";
	private static final String CHANGE_FOLDER = "folder";
	private static final String PASTE_URL = "paste";
	private static final String ESCAPE_ACTION = "escape";
	private static final int FRAME_WIDTH = 520;
	private static final int FRAME_HEIGHT = 222;

	private JLabel msgLabel;
	private JLabel background;
	private LiteButton save;
	private LiteButton folder;
	private LiteButton paste;
	private LiteTextBox install;
	private JFileChooser fileChooser;
	private int mouseX = 0, mouseY = 0;
	private PackInfo info = null;
	private String url = "";
	private Document urlDoc;
	private File installDir;
	private LiteTextBox urlTextBox;

	public ImportOptions() {
		setTitle("Add a Pack");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		addMouseListener(this);
		addMouseMotionListener(this);
		setResizable(false);
		setUndecorated(true);
		initComponents();
	}

	public void initComponents() {
		Font minecraft = LauncherFrame.getMinecraftFont(12);

		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, ESCAPE_ACTION);
		getRootPane().getActionMap().put(ESCAPE_ACTION, escapeAction);

		background = new JLabel();
		background.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
		LauncherFrame.setIcon(background, "platformBackground.png", background.getWidth(), background.getHeight());

		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		ImageButton optionsQuit = new ImageButton(ResourceUtils.getIcon("quit.png", 28, 28), ResourceUtils.getIcon("quit.png", 28, 28));
		optionsQuit.setRolloverIcon(ResourceUtils.getIcon("quitHover.png", 28, 28));
		optionsQuit.setBounds(FRAME_WIDTH - 38, 10, 28, 28);
		optionsQuit.setActionCommand(QUIT_ACTION);
		optionsQuit.addActionListener(this);

		msgLabel = new JLabel();
		msgLabel.setBounds(10, 75, FRAME_WIDTH - 20, 25);
		msgLabel.setText("Enter your Technic Platform delivery URL below to add a new pack:");
		msgLabel.setForeground(Color.white);
		msgLabel.setFont(minecraft);

		urlTextBox = new LiteTextBox(this, "Paste Platform URL Here");
		urlTextBox.setBounds(10, msgLabel.getY() + msgLabel.getHeight() + 5, FRAME_WIDTH - 115, 30);
		urlTextBox.setFont(minecraft);
		urlTextBox.getDocument().addDocumentListener(this);
		urlDoc = urlTextBox.getDocument();

		save = new LiteButton("Add Modpack");
		save.setFont(minecraft.deriveFont(14F));
		save.setBounds(FRAME_WIDTH - 145, FRAME_HEIGHT - 40, 135, 30);
		save.setActionCommand(IMPORT_ACTION);
		save.addActionListener(this);

		fileChooser = new JFileChooser(Utils.getLauncherDirectory());
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		folder = new LiteButton("Change Folder");
		folder.setFont(minecraft.deriveFont(14F));
		folder.setBounds(FRAME_WIDTH - 290, FRAME_HEIGHT - 40, 135, 30);
		folder.setActionCommand(CHANGE_FOLDER);
		folder.addActionListener(this);

		paste = new LiteButton("Paste");
		paste.setFont(minecraft.deriveFont(14F));
		paste.setBounds(FRAME_WIDTH - 95, msgLabel.getY() + msgLabel.getHeight() + 5, 85, 30);
		paste.setActionCommand(PASTE_URL);
		paste.addActionListener(this);
		paste.setVisible(true);

		install = new LiteTextBox(this, "");
		install.setBounds(10, FRAME_HEIGHT - 75, FRAME_WIDTH - 20, 25);
		install.setFont(minecraft.deriveFont(10F));
		install.setEnabled(false);
		install.setVisible(false);

		enableComponent(save, false);
		enableComponent(folder, false);
		enableComponent(paste, true);

		contentPane.add(install);
		contentPane.add(optionsQuit);
		contentPane.add(msgLabel);
		contentPane.add(folder);
		contentPane.add(paste);
		contentPane.add(urlTextBox);
		contentPane.add(save);
		contentPane.add(background);

		setLocationRelativeTo(this.getOwner());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComponent) {
			action(e.getActionCommand(), (JComponent) e.getSource());
		}
	}

	private void action(String action, JComponent c) {
		if (action.equals(QUIT_ACTION)) {
			dispose();
		} else if (action.equals(CHANGE_FOLDER)) {
			int result = fileChooser.showOpenDialog(this);

			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();

				if (!ZipUtils.checkEmpty(file)) {
					install.setText("Please select an empty directory.");
					return;
				}

				if (info.shouldForceDirectory() && file.getAbsolutePath().startsWith(Utils.getSettingsDirectory().getAbsolutePath())) {
					install.setText("This pack requires a directory outside of " + Utils.getSettingsDirectory().getAbsolutePath());
					return;
				}
				installDir = file;

				install.setText("Location: " + installDir.getPath());
				folder.setText("Change Folder");
				folder.setLocation(FRAME_WIDTH - 290, FRAME_HEIGHT - 40);
				enableComponent(save, true);
			}
		} else if (action.equals(IMPORT_ACTION)) {
			if (info != null) {
				InstalledPack pack = new InstalledPack(info.getName(), true);
				pack.setRefreshListener(Launcher.getInstance());
				pack.setInfo(info);
				Launcher.getFrame().getSelector().addPack(pack);
				dispose();
			}
		} else if (action.equals(PASTE_URL)) {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable clipData = clipboard.getContents(clipboard);
			if (clipData != null) {
				try {
					if (clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
						String s = (String) (clipData.getTransferData(DataFlavor.stringFlavor));
						urlDoc.remove(0, urlDoc.getLength());
						urlDoc.insertString(0, s, new SimpleAttributeSet());
						urlTextBox.setLabelVisible(false);
					}
				} catch (UnsupportedFlavorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void urlUpdated(Document doc) {
		try {
			final String url = doc.getText(0, doc.getLength()).trim();

			if (url.isEmpty()) {
				msgLabel.setText("Enter your Technic Platform delivery URL below to add a new pack:");
				enableComponent(save, false);
				enableComponent(folder, false);
				enableComponent(install, false);
				info = null;
				this.url = "";
				return;
			}

			if (matchUrl(url)) {
				msgLabel.setText("Attempting to fetch Modpack info...");
				// Turn everything off while the data is being fetched
				enableComponent(urlTextBox, false);
				enableComponent(paste, false);
				enableComponent(install, false);
				enableComponent(folder, false);
				enableComponent(save, false);
				// fetch the info asynchronously
				SwingWorker<PlatformPackInfo, Void> worker = new SwingWorker<PlatformPackInfo, Void>() {

					@Override
					protected PlatformPackInfo doInBackground() throws Exception {
						return RestObject.getRestObject(PlatformPackInfo.class, url);
					}

					@Override
					public void done() {
						try {
							PlatformPackInfo result = get();
							if (!result.hasSolder() && !(result.getUrl().startsWith("http://") || result.getUrl().startsWith("https://"))) {
								msgLabel.setText("Modpack has invalid download link. Consult modpack author.");
								return;
							} else {
								if (result.hasSolder()) {
									info = SolderPackInfo.getSolderPackInfo(result.getSolder(), result.getName());
								} else {
									info = result;
								}
							}
							System.out.println(info.getName());
							msgLabel.setText("Modpack: " + info.getDisplayName());
							ImportOptions.this.url = url;
							enableComponent(folder, true);
							enableComponent(install, true);
							if (info.shouldForceDirectory()) {
								install.setText("This pack requires a directory outside of " + Utils.getSettingsDirectory().getAbsolutePath());
								folder.setText("Select");
								folder.setLocation(FRAME_WIDTH - 145, FRAME_HEIGHT - 40);
								enableComponent(save, false);
							} else {
								installDir = new File(Utils.getLauncherDirectory(), info.getName());
								install.setText("Location: " + installDir.getPath());
								enableComponent(save, true);
							}
						} catch (ExecutionException e) {
							msgLabel.setText("Error parsing platform response");
							enableComponent(save, false);
							enableComponent(folder, false);
							enableComponent(install, false);
							info = null;
							ImportOptions.this.url = "";
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Interrupted exception?
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
							msgLabel.setText("Modpack has invalid solder link. Consult modpack author.");
						} finally {
							// always turn these back on
							enableComponent(urlTextBox, true);
							enableComponent(paste, true);
						}
					}
				};
				worker.execute();
			} else {
				msgLabel.setText("Invalid Technic Platform delivery URL");
				enableComponent(save, false);
				enableComponent(folder, false);
				enableComponent(install, false);
				info = null;
				this.url = "";
			}

		} catch (BadLocationException e) {
			// This should never ever happen.
			// Java is stupid for not having a getAllText of some kind on the
			// Document class
			e.printStackTrace();
		}
	}

	public boolean matchUrl(String url) {
		boolean result = false;
		result = (url.matches(PlatformConstants.MODPACK + "([a-zA-Z0-9-]+)") || result);
		return result;
	}

	public void enableComponent(JComponent component, boolean enable) {
		component.setEnabled(enable);
		component.setVisible(enable);
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

	@Override
	public void changedUpdate(DocumentEvent e) {
		urlUpdated(e.getDocument());
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		urlUpdated(e.getDocument());
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		urlUpdated(e.getDocument());
	}
}
