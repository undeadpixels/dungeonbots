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
	private JScrollPane _EditorScroller;
	

	public JCodeEditor() {
		
		setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
		setLayout(new BorderLayout());
		
		/*_SyntaxStyles = makeDefaultStyles();
		for (TokenType key : _SyntaxStyles.keySet())
			SyntaxStyles.getInstance().put(key, _SyntaxStyles.get(key));*/
		
		
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
