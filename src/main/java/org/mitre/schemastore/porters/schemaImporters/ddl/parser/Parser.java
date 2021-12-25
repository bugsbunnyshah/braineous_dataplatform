package org.mitre.schemastore.porters.schemaImporters.ddl.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects.Column;
import org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects.Comment;
import org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects.Element;
import org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects.ForeignKey;
import org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects.PrimaryKey;

import de.susebox.jtopas.CharArraySource;
import de.susebox.jtopas.Flags;
import de.susebox.jtopas.StandardTokenizer;
import de.susebox.jtopas.StandardTokenizerProperties;
import de.susebox.jtopas.Token;
import de.susebox.jtopas.Tokenizer;
import de.susebox.jtopas.TokenizerProperties;
import de.susebox.jtopas.TokenizerSource;

public class Parser {
	private Tables tables = new Tables();

	public static ArrayList<TokenDetail> parseForKeywords(String command, List<String> keywords, boolean parseForComments) {
        ArrayList<TokenDetail> tokens = new ArrayList<TokenDetail>();
        command = command.trim();

        // debugging
		System.out.println("Command: '" + command + "'");

        try {
			TokenizerProperties p = new StandardTokenizerProperties();
			if (parseForComments) {
		        p.setParseFlags(Flags.F_NO_CASE |
		        				Flags.F_TOKEN_POS_ONLY |
		        				Flags.F_RETURN_LINE_COMMENTS |
		        				Flags.F_RETURN_BLOCK_COMMENTS);
			} else {
		        p.setParseFlags(Flags.F_NO_CASE |
        						Flags.F_TOKEN_POS_ONLY);
			}
	        p.setSeparators(TokenizerProperties.DEFAULT_SEPARATORS);

	        // keywords associated with create
	        p.addLineComment("--");
	        p.addBlockComment("/*", "*/");
	        p.addBlockComment("/*+", "*/");
	        p.addString("\"", "\"", "\\\\");
	        p.addString("'", "'", "\\\\");
	        p.addString("'", "'", "'");
	        p.addString("`", "`", "\\\\");
	        p.addString("[", "]", "\\\\");
	        p.addSpecialSequence(",");
	        p.addSpecialSequence("(");
	        p.addSpecialSequence(")");

	        // add our custom list of keywords
	        for (String keyword : keywords) { p.addKeyword(keyword); }

	        // create the tokenizer
	        Tokenizer tokenizer = new StandardTokenizer(p);
	        TokenizerSource source = new CharArraySource(command.toCharArray());

	        try {
	        	tokenizer.setSource(source);

	            // tokenize the file and print basically
	            // formatted context to stdout
	            while (tokenizer.hasMoreToken()) {
	            	Token token = tokenizer.nextToken();
	            	if (token.getType() != Token.EOF) {
	            		// extract the value from the command
	            		String value = command.substring(token.getStartPosition(), token.getStartPosition() + token.getLength());

	            		// unless this is a string or a comment, make it upper case
	            		if (token.getType() != Token.LINE_COMMENT && token.getType() != Token.BLOCK_COMMENT && token.getType() != Token.STRING) { value = value.toUpperCase(); }

	            		// create a new token and add it to the list of tokens
	            		tokens.add(new TokenDetail(value, token.getType()));
	            	}
	            }
            } finally {
                // never forget to release resources and references
                tokenizer.close();
            }
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return tokens;
	}

	public static ArrayList<ArrayList<String>> parseForCommands(String ddl) {
		ArrayList<ArrayList<String>> commands = new ArrayList<ArrayList<String>>();

		// we are only interested in: CREATE TABLE, ALTER TABLE, COMMENT ON, DROP TABLE
		try {
			TokenizerProperties p = new StandardTokenizerProperties();
	        p.setParseFlags(Flags.F_NO_CASE |
	        				Flags.F_TOKEN_POS_ONLY |
	        				Flags.F_RETURN_LINE_COMMENTS |
	        				Flags.F_RETURN_BLOCK_COMMENTS);
	        p.setSeparators(null);

	        // keywords associated with create
	        p.addLineComment("--");
	        p.addBlockComment("/*", "*/");
	        p.addBlockComment("/*+", "*/");
	        p.addString("\"", "\"", "\\\\");
	        p.addString("'", "'", "\\\\");
	        p.addString("'", "'", "'");
	        p.addString("`", "`", "\\\\");
	        p.addString("[", "]", "\\\\");
	        p.addSpecialSequence(",");
	        p.addSpecialSequence("(");
	        p.addSpecialSequence(")");
	        p.addSpecialSequence(".");
	        p.addSeparators(";");

	        // these are the only commands we are looking for
	        p.addKeyword("create");
	        p.addKeyword("alter");
	        p.addKeyword("drop");
	        p.addKeyword("comment");

	        // create the tokenizer
	        Tokenizer tokenizer = new StandardTokenizer(p);
	        TokenizerSource source = new CharArraySource(ddl.toCharArray());
	        ArrayList<String> tokens = new ArrayList<String>();

	        try {
	        	tokenizer.setSource(source);

	            // tokenize the file and print basically
	            // formatted context to stdout
	            while (tokenizer.hasMoreToken()) {
	            	Token token = tokenizer.nextToken();
	            	int type = token.getType();

	            	if (tokens.size() == 0) {
		            	// if we have a comment as our first token, we aren't going to include it
	            		if (type == Token.BLOCK_COMMENT || type == Token.LINE_COMMENT) { continue; }

	            		// if we have no tokens and we don't see something that is a keyword then we want then ignore it
	            		if (type != Token.KEYWORD) { continue; } 
	            	}

	            	if (type == Token.SEPARATOR) {
	            		if (tokens.size() > 0) {
		            		// add the list of tokens to our list of commands
	            			commands.add(tokens);

	            			// clear our list of tokens
	            			tokens = new ArrayList<String>();
	            		}
	            	} else {
	            		// extract the value from the command
	            		String value = ddl.substring(token.getStartPosition(), token.getStartPosition() + token.getLength());

	            		// unless this is a string or a comment, make it upper case
	            		if (type != Token.LINE_COMMENT && type != Token.BLOCK_COMMENT && type != Token.STRING) { value = value.toUpperCase(); }

	            		// add it to our list
	            		tokens.add(value);
	            	}
	            }
            } finally {
                // never forget to release resources and references
                tokenizer.close();
            }
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return commands;
	}

	public Tables parse(ArrayList<String> tokens) throws Exception {
		StringBuffer commandBuffer = new StringBuffer();
		for (String token : tokens) {
			commandBuffer.append(token);
			commandBuffer.append(" "); // add an extra space on to the end of each token
		}

		try {
			String command = commandBuffer.toString();
			if (tokens.get(0).equals("CREATE")) { parseCommandCreate(command); }
			if (tokens.get(0).equals("ALTER")) { parseCommandAlter(command); }
			if (tokens.get(0).equals("DROP")) { parseCommandDrop(command); }
			if (tokens.get(0).equals("COMMENT")) { parseCommandComment(command); }
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			throw new Exception("ERROR: " + e.getMessage());
		}
		System.out.println("");

		// take the data structures we've put together and put them into a Tables object
		return tables;
	}

	private void parseCommandCreate(String command) throws Exception {
		final int TOKEN_CREATE = 1;
		final int TOKEN_TABLE = 2;
		final int TOKEN_NAME = 3;
		final int TOKEN_COLUMNS_AND_CONSTRAINTS = 4;

		// parse our create statement with the list of keywords that we know of
		String[] keywords = new String[]{"create","global","temporary","table","view","if","not","exists",
										 "constraint","references","primary","foreign","key","comment",
										 "unique","check","on","update","cascade"};

		ArrayList<TokenDetail> tokens = Parser.parseForKeywords(command, Arrays.asList(keywords), true);

		// go through our list of tokens and find the ones that are relevant to what we're doing
		// then put together a properly formatted list and pass it to our table parser

		// store information about what we've parsed
		String tableName = null;
		ArrayList<TokenDetail> currentColumnAndConstraintTokens = new ArrayList<TokenDetail>();
		ArrayList<ArrayList<TokenDetail>> allColumnAndConstraintTokens = new ArrayList<ArrayList<TokenDetail>>();

		int nextToken = TOKEN_CREATE;
		int countParentheses = 0;
		int size = tokens.size();
		for (int i = 0; i < size; i++) {
			TokenDetail currentToken = tokens.get(i);

			// STEP 1: look to see if we are creating something
			if (nextToken == TOKEN_CREATE) {
				if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("CREATE")) {
					nextToken = TOKEN_TABLE;
				}
				continue;
			}

			// STEP 2: look to see if we are creating a table
			// if we run into the "TABLE" token, then the next thing we are going to look for is the name of the table
			if(nextToken == TOKEN_TABLE)
			{
				if (currentToken.getType() == Token.NORMAL && (currentToken.getValue().equalsIgnoreCase("database") ||
						currentToken.getValue().equalsIgnoreCase("type"))) return;
				if(currentToken.getType() == Token.KEYWORD)
				{
					if(currentToken.getValue().equals("TABLE")) nextToken = TOKEN_NAME;
					else if(currentToken.getValue().equals("VIEW")) return;
				}
				continue;
			}

			// STEP 3: find the name of the table
			if (nextToken == TOKEN_NAME) {
				// create a string buffer and loop through until i see something other than a string, a normal, or a period
				StringBuffer tableNameBuffer = new StringBuffer();
				for (int j = i; j < size; i++, j++) {
					currentToken = tokens.get(j);

					// if this is a string, a normal, or a period, add it to our list
					if (currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING || currentToken.getValue().equals(".")) {
						tableNameBuffer.append(currentToken.getValue());
						continue;
					}

					// if this is a comment, skip it and continue
					if (currentToken.getType() == Token.LINE_COMMENT || currentToken.getType() == Token.BLOCK_COMMENT) {
						continue;
					}

					// if this is a keyword and we have no table name yet, try the next token
					if (currentToken.getType() == Token.KEYWORD && tableNameBuffer.length() == 0) {
						continue;
					}

					// if we didn't add it to our list (or skip it), then back up our list by one and get out of it
					i = (i - 1);
					break;
				}


				tableName = Parser.cleanString(Parser.removeSchema(Parser.removeQuotes(tableNameBuffer.toString())));
				if (tableName.contains("NAS_SECTOR")) {
					System.out.println("Get set");
				}
				if (tableName == null) { throw new Exception("Could not find the name of the table."); }

				nextToken = TOKEN_COLUMNS_AND_CONSTRAINTS;
				continue;
			}

			if (nextToken == TOKEN_COLUMNS_AND_CONSTRAINTS) {
				// if this is a comma, take the list of tokens and add it to the the complete list 
				if (currentToken.getType() == Token.SPECIAL_SEQUENCE && currentToken.getValue().equals(",") && countParentheses == 1) {
					if (currentColumnAndConstraintTokens.size() > 0) {
						allColumnAndConstraintTokens.add(currentColumnAndConstraintTokens);
						currentColumnAndConstraintTokens = new ArrayList<TokenDetail>();
					}
					continue;
				}

				// if this is a comment and we don't have any other tokens and we have columns already then tack it onto the last token
				if (currentToken.getType() == Token.LINE_COMMENT || currentToken.getType() == Token.BLOCK_COMMENT) {
					if (allColumnAndConstraintTokens.size() > 0 && currentColumnAndConstraintTokens.size() == 0) {
						// if this is a comment and we have no tokens then append it to the previous list
						// if this is a comment and we have tokens then keep it in place and it will be attached to the current column
						System.out.println("Appending comment to previous column: '" + currentToken.getValue() + "'");
						allColumnAndConstraintTokens.get(allColumnAndConstraintTokens.size() - 1).add(currentToken);
						continue;
					}
				}

				if (currentToken.getType() == Token.SPECIAL_SEQUENCE && (currentToken.getValue().equals("(") || currentToken.getValue().equals(")"))) {
					if (currentToken.getValue().equals("(")) {
						++countParentheses;
						if (countParentheses == 1) {
							// this is the opening parentheses to our list, we don't need to record it
							continue;
						} 
					}
					if (currentToken.getValue().equals(")")) {
						--countParentheses;
						if (countParentheses == 0) {
							// this is the closing parentheses to our list, we don't need to record it
							// but we do need to record it and bust out of here
							if (currentColumnAndConstraintTokens.size() > 0) {
								allColumnAndConstraintTokens.add(currentColumnAndConstraintTokens);
								currentColumnAndConstraintTokens = new ArrayList<TokenDetail>();
							}
							break;
						}
					}
					if (countParentheses > 1) {
						// if we have more than one set of parentheses, we want to record them
						currentColumnAndConstraintTokens.add(currentToken);
						continue;
					}
				}

				// oooooook add it to the list
				currentColumnAndConstraintTokens.add(currentToken);
			}
		}

		// make sure we have a table name
		if (tableName == null) { throw new Exception("Could not create table. No table name found."); }
		tableName = tableName.toUpperCase();

		// tease out some columns and constraints from the table definition
		ArrayList<Element> elements = new ArrayList<Element>();
		for (int i = 0; i < allColumnAndConstraintTokens.size(); i++) {
			elements.addAll(parseColumnsAndConstraints(tableName, allColumnAndConstraintTokens.get(i)));
		}

		// add everything to the tables
		tables.createTable(tableName, elements);
	}

	/*
	 * This should match statements like this:
	 *   add [column] {column definition}
	 *   add ({column definition}, {column definition}, {...})
	 *   add [constraint] {constraint definition}
	 *   drop [column] {column name}								<- don't drop if being used in a constraint
	 *   drop ({column name}, {column name}, {...})					<- don't drop if being used in a constraint
	 *   drop constraint {constraint name}							<- if this is a primary key then find columns marked as "key" and remove the marking, then remove any foreign keys pointing to these columns
	 *   drop primary key											<- find columns marked as "key" and remove the marking, then remove any foreign keys pointing to these columns
	 *   rename [to] {new name}										<- make sure to update constraints referencing this table
	 *   rename column {old name} to {new name}						<- make sure to update constraints referencing this column
	 *   rename constraint {old name} to {new name}					<- make sure to actually update the constraints
	 *   modify [column] {column definition}						<- only really worried about situations where the datatype is changing, though if no comment is specified on a column then remove any comment on the column
	 *   modify ({column definition})								<- only really worried about situations where the datatype is changing, though if no comment is specified on a column then remove any comment on the column
	 *   change [column] {old name} {new name} {column definition}	<- really just a combo of renaming and modifying so apply those rules as necessary
	 */
	private void parseCommandAlter(String command) throws Exception {
		// parse our alter statement with the list of keywords that we know of
		ArrayList<String> keywords = new ArrayList<String>();
		keywords.add("alter");
		keywords.add("table");
		keywords.add("add");
		keywords.add("drop");
		keywords.add("rename");
		keywords.add("modify");
		keywords.add("change");
		keywords.add("to");
		keywords.add("column");
		keywords.add("constraint");
		keywords.add("references");
		keywords.add("primary");
		keywords.add("foreign");
		keywords.add("key");
		keywords.add("comment");
		keywords.add("unique");
		keywords.add("check");
		ArrayList<TokenDetail> tokens = Parser.parseForKeywords(command, keywords, false);

		// tell what type of token we are looking for next
		final int TOKEN_ALTER = 1;
		final int TOKEN_TABLE = 2;
		final int TOKEN_NAME = 3;
		final int TOKEN_COLUMNS_AND_CONSTRAINTS = 4;

		// tell what type of alter we are doing
		final int ALTER_ADD = 1;
		final int ALTER_DROP = 2;
		final int ALTER_RENAME = 3;
		final int ALTER_MODIFY = 4;
		final int ALTER_CHANGE = 5;

		// store information about what we've parsed
		String tableName = null;
		ArrayList<TokenDetail> allColumnAndConstraintTokens = new ArrayList<TokenDetail>();

		int alterType = 0;
		int nextToken = TOKEN_ALTER;
		int size = tokens.size();
		for (int i = 0; i < size; i++) {
			TokenDetail currentToken = tokens.get(i);

			// STEP 1: look to see if we are creating something
			if (nextToken == TOKEN_ALTER) {
				if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("ALTER")) {
					nextToken = TOKEN_TABLE;
				}
				continue;
			}

			// STEP 2: look to see if we are creating a table
			// if we run into the "TABLE" token, then the next thing we are going to look for is the name of the table
			if (nextToken == TOKEN_TABLE) {
				if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("TABLE")) {
					nextToken = TOKEN_NAME;
				}
				continue;
			}

			// STEP 3: find the name of the table
			if (nextToken == TOKEN_NAME) {
				// create a string buffer and loop through until i see something other than a string, a normal, or a period
				StringBuffer tableNameBuffer = new StringBuffer();
				for (int j = i; j < size; i++, j++) {
					currentToken = tokens.get(j);

					// if this is a string, a normal, or a period, add it to our list
					if (currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING || currentToken.getValue().equals(".")) {
						tableNameBuffer.append(currentToken.getValue());
						continue;
					}

					// if this is a comment, skip it and continue
					if (currentToken.getType() == Token.LINE_COMMENT || currentToken.getType() == Token.BLOCK_COMMENT) {
						continue;
					}

					// if this is a keyword and we have no table name yet, try the next token
					if (currentToken.getType() == Token.KEYWORD && tableNameBuffer.length() == 0) {
						continue;
					}

					// if we didn't add it to our list (or skip it), then back up our list by one and get out of it
					i = (i - 1);
					break;
				}


				tableName = Parser.cleanString(Parser.removeSchema(Parser.removeQuotes(tableNameBuffer.toString())));
				if (tableName == null) { throw new Exception("Could not find the name of the table."); }

				nextToken = TOKEN_COLUMNS_AND_CONSTRAINTS;
				continue;
			}

			if (nextToken == TOKEN_COLUMNS_AND_CONSTRAINTS) {
				if (alterType == 0) {
					if (currentToken.getType() == Token.KEYWORD) {
						if (currentToken.getValue().equals("ADD")) { alterType = ALTER_ADD; }
						if (currentToken.getValue().equals("DROP")) { alterType = ALTER_DROP; }
						if (currentToken.getValue().equals("RENAME")) { alterType = ALTER_RENAME; }
						if (currentToken.getValue().equals("MODIFY")) { alterType = ALTER_MODIFY; }
						if (currentToken.getValue().equals("CHANGE")) { alterType = ALTER_CHANGE; }
						if (alterType != 0) { allColumnAndConstraintTokens.add(currentToken); } // if this token told us what we have then start adding it to the list
					}
				} else {
					allColumnAndConstraintTokens.add(currentToken);
				}
			}
		}

