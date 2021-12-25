// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.mappingPane;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.Vector;

import javax.swing.tree.TreePath;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.schemaTree.SchemaTree;

/**
 * Displays either the source or target schema tree (with offsets)
 * @author CWOLF
 */
public class SchemaTreeImp extends SchemaTree
{
	Point offset;			// Tracks the offset of the schema tree pane
	int firstVisibleRow;	// Tracks the first visible schema tree row
	int lastVisibleRow;		// Tracks the last visible schema tree row
	
	/** Initializes the schema tree implementation */
	public SchemaTreeImp(Integer role, HarmonyModel harmonyModel)
		{ super(role, harmonyModel); }
	
	/** Retrieves the buffered row bounds */
	public Rectangle getBufferedRowBounds(int row)
	{
		Rectangle rect = new Rectangle(super.getBufferedRowBounds(row));
		rect.translate(offset.x,offset.y);
		return rect;
	}
	
	/** Retrieves the requested path bounds */
	public Rectangle getPthBounds(TreePath path)
	{ 	
		Rectangle rect = new Rectangle(getPathBounds(path)); 
		rect.translate(offset.x,offset.y);
		return rect; 
	}

	/** Returns the path associated with the specified location */
	public TreePath getPathForLoc(int x, int y)
		{ return super.getPathForLocation(x-offset.x,y-offset.y); }
	
	/** Returns the closest row to the specified location */
	int getClosestRowForLocation(Point2D point)
		{ return super.getClosestRowForLocation((int)point.getX()-offset.x,(int)point.getY()-offset.y); }
	
	//-------------------------------------------------
	// Purpose: Handles mouse events on the schema tree
	//-------------------------------------------------
	private Vector<MouseListener> mouseListeners = new Vector<MouseListener>();
	public void addMouseListenr(MouseListener listener) { mouseListeners.add(listener); }
	public void removeMouseListenr(MouseListener listener) { mouseListeners.remove(listener); }
	public void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
		e.translatePoint(offset.x,offset.y);
		for (MouseListener listener : mouseListeners) {
			switch(e.getID()) {
				case MouseEvent.MOUSE_CLICKED: listener.mouseClicked(e); break;
				case MouseEvent.MOUSE_PRESSED: listener.mousePressed(e); break;
				case MouseEvent.MOUSE_RELEASED: listener.mouseReleased(e); break;
				case MouseEvent.MOUSE_ENTERED: listener.mouseEntered(e); break;
				case MouseEvent.MOUSE_EXITED: listener.mouseExited(e); break;
			}
		}
	}

	//--------------------------------------------------------
	// Purpose: Handles mouse motion events on the schema tree
	//--------------------------------------------------------
	private Vector<MouseMotionListener> mouseMotionListeners = new Vector<MouseMotionListener>();
	public void addMouseMotionListenr(MouseMotionListener listener) { mouseMotionListeners.add(listener); }
	public void removeMouseMotionListenr(MouseMotionListener listener) { mouseMotionListeners.remove(listener); }
	public void processMouseMotionEvent(MouseEvent e) {
		super.processMouseMotionEvent(e);
		e.translatePoint(offset.x,offset.y);
		for(MouseMotionListener listener : mouseMotionListeners) {
			switch(e.getID()) {
				case MouseEvent.MOUSE_DRAGGED: listener.mouseDragged(e);
				case MouseEvent.MOUSE_MOVED: listener.mouseMoved(e);
			}
		}
	}
}
