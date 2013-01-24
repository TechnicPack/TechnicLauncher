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
/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010, 2011 Albert Pham <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.spoutcraft.launcher.skin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.commons.io.IOUtils;

import org.spoutcraft.launcher.skin.components.LoginFrame;
import org.spoutcraft.launcher.util.Compatibility;

/**
 * Console dialog for showing console messages.
 *
 * @author sk89q
 *
 * This code reused & relicensed as LGPL v 3 with permission.
 */
public class ConsoleFrame extends JFrame implements MouseListener{
	private static final long serialVersionUID = 1L;
	private static final Logger rootLogger = Logger.getLogger("launcher");
	private Process trackProc;
	private Handler loggerHandler;
	private JTextComponent textComponent;
	private Document document;
	private int numLines;
	private boolean colorEnabled = false;
	private final SimpleAttributeSet defaultAttributes = new SimpleAttributeSet();
	private final SimpleAttributeSet highlightedAttributes;
	private final SimpleAttributeSet errorAttributes;
	private final SimpleAttributeSet infoAttributes;
	private final SimpleAttributeSet debugAttributes;

	/**
	 * Construct the frame.
	 *
	 * @param numLines number of lines to show at a time
	 * @param colorEnabled true to enable a colored console
	 */
	public ConsoleFrame(int numLines, boolean colorEnabled) {
		this(numLines, colorEnabled, null, false);
	}

