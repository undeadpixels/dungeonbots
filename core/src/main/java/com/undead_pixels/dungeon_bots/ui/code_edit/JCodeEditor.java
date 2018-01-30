/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.StyledDocument;

import jsyntaxpane.SyntaxStyle;
import jsyntaxpane.SyntaxStyles;
import jsyntaxpane.TokenType;

/**
 * @author Wesley
 *
 */
public class JCodeEditor extends JPanel {

	private EditabilityChart _Editable;
	private StyledDocument _Document;
	private Map<TokenType, SyntaxStyle> _Styles;

	public JCodeEditor() {

		_Styles = makeDefaultStyles();
		for (TokenType key : _Styles.keySet())
			SyntaxStyles.getInstance().put(key, _Styles.get(key));
		
		JEditorPane editor = new JEditorPane();
		editor.setContentType("text/lua");
		editor.setText("-- this is a test\n\n"
				+ "function f()\n"
				+ "    foo()\n"
				+ "    bar = baz * 16\n"
				+ "    s = \"str\" .. 1\n"
				+ "    if true then\n"
				+ "        print(\"something was true\")\n"
				+ "    end\n"
				+ "end\n");
		

	}

	private static Map<TokenType, SyntaxStyle> makeDefaultStyles() {
		HashMap<TokenType, SyntaxStyle> styles = new HashMap<TokenType, SyntaxStyle>();

		// Comments as described in original JSyntaxPane lib

		// Language operators
		styles.put(TokenType.OPERATOR, new SyntaxStyle(Color.WHITE, false, false));

		// Delimiters. Constructs that are not necessarily operators for a
		// language
		styles.put(TokenType.DELIMITER, new SyntaxStyle(Color.ORANGE, false, false));

		// language reserved keywords
		styles.put(TokenType.KEYWORD, new SyntaxStyle(Color.BLUE, false, false));

		// Other language reserved keywords, like C #defines
		styles.put(TokenType.KEYWORD2, new SyntaxStyle(Color.CYAN, true, true));

		// identifiers, variable names, class names
		styles.put(TokenType.IDENTIFIER, new SyntaxStyle(Color.PINK, true, true));

		// numbers in various formats
		styles.put(TokenType.NUMBER, new SyntaxStyle(Color.GREEN, true, true)); ///

		// String
		styles.put(TokenType.STRING, new SyntaxStyle(Color.GREEN, true, true));

		// For highlighting meta chars within a String
		styles.put(TokenType.STRING2, new SyntaxStyle(Color.GREEN, true, true));

		// comments
		styles.put(TokenType.COMMENT, new SyntaxStyle(Color.GREEN, true, true));

		// special stuff within comments
		styles.put(TokenType.COMMENT2, new SyntaxStyle(Color.GREEN, true, true));

		// regular expressions
		styles.put(TokenType.REGEX, new SyntaxStyle(Color.GREEN, true, true));

		// special chars within regular expressions
		styles.put(TokenType.REGEX2, new SyntaxStyle(Color.GREEN, true, true));

		// Types, usually not keywords, but supported by the language
		styles.put(TokenType.TYPE, new SyntaxStyle(Color.GREEN, true, true));

		// Types from standard libraries
		styles.put(TokenType.TYPE2, new SyntaxStyle(Color.GREEN, true, true));

		// Types for users
		styles.put(TokenType.TYPE3, new SyntaxStyle(Color.GREEN, true, true));

		// any other text
		styles.put(TokenType.DEFAULT, new SyntaxStyle(Color.GREEN, true, true));

		// Text that should be highlighted as a warning
		styles.put(TokenType.WARNING, new SyntaxStyle(Color.GREEN, true, true));

		// Text that signals an error
		styles.put(TokenType.ERROR, new SyntaxStyle(Color.GREEN, true, true));

		return styles;
	}

	
	public void setContents(String contents, EditabilityChart editable) {
		this._Editable = editable;

		SyntaxStyles.getInstance().put(TokenType.COMMENT, new SyntaxStyle(Color.cyan, true, true));
		JEditorPane editor = new JEditorPane();
		editor.setContentType("text/lua");

	}

}
