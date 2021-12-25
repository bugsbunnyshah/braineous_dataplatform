// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.schemastore.porters.projectExporters;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.ProjectSchema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 * Class for exporting projects to a pie chart showing schemas' matched percentage
 * @author CWOLF
 */
public class PieChartExporter extends ProjectExporter
{
	/** Returns the exporter name */
	public String getName()
		{ return "Pie Chart Exporter"; }
	
	/** Returns the exporter description */
	public String getDescription()
		{ return "This exporter is used to export pie charts illustrating how matched the various schemas are"; }

	/** Returns the file types associated with this converter */
	public String getFileType()
		{ return ".zip"; }
	
	/** Generate the list of scores for the specified schema */
	private HashMap<Integer,Double> getScores(Integer schemaID, HashMap<Mapping,ArrayList<MappingCell>> mappings)
	{
		HashMap<Integer,Double> scores = new HashMap<Integer,Double>();
		
		new HashMap<Integer,Double>();
    	for(Mapping mapping : mappings.keySet())
    	{
    		// Determine if the schema is the source or target schema
    		boolean isSource = mapping.getSourceId().equals(schemaID);
    		if(!isSource && !mapping.getTargetId().equals(schemaID)) continue;
    		
    		// Gather up scores from the mapping
   			for(MappingCell mappingCell : mappings.get(mapping))
   			{
   				// Get the affected element IDs
    			ArrayList<Integer> elementIDs = new ArrayList<Integer>();
    			if(isSource) elementIDs.addAll(Arrays.asList(mappingCell.getElementInputIDs()));
    			else elementIDs.add(mappingCell.getOutput());
    			
    			// Stores the scores
    			for(Integer elementID : elementIDs)
    			{
    				Double score = scores.get(elementID);
    				if(score==null || mappingCell.getScore()>score)
    					scores.put(elementID, mappingCell.getScore());
    			}
   			}
    	}
		
		return scores;
	}

	/** Return a file containing a pie chart showing the matched ratio for the specified schema */
	private File generateFile(ProjectSchema schema, HashMap<Integer,Double> scores) throws IOException
	{
		// Counts for good, weak, and no link nodes
		int goodCount = 0;
		int weakCount = 0;
		int noCount = 0;
		
		// Cycles through all tree nodes to identify good, weak, and no links
		HierarchicalSchemaInfo schemaInfo = new HierarchicalSchemaInfo(client.getSchemaInfo(schema.getId()),schema.geetSchemaModel());
		for(SchemaElement element : schemaInfo.getHierarchicalElements())
		{
			Double score = scores.get(element.getId());
			if(score==null) score=0.0;
   			if(score>0.75) goodCount++;
   			else if(score>0.25) weakCount++;
   			else noCount++;
		}

		// If links exist, calculate the percentages of each type of match
		if(goodCount+weakCount+noCount==0) noCount++;

		// Generate the percentages for nodes containing good, weak, and no links
		File tempFile = File.createTempFile("PieChart",".jpg");
		DefaultPieDataset data = new DefaultPieDataset();
		data.setValue("Good Match",1.0*goodCount/(goodCount+weakCount+noCount));
		data.setValue("Weak Match",1.0*weakCount/(goodCount+weakCount+noCount));
		data.setValue("No Match",1.0*noCount/(goodCount+weakCount+noCount));
		
		// Generate a chart containing a pie plot for this data
        JFreeChart chart = ChartFactory.createPieChart("Matched Nodes for Schema "+schema.getName(),data,true,true,false);

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
        
        // Save the generated chart as a JPEG
        ChartUtilities.saveChartAsJPEG(tempFile,chart,500,300);
		return tempFile;
	}

	/** Copies the specified file with the specified name to the specified zip output stream */
	private void copyFileToZip(File file, String name, ZipOutputStream out)
	{
		try {
			int byteCount;
			byte[] data = new byte[4096];
			FileInputStream in = new FileInputStream(file);
			ZipEntry entry = new ZipEntry(name);
			out.putNextEntry(entry);
			while((byteCount = in.read(data)) != -1)
				out.write(data, 0, byteCount);
			in.close();
		} catch(Exception e) {}
	}
	
	/** Generates pie charts showing the matched ratio for all schemas in this project */
	public void exportProject(Project project, HashMap<Mapping,ArrayList<MappingCell>> mappings, File file) throws IOException
	{
		// Initialize the output stream for the zip file
	    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));

		// Store the schema pie charts to the zip file
	    for(ProjectSchema schema : project.getSchemas())
	    {
	    	// Compile list of scores for the specified schema
	    	HashMap<Integer,Double> scores = getScores(schema.getId(), mappings);
	    	
	    	// Generate the pie chart file
	    	String fileName = schema.getName() + "(" + schema.getId() + ").jpg";
	    	copyFileToZip(generateFile(schema,scores),fileName,out);
		}
		
		// Close the zip file
		out.close();
	}
}