// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.schemastore.model.SchemaElement;

/**
 * Displays the dialog which allows links to be accepted/rejected
 * @author CWOLF
 */
public class SchemaStatisticsDialog extends JInternalFrame
{
	/** Schema for which the statistics are to be displayed */
	private Integer schemaID;

	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Class for displaying the matched node statistics */
	private class MatchedNodeStatistics extends JPanel
	{
		/** Constructs the pane displaying the matched nodes */
		MatchedNodeStatistics()
			{ setPreferredSize(new Dimension(500,300)); } 
		
		/** Draws the matched node statistics */
		public void paint(Graphics g)
		{
			// Counts for good, weak, and no link nodes
			int goodCount = 0;
			int weakCount = 0;
			int noCount = 0;
		
			// Cycles through all tree nodes to identify good, weak, and no links
			for(SchemaElement schemaElement : harmonyModel.getSchemaManager().getSchemaInfo(schemaID).getHierarchicalElements())
			{
				double maxConf = Double.MIN_VALUE;
				for(Integer mappingCellID : harmonyModel.getMappingManager().getMappingCellsByElement(schemaElement.getId()))
				{
					double conf = harmonyModel.getMappingManager().getMappingCell(mappingCellID).getScore();
					if(conf>maxConf) maxConf=conf;
				}
				if(maxConf>0.75) goodCount++;
				else if(maxConf>0.25) weakCount++;
				else noCount++;
			}

			// If links exist, calculate the percentages of each type of match
			if(goodCount+weakCount+noCount==0) noCount++;
		
			// Generate the percentages for nodes containing good, weak, and no links
			DefaultPieDataset data = new DefaultPieDataset();
			data.setValue("Good Match",1.0*goodCount/(goodCount+weakCount+noCount));
			data.setValue("Weak Match",1.0*weakCount/(goodCount+weakCount+noCount));
			data.setValue("No Match",1.0*noCount/(goodCount+weakCount+noCount));
			
			// Generate a chart containing a pie plot for this data
			String schemaName = harmonyModel.getSchemaManager().getSchema(schemaID).getName();
	        JFreeChart chart = ChartFactory.createPieChart("Matched Nodes for Schema "+schemaName,data,true,true,false);

	        // Modify the pie plot settings
	        PiePlot plot = (PiePlot) chart.getPlot();
	        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
	        plot.setNoDataMessage("No data available");
	        plot.setCircular(false);
	        plot.setLabelGap(0.02);
	        plot.setSectionPaint(0,Color.GREEN);
	        plot.setSectionPaint(1,Color.YELLOW);
	        plot.setSectionPaint(2,Color.RED);
	        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}"));
	        
	        chart.draw((Graphics2D)g,new Rectangle(500,300));
		}
	}
	
	/** Initializes the link dialog */
	public SchemaStatisticsDialog(Integer schemaID, HarmonyModel harmonyModel)
	{
		super("Statistics for Schema "+harmonyModel.getSchemaManager().getSchema(schemaID).getName());
		this.harmonyModel = harmonyModel;
		
		// Initialize the selected schema
		this.schemaID = schemaID;
		
		// Set up the main dialog pane
		JPanel pane = new JPanel();
		pane.setBorder(new LineBorder(Color.black));
		pane.setLayout(new BorderLayout());
		pane.add(new MatchedNodeStatistics(),BorderLayout.CENTER);
		
		// Initialize the dialog parameters
		setClosable(true);
		setContentPane(pane);
		pack();
 		setVisible(true);
	}
}
