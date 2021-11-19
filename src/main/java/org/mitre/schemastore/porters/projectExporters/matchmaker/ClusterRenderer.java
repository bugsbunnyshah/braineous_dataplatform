package org.mitre.schemastore.porters.projectExporters.matchmaker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 * This object starts with a completed set of clustered results, and determines
 * how they should be written and formatted into an Excel format. So most of the
 * eccentricities of Excel are buried inside of this object.
 * 
 * @author HAOLI
 */
public class ClusterRenderer {

	private HSSFWorkbook wb;
	private HSSFSheet sheet;
	private HashMap<Integer, Integer> schemaColumnPosHash; // <schemaID, column
	// position>

	private Integer[] schemaIDs;
	
	private ArrayList<Synset> synsets;

	private SchemaStoreClient schemaStoreClient;

	private Integer[] matchCount;

	/**
	 * The number of columns that a particular schema gets in the output XLS
	 * file
	 */
	private static short SCHEMA_COLUMN_BLOCK_SIZE = 3;

	public ClusterRenderer(ClusterNode cluster, SchemaStoreClient schemaStoreClient, Integer[] schemaIDs) {
		this(cluster.synsets, schemaStoreClient, schemaIDs );
	}
	public ClusterRenderer(ArrayList<Synset> synsets, SchemaStoreClient schemaStoreClient, Integer[] schemaIDs ) {
		this.synsets = synsets;
		this.schemaStoreClient = schemaStoreClient;
		this.schemaIDs = schemaIDs;
		allotColumnBlocks();
		matchCount = new Integer[schemaIDs.length];
		for (int i = 0; i < schemaIDs.length; i++)
			matchCount[i] = 0;
	}

	public void print(File output) throws IOException {
		System.out.println("Writing output");
		System.out.println("Printing collated matches to " + output);
		int numSchema = schemaColumnPosHash.keySet().size();
		
		wb = new HSSFWorkbook();
		sheet = wb.createSheet();
		int rowIDX = 0;

		// print sheet headings for each schema (schemaId, value, description)
		HSSFRow row = sheet.createRow(rowIDX++);
		Iterator<Integer> sItr = schemaColumnPosHash.keySet().iterator();
		while (sItr.hasNext()) {
			Integer schemaId = sItr.next();
			Integer columnIdx = schemaColumnPosHash.get(schemaId);
			HSSFCell schemaNameCell = row.createCell(columnIdx);
			schemaNameCell.setCellValue(new HSSFRichTextString(schemaId.toString())); 

			HSSFCell nameCell = row.createCell(columnIdx + 1);
			nameCell.setCellValue(new HSSFRichTextString("value"));

			HSSFCell descCell = row.createCell(columnIdx + 2);
			descCell.setCellValue(new HSSFRichTextString("description"));
		}
		// average score column heading
		HSSFCell averageCell = row.createCell(numSchema * SCHEMA_COLUMN_BLOCK_SIZE + 1);
		averageCell.setCellValue(new HSSFRichTextString("average"));

		int clusters = synsets.size();
		int everyN = (int) Math.ceil((double) clusters / (double) 100);
		if (everyN <= 0) everyN = clusters / 4;
		if (everyN <= 0) everyN = clusters;

		System.out.println("Collator: everyN = " + everyN + " for " + clusters + " clusters");
		ArrayList<SynsetTerm> nodes;
		// output clusters (synsets)
		for (Synset synset : synsets) {
			nodes = synset.getGroup();
			if (nodes.size() < 1) continue; 

			row = sheet.createRow(rowIDX++);

			// print process
			if (rowIDX % everyN == 0) {
				int pct = (int) (((float) rowIDX / (float) clusters) * (float) 100);
				System.out.println("Progress..." + pct);
			}
			// update match count
			matchCount[nodes.size() - 1]++;

			// print elements of a Synset across a row
			double score = 0;
			int synsetSize = 0;
			for (SynsetTerm stNode : nodes) {
				
				Integer colNum = getColumnIdx(stNode.schemaId);
				if (colNum < 0) return;

				HSSFCell idCell = row.getCell(colNum);
				if (idCell == null) {
					idCell = row.createCell(colNum);
					idCell.setCellValue(new HSSFRichTextString( stNode.elementId.toString()) );
				}

				// calculate average score
				for (int ptr = 0; ptr < stNode.pointers.size(); ptr++) {
					score += stNode.distances.get(ptr);
					synsetSize++;
				}
			}

			// print average score
			double average = score / (double) synsetSize;
			HSSFCell avgScoreCell = row.createCell(numSchema * SCHEMA_COLUMN_BLOCK_SIZE + 1);
			avgScoreCell.setCellValue(average);
		}

		// print actual content instead of ids
		System.out.println("Cleaning up");
		printPretty();

		// highlight excel unmatched items
		ExcelFormatting.hiliteImperfections(wb, sheet, schemaColumnPosHash);

		// Write it to a file...
		FileOutputStream os = new FileOutputStream(output); 
		wb.write(os);
		os.close();

		int totalCt = synsets.size();
		System.out.println("Total result row: " + totalCt);
		System.out.println("Number of mapped schemas elements: ");
		double percent;
		for (int i = matchCount.length - 1; i >= 0; i--) {
			percent = (double) matchCount[i] / (double) totalCt * (double) 100;
			System.out.println((i + 1) + " elements = " + matchCount[i] + " (" + percent + "%)");
		}
	}

