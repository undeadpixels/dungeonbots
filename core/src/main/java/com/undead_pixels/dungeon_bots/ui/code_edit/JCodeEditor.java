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

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.text.Highlighter;

import com.undead_pixels.dungeon_bots.utils.builders.UIBuilder;

/**
 * @author Wesley
 *
 */
public class JCodeEditor extends JPanel implements ActionListener {

	private JEditorPane _Editor;
	private JScrollPane _EditorScroller;
	private Highlighter _Highlighter;

	public JCodeEditor() {

		setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
		setLayout(new BorderLayout());

		JToolBar toolBar = new JToolBar();
		toolBar.setPreferredSize(new Dimension(200, 30));
		JButton bttnCut = UIBuilder.makeButton("cut.jpg", "CUT", "Cut a highlighted section", "Cut", this);
		JButton bttnCopy = UIBuilder.makeButton("copy.jpg", "COPY", "Copy a highlighted section", "Copy", this);
		JButton bttnPaste = UIBuilder.makeButton("paste.jpg", "PASTE", "Paste at the cursor", "Paste", this);		
		toolBar.add(bttnCut);
		toolBar.add(bttnCopy);
		toolBar.add(bttnPaste);
		toolBar.add(
				UIBuilder.makeButton("uneditable.jpg", "NOT_EDITABLE", "Selection not editable", "Not Editable", this));
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

	/** Returns the code in this editor, including any markup flags. */
	public String getCodeMarked() {
		return _Editor.getText();
	}

	/**
	 * The underlineHighlighter and associated painter came from
	 * http://www.java2s.com/Tutorials/Java/Swing_How_to/JTextPane/Highlight_Word_in_JTextPane.htm
	 * 
	 *//*
		 * private static class UnderlineHighlighter extends DefaultHighlighter
		 * {
		 * 
		 * protected static final Highlighter.HighlightPainter sharedPainter =
		 * new UnderlineHighlightPainter(null); protected
		 * Highlighter.HighlightPainter painter;
		 * 
		 * public UnderlineHighlighter(java.awt.Color c) { painter = (c == null
		 * ? sharedPainter : new UnderlineHighlightPainter(c)); }
		 * 
		 * public Object addHighlight(int p0, int p1) throws
		 * BadLocationException { return addHighlight(p0, p1, painter); }
		 * 
		 * @Override public void setDrawsLayeredHighlights(boolean newValue) {
		 * super.setDrawsLayeredHighlights(true); }
		 * 
		 * }
		 */
	/*
	 * private static class UnderlineHighlightPainter extends
	 * LayeredHighlighter.LayerPainter { protected java.awt.Color color;
	 * 
	 * public UnderlineHighlightPainter(java.awt.Color c) { color = c; }
	 * 
	 * @Override public void paint(Graphics g, int offs0, int offs1, Shape
	 * bounds, JTextComponent c) { }
	 * 
	 * @Override public Shape paintLayer(Graphics g, int offs0, int offs1, Shape
	 * bounds, JTextComponent c, View view) { g.setColor(color == null ?
	 * c.getSelectionColor() : color); Rectangle rect = null; if (offs0 ==
	 * view.getStartOffset() && offs1 == view.getEndOffset()) { if (bounds
	 * instanceof Rectangle) { rect = (Rectangle) bounds; } else { rect =
	 * bounds.getBounds(); } } else { try { Shape shape =
	 * view.modelToView(offs0, Position.Bias.Forward, offs1,
	 * Position.Bias.Backward, bounds); rect = (shape instanceof Rectangle) ?
	 * (Rectangle) shape : shape.getBounds(); } catch (BadLocationException e) {
	 * return null; } } FontMetrics fm = c.getFontMetrics(c.getFont()); int
	 * baseline = rect.y + rect.height - fm.getDescent() + 1; g.drawLine(rect.x,
	 * baseline, rect.x + rect.width, baseline); g.drawLine(rect.x, baseline +
	 * 1, rect.x + rect.width, baseline + 1); return rect; } }
	 */
}
