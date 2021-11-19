package org.mitre.schemastore.utils;

import java.util.ArrayList;
import java.util.Arrays;

/** Class used to token text into a list of strings */
public class Tokenizer
{
	/** Splits text into a list of words by camelCase and non-alphanumeric symbols */
	static public ArrayList<String> tokenize(String text)
	{
		text = text.replaceAll("([a-z0-9])(A-Z)","$1 $2");
		for(int j = 0; j < (text.length() - 1); j++)
			if(Character.isLowerCase(text.charAt(j)) && Character.isUpperCase(text.charAt(j+1)))
				text = text.substring(0, j+1)+" "+text.substring(j+1);
		return new ArrayList<String>(Arrays.asList(text.split("[^a-zA-Z0-9]+")));
	}
}
