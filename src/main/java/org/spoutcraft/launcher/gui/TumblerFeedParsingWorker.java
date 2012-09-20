package org.spoutcraft.launcher.gui;

import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;

import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.spoutcraft.launcher.MirrorUtils;
import org.spoutcraft.launcher.Util;

public class TumblerFeedParsingWorker extends SwingWorker<Object, Object> implements PropertyChangeListener {
	JTextPane							editorPane;
	private static String	username		= null;
	boolean								isUpdating	= false;

	public TumblerFeedParsingWorker(JTextPane editorPane) {
		this.editorPane = editorPane;
	}

	public static void setUser(String name) {
		username = name;
		// updatePage();
	}

	@Override
	protected Object doInBackground() {
		URL url = null;
		try {
			url = new URL("http://alexhulbert.com/AtexFeed.htm");

			if (MirrorUtils.isAddressReachable(url.toString())) {
				editorPane.setVisible(false);
				editorPane.setContentType("text/html");
				// editorPane.setEditable(false);
				ToolTipManager.sharedInstance().registerComponent(editorPane);
				editorPane.addHyperlinkListener(new HyperlinkListener() {
					@Override
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (HyperlinkEvent.EventType.ACTIVATED == e.getEventType()) {
							try {
								if (Desktop.isDesktopSupported()) {
									Desktop.getDesktop().browse(e.getURL().toURI());
								}
							} catch (IOException e1) {
								e1.printStackTrace();
							} catch (URISyntaxException e1) {
								e1.printStackTrace();
							}
						}
					}
				});

				editorPane.addPropertyChangeListener(this);
				editorPane.setPage(url);
			} else {
				editorPane.setText("Oh Noes! Our Tumblr Feed is Down!");
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			editorPane.setText("Oh Noes! Our Tumblr Server is Down!");
			Util.log("Tumbler log @ '%' not avaliable.", url);
		}

		return null;
	}

	private String getUsername() {
		return username != null ? username : "Player";
	}

	private String getTimeOfDay() {
		int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if (hours < 6) return "Night";
		if (hours < 12) return "Morning";
		if (hours < 14) return "Day";
		if (hours < 18) return "Afternoon";
		if (hours < 22) { return "Evening"; }
		return "Night";
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!isUpdating && evt.getPropertyName().equals("page")) {
			updatePage();
		}
	}

	private void updatePage() {
		isUpdating = true;
		// HTMLDocument htmlDocument = (HTMLDocument) evt.getNewValue();
		String text = editorPane.getText();
		text = text.replaceAll("@time_of_day", getTimeOfDay());
		text = text.replaceAll("@username", getUsername());
		editorPane.setText(text);
		editorPane.setVisible(true);
		isUpdating = false;
	}
}
