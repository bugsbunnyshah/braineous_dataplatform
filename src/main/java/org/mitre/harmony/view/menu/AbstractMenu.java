// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.menu;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/** Create a abstract menu class */
abstract class AbstractMenu extends JMenu
{
	/** Constructs the menu */
	protected AbstractMenu(String label)
		{ super(label); }
	
	/** Creates a menu item */
	protected JMenuItem createMenuItem(String name, int mnemonic, Action action)
	{
		JMenuItem item = new JMenuItem(action);
		item.setText(name);
		item.setMnemonic(mnemonic);
		return item;
	}
	
	/** Creates a checkbox menu item */
	protected JMenuItem createCheckboxItem(String name, boolean selected, Action action)
	{
		JMenuItem item = new JCheckBoxMenuItem(action);
		item.setText(name);
		item.setSelected(selected);
		return item;
	}
}