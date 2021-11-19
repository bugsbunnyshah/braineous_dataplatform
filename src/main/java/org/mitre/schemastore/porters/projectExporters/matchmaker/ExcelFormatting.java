package org.mitre.schemastore.porters.projectExporters.matchmaker;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * All excel formatting stuff that we wanted to move out of the collator. Only put stuff in this
 * file if it has to do with rendering contents to Excel. Clustering stuff goes elsewhere.
 * 
 * @author DMALLEN
 * 
 */
public class ExcelFormatting {

        /**
         * Highlight yellow the rows that have inconsistent matched values. Highlight in red the rows
         * that don't have any corresponding match.
         */
        public static void hiliteImperfections(HSSFWorkbook wb, HSSFSheet sheet,
                        HashMap<Integer, Integer> schemaColumnPosHash) {
                // style : red background
                HSSFCellStyle red = wb.createCellStyle();
                red.setFillPattern((short) 1);
                red.setFillForegroundColor(new HSSFColor.RED().getIndex());

                // style : yellow background
                HSSFCellStyle yellow = wb.createCellStyle();
                yellow.setFillPattern((short) 1);
                yellow.setFillForegroundColor(new HSSFColor.YELLOW().getIndex());

                // style : border left
                HSSFCellStyle border = wb.createCellStyle();
                border.setBorderLeft((short) 5);

                // style: red cells with border left
                HSSFCellStyle redBorder = wb.createCellStyle();
                redBorder.setFillPattern((short) 1);
                redBorder.setFillForegroundColor(new HSSFColor.RED().getIndex());
                redBorder.setBorderLeft((short) 5);

                // style: yellow cells with border left
                HSSFCellStyle yellowBorder = wb.createCellStyle();
                yellowBorder.setFillPattern((short) 1);
                yellowBorder.setFillForegroundColor(new HSSFColor.YELLOW().getIndex());
                yellowBorder.setBorderLeft((short) 5);
                
                int N = schemaColumnPosHash.keySet().size();
                int lastRow = sheet.getLastRowNum();

                // initialize cell position indices
                int[] firstCellIDX = new int[N];
                Iterator<Integer> posItr = schemaColumnPosHash.values().iterator();
                for (int f = 0; f < N; f++)
                        firstCellIDX[f] = posItr.next();

                for (int f = 0; f < N; f++) {
                        sheet.autoSizeColumn((short) (f * 3));
                        sheet.autoSizeColumn((short) ((f * 3) + 1));
                        sheet.autoSizeColumn((short) ((f * 3) + 2));
                } // End for

                // Loop through every row in the spreadsheet...
                for (int i = 1; i <= lastRow; i++) {
                        HSSFRow row = sheet.getRow(i);
                        HSSFCell cell0, cell1, cell2; // domain, domain value, description
                        String domainCmpValue = null;
                        String valueCmpValue = null;
                        String descCmpValue = null;

                        boolean highlightValue = false;
                        boolean highlightDesc = false;

                        Boolean[][] samenessMarkers = new Boolean[N][3];

                        // keeps track of yellow cell group Positions
                        // This loops through every column set representing 1 schema.
                        for (int c = 0; c < N; c++) {
                                int groupPos = firstCellIDX[c];
                                cell0 = row.getCell(groupPos);

                                boolean domainSameVal = true;
                                boolean valueSameVal = true;
                                boolean descrSameVal = true;

                                if (cell0 != null && cell0.getRichStringCellValue().toString().trim().length() > 0 ) {
                                        cell1 = row.getCell(firstCellIDX[c] + 1);
                                        cell2 = row.getCell(firstCellIDX[c] + 2);

                                        // skip domain comparisons
                                        if (cell1 == null) {
                                                Boolean[] markers = new Boolean[] { new Boolean(domainCmpValue),
                                                                new Boolean(valueCmpValue), new Boolean(descCmpValue) };
                                                samenessMarkers[c] = markers;
                                                continue;
                                        }

                                        if (valueCmpValue == null) valueCmpValue = cell1.getRichStringCellValue().toString().trim().toLowerCase();
                                        else if (!valueCmpValue.equals(cell1.getRichStringCellValue().toString().trim().toLowerCase())) {
                                                valueSameVal = false;
                                                highlightValue = true;
                                        }

                                        if (descCmpValue == null) descCmpValue = cell2.getRichStringCellValue().toString().trim().toLowerCase();
                                        else if (!descCmpValue.equals(cell2.getRichStringCellValue().toString().trim().toLowerCase())) {
                                                descrSameVal = false;
                                                highlightDesc = true;
                                        }
                                } // End if

                                Boolean[] markers = new Boolean[] { new Boolean(domainSameVal),
                                                new Boolean(valueSameVal), new Boolean(descrSameVal) };
                                samenessMarkers[c] = markers;
                        } // End for

                        // Loop back through the column sets and color them appropriately.
                        for (int c = 0; c < N; c++) {
                                // Boolean[] markers = samenessMarkers[c];
                                int groupPos = firstCellIDX[c];
                                cell0 = row.getCell(groupPos);

                                if (cell0 == null || cell0.getRichStringCellValue().toString().trim().length() == 0 ) {
                                        // color me red b/c I'm empty
                                        cell0 = row.createCell(groupPos);
                                        cell1 = row.createCell(groupPos + 1); 
                                        cell2 = row.createCell(groupPos + 2); 

                                        cell0.setCellStyle(redBorder);
                                        cell1.setCellStyle(red);
                                        cell2.setCellStyle(red);
                                        continue;
                                }

                                // Default style for things whose value does match.
                                cell0.setCellStyle(border);
                                cell0 = row.getCell(groupPos);
                                cell1 = row.getCell(groupPos + 1);
                                cell2 = row.getCell(groupPos + 2);

                                if (highlightValue) // (!valueMatches)
                                cell1.setCellStyle(yellow);
                                if (highlightDesc) // (!descMatches)
                                cell2.setCellStyle(yellow);

                        }
                }
        }
}
