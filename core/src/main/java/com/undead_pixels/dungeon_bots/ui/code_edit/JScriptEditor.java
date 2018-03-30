/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;

import com.undead_pixels.dungeon_bots.math.IntegerSet;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;

/**
 * @author Wesley
 *
 */
public final class JScriptEditor extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The script being edited. */
	private UserScript _Script = null;
	final JEditorPane editor;
	private SecurityLevel _SecurityLevel;
	private Controller _Controller;
	private JComboBox<Font> _FontChooser;
	private JComboBox<Integer> _FontSizeChooser;


	/* ================================================================
	 * JCodeEditor CONSTRUCTION MEMBERS
	 * ================================================================ */

	public JScriptEditor(SecurityLevel securityLevel) {

		_Controller = new Controller();
		_SecurityLevel = securityLevel;

		setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
		setLayout(new BorderLayout());

		JToolBar toolBar = new JToolBar();
		toolBar.setPreferredSize(new Dimension(200, 30));
		JButton bttnCut = UIBuilder.buildButton().image("icons/cut.png").toolTip("Cut a selected section.")
				.action("CUT", _Controller).create();
		JButton bttnCopy = UIBuilder.buildButton().image("icons/copy.png").toolTip("Copy a selected section.")
				.action("COPY", _Controller).create();
		JButton bttnPaste = UIBuilder.buildButton().image("icons/paste.png").toolTip("Paste at the cursor.")
				.action("PASTE", _Controller).create();
		JButton bttnHelp = UIBuilder.buildButton().image("icons/question.png")
				.toolTip("Get help with the command line.").action("HELP", _Controller).focusable(false)
				.preferredSize(30, 30).create();


		editor = new JEditorPane();
		JScrollPane editorScroller = new JScrollPane(editor);
		add(editorScroller, BorderLayout.CENTER);
		editor.setEditable(false);
		editor.setFocusable(true);
		editor.setContentType("text/lua");
		editor.addCaretListener(_Controller);
		editor.setHighlighter(_Controller._Highlighter);

		_FontChooser = new JComboBox<Font>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());
		_FontChooser.setRenderer(_FontNameRenderer);
		_FontChooser.setSelectedItem(editor.getFont());
		_FontChooser.addActionListener(_Controller);

		_FontSizeChooser = new JComboBox<Integer>(new Integer[] { 8, 10, 12, 14, 16, 18, 20, 24, 28, 36, 72 });
		_FontSizeChooser.setSelectedItem(editor.getFont().getSize());
		_FontSizeChooser.addActionListener(_Controller);


		toolBar.add(bttnCut);
		toolBar.add(bttnCopy);
		toolBar.add(bttnPaste);
		toolBar.add(bttnHelp);
		toolBar.add(_FontChooser);
		toolBar.add(_FontSizeChooser);
		if (securityLevel.level >= SecurityLevel.AUTHOR.level) {
			JToggleButton lockButton = UIBuilder.buildToggleButton().image("icons/lock.png").text("Lock")
					.toolTip("Lock selected text.").action("TOGGLE_LOCK", _Controller).create();
			toolBar.add(lockButton);
			_Controller.setLockButton(lockButton);
			_Controller.setLockColor(Color.blue);
		}

		add(toolBar, BorderLayout.PAGE_START);

	}


	public boolean isEditable() {
		return editor.isEditable();
	}


	public void setEditable(boolean value) {
		editor.setEditable(value);
		editor.setEnabled(value);
	}


	@Override
	public boolean isEnabled() {
		return editor.isEnabled();
	}


	@Override
	public void setEnabled(boolean value) {
		editor.setEnabled(value);
	}


	/** Returns a reference to the script currently being edited. */
	public UserScript getScript() {
		return _Script;
	}


	/**
	 * Sets the editor to modify the given script. Note that the script object
	 * will not be modified in this editor, but a new script will be returned 
	 * from the getScript() call.
	 */
	public void setScript(UserScript script) {

		// Ensure that the text's lock filter cannot interfere with printing the
		// text for the first time.
		_Controller.setFiltering(false);

		_Script = script;
		setLiveEditing(true);
		editor.setText(script.code);
		_Controller.resetLocks();

		// Restore filtering.
		//_Controller.setFiltering(true);
	}


	/**
	 * Overwrites the current script with the contents of this editor.  If no script is being edited, does nothing.
	 */
	public void saveScript() {
		if (_Script == null)
			return;
		try {
			_Script.code = editor.getText();
			_Script.setLocks(_Controller.getHighlightIntervals());
		} catch (BadLocationException blex) {
			System.err.println("Could not save locks to code. " + blex.getMessage());
		}

	}


	public void setLiveEditing(boolean value) {
		if (_Controller._LockFilter != null)
			_Controller._LockFilter.setLocked(!value);
	}


	private final HashSet<ActionListener> _ActionListeners = new HashSet<ActionListener>();


	// This class fires action events. Add a listener to receive those events.
	public void addActionListener(ActionListener l) {
		_ActionListeners.add(l);
	}


	public static final ListCellRenderer<Font> _FontNameRenderer = new ListCellRenderer<Font>() {

		@Override
		public Component getListCellRendererComponent(JList<? extends Font> fontList, Font font, int index,
				boolean isSelected, boolean hasFocus) {
			return new JLabel(font.getName());
		}

	};


	/** Can turn text editing on or off. This is the object that prevents code from 
	 * being typed in a locked section. */
	static class LockFilter extends DocumentFilter {

		private boolean _locked = true;
		private final AbstractDocument  _Doc;


		public LockFilter(AbstractDocument document, boolean isLocked) {
			this._Doc = document;
			this._locked = isLocked;
		}

/**Creates a new, unlocked LockFilter.*/
		public LockFilter(AbstractDocument document) {
			this(document, false);
		}


		/**
		 * Determine whether the filter will allow editing of the attached
		 * document.
		 */
		public void setLocked(boolean value) {
			_locked = value;
		}


		public boolean getLocked() {
			return _locked;
		}


		@Override
		public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
				throws BadLocationException {

			if (_locked)
				return;

			// Parentheses problem fix - this is somewhat hackish but it works
			if (text.equals(")") && offset < _Doc.getLength() && _Doc.getText(offset, 1).equals(")")) {
				replace(fb, offset, 1, ")", attr);
				return;

			}

			else
				super.insertString(fb, offset, text, attr);
		}


		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
				throws BadLocationException {
			if (_locked)
				return;
			else
				super.replace(fb, offset, length, text, attrs);

		}


		@Override
		public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
			if (_locked)
				return;
			else
				super.remove(fb, offset, length);
		}
	}


	/* ================================================================
	 * JScriptEditor CONTROLLER
	 * ================================================================ */
	/** The controller class for the JScriptEditor. This class handles code lock, as well 
	 * as cut/copy/paste.*/
	private class Controller implements CaretListener, ActionListener {

		private DefaultHighlighter _Highlighter;
		protected UnderlinePainter _Painter;
		private JToggleButton _LockButton = null;
		private LockFilter _LockFilter;

		private int _SelectionStart = -1;
		private int _SelectionEnd = -1;


		public Controller() {
			this._Painter = new UnderlinePainter(Color.blue);
			this._Highlighter = new DefaultHighlighter();
			_Highlighter.setDrawsLayeredHighlights(true);
		}


		/** Called when buttons are hit. */
		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == _FontChooser || e.getSource() == _FontSizeChooser) {
				Font choice = (Font) _FontChooser.getSelectedItem();
				int size = (int) _FontSizeChooser.getSelectedItem();
				Font f = new Font(choice.getFontName(), Font.PLAIN, size);
				editor.setFont(f);
				return;
			}
			switch (e.getActionCommand()) {
			case "TOGGLE_LOCK":
				if (_LockButton != null && _SecurityLevel.level >= SecurityLevel.AUTHOR.level) {
					if (!lock())
						unlock();
				}
				break;
			case "CUT":
				editor.cut();
				break;
			case "COPY":
				editor.copy();
				break;
			case "PASTE":
				editor.paste();
				break;
			case "HELP":
				// Pass on the event to every listener.
				e = new ActionEvent(this, e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers());
				for (ActionListener l : _ActionListeners)
					l.actionPerformed(e);
				break;
			default:
				System.out.println("JScriptEditor has not implemented command: " + e.getActionCommand());
			}

		}


		/** Called when selection changes. */
		@Override
		public void caretUpdate(CaretEvent e) {
			_SelectionStart = e.getMark();
			_SelectionEnd = e.getDot();

			// Prohibit editing if in a locked region.
			if (_LockFilter != null) {
				/*
				if (_SecurityLevel.level < SecurityLevel.AUTHOR.level)
					_LockFilter.setLocked(!getHighlightIntervals().includes(_SelectionStart));
				else
					_LockFilter.setLocked(true);
*/
			}

			updateButton();
		}


		/* ================================================================
		 * JScriptEditor.Controller CODE LOCK MEMBERS
		 * ================================================================ */

		/** Determine whether the controller should be lock-filtering or not. */
		public void setFiltering(boolean value) {
			AbstractDocument doc = (AbstractDocument) editor.getDocument();
			if (doc.getDocumentFilter() == null)
				doc.setDocumentFilter(_LockFilter = new LockFilter(doc));
			_LockFilter.setLocked(value);
		}


		/** Sets the button that will control locking for this controller. */
		public void setLockButton(JToggleButton button) {
			this._LockButton = button;
		}


		/** Sets the lock color for this controller. */
		public void setLockColor(Color color) {
			_Painter._Color = color;

		}


		private IntegerSet getHighlightIntervals() {
			IntegerSet result = new IntegerSet();
			// for (DefaultHighlighter.Highlight h :
			// _Highlighter.getHighlights())
			// result.add(h.getStartOffset(), h.getEndOffset() - 1);
			return result;
		}


		/** Removes all locks. */
		public void clearLocks() {
			editor.setHighlighter(_Highlighter);
			if (_Highlighter == null)
				return;
			_Highlighter.removeAllHighlights();
			updateButton();
		}


		/**
		 * Locks everything from the selection start to the selection end,
		 * exclusive.
		 */
		public boolean lock() {
			return lock(_SelectionStart, _SelectionEnd - 1);
		}


		/**
		 * Locks everything from the indicated start to the indicated end,
		 * inclusive.
		 */
		private boolean lock(int start, int end) {
			if (start < 0 || end < 0)
				return false;
			if (start > end)
				return false;
			IntegerSet locks = getHighlightIntervals();
			if (locks.includes(start, end))
				return false;

			locks.add(start, end);
			_Highlighter.removeAllHighlights();
			try {
				for (IntegerSet.Interval interval : locks)
					_Highlighter.addHighlight(interval.start, interval.end + 1, _Painter);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			updateButton();
			return true;
		}


		/**
		 * Sets all locks as specified in the original UserScript. Note that the
		 * code may have changed since then, so an error may be thrown.
		 */
		public void resetLocks() {
			clearLocks();
			IntegerSet set = new IntegerSet();
			for (IntegerSet.Interval i : _Script.locks) {
				set.add(i.start, i.end);
			}
			for (IntegerSet.Interval i : set)
				try {
					_Highlighter.addHighlight(i.start, i.end + 1, _Painter);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			updateButton();
		}


		/**
		 * Unlocks everything from the selection start to the selection end,
		 * exclusive.
		 */
		public boolean unlock() {
			return unlock(_SelectionStart, _SelectionEnd - 1);
		}


		/**
		 * Unlocks everything from the indicated start to the indicated end,
		 * inclusive.
		 */
		public boolean unlock(int start, int end) {
			if (start < 0 || end < 0)
				return false;
			if (start > end)
				return false;
			IntegerSet locks = getHighlightIntervals();
			if (!locks.any(start, end))
				return false;
			locks.remove(start, end);
			_Highlighter.removeAllHighlights();
			try {
				for (IntegerSet.Interval interval : locks)
					_Highlighter.addHighlight(interval.start, interval.end + 1, _Painter);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			updateButton();
			return true;
		}


		/** Updates the status of the GUI locking button. */
		public void updateButton() {
			if (_LockButton == null)
				return;

			if (_SelectionStart >= _SelectionEnd) {
				_LockButton.setSelected(false);
				_LockButton.setEnabled(false);
			} else if (getHighlightIntervals().includes(_SelectionStart, _SelectionEnd - 1)) {
				_LockButton.setEnabled(true);
				_LockButton.setSelected(false);
			} else {
				_LockButton.setEnabled(true);
				_LockButton.setSelected(false);
			}
		}


		private class UnderlinePainter extends LayeredHighlighter.LayerPainter {

			// From:
			// http://www.java2s.com/Tutorials/Java/Swing_How_to/JTextPane/Highlight_Word_in_JTextPane.htm
			// by unnamed, downloaded 2/14/18

			private Color _Color;


			public UnderlinePainter(Color color) {
				if (color == null)
					throw new IllegalStateException("Color cannot be null.");
				this._Color = color;

			}


			@Override
			public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
				// No painting - using an underline instead.
			}


			@Override
			public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
				g.setColor(_Color);
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


}
