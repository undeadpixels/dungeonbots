/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;

import com.undead_pixels.dungeon_bots.math.IntegerIntervalSet;
import com.undead_pixels.dungeon_bots.math.IntervalSet;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.annotations.UserScript;
import com.undead_pixels.dungeon_bots.utils.builders.UIBuilder;

import jsyntaxpane.SyntaxDocument;

/**
 * @author Wesley
 *
 */
@SuppressWarnings("serial")
public final class JScriptEditor extends JPanel {

	/** The script being edited. */
	private UserScript _Script = null;
	private final JEditorPane _Editor;
	private final JScrollPane _EditorScroller;
	private final JToggleButton _LockButton;
	private SecurityLevel _SecurityLevel;
	private Controller _Controller;

	private int _SelectionStart = -1;
	private int _SelectionEnd = -1;
	IntegerIntervalSet _Locks = null;

	/*
	 * ================================================================
	 * JCodeEditor CONSTRUCTION MEMBERS
	 * ================================================================
	 */

	public JScriptEditor(SecurityLevel securityLevel) {
		_Controller = new Controller();
		_SecurityLevel = securityLevel;
		_Locks = new IntegerIntervalSet();

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

		_LockController = new LockController(Color.blue);
		_Editor.setHighlighter(_LockController);
	}

	/**
	 * Sets the editor to modify the given script. Note that the script object
	 * will not be modified, but a new script will be returned from the
	 * getScript() call.
	 */
	public void setScript(UserScript script) {
		_Script = script;
		_Editor.setText(script.code);
		_LockController.removeAllHighlights();
		_Locks = new IntegerIntervalSet();
		_LockController.lock(_Locks);
	}

	/** Returns a new script from the current contents of the editor. */
	public UserScript getScript() {
		UserScript result = new UserScript(_Script.name, _Editor.getText(), _Script.level);
		int highlightCount = _LockController.getHighlights().length;
		for (int i = 0; i < highlightCount; i++) {
			Highlighter.Highlight h = _LockController.getHighlights()[i];
			throw new IllegalStateException("Not implemented yet.");
		}
		return result;
	}

	/*
	 * ================================================================
	 * JScriptEditor CODE LOCK MEMBERS
	 * ================================================================
	 */

	/**
	 * This object handles code lock control by maintaining the list of
	 * highlighted areas.
	 */
	LockController _LockController;

	private class LockController extends DefaultHighlighter {
		// From:
		// http://www.java2s.com/Tutorials/Java/Swing_How_to/JTextPane/Highlight_Word_in_JTextPane.htm
		// by unnamed, downloaded 2/14/18

		protected Highlighter.HighlightPainter _Painter;

		public LockController(Color color) {
			if (color == null)
				throw new IllegalStateException("Color cannot be null.");
			_Painter = new LockHighlightPainter(color);
		}

		/**
		 * Locks every included item.
		 */
		public void lock(IntegerIntervalSet locks) {
			for (IntervalSet<Integer>.Interval interval : locks) {
				_Locks.add(interval);
			}
		}

		/**
		 * Locks everything from the selection start to the selection end,
		 * inclusive.
		 */
		public void lock() {
			lock(_SelectionStart, _SelectionEnd);
		}

		/**
		 * Locks everything from the indicated start to the indicated end,
		 * inclusive.
		 */
		public void lock(int from, int to) {
			_Locks.add(from, to);
		}

		/**
		 * Returns whether this controller has all the code locked from the
		 * indicated start to end.
		 */
		public boolean isLocked(int from, int to) {
			return _Locks.includes(from, to);
		}

		@Override
		public void setDrawsLayeredHighlights(boolean newValue) {
			super.setDrawsLayeredHighlights(true);
		}

		private class LockHighlightPainter extends LayeredHighlighter.LayerPainter {

			// From:
			// http://www.java2s.com/Tutorials/Java/Swing_How_to/JTextPane/Highlight_Word_in_JTextPane.htm
			// by unnamed, downloaded 2/14/18

			protected Color color;

			public LockHighlightPainter(Color color) {
				this.color = color;
			}

			@Override
			public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
				// No painting - using an underline instead.
			}

			@Override
			public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
				g.setColor(color == null ? c.getSelectionColor() : color);
				Rectangle rect = null;
				if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
					if (bounds instanceof Rectangle) {
						rect = (Rectangle) bounds;
					} else {
						rect = bounds.getBounds();
					}
				} else {
					try {
						Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward,
								bounds);
						rect = (shape instanceof Rectangle) ? (Rectangle) shape : shape.getBounds();
					} catch (BadLocationException e) {
						return null;
					}
				}
				FontMetrics fm = c.getFontMetrics(c.getFont());
				int baseline = rect.y + rect.height - fm.getDescent() + 1;
				g.drawLine(rect.x, baseline, rect.x + rect.width, baseline);
				g.drawLine(rect.x, baseline + 1, rect.x + rect.width, baseline + 1);
				return rect;
			}
		}

	}

	/*
	 * ================================================================
	 * JScriptEditor CONTROLLER
	 * ================================================================
	 */
	/** The controller class for the JScriptEditor. */
	private class Controller implements CaretListener, ActionListener, DocumentListener {

		/** Called when buttons are hit. */
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {

			case "TOGGLE_LOCK":
				if (_SecurityLevel.level >= SecurityLevel.AUTHOR.level) {
					_LockController.lock();
				}
				break;
			case "CUT":
				_Editor.cut();
				break;
			case "COPY":
				_Editor.copy();
				break;
			case "PASTE":
				_Editor.paste();
				break;
			}

		}

		/** Called when selection changes. */
		@Override
		public void caretUpdate(CaretEvent e) {
			_SelectionStart = e.getMark();
			_SelectionEnd = e.getDot();
			if (_SecurityLevel.level >= SecurityLevel.AUTHOR.level) {
				boolean selectionExists = _SelectionStart < _SelectionEnd;
				_LockButton.setEnabled(selectionExists);
				if (selectionExists) {
					_LockButton.setSelected(_LockController.isLocked(_SelectionStart, _SelectionEnd));
				}

			}
		}

		@Override
		public void changedUpdate(DocumentEvent arg0) {
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
		}
	}

}
