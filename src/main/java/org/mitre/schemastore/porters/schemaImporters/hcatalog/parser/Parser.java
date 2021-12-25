package org.mitre.schemastore.porters.schemaImporters.hcatalog.parser;

import java.util.ArrayList;
import java.util.List;

import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.ArrayDataType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HCatRestColumn;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HiveDataType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HiveDataType.DataType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.MapDataType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.StructDataType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.UnionDataType;


import de.susebox.jtopas.CharArraySource;
import de.susebox.jtopas.Flags;
import de.susebox.jtopas.StandardTokenizer;
import de.susebox.jtopas.StandardTokenizerProperties;
import de.susebox.jtopas.Token;
import de.susebox.jtopas.Tokenizer;
import de.susebox.jtopas.TokenizerException;
import de.susebox.jtopas.TokenizerProperties;
import de.susebox.jtopas.TokenizerSource;


public class Parser {

        private String typeString;
        private static TokenizerProperties p;
        private Tokenizer tokenizer;

        
        static  {
                p = new StandardTokenizerProperties();
	        p.setParseFlags(Flags.F_NO_CASE |  Flags.F_RETURN_IMAGE_PARTS)
	        				;
	        p.setSeparators(null);

	        // keywords associated with create


	        p.addSpecialSequence("<");
	        p.addSpecialSequence(">");
	        p.addSeparators(":");
                p.addSeparators(",");

	        // these are the only commands we are looking for
	        p.addKeyword("struct", DataType.STRUCT);
	        p.addKeyword("uniontype",DataType.UNION);
	        p.addKeyword("map", DataType.MAP);
	        p.addKeyword("array", DataType.ARRAY);
                p.addKeyword("bigint", DataType.BIGINT);
                p.addKeyword("binary", DataType.BINARY);
                p.addKeyword("boolean", DataType.BOOLEAN);
                p.addKeyword("char", DataType.CHAR);
                p.addKeyword("decimal", DataType.DECIMAL);
                p.addKeyword("double", DataType.DOUBLE);
                p.addKeyword("float", DataType.FLOAT);
                p.addKeyword("int", DataType.INT);
                p.addKeyword("smallint", DataType.SMALLINT);
                p.addKeyword("string", DataType.STRING);
                p.addKeyword("timestamp", DataType.TIMESTAMP);
                p.addKeyword("tinyint", DataType.TINYINT);
                p.addKeyword("varchar", DataType.VARCHAR);
                p.addKeyword("void", DataType.VOID);

        }
        public Parser(String typeString) {
            this.typeString = typeString;
        }
        
        public synchronized void setTypeString(String typeString) {
            this.typeString = typeString;
        }
	public synchronized HiveDataType parse() throws TokenizerException {

	        // create the tokenizer
	        tokenizer = new StandardTokenizer(p);
	        TokenizerSource source = new CharArraySource(typeString.toCharArray());
                tokenizer.setSource(source);
                return processTokens();

	          
	}
        
        private HiveDataType processTokens() throws TokenizerException {
	           Token token = tokenizer.nextToken();
	            int type = token.getType();
                    if (type != Token.KEYWORD){
                        throw new RuntimeException("Not a keyword " + token.getImage());
                    }
                    DataType datatype = (DataType) token.getCompanion();
                    HiveDataType returnType;
                    int startPos = token.getStartPosition();
                    switch (datatype) {
                        case ARRAY:  returnType =processArray();
                            break;
                        case MAP : returnType =  processMap();
                              break;
                        case UNION : returnType =  processUnion();
                            break;
                        case STRUCT : returnType =  processStruct();
                            break;
                        default : returnType = datatype.getHiveDataType();
                    }
                    
                    returnType.setTypeString(typeString.substring(startPos, tokenizer.getReadPosition()));
                    return returnType;
   
        }
        private  HiveDataType processArray() throws TokenizerException {
            Token nextToken = tokenizer.nextToken();
            if (!nextToken.getImage().equals("<"))
                throw new TokenizerException("Unexpected sequence after array statement: " + nextToken.getImage());
           HiveDataType listType = processTokens();
            nextToken = tokenizer.nextToken();
            if (!nextToken.getImage().equals(">"))
                throw new TokenizerException("Unexpected sequence after array type declaration: " + nextToken.getImage());
            
            return new ArrayDataType(listType);
           
        }
        private HiveDataType processMap() throws TokenizerException {
            Token nextToken = tokenizer.nextToken();
            if (!nextToken.getImage().equals("<"))
                throw new TokenizerException("Unexpected sequence after map statement: " + nextToken.getImage());
            HiveDataType keyType = processTokens();
            nextToken = tokenizer.nextToken();
            if (!nextToken.getImage().equals(","))
            {
                throw new TokenizerException("Unexpected sequence after map key type declaration: " + nextToken.getImage());
            }
            HiveDataType valueType = processTokens();
            nextToken = tokenizer.nextToken();
            if (!nextToken.getImage().equals(">"))
                throw new TokenizerException("Unexpected sequence after map value declaration: " + nextToken.getImage());
            
            return new MapDataType(keyType, valueType);
           
        }
        private HiveDataType processUnion() throws TokenizerException {
             Token nextToken = tokenizer.nextToken();
            if (!nextToken.getImage().equals("<"))
                throw new TokenizerException("Unexpected sequence after union statement: " + nextToken.getImage());
            ArrayList<HiveDataType> uniontypes = new ArrayList<HiveDataType>();
            while (!nextToken.getImage().equals(">")){
                uniontypes.add(processTokens());
                nextToken = tokenizer.nextToken();
                if (!nextToken.getImage().equals(",") && !nextToken.getImage().equals(">"))
                    throw new TokenizerException("Unexpected sequence in union statement: " + nextToken.getImage());
            }
            
            
            return new UnionDataType(uniontypes);
        }
        private  HiveDataType processStruct() throws TokenizerException {
              Token nextToken = tokenizer.nextToken();
            if (!nextToken.getImage().equals("<"))
                throw new TokenizerException("Unexpected sequence after struct statement: " + nextToken.getImage());
            List<HCatRestColumn> structColumns = new ArrayList<HCatRestColumn>();
            while (!nextToken.getImage().equals(">")){
                String columnName = tokenizer.nextToken().getImage();
                String type = null;
                String comment = null;
                nextToken = tokenizer.nextToken();
                if (!nextToken.getImage().equals(":")) {
                    throw new TokenizerException("Unexpected sequence in struct after field name" + nextToken.getImage());
                }
                HiveDataType columnType = processTokens();
                nextToken = tokenizer.nextToken();
                if (nextToken.getImage().equalsIgnoreCase("comment")) {
                    // fill in comment field; not currently returned
                }
                HCatRestColumn current = new HCatRestColumn();
                current.setName(columnName);
                current.setHiveType(columnType);
                current.setType(columnType.getTypeString());
                current.setComment(comment);
                structColumns.add(current);
                
            }
            return new StructDataType(structColumns);
        }
        public static void main (String [] args) throws TokenizerException {
            String testString = "struct<street:string,city:string,state:string,"
                    +"zip:int, secondAddress:struct<street:string,city:string, " +
                    " state:string,zip:uniontype<int,float, char, varchar>>,"
                    + "percentages : map<string,float>, subordinates : array<string>,"+
                    " otheraddresses : array<struct<street:string,city:string,state:string,zip:int>>>";
            Parser parser = new Parser(testString);
            HiveDataType type = parser.parse();
            System.out.println(type);
        }

	
}
