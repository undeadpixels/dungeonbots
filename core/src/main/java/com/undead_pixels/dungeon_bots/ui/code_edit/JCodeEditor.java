/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.text.StyledDocument;

import jsyntaxpane.SyntaxStyle;
import jsyntaxpane.SyntaxStyles;
import jsyntaxpane.TokenType;

/**
 * @author Wesley
 *
 */
public class JCodeEditor extends JPanel implements ActionListener {

	private EditabilityChart _Editable;
	private JEditorPane _Editor;
	private Map<TokenType, SyntaxStyle> _SyntaxStyles;
	private JScrollPane _EditorScroller;

	public JCodeEditor() {
		
		setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
		setLayout(new BorderLayout());
		
		_SyntaxStyles = makeDefaultStyles();
		for (TokenType key : _SyntaxStyles.keySet())
			SyntaxStyles.getInstance().put(key, _SyntaxStyles.get(key));
		
		
		JToolBar toolBar = new JToolBar();
		toolBar.add(JCodeREPL.makeButton("cut.gif", "CUT", "Cut a highlighted section", "Cut", this));
		toolBar.add(JCodeREPL.makeButton("copy.gif", "COPY", "Copy a highlighted section", "Copy", this));
		toolBar.add(JCodeREPL.makeButton("paste.gif", "PASTE", "Paste at the cursor", "Paste", this));
		add(toolBar, BorderLayout.PAGE_START);

		
		
		
		_Editor = new JEditorPane();
		_EditorScroller = new JScrollPane(_Editor);
		add(_EditorScroller,  BorderLayout.CENTER);
		_Editor.setEditable(true);
		_Editor.setFocusable(true);
		_Editor.setContentType("text/lua");
		_Editor.setText("-- this is a test\n\n"
				+ "function f()\n"
				+ "    foo()\n"
				+ "    bar = baz * 16\n"
				+ "    s = \"str\" .. 1\n"
				+ "    if true then\n"
				+ "        print(\"something was true\")\n"
				+ "    end\n"
				+ "end\n");
		
		

	}
	
	/*
	 * ================================================================
	 * JCodeEditor CONSTRUCTION MEMBERS
	 * ================================================================
	 */

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

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
