/**
 * 
 */
package com.undead_pixels.dungeon_bots.utils;


/**
 * @author kevin
 *
 */
public class StringWrap {

	
	/**
	 * A stupid function to do line wrapping for JLabels, since JLabels don't support it and JTextAreas are uncooperative
	 */
	public static String wrap(String text, int cols) {
		StringBuilder sb = new StringBuilder(text);

		// https://stackoverflow.com/questions/4212675/wrap-the-string-after-a-number-of-characters-word-wise-in-java
		int i = 0;
		while (i + cols < sb.length()) {
			int newlinePlace = sb.lastIndexOf("\n", i + cols);
			if(newlinePlace > i) {
				i = newlinePlace;
			} else {
				int nextI = sb.lastIndexOf(" ", i + cols);
				if(i == nextI) {
					i += cols;
				} else {
					i = nextI;
				}
			}
			if(i == -1) break;
			sb.replace(i, i + 1, "\n");
		}
		
		String retStr = sb.toString();
		retStr = retStr.replace("<", "&lt;");
		retStr = retStr.replace(">", "&gt;");
		retStr = retStr.replace("\n", "<br>");
		return "<html>"+retStr+"</html>";
	}
}
