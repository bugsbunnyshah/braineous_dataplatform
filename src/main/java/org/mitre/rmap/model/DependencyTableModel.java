// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.
package org.mitre.rmap.model;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class DependencyTableModel implements TableModel {
	private String[] columnNames = {"", "Dependency"};
	private Object[][] data;

	public DependencyTableModel(Object[][] passed){ data = passed; }
	   
	public void setData(Object[][] passed) { data = passed; }
	public Object[][] getData() { return data; }
	   
	// append new data to end of table
	public void addData(Object newPassed){
		Object[][] newData = new Object[data.length + 1][data[0].length];
  
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data.length; j++) {
				newData[i][j] = data[i][j];
			}
		}

		newData[newData.length - 1][0] = new Boolean(false);
		newData[newData.length - 1][1] = newPassed;
	}

	public Class<?> getColumnClass(int columnIndex) {
		if      (columnIndex == 0) { return Boolean.class; }
	   	else if (columnIndex == 1) { return String.class; }
	   	else { return null; }
   	}

	public int getColumnCount() { 
		return columnNames.length;
	}
		
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	public int getRowCount() {
		return data.length;
	}
		
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data[rowIndex][columnIndex];
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex == 0);
	}

	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		data[rowIndex][columnIndex] = value;
	}

	public void removeTableModelListener(TableModelListener l) {}
	public void addTableModelListener(TableModelListener l) {}
}