	/**
	 * Construct the frame.
	 *
	 * @param numLines number of lines to show at a time
	 * @param colorEnabled true to enable a colored console
	 * @param trackProc process to track
	 * @param killProcess true to kill the process on console close
	 */
	public ConsoleFrame(int numLines, boolean colorEnabled, final Process trackProc, final boolean killProcess) {
		super("Technic Launcher Console");
		this.numLines = numLines;
		this.colorEnabled = colorEnabled;
		this.trackProc = trackProc;

		this.highlightedAttributes = new SimpleAttributeSet();
		StyleConstants.setForeground(highlightedAttributes, Color.BLACK);
		StyleConstants.setBackground(highlightedAttributes, Color.YELLOW);

		this.errorAttributes = new SimpleAttributeSet();
		StyleConstants.setForeground(errorAttributes, new Color(200, 0, 0));
		this.infoAttributes = new SimpleAttributeSet();
		StyleConstants.setForeground(infoAttributes, new Color(200, 0, 0));
		this.debugAttributes = new SimpleAttributeSet();
		StyleConstants.setForeground(debugAttributes, Color.DARK_GRAY);

		setSize(new Dimension(650, 400));
		buildUI();

		Compatibility.setIconImage(this, Toolkit.getDefaultToolkit().getImage(LoginFrame.spoutcraftIcon));

		if (trackProc != null) {
			track(trackProc);
		}

		addMouseListener(this);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				if (trackProc != null && killProcess) {
					trackProc.destroy();
					if (loggerHandler != null) {
						rootLogger.removeHandler(loggerHandler);
					}
					event.getWindow().dispose();
				}
			}
		});
	}

	/**
	 * Build the interface.
	 */
	private void buildUI() {
		if (colorEnabled) {
			JTextPane text = new JTextPane();
			this.textComponent = text;
		} else {
			JTextArea text = new JTextArea();
			this.textComponent = text;
			text.setLineWrap(true);

		}
		textComponent.addMouseListener(this);
		textComponent.setFont(getMonospaceFont());
		textComponent.setEditable(false);
		DefaultCaret caret = (DefaultCaret) textComponent.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		document = textComponent.getDocument();
		document.addDocumentListener(new LimitLinesDocumentListener(numLines, true));

		JScrollPane scrollText = new JScrollPane(textComponent);
		scrollText.setBorder(null);
		scrollText.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		add(scrollText, BorderLayout.CENTER);
	}

	/**
	 * Log a message.
	 *
	 * @param line line
	 */
	public void log(String line) {
		log(line, null);
	}

	/**
	 * Log a message given the {@link AttributeSet}.
	 *
	 * @param line line
	 * @param attributes attribute set, or null for none
	 */
	public void log(String line, AttributeSet attributes) {
		if (colorEnabled) {
			if (line.startsWith("(!!)")) {
				attributes = highlightedAttributes;
			}
		}

		try {
			int offset = document.getLength();
			document.insertString(offset, line,	(attributes != null && colorEnabled) ? attributes : defaultAttributes);
			textComponent.setCaretPosition(document.getLength());
		} catch (BadLocationException ble) {
		} catch (NullPointerException npe) {
		}
	}

	/**
	 * Get an output stream that can be written to.
	 *
	 * @return output stream
	 */
	public ConsoleOutputStream getOutputStream() {
		return getOutputStream((AttributeSet) null);
	}

	/**
	 * Get an output stream with the given attribute set.
	 *
	 * @param attributes attributes
	 * @return output stream
	 */
	public ConsoleOutputStream getOutputStream(AttributeSet attributes) {
		return new ConsoleOutputStream(attributes);
	}

	/**
	 * Get an output stream using the give color.
	 *
	 * @param color color to use
	 * @return output stream
	 */
	public ConsoleOutputStream getOutputStream(Color color) {
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setForeground(attributes, color);
		return getOutputStream(attributes);
	}

	/**
	 * Consume an input stream and print it to the dialog. The consumer
	 * will be in a separate daemon thread.
	 *
	 * @param from stream to read
	 */
	public void consume(InputStream from) {
		consume(from, getOutputStream());
	}

	/**
	 * Consume an input stream and print it to the dialog. The consumer
	 * will be in a separate daemon thread.
	 *
	 * @param from stream to read
	 * @param color color to use
	 */
	public void consume(InputStream from, Color color) {
		consume(from, getOutputStream(color));
	}

	/**
	 * Consume an input stream and print it to the dialog. The consumer
	 * will be in a separate daemon thread.
	 *
	 * @param from stream to read
	 * @param attributes attributes
	 */
	public void consume(InputStream from, AttributeSet attributes) {
		consume(from, getOutputStream(attributes));
	}

	/**
	 * Internal method to consume a stream.
	 *
	 * @param from stream to consume
	 * @param outputStream console stream to write to
	 */
	private void consume(InputStream from, ConsoleOutputStream outputStream) {
		final InputStream in = from;
		final PrintWriter out = new PrintWriter(outputStream, true);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] buffer = new byte[1024];
				try {
					int len;
					while ((len = in.read(buffer)) != -1) {
						String s = new String(buffer, 0, len);
						System.out.print(s);
						out.append(s);
						out.flush();
					}
				} catch (IOException e) {
				} finally {
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(out);
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Track a process in a separate daemon thread.
	 *
	 * @param process process
	 */
	private void track(Process process) {
		final PrintWriter out = new PrintWriter(getOutputStream(Color.MAGENTA), true);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int code = trackProc.waitFor();
					out.println("Process ended with code " + code);
				} catch (InterruptedException e) {
					out.println("Process tracking interrupted!");
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Registera global logger listener.
	 */
	public void registerLoggerHandler() {
		for (Handler handler : rootLogger.getHandlers()) {
			rootLogger.removeHandler(handler);
		}

		loggerHandler = new ConsoleLoggerHandler();
		rootLogger.addHandler(loggerHandler);
	}

	/**
	 * Used to send console messages to the console.
	 */
	public class ConsoleOutputStream extends ByteArrayOutputStream {
		private AttributeSet attributes;

		private ConsoleOutputStream(AttributeSet attributes) {
			this.attributes = attributes;
		}

		@Override
		public void flush() {
			String data = toString();
			if (data.length() == 0) return;
			log(data, attributes);
			reset();
		}
	}

	/**
	 * Used to send logger messages to the console.
	 */
	private class ConsoleLoggerHandler extends Handler {
		@Override
		public void publish(LogRecord record) {
			Level level = record.getLevel();
			Throwable t = record.getThrown();
			AttributeSet attributes = defaultAttributes;

			if (level.intValue() >= Level.WARNING.intValue()) {
				attributes = errorAttributes;
			} else if (level.intValue() < Level.INFO.intValue()) {
				attributes = debugAttributes;
			}

			log(record.getMessage() + "\n", attributes);
			if (t != null) {
				log(getStackTrace(t) + "\n", attributes);
			}
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() throws SecurityException {
		}
	}

	private static String[] monospaceFontNames = {"Consolas", "DejaVu Sans Mono", "Bitstream Vera Sans Mono", "Lucida Console"};

	/**
	 * Get a supported monospace font.
	 *
	 * @return font
	 */
	public static Font getMonospaceFont() {
		for (String fontName : monospaceFontNames) {
			Font font = Font.decode(fontName + "-11");
			if (!font.getFamily().equalsIgnoreCase("Dialog")) {
				return font;
			}
		}
		return new Font("Monospace", Font.PLAIN, 11);
	}

	/**
	 * Get a stack trace as a string.
	 *
	 * @param t exception
	 * @return stack trace
	 */
	public static String getStackTrace(Throwable t) {
		Writer result = new StringWriter();
		try {
			PrintWriter printWriter = new PrintWriter(result);
			t.printStackTrace(printWriter);
		} finally {
			IOUtils.closeQuietly(result);
		}
		return result.toString();
	}

	@Override
	public void mousePressed(MouseEvent e){
		if (e.isPopupTrigger()) {
			doPop(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e){
		if (e.isPopupTrigger()) {
			doPop(e);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	private void doPop(MouseEvent e){
		ContextMenu menu = new ContextMenu();
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	private class ContextMenu extends JPopupMenu {
		private static final long serialVersionUID = 1L;
		JMenuItem copy;
		JMenuItem clear;
		public ContextMenu() {
			copy = new JMenuItem("Copy");
			add(copy);
			copy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					textComponent.copy();
				}
			});

			clear = new JMenuItem("Clear");
			add(clear);
			clear.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					textComponent.setText("");
				}
			});
		}
	}
}
