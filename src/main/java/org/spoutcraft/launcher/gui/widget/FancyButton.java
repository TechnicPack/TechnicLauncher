package org.spoutcraft.launcher.gui.widget;

import javax.swing.Icon;
import javax.swing.JRadioButton;

@SuppressWarnings("serial")
public class FancyButton extends JRadioButton {
	public FancyButton(Icon icon, Icon pressed, Icon rollover) {
		super(icon);
		setFocusPainted(false);
		setRolloverEnabled(true);
		setRolloverIcon(rollover);
		setPressedIcon(pressed);
		setSelectedIcon(pressed);
		setBorderPainted(false);
		setContentAreaFilled(false);
	}
}