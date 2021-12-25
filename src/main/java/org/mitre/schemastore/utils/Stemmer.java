// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.schemastore.utils;

/**
 * The Stemmer class transforms a word into its root form.
 * Implementing the Porter Stemming Algorithm
 * Porter, 1980, An algorithm for suffix stripping, Program, Vol. 14, no. 3, pp 130-137,
 */
public class Stemmer
{ 
	/** Stores the word being stemmed */
	private char[] word;

	/** Stores the current pointer to the word */
	private int ptr;
	
	/** Stores the pointer to the last character */
	private int lastChar;

	/** Constructs the stemmer */
	private Stemmer(String word)
	{
		lastChar = word.length()-1;
		this.word = new char[lastChar+50];
		for(int i=0; i<word.length(); i++) this.word[i] = word.charAt(i);
	}

	/** Indicates if the specified letter is a consonant */
	private final boolean isConsonant(int ptr)
	{
		switch (word[ptr])
		{
			case 'a': case 'e': case 'i': case 'o': case 'u': return false;
			case 'y': return (ptr==0) ? true : !isConsonant(ptr-1);
			default: return true;
		}
	}
	/** Indicates the number of vowel/consonant cycles which occur in the specified stem */
	private final int getVowelConsonantCycles(int stemEnd)
	{
		int count=0;
		boolean lastIsConsonant=true;
		for(int ptr=0; ptr<stemEnd; ptr++)
		{
			// Note changes between consonant and vowels
			boolean isConsonant = isConsonant(ptr);
			if(isConsonant!=lastIsConsonant)
			{
				// Mark if vowel/consonant cycle completes
				if(isConsonant) count++;
				lastIsConsonant = isConsonant;
			}
		}
		return count;
	}

	/** Indicates if there is a vowel in the specified stem */
	private final boolean containsVowel(int stemEnd)
	{
		for (int ptr=0; ptr<=stemEnd; ptr++)
			if (!isConsonant(ptr)) return true;
		return false;
	}

	/** Indicates the existence of a double consonant */
	private final boolean isDoubleConsonant(int ptr)
	{
		if (ptr<1 || word[ptr]!=word[ptr-1]) return false;
		return isConsonant(ptr);
	}

	/**
	 * Indicates if a consonant-vowel-consonant pattern exists and that the second
	 * consonant is not w, x, or y. this is used when trying to restore an e at the
	 * end of a short word. e.g. cav(e), lov(e), hop(e), crim(e), but snow, box, tray.
	 */
	private final boolean hasCVCPattern(int ptr)
	{
		if (ptr<2 || !isConsonant(ptr) || isConsonant(ptr-1) || !isConsonant(ptr-2)) return false;
		if (word[ptr]=='w' || word[ptr]=='x' || word[ptr]=='y') return false;
		return true;
	}

	private final boolean ends(String s)
	{
		int l = s.length();
		int o = lastChar-l+1;
		if (o < 0) return false;
		for (int i = 0; i < l; i++) if (word[o+i] != s.charAt(i)) return false;
		ptr = lastChar-l;
		return true;
	}

	/* setto(s) sets (j+1),...k to the characters in the string s, readjusting k. */
	private final void setto(String s)
	{
		int l = s.length();
		int o = ptr+1;
		for (int i = 0; i < l; i++) word[o+i] = s.charAt(i);
		lastChar = ptr+l;
	}

	/* r(s) is used further down. */
	private final void r(String s) { if (getVowelConsonantCycles(ptr) > 0) setto(s); }

	/* step1() gets rid of plurals and -ed or -ing. e.g.
       caresses  ->  caress
       ponies    ->  poni
       ties      ->  ti
       caress    ->  caress
       cats      ->  cat

       feed      ->  feed
       agreed    ->  agree
       disabled  ->  disable

       matting   ->  mat
       mating    ->  mate
       meeting   ->  meet
       milling   ->  mill
       messing   ->  mess

       meetings  ->  meet
	 */
	private final void step1()
	{ 
		if (word[lastChar] == 's')
		{
			if (ends("sses")) lastChar -= 2;
			else if (ends("ies")) setto("i");
			else if (word[lastChar-1] != 's') lastChar--;
		}
		
		if (ends("eed")) { if (getVowelConsonantCycles(ptr) > 0) lastChar--; }
		else if ((ends("ed") || ends("ing")) && containsVowel(ptr))
		{
			lastChar = ptr;
			if (ends("at")) setto("ate");
			else if (ends("bl")) setto("ble");
			else if (ends("iz")) setto("ize");
			else if (isDoubleConsonant(lastChar))
			{
				lastChar--;
				int ch = word[lastChar];
				if (ch == 'l' || ch == 's' || ch == 'z') lastChar++;
			}
			else if (getVowelConsonantCycles(ptr) == 1 && hasCVCPattern(lastChar)) setto("e");
		}
	}

