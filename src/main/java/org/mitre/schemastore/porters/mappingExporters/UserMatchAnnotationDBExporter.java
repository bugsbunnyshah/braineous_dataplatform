// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.schemastore.porters.mappingExporters;

import java.io.File;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;

public class UserMatchAnnotationDBExporter  extends UserMatchAnnotationExporter
{
	/** Constructs the DataDictionary exporter */
	public UserMatchAnnotationDBExporter()
		{ super(); }
	
	/** Returns the file types associated with this converter */
	public String getFileType()
		{ return ".mdb"; }
	
	/** Indicates if the file is overwritten */
	public boolean isFileOverwritten()
		{ return false; }
	
	/** Returns the exporter name */
	public String getName()
		{ return "User Annotation Match DB Exporter"; }
	
	/** Returns the exporter description */
	public String getDescription()
		{ return "This exporter can be used to export all pairings of terms within the mapping"; }
	
	/** Generates a list of the top 100 matches for this project */
	public void exportMapping(Mapping mapping, ArrayList<MappingCell> mappingCells, File file) throws IOException
	{	
		
		String schema1Name = client.getSchema(mapping.getSourceId()).getName();
		String schema2Name = client.getSchema(mapping.getTargetId()).getName(); 	
		Database mdb = null;
		
		// Initializes the db for the specified URI
		try {
			//  connect to MS Access database
			mdb = Database.create(Database.FileFormat.V2003, file);
			
	        System.out.println ("Database connection established."); 			    
	                      
	        Table t = new TableBuilder("T_Schema")
	        .addColumn(new ColumnBuilder("schema_PK")
	                   .setSQLType(Types.INTEGER)
	                   .toColumn())
	        .addColumn(new ColumnBuilder("schemaName")
	                   .setSQLType(Types.VARCHAR)
	                   .toColumn())
	        .addColumn(new ColumnBuilder("schemaDescription")
	                   .setSQLType(Types.LONGVARCHAR)
	                   .toColumn())
	        .addColumn(new ColumnBuilder("schemaDateTime")
	                   .setSQLType(Types.DATE)
	                   .toColumn())
	        .toTable(mdb);

						
			int id1 = mapping.getSourceId();
			int id2 = mapping.getTargetId();
			Date today = new Date();
			String schema1Des = client.getSchema(id1).getDescription();
			String schema2Des = client.getSchema(id2).getDescription();

			t.addRow(id1, schema1Name, schema1Des, today);
			t.addRow(id2, schema2Name, schema2Des, today);
			
			
			Table t2 = new TableBuilder("tblTimestamps")
	        .addColumn(new ColumnBuilder("exportDateTime")
	                   .setSQLType(Types.DATE)
	                   .toColumn())
	        .toTable(mdb);
			t2.addRow(today);
    
	        Table t3 = new TableBuilder("T_MatchLink")
	        .addColumn(new ColumnBuilder("linkIdentifier_PK")
	                   .setSQLType(Types.INTEGER)
	                   .toColumn())
	        .addColumn(new ColumnBuilder("leftSchema_FK")
	                   .setSQLType(Types.INTEGER)
	                   .toColumn())
	        .addColumn(new ColumnBuilder("leftNodePath")
	                   .setSQLType(Types.LONGVARCHAR)
	                   .toColumn())
	        .addColumn(new ColumnBuilder("rightSchema_FK")
	                   .setSQLType(Types.INTEGER)
	                   .toColumn())
	        .addColumn(new ColumnBuilder("rightNodePath")
	                   .setSQLType(Types.LONGVARCHAR)
	                   .toColumn())           
	        .addColumn(new ColumnBuilder("linkWeight")
	                   .setSQLType(Types.VARCHAR)
	                   .toColumn())
	        .addColumn(new ColumnBuilder("linkAuthor")
	                   .setSQLType(Types.VARCHAR)
	                   .toColumn())
	        .addColumn(new ColumnBuilder("linkDateTime")
	                   .setSQLType(Types.VARCHAR)
	                   .toColumn())      
	        .addColumn(new ColumnBuilder("linkTransform")
	                   .setSQLType(Types.VARCHAR)
	                   .toColumn()) 
	        .addColumn(new ColumnBuilder("linkNote")
	                   .setSQLType(Types.LONGVARCHAR)
	                   .toColumn())           
	        .toTable(mdb);    	        

	        HierarchicalSchemaInfo source = new HierarchicalSchemaInfo(client.getSchemaInfo(mapping.getSourceId()));
			setSourceInfo(source);
			HierarchicalSchemaInfo target = new HierarchicalSchemaInfo(client.getSchemaInfo(mapping.getTargetId()));
			setTargetInfo(target);	        

			System.out.println("#mapping cells="+mappingCells.size());
			// Get the list of mapping cells
			CompressedList matchList = new CompressedList();
			for(MappingCell mappingCell : mappingCells) {
				matchList.addMappingCell(mappingCell);
			}
	   		List<CompressedMatch> matches = matchList.getMatches();
			Collections.sort(matches);
	  		
			int linkIdentifier_PK = 0; // the primary key for the table
			for(CompressedMatch match : matches) { 
				linkIdentifier_PK++;

				int leftSchema_FK = mapping.getSourceId();
				int rightSchema_FK = mapping.getTargetId();
				
				String leftNodePath = match.getPaths1();
				String rightNodePath = match.getPaths2();
				double linkWeight = match.getScore();
				String linkAuthor = match.getAuthor();  if (linkAuthor==null || linkAuthor.equals("null")) { linkAuthor=""; }
				String linkDateTime = match.getDate();  if (linkDateTime==null|| linkDateTime.equals("null")) { linkDateTime=""; }
				String linkTransform = match.getTransform();  if (linkTransform==null|| linkTransform.equals("null")) { linkTransform=""; }
				String linkNote = match.getNotes();  if (linkNote==null|| linkNote.equals("null")) { linkNote=""; }
				t3.addRow(linkIdentifier_PK, leftSchema_FK, leftNodePath, rightSchema_FK, rightNodePath, 
						""+linkWeight, linkAuthor, linkDateTime, linkTransform, linkNote);
			}  // for matches loop	
			mdb.close();
		} //end try
		catch(Exception e) { 
		    System.err.println ("Error with database" +file); 
		    e.printStackTrace(); 
		}
	}
	
	public String scrubFileName(String original) {
		String newFileName = original;
		newFileName = newFileName.replaceAll("[^a-zA-Z _\\(\\)0-9\\-.]","");			
		return newFileName;
	}
}