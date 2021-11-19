package org.mitre.rmap.model.exports;

public interface Export {
	// this method will always be called at the beginning of an export
	// it might have things like sequences defined, etc.
	String getOpeningSQL();
	
	// some databases have special table options that should go here
	String getCreateTableOptions();
	
	// if, for example, you created a sequence at the top
	// you should destroy the sequence here at the bottom
	String getClosingSQL();
}