	/* step2() turns terminal y to i when there is another vowel in the stem. */
	private final void step2() { if (ends("y") && containsVowel(ptr)) word[lastChar] = 'i'; }

	/* step3() maps double suffices to single ones. so -ization ( = -ize plus
   	   -ation) maps to -ize etc. note that the string before the suffix must give
   	   m() > 0. */
	private final void step3()
	{
		if (lastChar == 0) return;
		switch (word[lastChar-1])
		{
    		case 'a': if (ends("ational")) { r("ate"); break; }
    				  if (ends("tional")) { r("tion"); break; }
    				  break;
    		case 'c': if (ends("enci")) { r("ence"); break; }
    				  if (ends("anci")) { r("ance"); break; }
    				  break;
    		case 'e': if (ends("izer")) { r("ize"); break; }
    				  break;
    		case 'l': if (ends("bli")) { r("ble"); break; }
    				  if (ends("alli")) { r("al"); break; }
    				  if (ends("entli")) { r("ent"); break; }
    				  if (ends("eli")) { r("e"); break; }
    				  if (ends("ousli")) { r("ous"); break; }
    				  break;
    		case 'o': if (ends("ization")) { r("ize"); break; }
    				  if (ends("ation")) { r("ate"); break; }
    				  if (ends("ator")) { r("ate"); break; }
    				  break;
    		case 's': if (ends("alism")) { r("al"); break; }
    				  if (ends("iveness")) { r("ive"); break; }
    				  if (ends("fulness")) { r("ful"); break; }
    				  if (ends("ousness")) { r("ous"); break; }
    				  break;
    		case 't': if (ends("aliti")) { r("al"); break; }
    				  if (ends("iviti")) { r("ive"); break; }
    				  if (ends("biliti")) { r("ble"); break; }
    				  break;
    		case 'g': if (ends("logi")) { r("log"); break; }
		}
	}

	/* step4() deals with -ic-, -full, -ness etc. similar strategy to step3. */
	private final void step4()
	{
		switch (word[lastChar])
		{
			case 'e': if (ends("icate")) { r("ic"); break; }
					  if (ends("ative")) { r(""); break; }
					  if (ends("alize")) { r("al"); break; }
					  break;
			case 'i': if (ends("iciti")) { r("ic"); break; }
					  break;
			case 'l': if (ends("ical")) { r("ic"); break; }
					  if (ends("ful")) { r(""); break; }
					  break;
			case 's': if (ends("ness")) { r(""); break; }
					  break;
		}
	}

	/** Step5: Takes off -ant, -ence etc., in context <c>vcvc<v>. */
	private final void step5()
	{
		if (lastChar == 0) return;
		switch (word[lastChar-1])
		{
			case 'a': if (ends("al")) break; return;
			case 'c': if (ends("ance")) break;
					  if (ends("ence")) break; return;
			case 'e': if (ends("er")) break; return;
			case 'i': if (ends("ic")) break; return;
			case 'l': if (ends("able")) break;
					  if (ends("ible")) break; return;
			case 'n': if (ends("ant")) break;
					  if (ends("ement")) break;
					  if (ends("ment")) break; /* element etc. not stripped before the m */
					  if (ends("ent")) break; return;
			case 'o': if (ends("ion") && ptr >= 0 && (word[ptr] == 's' || word[ptr] == 't')) break; /* j >= 0 fixes Bug 2 */
					  if (ends("ou")) break; return; /* takes care of -ous */
			case 's': if (ends("ism")) break; return;
			case 't': if (ends("ate")) break;
					  if (ends("iti")) break; return;
			case 'u': if (ends("ous")) break; return;
			case 'v': if (ends("ive")) break; return;
			case 'z': if (ends("ize")) break; return;
			default: return;
		}
		if (getVowelConsonantCycles(ptr) > 1) lastChar = ptr;
	}

	/** Step6: Removes the final -e if more than one vowel/consonant cycle exists */
	private final void step6()
	{ 
		if (word[lastChar] == 'e')
			{ if (getVowelConsonantCycles(lastChar) > 0 && !hasCVCPattern(lastChar-1)) lastChar--; }
		if (word[lastChar] == 'l' && isDoubleConsonant(lastChar) && getVowelConsonantCycles(ptr) > 1) lastChar--;
	}
	
	/** Stems the specified word */
	static public String stem(String word)
	{
		// Don't proceed if no word or a single character word
		if(word==null) return "";
		if(word.length()<=1) return word;
		
		// Perform the stemming
		Stemmer stemmer = new Stemmer(word);
		stemmer.step1();
		stemmer.step2();
		stemmer.step3();
		stemmer.step4();
		stemmer.step5();
		stemmer.step6();
		return new String(stemmer.word,0,stemmer.lastChar+1);
	}
}