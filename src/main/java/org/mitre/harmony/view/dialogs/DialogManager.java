// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs;

import java.awt.Component;
import java.util.Stack;

import javax.swing.DefaultDesktopManager;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.mitre.harmony.view.harmonyPane.HarmonyFrame;

/**
 * Manages the display of dialogs in Harmony (which are really internal frames)
 * @author CWOLF
 */
public class DialogManager extends DefaultDesktopManager implements InternalFrameListener
{
	/** Stores reference to the HarmonyFrame */
	private HarmonyFrame harmonyFrame = null;
	
	/** Stores a stack of displayed dialogs */
	private Stack<JInternalFrame> displayedDialogs = new Stack<JInternalFrame>();
	
	/** Enable/disable the Harmony menu bar */
	private void setMenuBarEnabled(boolean enabled)
	{
		JMenuBar menuBar = harmonyFrame.getJMenuBar();
		for(int i=0; i<menuBar.getMenuCount(); i++)
			menuBar.getMenu(i).setEnabled(enabled);
	}
	
	/** Constructs the HarmonyFrame manager */
	public DialogManager(HarmonyFrame harmonyFrame)
		{ this.harmonyFrame = harmonyFrame; }

	/** Returns the currently displayed dialog */
	public JInternalFrame getDialog()
		{ return displayedDialogs.peek(); }
	
	/** Shows a dialog (internal frame) on top of the mapping pane */
	public void openDialog(JInternalFrame dialog)
	{
		Component parent = displayedDialogs.size()==0 ? harmonyFrame : displayedDialogs.peek();
		
		// Disable the parent dialog and menu bar (cause modal type behavior)
		if(displayedDialogs.size()>0)
		{
			try { displayedDialogs.peek().setSelected(false); } catch(Exception e) {}
			displayedDialogs.peek().setEnabled(false);
		}
		else setMenuBarEnabled(false);
		displayedDialogs.push(dialog);
		
		// Calculate the shift
		Integer xShift=parent.getX(), yShift=parent.getY();
		Component base = parent;
		while(!(base.equals(harmonyFrame)))
		{
			base = base.getParent();
			xShift += base.getX(); yShift += base.getY();
		}
		
		if(dialog!=null)
		{
			// Place the dialog
			Integer x = xShift + (parent.getWidth()-dialog.getWidth())/2;
			Integer y = yShift + (parent.getHeight()-dialog.getHeight())/2;
			dialog.setLocation(x, y);
	
			// Display the dialog
			harmonyFrame.add(dialog,JLayeredPane.POPUP_LAYER);
			dialog.addInternalFrameListener(this);
			try { dialog.setSelected(true); } catch(Exception e) {}
		}
	}

	/** Closes the topmost dialog */
	private void closeDialog()
	{
		JInternalFrame dialog = displayedDialogs.pop();
		if(dialog!=null) dialog.removeInternalFrameListener(this);
		if(displayedDialogs.size()>0)
		{
			displayedDialogs.peek().setEnabled(true);
			try { displayedDialogs.peek().setSelected(true); } catch(Exception e2) {}
		}
		else setMenuBarEnabled(true);
	}
	
	/** Locks the top dialog */
	public void lockFrame()
		{ openDialog(null); }
	
	/** Unlocks the top dialog */
	public void unlockFrame()
		{ closeDialog(); }

	/** Only activate if no dialog has higher priority */
	public void activateFrame(JInternalFrame dialog)
	{
		if(dialog.equals(displayedDialogs.peek())) super.activateFrame(dialog);
		else try { dialog.setSelected(false); } catch(Exception e) {}
	}

	/** Closes the topmost dialog */
	public void internalFrameClosed(InternalFrameEvent e)
		{ closeDialog(); }
	
	// Unused event listeners
	public void internalFrameOpened(InternalFrameEvent e) {}
	public void internalFrameClosing(InternalFrameEvent e) {}
	public void internalFrameIconified(InternalFrameEvent e) {}
	public void internalFrameDeiconified(InternalFrameEvent e) {}
	public void internalFrameActivated(InternalFrameEvent e) {}
	public void internalFrameDeactivated(InternalFrameEvent e) {}
}