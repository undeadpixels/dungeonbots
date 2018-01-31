/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.awt.BorderLayout;
import java.awt.Font;
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
import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.badlogic.gdx.graphics.Color;

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.Lexer;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.SyntaxStyle;
import jsyntaxpane.SyntaxStyles;
import jsyntaxpane.TokenType;
import jsyntaxpane.syntaxkits.LuaSyntaxKit;



/**
 * @author Wesley
 *
 */
public class JCodeEditor extends JPanel implements ActionListener {

	private JEditorPane _Editor;
	private JScrollPane _EditorScroller;

	public JCodeEditor() {

		setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
		setLayout(new BorderLayout());

		/*
		 * _SyntaxStyles = makeDefaultStyles(); for (TokenType key :
		 * _SyntaxStyles.keySet()) SyntaxStyles.getInstance().put(key,
		 * _SyntaxStyles.get(key));
		 */
		
		

		JToolBar toolBar = new JToolBar();
		toolBar.add(JCodeREPL.makeButton("cut.gif", "CUT", "Cut a highlighted section", "Cut", this));
		toolBar.add(JCodeREPL.makeButton("copy.gif", "COPY", "Copy a highlighted section", "Copy", this));
		toolBar.add(JCodeREPL.makeButton("paste.gif", "PASTE", "Paste at the cursor", "Paste", this));
		toolBar.add(
				JCodeREPL.makeButton("uneditable.gif", "NOT_EDITABLE", "Selection not editable", "Not Editable", this));
		add(toolBar, BorderLayout.PAGE_START);

		_Editor = new JEditorPane();
		_EditorScroller = new JScrollPane(_Editor);
		add(_EditorScroller, BorderLayout.CENTER);
		_Editor.setEditable(true);
		_Editor.setFocusable(true);
		_Editor.setContentType("text/lua");

		// doc.setCharacterAttributes(0, 25, sas, false);
		_Editor.setText("-- this is a test\n\n" + "function f()\n" + "    foo()\n" + "    bar = baz * 16\n"

				+ "    s = \"str\" .. 1\n" + "    if true then\n" + "        print(\"something was true\")\n"
				+ "    end\n" + "end\n");

	}

	/*
	 * ================================================================
	 * JCodeEditor CONSTRUCTION MEMBERS
	 * ================================================================
	 */

	/*
	 * public void setCode(String code) { int editable[] = new int[2];
	 * editable[0] = 0; editable[1] = 25; // code.length();
	 * 
	 * setCode(code, editable); }
	 */

	public void setCode(String code) {
		/*
		 * this._Editable = editable;
		 * 
		 * _Editor.getHighlighter().removeAllHighlights();
		 * 
		 * _Editor.setText(code);
		 * 
		 * for (int i = 0; i < editable.length; i += 2) { int start =
		 * editable[i], stop = editable[i + 1];
		 * 
		 * DefaultHighlightPainter uneditablePainter = new
		 * DefaultHighlighter.DefaultHighlightPainter( java.awt.Color.PINK);
		 * 
		 * try { _Editor.getHighlighter().addHighlight(start, stop,
		 * uneditablePainter); } catch (BadLocationException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 * 
		 * }
		 */

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

}
