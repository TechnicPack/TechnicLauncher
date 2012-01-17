package org.spoutcraft.launcher.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import javax.swing.JTextPane;
import javax.swing.SwingWorker;

import org.spoutcraft.launcher.MirrorUtils;

public class TumblerFeedParsingWorker extends SwingWorker<Object, Object>{
	JTextPane editorPane;
	private String username = null;
	public TumblerFeedParsingWorker(JTextPane editorPane) {
		this.editorPane = editorPane;
	}
	
	public void setUser(String name) {
		username = name;
	}

	@Override
	protected Object doInBackground() {
		URL url = null;
		try {
			url = new URL("http://urcraft.com/technic/changelog/");
			
			if (MirrorUtils.isAddressReachable(url.toString())) {
				editorPane.setVisible(false);
				editorPane.setPage(url);
				try {
					Thread.sleep(5000);
				}
				catch (InterruptedException e) { }
				
				String text = editorPane.getText();
				
				int index = text.indexOf("<!-- BEGIN TUMBLR CODE -->");
				int endIndex = text.indexOf("<!-- END TUMBLR CODE -->") + "<!-- END TUMBLR CODE -->".length();
				if (index > -1 && endIndex > -1) {
					text = text.substring(0, index) + text.substring(endIndex);
				}
				text = text.replaceAll("<li>", "- ");
				text = text.replaceAll("</li>", "<br/>");
				text = text.replaceAll("<p>", "");
				text = text.replace("</p>", "<br/>");
				text = text.replaceAll("</p>", "<br/><br/>");
				text = text.replaceAll("@time_of_day", getTimeOfDay());
				text = text.replaceAll("@username", getUsername());
				editorPane.setText(text);
				editorPane.setVisible(true);
			}
			else {
				editorPane.setText("Oh Noes! Our Tumblr Feed is Down!");
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			editorPane.setText("Oh Noes! Our Tumblr Server is Down!");
			System.out.println(String.format("Tumbler log @ '%' not avaliable.", url));
		}

		return null;
	}
	
	private String getUsername() {
		return username != null ? username : "Player";
	}
	
	private String getTimeOfDay() {
		int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if (hours < 6)
			return "Night";
		if (hours < 12)
			return "Morning";
		if (hours < 14)
			return "Day";
		if (hours < 18)
			return "Afternoon";
		if (hours < 22) {
			return "Evening";
		}
		return "Night";
	}

}
