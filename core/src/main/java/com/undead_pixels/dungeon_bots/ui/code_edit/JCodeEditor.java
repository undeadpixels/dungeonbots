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

	public static class InclusionMap {

		// Presumed to be non-inclusive until the first inflection is hit.

		private static class Inclusion {
			public int from;
			public int to;

			public Inclusion(int from, int to) {
				this.from = from;
				this.to = to;
			}

			public boolean brackets(int item) {
				return from <= item && item <= to;
			}
		}

		private ArrayList<Inclusion> _Inclusions;

		/** Factory method for creating a new, empty InclusionMap. */
		public static InclusionMap empty() {
			return new InclusionMap();
		}

		/**
		 * Factory method for creating a new, full InclusionMap from the given
		 * values.
		 */
		public static InclusionMap full(int from, int to) {
			InclusionMap result = new InclusionMap();
			result._Inclusions.add(new Inclusion(from, to));
			return result;
		}

		private InclusionMap() {
			_Inclusions = new ArrayList<Inclusion>();
		}

		private static boolean isOdd(int value) {
			return (value & 1) > 0;
		}

		public boolean include(int from, int to) {

			int fromIdx = findBracketingIndex(from, 0, _Inclusions.size());
			int toIdx = findBracketingIndex(to, (fromIdx >= 0) ? fromIdx : 0, _Inclusions.size());

			// No 'from' bracketer?
			if (fromIdx < 0) {

				// The 'to' bracketer exists, but not the 'from' bracketer?
				if (toIdx >= 0) {
					to = _Inclusions.get(toIdx).to;
					_Inclusions.get(0).from = from;
					_Inclusions.get(0).to = (to = _Inclusions.get(toIdx).to);
					while (toIdx > 0)
						_Inclusions.remove(toIdx--);
				}
				// No bracketers because _Inclusions is empty?
				else if (_Inclusions.size() == 0)
					_Inclusions.add(new Inclusion(from, to));

				// No bracketers because the new 'from' and 'to' fall AFTER the
				// existing inclusions?
				else if (_Inclusions.get(0).from > to) {
					if (_Inclusions.get(0).from == to + 1)
						_Inclusions.get(0).from = from;
					else
						_Inclusions.add(0, new Inclusion(from, to));
				}

				// No bracketers because the new 'from' and 'to' fall BEFORE the
				// existing inclusions?
				else if (_Inclusions.get(_Inclusions.size() - 1).to < from) {
					if (_Inclusions.get(_Inclusions.size() - 1).to == from - 1)
						_Inclusions.get(_Inclusions.size() - 1).to = to;
					else
						_Inclusions.add(new Inclusion(from, to));
				}
			}

			// There is a 'from bracketer, but not a 'to' bracketer?
			else if (toIdx < 0) {
				if (_Inclusions.get(_Inclusions.size() - 1).to == from - 1)
					_Inclusions.get(_Inclusions.size() - 1).to = to;
				else
					_Inclusions.add(new Inclusion(from, to));
			}

			// Entirely contained within one existing bracket?
			else if (fromIdx == toIdx)
				return false;

			// A span between 'from' and 'to'?
			else {
				_Inclusions.get(fromIdx).to = _Inclusions.get(toIdx).to;
				while (toIdx > fromIdx)
					_Inclusions.remove(toIdx--);
			}
			return true;

		}

		public boolean exclusion(int from, int to) {

		}

		private int findBracketingIndex(int item, int fromIdx, int toIdx) {
			// Use binary search to find which inclusion matches the item.
			do {
				int idx = fromIdx + ((toIdx - fromIdx) / 2);
				Inclusion focus = _Inclusions.get(idx);
				if (focus.brackets(item))
					return idx;
				else if (item < focus.from)
					toIdx = idx;
				else
					fromIdx = idx + 1;
			} while (fromIdx != toIdx);
			return -1;
		}

		public boolean includes(int item) {
			return findBracketingIndex(item, 0, _Inclusions.size()) >= 0;
		}

	}

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

	public void setCode(String code) {
		int editable[] = new int[2];
		editable[0] = 0;
		editable[1] = 25; // code.length();

		setCode(code, editable);
	}

	public void setCode(String code) {
		this._Editable = editable;

		_Editor.getHighlighter().removeAllHighlights();

		_Editor.setText(code);

		for (int i = 0; i < editable.length; i += 2) {
			int start = editable[i], stop = editable[i + 1];

			DefaultHighlightPainter uneditablePainter = new DefaultHighlighter.DefaultHighlightPainter(
					java.awt.Color.PINK);

			try {
				_Editor.getHighlighter().addHighlight(start, stop, uneditablePainter);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

}
