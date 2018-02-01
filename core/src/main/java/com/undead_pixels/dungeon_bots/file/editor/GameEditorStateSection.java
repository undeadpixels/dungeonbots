package com.undead_pixels.dungeon_bots.file.editor;

import java.text.ParseException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An arbitrary section of Lua code that is formatted in a specific way that the editor can parse
 * and know that it will produce consistent results.
 * 
 * This (inside of GameEditorState) can be used to both load and save levels, without running the full world.
 */
public abstract class LevelScriptSection {
	
	/**
	 * @return	A lua representation of this section
	 */
	public abstract String toLua();
	
	/**
	 * @param luaCode
	 * @throws ParseException
	 */
	public abstract void updateFromLuaStrings(String[] luaCode) throws ParseException;
	
	/**
	 * Extract a list of matches from text, given a regex
	 * 
	 * @param regex	The regex to use, containing groups
	 * @param text	The text to extract from
	 * @return		A list of all matches found
	 */
	public static String[] extractGroupsFromText(String regex, String text) {
		return extractGroupsFromText(Pattern.compile(regex), text);
	}
	
	/**
	 * Extract a list of matches from text, given a regex
	 * 
	 * @param p		The Pattern to use, containing groups
	 * @param text	The text to extract from
	 * @return		A list of all matches found
	 */
	public static String[] extractGroupsFromText(Pattern p, String text) {
		Matcher m = p.matcher(text);
		return extractGroupsFromText(m);
	}
	
	/**
	 * Extract a list of matches from text, given a regex
	 * 
	 * @param m		The Matcher created by matching a regex against some text
	 * @return		A list of all matches found
	 */
	public static String[] extractGroupsFromText(Matcher m) {
		if(m.matches()) {
			String[] ret = new String[m.groupCount()];

			for(int i = 0; i < ret.length; i++) {
				ret[i] = m.group(i+1);
			}

			return ret;
		} else {
			return new String[] {};
		}
	}

	/**
	 * Looks up an int in an array of strings
	 * 
	 * @param strs			String array to perform the lookup
	 * @param idx			Index within the string array
	 * @param defaultValue	The value to return if anything fails
	 * @return				The string at idx, converted to an int (or defaultValue if something failed)
	 */
	public static int intAt(String[] strs, int idx, int defaultValue) {
		if(idx >= strs.length) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(strs[idx]);
		} catch(NumberFormatException ex) {
			return defaultValue;
		}
	}
	
	/**
	 * Looks up an float in an array of strings
	 * 
	 * @param strs			String array to perform the lookup
	 * @param idx			Index within the string array
	 * @param defaultValue	The value to return if anything fails
	 * @return				The string at idx, converted to an float (or defaultValue if something failed)
	 */
	public static float floatAt(String[] strs, int idx, float defaultValue) {
		if(idx >= strs.length) {
			return defaultValue;
		}
		try {
			return Float.parseFloat(strs[idx]);
		} catch(NumberFormatException ex) {
			return defaultValue;
		}
	}
	
	/**
	 * Looks up an String in an array of strings
	 * 
	 * @param strs			String array to perform the lookup
	 * @param idx			Index within the string array
	 * @param defaultValue	The value to return if anything fails
	 * @return				The string at idx (or defaultValue if something failed)
	 */
	public static String stringAt(String[] strs, int idx, String defaultValue) {
		if(idx >= strs.length) {
			return defaultValue;
		}
		
		return strs[idx];
	}
	
	/**
	 * @param indentationLevel	How far to indent
	 * @return	Enough whitespace for the given indentation level
	 */
	public static String tab(int indentationLevel) {
		String ret = "";
		for(int i = 0; i < indentationLevel; i++) {
			ret += "  ";
		}
		
		return ret;
	}
}