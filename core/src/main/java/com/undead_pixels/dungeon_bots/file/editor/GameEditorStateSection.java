package com.undead_pixels.dungeon_bots.file.editor;

import java.text.ParseException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class GameEditorStateSection {
	public abstract String toLua();
	public abstract void updateFromLuaString(String[] luaCode) throws ParseException;
	
	public static String[] extract(String regex, String text) {
		return extract(Pattern.compile(regex), text);
	}
	public static String[] extract(Pattern p, String text) {
		Matcher m = p.matcher(text);
		return extract(m);
	}
	public static String[] extract(Matcher m) {
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
	public static String stringAt(String[] strs, int idx, String defaultValue) {
		if(idx >= strs.length) {
			return defaultValue;
		}
		
		return strs[idx];
	}
	
	public static String tab(int indentation) {
		String ret = "";
		for(int i = 0; i < indentation; i++) {
			ret += "  ";
		}
		
		return ret;
	}
}