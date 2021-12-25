// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.schemaTree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.filters.Focus;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.model.SchemaModel;
import org.mitre.schemastore.search.SchemaSearchResult;

/**
 * Renders schema tree node
 * @author CWOLF
 */
class SchemaTreeRenderer extends DefaultTreeCellRenderer
{
	// Fonts used for displaying schema tree node text
	static private Font ital = new Font("Dialog", Font.ITALIC,12);
	static private Font bold = new Font("Dialog", Font.BOLD, 12);
	
    // Icons used in displaying schema tree nodes
	static private Icon schemaIcon = getIcon("Schema.jpg");
	static private Icon schemaElementIcon = getIcon("SchemaElement.jpg");
	static private Icon finishedSchemaElementIcon = getIcon("FinishedSchemaElement.jpg");
	static private Icon attributeIcon = getIcon("Attribute.jpg");
	static private Icon finishedAttributeIcon = getIcon("FinishedAttribute.jpg");
	static private Icon hiddenRootIcon = getIcon("HiddenRoot.gif");
	static private Icon hiddenElementIcon = getIcon("HiddenElement.gif");
	static private Icon containmentIcon = getIcon("Containment.jpg");
	static private Icon finishedContainmentIcon = getIcon("FinishedContainment.jpg");
	static private Icon domainValueIcon = getIcon("DomainValue.jpg");
	static private Icon finishedDomainValueIcon = getIcon("FinishedDomainValue.jpg");
	static private Icon RelationshipIcon = getIcon("Relationship.jpg");
	
	
	/** Defines highlight color used in displaying highlighted schema tree nodes */
	static private Color nameHighlightColor = new Color(0xFFFF66);
	static private Color descHighlightColor = new Color(0xFFFFb3);

	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Retrieve the specified icon */
	static private Icon getIcon(String name)
	{
		URL url = SchemaTreeRenderer.class.getResource("/org/mitre/org.org.mitre.harmony/view/graphics/"+name);
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(url));
	}
	
	/** Constructs the Schema Tree renderer */
	public SchemaTreeRenderer(HarmonyModel harmonyModel)
		{ this.harmonyModel = harmonyModel; }
	
	/** Returns the rendered schema tree nodes */
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean highlighted, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		// Keeps track of various display states of node
		boolean isSelected = false;
		boolean isFocused = true;
		boolean isFinished = false;
		
		// Keeps track if match for search
		SchemaSearchResult result = null;
		
		// Retrieves the object to be rendered
		Object obj = ((DefaultMutableTreeNode)value).getUserObject();
		
		// Handle root nodes, the most simple case
		if(row == 0)
		{
			JPanel schemaLabelPane = new JPanel();
			schemaLabelPane.setBackground(Color.white);
			schemaLabelPane.add(new JLabel("Schemas"));
			return schemaLabelPane;
		}
		
		// Handles rendering of schema element nodes
		else if (obj instanceof Integer) 
		{
			// Gets schema node and associated schema tree
			Integer elementID = (Integer)obj;
			SchemaElement element = harmonyModel.getSchemaManager().getSchemaElement(elementID);
			SchemaTree schemaTree = (SchemaTree)tree;
			
			// Get the schema element domain
			Integer schemaID = SchemaTree.getSchema((DefaultMutableTreeNode)value);
			HierarchicalSchemaInfo schemaInfo = harmonyModel.getSchemaManager().getSchemaInfo(schemaID);
			Domain domain = schemaInfo.getDomainForElement(element.getId());

			// Sets the tool tip
			String tooltip = element.getDescription() + ((domain != null) ? " (Domain: " + domain.getName() + ")" : "");
			setToolTipText(tooltip);

			// Determine the display state of the node
			isFocused = harmonyModel.getFilters().isVisibleNode(schemaTree.getSide(),(DefaultMutableTreeNode)value);
			isSelected = harmonyModel.getSelectedInfo().isElementSelected(elementID,schemaTree.getSide());
			isFinished = harmonyModel.getPreferences().isFinished(schemaID,elementID);
			
			// Determine if the element is marked as hidden
			Focus focus = harmonyModel.getFilters().getFocus(schemaTree.getSide(), schemaID);
			boolean isHiddenRoot = focus!=null && focus.getHiddenIDs().contains(elementID);
			boolean isHiddenElement = focus!=null && focus.getHiddenElements().contains(elementID);
			
			// Set the text
			String name = schemaInfo.getDisplayName(element.getId());
			String text = "<html>" + name.replace("<","&lt;").replace(">","&gt;");
			if (element instanceof Attribute) {
				if (((Attribute)element).isKey()) {
					text += " <font color='#888888'>{K}</font>";
				}
			}
			if(harmonyModel.getPreferences().getShowSchemaTypes())
			{
				String typeString = null;
				SchemaElement type = schemaInfo.getType(schemaInfo, element.getId());
				if(type!=null) typeString =schemaInfo.getDisplayName(type.getId()).trim();
			//	String typeString = schemaInfo.getTypeString(schemaInfo,element.getId());
				if(typeString!=null) text += " <font color='#888888'>(" + typeString + ")</font>";
			}
			if (harmonyModel.getPreferences().getShowCardinality())
			{
				if (element instanceof Containment) {
					Containment c = (Containment)element;
					text += getCardinalityText(c.getMin(), c.getMax());
					
				}
				else if (element instanceof Attribute) {
					Attribute a = (Attribute)element;
					text += getCardinalityText(a.getMin(), a.getMax());
				}
				
			}
			text += "</html>";
			setText(text);
			
			// Set the icon
			if(isHiddenRoot) setIcon(hiddenRootIcon);
			else if(isHiddenElement) setIcon(hiddenElementIcon);
			else if(domain!=null && element instanceof Attribute) setIcon(isFinished ? finishedAttributeIcon : attributeIcon);
			else if ( element instanceof Containment) setIcon( isFinished ? finishedContainmentIcon: containmentIcon );  
			else if ( element instanceof DomainValue) setIcon( isFinished ? finishedDomainValueIcon: domainValueIcon );  
			else if ( element instanceof Relationship) setIcon( RelationshipIcon );  
			else setIcon(isFinished ? finishedSchemaElementIcon : schemaElementIcon);

			// Retrieves if element is match for current search
			result = harmonyModel.getSearchManager().getResult(elementID, schemaTree.getSide());
		}
		
		// Handles any other rows that need rendering
		else
		{
			// Determine if the node is in focus
			SchemaTree schemaTree = (SchemaTree)tree;
			if(obj instanceof Schema)
				isFocused = harmonyModel.getFilters().inFocus(schemaTree.getSide(),(DefaultMutableTreeNode)value);
			else isFocused = true;
			
			// Set the text and icon
			String text = " " + obj;
			if(obj instanceof Schema)
			{
				Schema schema = (Schema)obj;
				SchemaModel model = harmonyModel.getSchemaManager().getSchemaInfo(schema.getId()).getModel();
				text = "<html>" + schema.getName().replace("<","&lt;").replace(">","&gt;");
				text += " <font color='#0000ff'>(" + model.getName() + ")</font>";
				text += "</html>";
			}
			setText(text);
			setIcon(schemaIcon);
		}

		// Adjust the settings based on the state of the node
		selected = isFocused && isSelected;
		setFont(isFocused ? bold : ital);
		
		// Sets the node background color
		Color color = Color.white;
		if(result!=null && (harmonyModel.getSearchManager().getHighlightAll() || isFocused))
			color = result.nameMatched() ? nameHighlightColor : descHighlightColor;
		setBackgroundNonSelectionColor(color);
		
		// Returns the rendered node
		return this;
	}
	private String getCardinalityText(Integer min, Integer max) {
		String s = "";
		int mn = min==null?1:min;
		int mx = max==null?1:max;
		if (mn!=mx || mn != 1) {
			s += " <font color='#888888'>[";
			if (mn==mx) {
				s +=  mn;
			}
			else {
				s += mn + ".." + ((mx==-1)?"n":mx);
			}
			s += "] </font>";
		}
		return s;
	}
}