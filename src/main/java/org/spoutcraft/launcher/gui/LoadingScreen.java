package org.spoutcraft.launcher.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LoadingScreen extends JDialog {
	public JPanel			contentPane;
	public JPanel			closePanel;
	public JPanel			main;
	private ImageIcon	closeNormal;
	private JLabel		closeLabel;

	public LoadingScreen() {
		initialize();
		showWindow();
	}

	private void initialize() {
		// don't show a frame or title bar
		setUndecorated(true);
		setBounds(0, 0, 732, 224);

		// Create JPanel and set it as the content pane
		contentPane = new JPanel();
		setContentPane(contentPane);

		// If main has not already been created, create it.
		// Explained later
		if (main == null) {
			main = new JPanel();
		}

		// Create panel for close button
		closePanel = new JPanel(new BorderLayout());
	}

	@Override
	public JPanel getContentPane() {
		return main;
	}

	@Override
	public Component add(Component comp) {
		return main.add(comp);
	}

	@Override
	public void setLayout(LayoutManager manager) {
		if (main == null) {
			main = new JPanel();
			main.setLayout(new FlowLayout());
		} else {
			main.setLayout(manager);
		}

		if (!(getLayout() instanceof BorderLayout)) {
			super.setRootPaneCheckingEnabled(false);
			super.setLayout(new BorderLayout());
			super.setRootPane(super.getRootPane());
			super.setRootPaneCheckingEnabled(true);
		}
	}

	private void showWindow() {
		// If not set, default to FlowLayout
		if (main.getLayout() == null) {
			setLayout(new FlowLayout());
		}

		// close "button" - show this image by default
		closeNormal = new ImageIcon(getClass().getResource("/org/spoutcraft/launcher/splash_logo.png"));
		closeLabel = new JLabel(closeNormal);

		// Put the label with the image on the far right
		closePanel.add(closeLabel, BorderLayout.WEST);

		// Add the two panels to the content pane
		contentPane.setLayout(new BorderLayout());
		contentPane.add(closePanel, BorderLayout.NORTH);
		contentPane.add(main, BorderLayout.CENTER);

		// set raised beveled border for window
		contentPane.setBorder(BorderFactory.createRaisedBevelBorder());

		// Set position somewhere near the middle of the screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width / 2 - (getWidth() / 2), screenSize.height / 2 - (getHeight() / 2));

		// keep window on top of others
		setAlwaysOnTop(false);
	}

	public void close() {
		setVisible(false);

		dispose();
	}
}