		// make sure we have a table name
		if (tableName == null) { throw new Exception("Could not alter table. No table name found."); }
		tableName = tableName.toUpperCase();

		if (alterType != 0) {
			switch (alterType) {
				case ALTER_ADD:
					parseAlterTableAdd(tableName, allColumnAndConstraintTokens);
					break;
				case ALTER_DROP:
					parseAlterTableDrop(tableName, allColumnAndConstraintTokens);
					break;
				case ALTER_RENAME:
					parseAlterTableRename(tableName, allColumnAndConstraintTokens);
					break;
				case ALTER_MODIFY:
				case ALTER_CHANGE:
					parseAlterTableModifyAndChange(tableName, allColumnAndConstraintTokens);
					break;
			}
		} else {
			throw new Exception("Could not alter table. No recognized way to alter the table was found.");
		}
	}

	/*
	 * Definition:
 	 *   add [column] {column definition}
	 *   add ({column definition}, {column definition}, {...})
	 *   add [constraint] {constraint definition}
	 * 
	 * Examples:
	 *   alter table x add col1 number;
	 *   alter table x add column col1 number;
	 *   alter table x add (col1 number, col2 varchar2(100));
	 *   alter table x add col1 number comment 'asdf';
	 *   alter table x add primary key (col1);
	 *   alter table x add foreign key (col1) references y (col2);
	 *   alter table x add constraint primary key (col1);
	 *   alter table x add constraint z primary key (col1);
	 *   alter table x add col2 number after col1;
	 * 
	 * Adding a constraint or a column will have the tokens sent to the method parseColumnsAndConstraints
	 */
	private void parseAlterTableAdd(String tableName, ArrayList<TokenDetail> tokens) throws Exception {
		final int PROCESSING_COLUMNS = 1;
		final int PROCESSING_CONSTRAINT = 2;

		final int TOKEN_ADD = 1;
		final int TOKEN_OBJECT_TYPE = 2;
		final int TOKEN_COLUMNS_AND_CONSTRAINTS = 3;
		

		// store information about what we've parsed
		ArrayList<TokenDetail> currentColumnAndConstraintTokens = new ArrayList<TokenDetail>();
		ArrayList<ArrayList<TokenDetail>> allColumnAndConstraintTokens = new ArrayList<ArrayList<TokenDetail>>();

		int alterType = PROCESSING_COLUMNS;
		int nextToken = TOKEN_ADD;
		int countParentheses = 0;
		int size = tokens.size();
		for (int i = 0; i < size; i++) {
			TokenDetail currentToken = tokens.get(i);

			// STEP 1: look to see if we are adding something
			if (nextToken == TOKEN_ADD) {
				if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("ADD")) {
					nextToken = TOKEN_OBJECT_TYPE;
				}
				continue;
			}

			// STEP 2: see what we are adding (column, constraint, primary key, foreign key)
			if (nextToken == TOKEN_OBJECT_TYPE) {
				if (currentToken.getType() == Token.KEYWORD) {
					if (currentToken.getValue().equals("COLUMN")) { alterType = PROCESSING_COLUMNS; }
					if (currentToken.getValue().equals("CONSTRAINT")) { alterType = PROCESSING_CONSTRAINT; }
					if (currentToken.getValue().equals("FOREIGN")) { alterType = PROCESSING_CONSTRAINT; }
					if (currentToken.getValue().equals("PRIMARY")) { alterType = PROCESSING_CONSTRAINT; }
				} else {
					// we see a normal/string/special_sequence
					alterType = PROCESSING_COLUMNS;
				}
				nextToken = TOKEN_COLUMNS_AND_CONSTRAINTS;
			}

			// STEP 2: find the columns and constraints we're going to pass to the column and constraint parser
			if (nextToken == TOKEN_COLUMNS_AND_CONSTRAINTS) {
				// if this is a comma, take the list of tokens and add it to the the complete list 
				if (currentToken.getType() == Token.SPECIAL_SEQUENCE && currentToken.getValue().equals(",") && countParentheses == 1) {
					if (currentColumnAndConstraintTokens.size() > 0) {
						allColumnAndConstraintTokens.add(currentColumnAndConstraintTokens);
						currentColumnAndConstraintTokens = new ArrayList<TokenDetail>();
					}
					continue;
				}

				// if this is a comment and we don't have any other tokens and we have columns already then tack it onto the last token
				if (currentToken.getType() == Token.LINE_COMMENT || currentToken.getType() == Token.BLOCK_COMMENT) {
					if (allColumnAndConstraintTokens.size() > 0 && currentColumnAndConstraintTokens.size() == 0) {
						// if this is a comment and we have no tokens then append it to the previous list
						// if this is a comment and we have tokens then keep it in place and it will be attached to the current column
						System.out.println("Appending comment to previous column: '" + currentToken.getValue() + "'");
						allColumnAndConstraintTokens.get(allColumnAndConstraintTokens.size() - 1).add(currentToken);
						continue;
					}
				}

				if (currentToken.getType() == Token.SPECIAL_SEQUENCE && (currentToken.getValue().equals("(") || currentToken.getValue().equals(")"))) {
					if (alterType == PROCESSING_CONSTRAINT) {
						currentColumnAndConstraintTokens.add(currentToken);
						continue;
					}
					if (alterType == PROCESSING_COLUMNS) {
						if (currentToken.getValue().equals("(")) {
							++countParentheses;
							if (countParentheses == 1) {
								// this is the opening parentheses to our list, we don't need to record it
								continue;
							} 
						}
						if (currentToken.getValue().equals(")")) {
							--countParentheses;
							if (countParentheses == 0) {
								// this is the closing parentheses to our list, we don't need to record it
								// but we do need to record it and bust out of here
								if (currentColumnAndConstraintTokens.size() > 0) {
									allColumnAndConstraintTokens.add(currentColumnAndConstraintTokens);
									currentColumnAndConstraintTokens = new ArrayList<TokenDetail>();
								}
								break;
							}
						}
						if (countParentheses > 1) {
							// if we have more than one set of parentheses, we want to record them
							currentColumnAndConstraintTokens.add(currentToken);
							continue;
						}
					}
				}

				// oooooook add it to the list
				currentColumnAndConstraintTokens.add(currentToken);
			}
		}

		if (currentColumnAndConstraintTokens.size() > 0) {
			allColumnAndConstraintTokens.add(currentColumnAndConstraintTokens);
			currentColumnAndConstraintTokens = new ArrayList<TokenDetail>();
		}

		// tease out some columns and constraints from the table definition
		ArrayList<Element> elements = new ArrayList<Element>();
		for (int i = 0; i < allColumnAndConstraintTokens.size(); i++) {
			elements.addAll(parseColumnsAndConstraints(tableName, allColumnAndConstraintTokens.get(i)));
		}

		tables.addToTable(tableName, elements);
	}

	/*
	 * Definition:
 	 *   drop [column] {column name}								<- don't drop if being used in a constraint
	 *   drop ({column name}, {column name}, {...})					<- don't drop if being used in a constraint
	 *   drop constraint {constraint name}							<- if this is a primary key then find columns marked as "key" and remove the marking, then remove any foreign keys pointing to these columns
	 *   drop primary key											<- find columns marked as "key" and remove the marking, then remove any foreign keys pointing to these columns
	 *   drop foreign key {constraint name}
	 * 
	 * Examples:
	 *   alter table x drop col1;
	 *   alter table x drop column col1;
	 *   alter table x drop (col1, col2, col3);
	 *   alter table x drop constraint z;
	 *   alter table x drop primary key;
	 *   alter table x drop foreign key z;
	 */
	private void parseAlterTableDrop(String tableName, ArrayList<TokenDetail> tokens) throws Exception  {
		final int PROCESSING_COLUMNS = 1;
		final int PROCESSING_CONSTRAINT = 2; // includes a name for the constraint
		final int PROCESSING_PRIMARY_KEY = 3; // does not include a constraint name

		final int TOKEN_DROP = 1;
		final int TOKEN_OBJECT_TYPE = 2;
		final int TOKEN_OBJECT_VALUES = 3;

		// contains a list of everything we're about to drop
		ArrayList<String> names = new ArrayList<String>();

		int alterType = 0;
		int nextToken = TOKEN_DROP;
		int countParentheses = 0;
		int size = tokens.size();
		for (int i = 0; i < size; i++) {
			TokenDetail currentToken = tokens.get(i);

			// STEP 1: look to see if we are dropping something
			if (nextToken == TOKEN_DROP) {
				if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("DROP")) {
					nextToken = TOKEN_OBJECT_TYPE;
				}
				continue;
			}

			// STEP 2: see if we get an object type (COLUMN, CONSTRAINT, PRIMARY, FOREIGN)
			// if we immediately see a "normal" or a "string" or an opening parenthesis then we are looking at a column
			if (nextToken == TOKEN_OBJECT_TYPE) {
				if (currentToken.getType() == Token.KEYWORD) {
					if (currentToken.getValue().equals("COLUMN")) { alterType = PROCESSING_COLUMNS; }
					if (currentToken.getValue().equals("CONSTRAINT")) { alterType = PROCESSING_CONSTRAINT; }
					if (currentToken.getValue().equals("FOREIGN")) { alterType = PROCESSING_CONSTRAINT; }
					if (currentToken.getValue().equals("PRIMARY")) { alterType = PROCESSING_PRIMARY_KEY; }
				} else {
					// we see a normal/string/special_sequence
					alterType = PROCESSING_COLUMNS;
				}
				nextToken = TOKEN_OBJECT_VALUES;
			}

			// STEP 3: get the name of whatever it is we're dropping
			if (nextToken == TOKEN_OBJECT_VALUES) {
				// if we are dropping a primary key break out of here, there's nothing to find
				if (alterType == PROCESSING_PRIMARY_KEY) { break; }

				// count our parentheses (if any)
				// because if we see a set of parentheses then we want to break after they end
				if (currentToken.getType() == Token.SPECIAL_SEQUENCE && (currentToken.getValue().equals("(") || currentToken.getValue().equals(")"))) {
					if (currentToken.getValue().equals("(")) {
						countParentheses++;
					}
					if (currentToken.getValue().equals(")")) {
						countParentheses--;
						if (countParentheses == 0) { break; }
					}
				}

				// loop until we see a normal/string
				// if this is a primary key we won't see anything
				if (currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING){
					if (alterType == PROCESSING_COLUMNS) { names.add(currentToken.toString()); }
					if (alterType == PROCESSING_CONSTRAINT) { names.add(currentToken.toString()); }
				}
			}
		}
		
		// STEP 4: tell the table object to drop whatever it is we are dropping
		switch (alterType) {
			case PROCESSING_COLUMNS:
				tables.dropColumns(tableName, names);
				break;
			case PROCESSING_CONSTRAINT:
				tables.dropConstraints(tableName, names);
				break;
			case PROCESSING_PRIMARY_KEY:
				tables.dropPrimaryKey(tableName);
				break;
		}
	}

	/*
	 * Definition:
 	 *   rename [to] {new name}										<- make sure to update constraints referencing this table
	 *   rename column {old name} to {new name}						<- make sure to update constraints referencing this column
	 *   rename constraint {old name} to {new name}					<- make sure to actually update the constraints
	 * 
	 * Examples:
	 *   alter table x rename y;
	 *   alter table x rename to y;
	 *   alter table x rename column col1 to col2;
	 *   alter table x rename constraint y to z;
	 */
	private void parseAlterTableRename(String tableName, ArrayList<TokenDetail> tokens) throws Exception  {
		final int PROCESSING_TABLE = 1;
		final int PROCESSING_COLUMN = 2;
		final int PROCESSING_CONSTRAINT = 3;

		final int TOKEN_RENAME = 1;
		final int TOKEN_OBJECT_TYPE = 2;
		final int TOKEN_OBJECTS = 3;

		String sourceObject = null;
		String targetObject = null;

		int alterType = PROCESSING_TABLE;
		int nextToken = TOKEN_RENAME;
		int size = tokens.size();
		for (int i = 0; i < size; i++) {
			TokenDetail currentToken = tokens.get(i);

			// STEP 1: look to see if we are dropping something
			if (nextToken == TOKEN_RENAME) {
				if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("RENAME")) {
					nextToken = TOKEN_OBJECT_TYPE;
				}
				continue;
			}

			// STEP 2: see if we get an object type (COLUMN, CONSTRAINT, PRIMARY, FOREIGN)
			// if we immediately see a "normal" or a "string" or an opening parenthesis then we are looking at a column
			if (nextToken == TOKEN_OBJECT_TYPE) {
				if (currentToken.getType() == Token.KEYWORD) {
					if (currentToken.getValue().equals("COLUMN")) { alterType = PROCESSING_COLUMN; }
					if (currentToken.getValue().equals("CONSTRAINT")) { alterType = PROCESSING_CONSTRAINT; }
					if (currentToken.getValue().equals("TO")) { alterType = PROCESSING_TABLE; }
				} else {
					// we see a normal/string/special_sequence
					alterType = PROCESSING_TABLE;
				}
				nextToken = TOKEN_OBJECTS;
			}

			if (nextToken == TOKEN_OBJECTS) {
				// if this is a table rename look for the first normal/string and that is the new table name and we're done
				if (alterType == PROCESSING_TABLE) {
					sourceObject = tableName;

					// our target token is either going to be the first word or the first word after the word "to"
					// if it isn't then we have a problem
					if (currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING) { targetObject = currentToken.getValue(); }
					if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("TO")) { continue; }
				}

				// if this is a constraint or column then the first normal/string is the original name and the second normal/string is the new name
				if (alterType == PROCESSING_COLUMN || alterType == PROCESSING_CONSTRAINT) {
					for (int j = 0; j < size; i++, j++) {
						currentToken = tokens.get(j);

						if (sourceObject != null && targetObject != null) { break; }
						if ((currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING) && sourceObject == null) { sourceObject = currentToken.getValue(); continue; }
						if ((currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING) && targetObject == null) { targetObject = currentToken.getValue(); continue; }
						continue;
					}
				}
			}
		}

		switch (alterType) {
			case PROCESSING_TABLE:
				tables.renameTable(sourceObject, targetObject);
				break;
			case PROCESSING_COLUMN:
				tables.renameColumn(tableName, sourceObject, targetObject);
				break;
			case PROCESSING_CONSTRAINT:
				tables.renameConstraint(sourceObject, targetObject);
				break;
		}
	}

	/*
	 * Definition:
	 *   modify [column] {column definition}						<- only really worried about situations where the datatype is changing, though if no comment is specified on a column then remove any comment on the column
	 *   modify ({column definition})								<- only really worried about situations where the datatype is changing, though if no comment is specified on a column then remove any comment on the column
	 *   change [column] {old name} {new name} {column definition}	<- really just a combo of renaming and modifying so apply those rules as necessary
	 * 
	 * Examples:
	 *   alter table x modify col1 varchar(100);
	 *   alter table x modify column col1 number comment 'asdf'
	 *   alter table x change col1 col2 varchar(100);
	 *   alter table x change col1 col2 number comment 'asdf';
	 *   alter table x change column col1 col2 number;
	 */
	private void parseAlterTableModifyAndChange(String tableName, ArrayList<TokenDetail> tokens) throws Exception  {
		final int PROCESSING_MODIFY = 1;
		final int PROCESSING_CHANGE = 2;

		final int TOKEN_MODIFY_OR_CHANGE = 1;
		final int TOKEN_OLD_NAME = 2;
		final int TOKEN_NEW_NAME = 3;
		final int TOKEN_COLUMN_TYPE = 4;
		final int TOKEN_COLUMN_EXTRA = 5;
		final int TOKEN_COMMENT_VALUE = 6;

		// store information about what we've parsed
		String oldColumnName = null;
		String newColumnName = null;
		String newColumnType = null;
		String newColumnComment = null;
		boolean newPrimaryKey = false;

		int alterType = 0;
		int nextToken = TOKEN_MODIFY_OR_CHANGE;
		int size = tokens.size();
		for (int i = 0; i < size; i++) {
			TokenDetail currentToken = tokens.get(i);

			// STEP 1: look to see if we are modifying or changing something
			if (nextToken == TOKEN_MODIFY_OR_CHANGE) {
				if (currentToken.getType() == Token.KEYWORD & currentToken.getValue().equals("MODIFY")) {
					alterType = PROCESSING_MODIFY;
					nextToken = TOKEN_OLD_NAME;
				}
				if (currentToken.getType() == Token.KEYWORD & currentToken.getValue().equals("CHANGE")) {
					alterType = PROCESSING_CHANGE;
					nextToken = TOKEN_OLD_NAME;
				}
				continue;
			}

			// STEP 2: find the old name
			if (nextToken == TOKEN_OLD_NAME) {
				if (currentToken.getType() == Token.STRING || currentToken.getType() == Token.NORMAL) {
					if (alterType == PROCESSING_MODIFY) {
						oldColumnName = Parser.cleanString(currentToken.getValue());
						newColumnName = Parser.cleanString(currentToken.getValue());
						nextToken = TOKEN_COLUMN_TYPE;
					}
					if (alterType == PROCESSING_CHANGE) {
						oldColumnName = Parser.cleanString(currentToken.getValue());
						nextToken = TOKEN_NEW_NAME;
					}
				}
				continue;
			}

			// STEP 3: find the new name
			if (nextToken == TOKEN_NEW_NAME) {
				if (currentToken.getType() == Token.STRING || currentToken.getType() == Token.NORMAL) {
					if (alterType == PROCESSING_CHANGE) {
						newColumnName = currentToken.getValue();
						nextToken = TOKEN_COLUMN_TYPE;
					}
				}
				continue;
			}

			// STEP 4: get the definition of the new column
			if (nextToken == TOKEN_COLUMN_TYPE) {
				// the first normal word after the column name is the new column data type
				if (currentToken.getType() == Token.NORMAL) {
					newColumnType = Parser.cleanString(currentToken.getValue());
				}
				continue;
			}

			if (nextToken == TOKEN_COLUMN_EXTRA) {
				// now we are looking for either comment or primary key values
				// the first string after the word "COMMENT" is the comment
				if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("COMMENT")) {
					nextToken = TOKEN_COMMENT_VALUE;
				}

				// if we see the word "primary" then this column is now a primary key
				if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("PRIMARY")) {
					newPrimaryKey = true;
				}
				continue;
			}

			if (nextToken == TOKEN_COMMENT_VALUE) {
				if (currentToken.getType() == Token.STRING) {
					newColumnComment = Parser.cleanString(Parser.removeQuotes(currentToken.getValue()));
				}
				nextToken = TOKEN_COLUMN_EXTRA;
				continue;
			}
		}

		tables.modifyColumn(tableName, oldColumnName, newColumnName, newColumnType, newColumnComment, newPrimaryKey);
	}

	private ArrayList<Element> parseColumnsAndConstraints(String tableName, ArrayList<TokenDetail> tokens) throws Exception {
		final int PROCESSING_COLUMN = 1;
		final int PROCESSING_CONSTRAINT = 2;
		final int PROCESSING_PRIMARY_KEY = 3;
		final int PROCESSING_FOREIGN_KEY_SOURCE = 4;
		final int PROCESSING_FOREIGN_KEY_TARGET = 5;
		final int PROCESSING_COMMENTS = 6;

		StringBuffer columnComment = new StringBuffer(); // column comments can span multiple tokens
		ArrayList<TokenDetail> columnTokens = new ArrayList<TokenDetail>();
		ArrayList<TokenDetail> constraintTokens = new ArrayList<TokenDetail>();
		ArrayList<TokenDetail> primaryKeyTokens = new ArrayList<TokenDetail>();
		ArrayList<TokenDetail> foreignKeySourceTokens = new ArrayList<TokenDetail>();
		ArrayList<TokenDetail> foreignKeyTargetTokens = new ArrayList<TokenDetail>();
		ArrayList<TokenDetail> commentTokens = new ArrayList<TokenDetail>();
		ArrayList<Element> elements = new ArrayList<Element>();

		int currentlyProcessing = PROCESSING_COLUMN;
		for (int i = 0; i < tokens.size(); i++) {
			TokenDetail currentToken = tokens.get(i);

			// if this is a comment then put it together with any other comments we may have
			if (currentToken.getType() == Token.LINE_COMMENT || currentToken.getType() == Token.BLOCK_COMMENT) {
				if (columnComment.length() > 0) { columnComment.append(" "); }
				columnComment.append(currentToken.getValue());
				continue;
			}

			if (currentToken.getType() == Token.KEYWORD) {
				// if we see the word "key" and we aren't already processing a constraint or a primary/foreign key then ignore it
				if (currentToken.getValue().equals("KEY") &&
						currentlyProcessing != PROCESSING_CONSTRAINT &&
						currentlyProcessing != PROCESSING_PRIMARY_KEY &&
						currentlyProcessing != PROCESSING_FOREIGN_KEY_SOURCE &&
						currentlyProcessing != PROCESSING_FOREIGN_KEY_TARGET) {
					currentlyProcessing = 0;
				}
				// if we see a unique constraint then we don't care
				if (currentToken.getValue().equals("UNIQUE")) {
					currentlyProcessing = 0;
				}
				// these are what we're interested in
				if (currentToken.getValue().equals("CONSTRAINT")) { currentlyProcessing = PROCESSING_CONSTRAINT; }
				if (currentToken.getValue().equals("PRIMARY"))    { currentlyProcessing = PROCESSING_PRIMARY_KEY; }
				if (currentToken.getValue().equals("FOREIGN"))    { currentlyProcessing = PROCESSING_FOREIGN_KEY_SOURCE; }
				if (currentToken.getValue().equals("REFERENCES")) { currentlyProcessing = PROCESSING_FOREIGN_KEY_TARGET; }
				if (currentToken.getValue().equals("COMMENT"))    { currentlyProcessing = PROCESSING_COMMENTS; }
			}

			switch (currentlyProcessing) {
				case PROCESSING_COLUMN:
					columnTokens.add(currentToken);
					break;
				case PROCESSING_CONSTRAINT:
					constraintTokens.add(currentToken);
					break;
				case PROCESSING_PRIMARY_KEY:
					primaryKeyTokens.add(currentToken);
					break;
				case PROCESSING_FOREIGN_KEY_SOURCE:
					foreignKeySourceTokens.add(currentToken);
					break;
				case PROCESSING_FOREIGN_KEY_TARGET:
					foreignKeyTargetTokens.add(currentToken);
					break;
				case PROCESSING_COMMENTS:
					commentTokens.add(currentToken);
					break;

			}
		}

		String columnName = null;
		String columnType = null;
		boolean isNullable = true;
		String constraintName = null;
		ArrayList<String> primaryKeyColumns = new ArrayList<String>();
		ArrayList<String> foreignKeySourceColumns = new ArrayList<String>();
		ArrayList<String> foreignKeyTargetColumns = new ArrayList<String>();
		String foreignKeyTargetTable = null;

		// process any column tokens first
		if (columnTokens.size() > 0) {
			final int T_COLUMN_NAME = 1;
			final int T_COLUMN_TYPE = 2;
			final int T_COLUMN_NOT_NULL = 3;
			int nextToken = T_COLUMN_NAME;

			for (int i = 0; i < columnTokens.size(); i++)
			{
				TokenDetail currentToken = columnTokens.get(i);

				// Retrieve the column name
				if (nextToken == T_COLUMN_NAME)
					if (currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING)
					{
						columnName = Parser.cleanString(Parser.removeQuotes(currentToken.getValue()));
						nextToken = T_COLUMN_TYPE;
						continue;
					}

				// Retrieve the column type
				if (nextToken == T_COLUMN_TYPE)
					if (currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING)
					{
						columnType = Parser.cleanString(Parser.removeQuotes(currentToken.getValue()));
						nextToken = T_COLUMN_NOT_NULL;
						continue;
					}
				
				// Retrieve that the column is not null
				if (nextToken == T_COLUMN_NOT_NULL)
					if (currentToken.getValue().equals("NOT") && i < columnTokens.size()-1)
						if (columnTokens.get(i+1).getValue().equals("NULL"))
							{ isNullable = false; continue; }
			}

			elements.add(new Column(columnName, tableName, columnType, isNullable));
		}

		// look for comments in the command
		if (commentTokens.size() >= 2) {
			// the first set of quotes immediately following the word "COMMENT" is our comment
			// tack it onto the list of comments and ignore anything after it

			TokenDetail currentToken = commentTokens.get(0);
			if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("COMMENT")) {
				currentToken = commentTokens.get(1);
				if (currentToken.getType() == Token.STRING) {
					if (columnComment.length() > 0) { columnComment.append(" "); }
					columnComment.append(currentToken.getValue());
				}
			}
		}

		// look to see if we have a constraint name
		if (constraintTokens.size() >= 2) {
			// the first normal/string word following the word "CONSTRAINT" is the constraint's name
			// if that column isn't a normal/string column then we have no constraint name
			TokenDetail currentToken = constraintTokens.get(0);
			if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("CONSTRAINT")) {
				currentToken = constraintTokens.get(1);
				if (currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING){
					constraintName = Parser.cleanString(Parser.removeQuotes(currentToken.getValue()));
				}
			}
		}

		// look for primary key constraints
		if (primaryKeyTokens.size() > 0) {
			// ensure that the first token is the word "PRIMARY"
			// immediately after the word "PRIMARY" we may see the word "KEY"; if we don't then we are done and we have a primary key on the current column
			// somewhere after the word "KEY" we may see a list of columns in parentheses; if we don't then we are done and we have a primary key on the current column
			TokenDetail currentToken = primaryKeyTokens.get(0);
			if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("PRIMARY")) {
				// if there is no token after the word "PRIMARY" or that token is NOT the word "KEY" then the current column is a primary key and we are done
				if (!(primaryKeyTokens.size() > 1 && primaryKeyTokens.get(1).getType() == Token.KEYWORD && primaryKeyTokens.get(1).getValue().equals("KEY"))) {
					if (columnName != null) {
						primaryKeyColumns.add(Parser.cleanString(Parser.removeQuotes(columnName)));
					}
				} else {
					// going to start after the word "KEY"
					int countParentheses = 0;
					for (int i = 2; i < primaryKeyTokens.size(); i++) {
						currentToken = primaryKeyTokens.get(i);

						// if we've run into a keyword then we are done
						if (currentToken.getType() == Token.KEYWORD) { break; }

						// if we have a parentheses count it
						if (currentToken.getType() == Token.SPECIAL_SEQUENCE && (currentToken.getValue().equals("(") || currentToken.getValue().equals(")"))) {
							if (currentToken.getValue().equals("(")) { countParentheses++; }
							if (currentToken.getValue().equals(")")) { countParentheses++; }
							if (countParentheses == 0) { break; }
							continue;
						}

						// if we have a comma then skip it
						if (currentToken.getType() == Token.SPECIAL_SEQUENCE && currentToken.getValue().equals(",")) { continue; }

						// if we have one parentheses and we see a normal/string then add it to our list
						if (countParentheses == 1 && (currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING)) {
							primaryKeyColumns.add(Parser.cleanString(Parser.removeQuotes(currentToken.getValue())));
						}
					}

					// if after all that we don't have any columns then the column is the current one
					if (primaryKeyColumns.size() == 0 && columnName != null) {
						primaryKeyColumns.add(Parser.cleanString(Parser.removeQuotes(columnName)));
					}
				}
			}

			elements.add(new PrimaryKey(constraintName, tableName, primaryKeyColumns));
		}
		

		// look for any foreign key source information
		if (foreignKeySourceTokens.size() >= 2) {
			// ensure that our first two tokens are the words "FOREIGN KEY"
			// after that the first set of parentheses will contain our column list. this will always exist.
			if (foreignKeySourceTokens.get(0).getType() == Token.KEYWORD && foreignKeySourceTokens.get(0).getValue().equals("FOREIGN") &&
				foreignKeySourceTokens.get(1).getType() == Token.KEYWORD && foreignKeySourceTokens.get(1).getValue().equals("KEY")) {
				// look for the first list of source columns
				// going to start after the word "KEY"
				int countParentheses = 0;
				for (int i = 2; i < foreignKeySourceTokens.size(); i++) {
					TokenDetail currentToken = foreignKeySourceTokens.get(i);

					// if we have a parentheses count it
					if (currentToken.getType() == Token.SPECIAL_SEQUENCE && (currentToken.getValue().equals("(") || currentToken.getValue().equals(")"))) {
						if (currentToken.getValue().equals("(")) { countParentheses++; }
						if (currentToken.getValue().equals(")")) { countParentheses++; }
						if (countParentheses == 0) { break; }
						continue;
					}

					// if we have a comma then skip it
					if (currentToken.getType() == Token.SPECIAL_SEQUENCE && currentToken.getValue().equals(",")) { continue; }

					// if we have one parentheses and we see a normal/string then add it to our list
					if (countParentheses == 1 && (currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING)) {
						foreignKeySourceColumns.add(Parser.cleanString(Parser.removeQuotes(currentToken.getValue())));
					}
				}
			}
		}

		// look for any foreign key target information
		if (foreignKeyTargetTokens.size() >= 2) {
			// ensure that the first token is the word "REFERENCES"
			// after the word references, the first string/normal/. will contain the target table name
			// the first set of parentheses after this word will contain the target column list
			// if there are no parentheses after this word before we hit the end or another keyword (like INDEX, KEY, CHECK) then the foreign key is on the column list found in the foreign key
			// if there is no foreign key column list then the foreign key is on the column defined earlier (either the source list or the column definition)
			if (foreignKeyTargetTokens.get(0).getType() == Token.KEYWORD && foreignKeyTargetTokens.get(0).getValue().equals("REFERENCES")) {
				// create a string buffer and loop through until i see something other than a string, a normal, or a period
				StringBuffer tableNameBuffer = new StringBuffer();
				for (int i = 1; i < foreignKeyTargetTokens.size(); i++) {
					TokenDetail currentToken = foreignKeyTargetTokens.get(i);

					// if this is a string, a normal, or a period, add it to our list
					if (currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING || currentToken.getValue().equals(".")) {
						tableNameBuffer.append(currentToken.getValue());
						continue;
					}

					// if this is a comment, skip it and continue
					if (currentToken.getType() == Token.LINE_COMMENT || currentToken.getType() == Token.BLOCK_COMMENT) {
						continue;
					}

					// if this is a keyword and we have no table name yet, try the next token
					if (currentToken.getType() == Token.KEYWORD && tableNameBuffer.length() == 0) {
						continue;
					}

					// if we didn't add it to our list (or skip it), then back up our list by one and get out of it
					i = (i - 1);
					break;
				}

				foreignKeyTargetTable = Parser.cleanString(Parser.removeSchema(Parser.removeQuotes(tableNameBuffer.toString())));
				if (foreignKeyTargetTable == null) { throw new Exception("Could not find the name of the remote table."); }

				int countParentheses = 0;
				for (int i = 1; i < foreignKeyTargetTokens.size(); i++) {
					TokenDetail currentToken = foreignKeyTargetTokens.get(i);

					// if we have a parentheses count it
					if (currentToken.getType() == Token.SPECIAL_SEQUENCE && (currentToken.getValue().equals("(") || currentToken.getValue().equals(")"))) {
						if (currentToken.getValue().equals("(")) { countParentheses++; }
						if (currentToken.getValue().equals(")")) { countParentheses++; }
						if (countParentheses == 0) { break; }
						continue;
					}

					// if we have a comma then skip it
					if (currentToken.getType() == Token.SPECIAL_SEQUENCE && currentToken.getValue().equals(",")) { continue; }

					// if we have one parentheses and we see a normal/string then add it to our list
					if (countParentheses == 1 && (currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING)) {
						foreignKeyTargetColumns.add(Parser.cleanString(Parser.removeQuotes(currentToken.getValue())));
					}
				}

			}

			// try to put together foreign keys now
			try {
				if (foreignKeyTargetTable != null && (foreignKeySourceColumns.size() > 0 || foreignKeyTargetColumns.size() > 0)) {
					// if we don't have any source columns then the source column is our current column name
					if (foreignKeySourceColumns.size() == 0 && columnName != null) { foreignKeySourceColumns.add(columnName); }
					// if we don't have any target columns then the target column is our current column name
					if (foreignKeyTargetColumns.size() == 0 && columnName != null) { foreignKeyTargetColumns.add(columnName); }
					// add the foreign key already
					elements.add(new ForeignKey(constraintName, tableName, foreignKeySourceColumns, foreignKeyTargetTable, foreignKeyTargetColumns));
				}
			} catch (Exception e) {
				System.out.println("Failed to add foreign key: " + e.getMessage());
			}
		}

		// add any comments we may have derived and this is a column definition
		if (columnComment.length() > 0 && columnName != null) {
			elements.add(new Comment(tableName, columnName, columnComment.toString()));
		}

		return elements;
	}

	/*
	 * This should match statements like this:
	 *   DROP TABLE a;
	 *   DROP TEMPORARY TABLE b;
	 *   DROP TABLE IF EXISTS c;
	 *   DROP TABLE d CASCADE;
	 *   DROP TABLE asdf.fdsa;
	 *   DROP TABLE e, f, g;
	 *   DROP TABLE `asdf`.`asdf`;
	 */
	private void parseCommandDrop(String command) throws Exception {
		final int TOKEN_DROP = 1;
		final int TOKEN_TABLE = 2;
		final int TOKEN_NAME = 3;

		// parse our drop statement with the list of keywords that we know of
		ArrayList<String> keywords = new ArrayList<String>();
		keywords.add("drop");
		keywords.add("temporary");
		keywords.add("table");
		keywords.add("hierarchy");
		keywords.add("if");
		keywords.add("exists");
		ArrayList<TokenDetail> tokens = Parser.parseForKeywords(command, keywords, false);

		// store information about what we've parsed
		ArrayList<String> tableNames = new ArrayList<String>();

		int nextToken = TOKEN_DROP;
		int size = tokens.size();
		for (int i = 0; i < size; i++) {
			TokenDetail currentToken = tokens.get(i);

			// STEP 1: look to see if we are dropping something
			if (nextToken == TOKEN_DROP) {
				if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("DROP")) {
					nextToken = TOKEN_TABLE;
				}
				continue;
			}

			// STEP 2: look to see if we are dropping a table
			if (nextToken == TOKEN_TABLE) {
				if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("TABLE")) {
					nextToken = TOKEN_NAME;
				}
				continue;
			}

			// STEP 3: find the names of the tables we are dropping
			if (nextToken == TOKEN_NAME) {
				// create a string buffer and loop through it until i see something other than a string, a normal, or a period
				StringBuffer tableNameBuffer = new StringBuffer();
				for (int j = i; j < size; i++, j++) {
					currentToken = tokens.get(j);

					// if we see two tokens in a row that are either normal or string then we are done parsing for table names
					// this is because two tokens of this type in a row means we have something like this:
					//	DROP TABLE x CASCADE CONSTRAINTS;
					// this might also be followed by some other hullabaloo that we aren't prepared for,
					// whereas we should be looking for something like this if we have more than one table name
					//  DROP TABLE x, y;
					// the second example has two tokens of the same type separated by a different type
					if ((tokens.get(j).getType() == Token.NORMAL || tokens.get(j).getType() == Token.STRING) &&
						(tokens.get(j - 1).getType() == Token.NORMAL || tokens.get(j - 1).getType() == Token.STRING)) {
						// we saw two of the same tokens in a row soooooo we are done looking for tokens
						nextToken = 0;
						break;
					}

					// if this is a string, a normal, or a period, add it to our list
					if (currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING || currentToken.getValue().equals(".")) {
						tableNameBuffer.append(currentToken.getValue());
						continue;
					}

					// if this is a comment, skip it and continue
					if (currentToken.getType() == Token.LINE_COMMENT || currentToken.getType() == Token.BLOCK_COMMENT) {
						continue;
					}

					// if this is a keyword and we have no table name yet, try the next token
					if (currentToken.getType() == Token.KEYWORD && tableNameBuffer.length() == 0) {
						continue;
					}

					// if this is a comma then continue on until we find the next field
					if (currentToken.getType() == Token.SPECIAL_SEQUENCE && currentToken.getValue().equals(",")) {
						break;
					}

					// if we didn't add it to our list (or skip it), then back up our list by one and get out of it
					i = (i - 1);
					break;
				}

				String tableName = Parser.cleanString(Parser.removeSchema(Parser.removeQuotes(tableNameBuffer.toString())));
				try {
					if (tableName == null) { throw new Exception("Could not find table name."); }
					tableNames.add(tableName);
				} catch (Exception e) {
					System.out.println("Failed to drop table: " + e.getMessage());
				}
				continue;
			}
		}

		// STEP 4: attempt to drop tables
		tables.dropTables(tableNames);
	}

	/*
	 * This should match statements like this:
	 *   COMMENT ON TABLE foo IS "this is a table";
	 *   COMMENT ON TABLE asdf.fdsa IS "this is a table in a specified schema";
	 *   COMMENT ON COLUMN asdf.fdsa.col1 IS "this is a column in a specified table in a specified schema";
	 *   COMMENT ON COLUMN fdsa.col1 IS "this column is in a table in the default schema";
	 */
	private void parseCommandComment(String command) throws Exception {
		final int COMMENT_ON = 1;
		final int COMMENT_ON_TABLE = 2;
		final int COMMENT_ON_COLUMN = 3;
		final int COMMENT_VALUE = 4;

		// parse our comment statement with the list of keywords that we know of
		ArrayList<String> keywords = new ArrayList<String>();
		keywords.add("comment");
		keywords.add("on");
		keywords.add("table");
		keywords.add("column");
		keywords.add("is");
		ArrayList<TokenDetail> tokens = Parser.parseForKeywords(command, keywords, false);

		int nextToken = 0;
		int commentType = 0;
		String tableName = null;
		String columnName = null;

		int size = tokens.size();
		for (int i = 0; i < size; i++) {
			TokenDetail currentToken = tokens.get(i);

			// STEP 1: look for the command to actually comment on things (looking for 'COMMENT')
			// the command will be in the first and second token
			if (currentToken.getType() == Token.KEYWORD && (currentToken.getValue().equals("COMMENT") || currentToken.getValue().equals("ON"))) {
				nextToken = COMMENT_ON;
				continue;
			}

			// STEP 2: look for the type of table we are going to comment on
			if (nextToken == COMMENT_ON) {
				if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("TABLE")) {
					nextToken = COMMENT_ON_TABLE;
					commentType = COMMENT_ON_TABLE;
					continue;
				}
				if (currentToken.getType() == Token.KEYWORD && currentToken.getValue().equals("COLUMN")) {
					nextToken = COMMENT_ON_COLUMN;
					commentType = COMMENT_ON_COLUMN;
					continue;
				}
			}

			// STEP 3: look for the schema/table/column to be commented upon
			if (nextToken == COMMENT_ON_TABLE || nextToken == COMMENT_ON_COLUMN) {
				StringBuffer nameBuffer = new StringBuffer();
				for (int j = i; j < size; i++, j++) {
					currentToken = tokens.get(j);

					// if this is a string, a normal, or a period, add it to our list
					if (currentToken.getType() == Token.NORMAL || currentToken.getType() == Token.STRING || currentToken.getValue().equals(".")) {
						nameBuffer.append(currentToken.getValue());
						continue;
					}

					// if this is a comment, skip it and continue
					if (currentToken.getType() == Token.LINE_COMMENT || currentToken.getType() == Token.BLOCK_COMMENT) {
						continue;
					}

					// if this is a keyword and we have no table name yet, try the next token
					if (currentToken.getType() == Token.KEYWORD && nameBuffer.length() == 0) {
						continue;
					}

					// if we didn't add it to our list, then we are done
					break;
				}

				if (nameBuffer.length() == 0) { throw new Exception("Could not find table name for comment."); }
				String[] namePieces = Parser.cleanString(Parser.removeQuotes(nameBuffer.toString())).split("\\.");

				if (nextToken == COMMENT_ON_TABLE) {
					if (namePieces.length < 1 || namePieces.length > 2) { throw new Exception("Table name '" + Parser.cleanString(Parser.removeQuotes(nameBuffer.toString())) + "' is not valid."); }
					tableName = namePieces[namePieces.length - 1];
				}
				if (nextToken == COMMENT_ON_COLUMN) {
					if (namePieces.length < 2 || namePieces.length > 3) { throw new Exception("Table name '" + Parser.cleanString(Parser.removeQuotes(nameBuffer.toString())) + "' is not valid."); }
					tableName = namePieces[namePieces.length - 2];
					columnName = namePieces[namePieces.length - 1];
				}
				nextToken = COMMENT_VALUE;
				continue;
			}

			// STEP 4: look for the actual comment and add it to the table
			if (nextToken == COMMENT_VALUE && currentToken.getType() == Token.STRING) {
				String commentText = Parser.removeQuotes(currentToken.getValue());
				switch (commentType) {
					case COMMENT_ON_TABLE:
						tables.addComment(new Comment(tableName, commentText));
						break;
					case COMMENT_ON_COLUMN:
						tables.addComment(new Comment(tableName, columnName, commentText));
						break;
				}
				//if (commentType == COMMENT_ON_TABLE) { tables.addComment(tableName, comment); }
				//if (commentType == COMMENT_ON_COLUMN) { tables.addComment(tableName, columnName, comment); }
			}
		}
	}

	public Tables getTables() {
		return tables;
	}

	public static String cleanString(String value) {
		if (value == null) { return null; }
		return value.toUpperCase();
	}

	public static String removeSchema(String value) {
		if (value == null) { return null; }
		if (value.equals(".")) { return value; }
		String[] tableNameParts = value.split("\\.");
		value = tableNameParts[tableNameParts.length - 1];
		value = value.trim();
	
		// if we have nothing left then make that none
		if (value.length() == 0) { return null; }
		return value;
	}

	public static String removeQuotes(String value) {
		value = value.replaceAll("'", "");
		value = value.replaceAll("\"", "");
		value = value.replaceAll("`", "");
		value = value.trim();
	
		// if we have nothing left then make that known
		if (value.length() == 0) { return null; }
		return value;
	}
}