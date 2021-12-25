// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.mappingCell;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.mitre.schemastore.model.MappingCell;

/**
 * Displays the annotation information about the mapping cell
 * @author CWOLF
 */
public class MappingCellAnnotationPane extends JPanel
{
	/** String for indicating that multiple values exist for the particular field */
	static final private String MULTIPLE_VALUES = "<Multiple Values>";
	
	/** Stores the mapping cells being annotated */
	private List<MappingCell> mappingCells = null;
	
	// Components used within the Mapping Cell dialog
	private JTextField dateField;
	private JTextField authorField;
	private JTextArea noteField;
	private ArrayList<JTextField> extraFields = new ArrayList<JTextField>();
	
	/** Returns the merged annotation fields */
	private AnnotationFields getMergedAnnotationFields(List<MappingCell> mappingCells)
	{
		// Initial the annotation fields
		AnnotationFields fields = new AnnotationFields(mappingCells.get(0));

		// Check to see if the annotations for all mapping cells match
		for(MappingCell mappingCell : mappingCells)
		{
			AnnotationFields newFields = new AnnotationFields(mappingCell);

			// Adjust the basic annotation fields
			if(!fields.getDate().equals(newFields.getDate())) fields.setDate(MULTIPLE_VALUES);
			if(!fields.getAuthor().equals(newFields.getAuthor())) fields.setAuthor(MULTIPLE_VALUES);
			if(!fields.getNote().equals(newFields.getNote())) fields.setNote(MULTIPLE_VALUES);
				
			// Adjust the extra annotation fields
			for(String field : fields.getExtraFields())
				if(!fields.getExtraField(field).equals(newFields.getExtraField(field)))
					fields.setExtraField(field, MULTIPLE_VALUES);
		}

		return fields;
	}

	/** Generates the labels pane */
	private JPanel getLabelsPane(AnnotationFields fields)
	{
		// Class for displaying a label
		class LabelPane extends JPanel
		{
			private LabelPane(String label)
				{ setLayout(new BorderLayout()); add(new JLabel(label),BorderLayout.EAST); }
		}
		
		// Generates the label panes
		JPanel labelPane = new JPanel();
		labelPane.setLayout(new GridLayout(2+fields.getExtraFields().size(),1));
		labelPane.add(new LabelPane("Date: "));
		labelPane.add(new LabelPane("Author: "));
		for(String field : fields.getExtraFields())
			labelPane.add(new LabelPane(field + ": "));
		return labelPane;
	}
	
	/** Generates the fields pane */
	private JPanel getFieldsPane(AnnotationFields fields)
	{
		// Construct the date field
		dateField = new JTextField(fields.getDate()); dateField.setMargin(new Insets(1,1,1,1));
		dateField.setEnabled(false);
		dateField.setBorder(BorderFactory.createEtchedBorder(Color.white,Color.gray));
		dateField.setDisabledTextColor(Color.gray);

		// Construct the author field
		authorField = new JTextField(fields.getAuthor()); authorField.setMargin(new Insets(1,1,1,1));
		authorField.setCaretPosition(0);

		// Construct the extra fields
		for(String field : fields.getExtraFields())
		{
			JTextField extraField = new JTextField(fields.getExtraField(field)); 
			extraField.setMargin(new Insets(1,1,1,1));
			extraFields.add(extraField);
		}

		// Build a pane with all of the annotation field boxes
		JPanel fieldPane = new JPanel();
		fieldPane.setLayout(new GridLayout(2+fields.getExtraFields().size(),1));
		fieldPane.add(dateField);
		fieldPane.add(authorField);
		for(JTextField extraField : extraFields) fieldPane.add(extraField);
		fieldPane.setPreferredSize(new Dimension(200,0));
		return fieldPane;
	}
	
	/** Generate the notes pane */
	private JPanel getNotesPane(AnnotationFields fields)
	{
		// Initialize annotation fields
		noteField = new JTextArea(fields.getNote()); noteField.setMargin(new Insets(1,1,1,1));
		noteField.setRows(5);
		noteField.setLineWrap(true);
		noteField.setWrapStyleWord(true);

		// Build a pane with the notes annotation
		JPanel notesPane = new JPanel();
		notesPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		notesPane.setLayout(new BorderLayout());
		notesPane.add(new JLabel("Notes:"),BorderLayout.NORTH);
		notesPane.add(new JScrollPane(noteField,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
		return notesPane;
	}
	
	/** Constructs the mapping cell annotations pane */
	MappingCellAnnotationPane(List<MappingCell> mappingCells)
	{
		this.mappingCells = mappingCells;
		
		// Gets the merged annotation fields
		AnnotationFields fields = getMergedAnnotationFields(mappingCells);
		
		// Build a pane for the annotations
		JPanel annotationsPane = new JPanel();
		annotationsPane.setBorder(BorderFactory.createEmptyBorder(0,3,3,3));
		annotationsPane.setLayout(new BorderLayout());
		annotationsPane.add(getLabelsPane(fields),BorderLayout.WEST);
		annotationsPane.add(getFieldsPane(fields),BorderLayout.CENTER);
		
		// Merge together all annotation
		setBorder(BorderFactory.createTitledBorder("Annotations"));
		setLayout(new BorderLayout());
		add(annotationsPane,BorderLayout.NORTH);
		add(getNotesPane(fields),BorderLayout.CENTER);
	}
	
	/** Returns the annotated mapping cells */
	ArrayList<MappingCell> getMappingCells()
	{
		ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>();
		for(MappingCell mappingCell : this.mappingCells)
		{
			AnnotationFields fields = new AnnotationFields(mappingCell);
			
			// Adjust the basic annotations
			if(!authorField.getText().equals(MULTIPLE_VALUES)) fields.setAuthor(authorField.getText());
			if(!noteField.getText().equals(MULTIPLE_VALUES)) fields.setNote(noteField.getText());

			// Adjust the extra annotations fields
			ArrayList<String> extraFieldLabels = fields.getExtraFields();
			for(int i=0; i<extraFieldLabels.size(); i++)
				if(!extraFields.get(i).equals(MULTIPLE_VALUES))
					fields.setExtraField(extraFieldLabels.get(i), extraFields.get(i).getText());
			
			// Generate the complete mapping cell notes field
			StringBuffer notes = new StringBuffer(fields.getNote());
			for(String field : fields.getExtraFields())
			{
				String value = fields.getExtraField(field);
				if(value.length()>0) notes.append(" <"+field+">"+value+"</"+field+">");
			}
			
			// Generate a modified mapping cell
			MappingCell newMappingCell = mappingCell.copy();
			newMappingCell.setAuthor(fields.getAuthor());
			newMappingCell.setNotes(notes.toString());
			mappingCells.add(newMappingCell);
		}
		return mappingCells;
	}
}
