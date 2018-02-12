/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.WindowListener;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputListener;
import javax.swing.text.Highlighter;

import com.undead_pixels.dungeon_bots.math.IntegerIntervalSet;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.annotations.UserScript;
import com.undead_pixels.dungeon_bots.utils.builders.UIBuilder;

import jsyntaxpane.SyntaxDocument;

/**
 * @author Wesley
 *
 */
public final class JScriptEditor extends JPanel {

	/** The script being edited. */
	UserScript _Script = null;
	private final JEditorPane _Editor;
	private final JScrollPane _EditorScroller;
	private final JToggleButton _LockButton;
	private Highlighter _Highlighter;
	private IntegerIntervalSet _Locked;
	private SecurityLevel _SecurityLevel;
	private Controller _Controller;

	private int _SelectionStart = -1;
	private int _SelectionEnd = -1;

	public JScriptEditor(SecurityLevel securityLevel) {
		_Controller = new Controller();
		_SecurityLevel = securityLevel;
		_Locked = new IntegerIntervalSet();

		setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
		setLayout(new BorderLayout());

		JToolBar toolBar = new JToolBar();
		toolBar.setPreferredSize(new Dimension(200, 30));
		JButton bttnCut = UIBuilder.makeButton("cut.jpg", "Cut a highlighted section", "CUT", _Controller);
		JButton bttnCopy = UIBuilder.makeButton("copy.jpg", "Copy a highlighted section", "COPY", _Controller);
		JButton bttnPaste = UIBuilder.makeButton("paste.jpg", "Paste at the cursor", "PASTE", _Controller);

		toolBar.add(bttnCut);
		toolBar.add(bttnCopy);
		toolBar.add(bttnPaste);
		if (securityLevel.level >= SecurityLevel.AUTHOR.level) {
			_LockButton = UIBuilder.makeToggleButton("lock.jpg", "Lock selected text", "Lock", "TOGGLE_LOCK",
					_Controller);
			_LockButton.setEnabled(false);
			toolBar.add(_LockButton);
		} else
			_LockButton = null;

		add(toolBar, BorderLayout.PAGE_START);

		_Editor = new JEditorPane();
		_EditorScroller = new JScrollPane(_Editor);
		add(_EditorScroller, BorderLayout.CENTER);
		_Editor.setEditable(true);
		_Editor.setFocusable(true);
		_Editor.setContentType("text/lua");
		_Editor.addCaretListener(_Controller);
		SyntaxDocument doc = (SyntaxDocument) _Editor.getDocument();
		doc.addDocumentListener(_Controller);
	}

	/*
	 * ================================================================
	 * JCodeEditor CONSTRUCTION MEMBERS
	 * ================================================================
	 */

	public void setScript(UserScript script) {
		_Script = script;
		_Locked = new IntegerIntervalSet(script.locks);
		_Editor.setText(script.code);
		highlightLocks();
	}

	/** Returns the code in this editor, including any markup flags. */
	public String getCodeMarked() {
		return _Editor.getText();
	}

	private void highlightLocks() {

		if (_Locked == null)
			return;
	}

	/** The controller class for the JScriptEditor. */
	private class Controller implements CaretListener, ActionListener, DocumentListener {

		/** Called when buttons are hit. */
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {

			case "TOGGLE_LOCK":
				if (_SecurityLevel.level >= SecurityLevel.AUTHOR.level) {
					if (_Locked.includes(_SelectionStart, _SelectionEnd)) {
						_Locked.remove(_SelectionStart, _SelectionEnd);
						_LockButton.setSelected(false);
					} else {
						_Locked.includes(_SelectionStart, _SelectionEnd);
						_LockButton.setSelected(true);
					}
				}
				break;
			case "CUT":
			case "COPY":
			case "PASTE":
				System.err.println("Have not implemented the command:" + e.getActionCommand());
				break;
			}

		}

		/** Called when selection changes. */
		@Override
		public void caretUpdate(CaretEvent e) {
			if (_SecurityLevel.level >= SecurityLevel.AUTHOR.level){
				_SelectionStart = e.getDot();
				_SelectionEnd = e.getMark();

				boolean isSelected = _SelectionStart < _SelectionEnd;
				if (isSelected)
					_LockButton.setSelected(_Locked.includes(_SelectionStart, _SelectionEnd));
				_LockButton.setEnabled(isSelected);
			}			
		}

		@Override
		public void changedUpdate(DocumentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	}

}