	/**
	 * Last step, going through each row to turn IDs into actual readable
	 * comments. Highlight empty cells;
	 * 
	 * @param sheet
	 * @throws RemoteException
	 */
	private void printPretty() {
		System.out.println("Print pretty clustered match  results...");

		int lastRow = sheet.getLastRowNum();

		for (Integer schemaId : schemaIDs) {
			System.out.println("     Print schema : " + schemaId);

			HierarchicalSchemaInfo schemaInfo;
			try {
				schemaInfo = new HierarchicalSchemaInfo(schemaStoreClient.getSchemaInfo(schemaId), null);

				Integer colIDX = schemaColumnPosHash.get(schemaId);

				// print schema name over ID
				HSSFRow row0 = sheet.getRow(0);
				HSSFCell cell = row0.getCell(colIDX);
				cell.setCellValue(new HSSFRichTextString(schemaInfo.getSchema().getName()));

				for (int i = 1; i <= lastRow; i++) {
					HSSFRow currRow = sheet.getRow(i);
					HSSFCell idCell = currRow.getCell(colIDX);

					if (idCell != null) {
						HSSFCell pathCell = idCell;
						HSSFCell elementCell = currRow.createCell(colIDX + 1);
						HSSFCell descCell = currRow.createCell(colIDX + 2);

						Integer elementID = Integer.decode(idCell.getRichStringCellValue().toString());
						SchemaElement e = schemaInfo.getElement(elementID);

						// path
						pathCell.setCellValue(new HSSFRichTextString(getPath(schemaInfo, elementID)));
						// value
						elementCell.setCellValue(new HSSFRichTextString(e.getName()));
						// description
						descCell.setCellValue(new HSSFRichTextString(e.getDescription()));
					}
				}
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * returns a string of paths for an element in a schema
	 * 
	 * @param schemaInfo
	 * @param elementID
	 * @return
	 */
	private String getPath(HierarchicalSchemaInfo schemaInfo, Integer elementID) {
		String path = "";
		ArrayList<ArrayList<SchemaElement>> paths = schemaInfo.getPaths(elementID);

		for (ArrayList<SchemaElement> p : paths) {
			for (SchemaElement node : p)
				path += "/" + node.getName();

			path += ";";
		}

		int pathLength = path.length();
		path = path.substring(0, pathLength - 1);

		if (path.length() == 0) return elementID.toString();
		else if (path.length() > 32768) {
			System.out.println("Truncated path for " + path + " due to Excel cell size limitation");
			return path.substring(0, 32767);
		}
		// else if (path.length() == 1) return "";
		else return path;
	}

	/**
	 * Allot 3 columns for each schema's elements
	 */
	private void allotColumnBlocks() {
		schemaColumnPosHash = new HashMap<Integer, Integer>();
		int columnCount = 0;
		for (Integer id : schemaIDs) {
			if (schemaColumnPosHash.get(id) == null) {
				schemaColumnPosHash.put(id, columnCount);
				columnCount += SCHEMA_COLUMN_BLOCK_SIZE;
			}
		}
	}

	// parse and print domain name
	private Integer getColumnIdx(Integer schemaId) {
		Integer col = schemaColumnPosHash.get(schemaId);
		if (col == null) return -1;
		else return col;
	}

}
