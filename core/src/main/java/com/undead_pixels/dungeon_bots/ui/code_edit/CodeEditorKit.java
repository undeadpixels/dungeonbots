package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.ViewFactory;

public class CodeEditorKit extends EditorKit {
	
	//How to implement a java editor kit:
	// http://web.archive.org/web/20080924140915/http://java.sun.com:80/products/jfc/tsc/articles/text/editor_kit/

	public CodeEditorKit() {
		super();		
	}

	@Override
	public Caret createCaret() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document createDefaultDocument() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Action[] getActions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ViewFactory getViewFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void read(InputStream arg0, Document arg1, int arg2) throws IOException, BadLocationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void read(Reader arg0, Document arg1, int arg2) throws IOException, BadLocationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(OutputStream arg0, Document arg1, int arg2, int arg3) throws IOException, BadLocationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(Writer arg0, Document arg1, int arg2, int arg3) throws IOException, BadLocationException {
		// TODO Auto-generated method stub

	}

}
