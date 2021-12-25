package org.mitre.harmony.view.dialogs.widgets;

import javax.swing.JLabel;

/** Displays an underlined label */
public class UnderlinedLabel extends JLabel
{
	/** Constructs the underlined label */
	public UnderlinedLabel(String text)
		{ super("<html><u>"+text+"</u></html>"); setOpaque(false); }
}
