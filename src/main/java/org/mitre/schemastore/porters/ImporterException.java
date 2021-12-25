// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.porters;

/** Class for throwing Importer exceptions */ @SuppressWarnings("serial")
public class ImporterException extends Exception
{
	/** Stores various types of importer exceptions */
	public static enum ImporterExceptionType {INVALID_URI, PARSE_FAILURE, DUPLICATION_FAILURE, IMPORT_FAILURE};
	
	/** Stores the exception type */
	private ImporterExceptionType exceptionType;
	
	/** Constructs an importer exception */
	public ImporterException(ImporterExceptionType exceptionType, String message)
		{ super(message); this.exceptionType = exceptionType; }
	
	/** Returns the exception type */
	public ImporterExceptionType getExceptionType()
		{ return exceptionType; }
	
	/** Returns the error message */
	public String getMessage()
		{ return exceptionType + ": " + super.getMessage(); }
}
