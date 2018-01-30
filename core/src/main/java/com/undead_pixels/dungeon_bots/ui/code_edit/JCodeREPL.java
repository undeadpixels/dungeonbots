package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaNil;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

public class JCodeREPL extends JPanel implements ActionListener {

	private LuaSandbox _Sandbox;
	private JScrollPane _MessageScroller;
	public final long MaxExecutionTime = 3000;
	final private int _MessageMax = 3000;
	private JTextPane _MessagePane;
	private JEditorPane _EditorPane;
	private Object _LastResult = null;
	private JButton _CancelBttn;
	private JButton _ExecuteBttn;
	private boolean _IsExecuting;

	/*
	 * ================================================================
	 * JCodeREPL CONSTRUCTION MEMBERS
	 * ================================================================
	 */

	/** Creates a new REPL. All code will execute in a brand-new sandbox. */
	public JCodeREPL() {
		this(new LuaSandbox(SecurityLevel.DEBUG));
	}

	/** Creates a new REPL. All code will execute in the given sandbox. */
	public JCodeREPL(LuaSandbox sandbox) {
		super(new BorderLayout());

		_Sandbox = sandbox;

		_EchoMessageStyle = createSimpleAttributeSet(Color.WHITE, Color.BLACK, false);
		_SystemMessageStyle = createSimpleAttributeSet(Color.GREEN, Color.BLACK, true);
		_ErrorMessageStyle = createSimpleAttributeSet(Color.RED, Color.BLACK, true);

		addComponents();

		_MessagePane.setText("");
		_EditorPane.setText("");

		addKeyBindings();
	}

	private void addComponents() {
		if (!(getLayout() instanceof BorderLayout)) {
			add(new JLabel("Wrong layout - must be a BorderLayout."));
			return;
		}
		setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
		_EditorPane = new JEditorPane();
		_EditorPane.setFocusable(true);

		add(makeREPLToolBar(), BorderLayout.PAGE_START);

		_MessagePane = new JTextPane();
		_MessageScroller = new JScrollPane(_MessagePane);

		add(_MessageScroller, BorderLayout.CENTER);
		_MessagePane.setFocusable(false);
		_MessagePane.setText("");

		JPanel executePanel = new JPanel(new BorderLayout());
		//_EditorPane.setPreferredSize(new Dimension(this.getPreferredSize().width, 100));
		this.add(executePanel, BorderLayout.PAGE_END);
		executePanel.add(new JScrollPane(_EditorPane), BorderLayout.CENTER);

		JPanel startStopPanel = new JPanel(new BorderLayout());
		_ExecuteBttn = makeButton("executeButton.gif", "EXECUTE", "Click to execute", ">", this);
		_CancelBttn = makeButton("cancelButton.gif", "CANCEL", "Click to cancel", "X", this);
		_CancelBttn.setEnabled(false);
		JButton helpButton = makeButton("helpButton.gif", "HELP", "Click for help", "?", this);
		startStopPanel.add(helpButton, BorderLayout.PAGE_START);
		startStopPanel.add(_ExecuteBttn, BorderLayout.CENTER);
		startStopPanel.add(_CancelBttn, BorderLayout.PAGE_END);
		executePanel.add(startStopPanel, BorderLayout.LINE_END);
	}

	private void addKeyBindings() {
		KeyStroke ks = KeyStroke.getKeyStroke("ENTER");
		InputMap map = this.getInputMap();
		map.put(ks, "EXECUTE");
	}

	private JToolBar makeREPLToolBar() {
		JToolBar result = new JToolBar();
		result.add(makeButton("testing.gif", "CLICK", "ToolTipA", "REPLToolA", this));
		result.add(makeButton("testing.gif", "CLICK", "ToolTipB", "REPLToolB", this));
		return result;
	}

	/**
	 * Creates a button with the appearance described. Don't forget to set a
	 * listener.
	 * 
	 * @param imageURL
	 *            The image resource that will appear in the button.
	 * @param actionCommand
	 *            The command that the button will issue.
	 * @param toolTipText
	 *            The tip presented when hovering the mouse over the button.
	 * @param altText
	 *            Something-or-other...
	 * @param l
	 *            The listener.
	 * @return Returns a JButton.
	 */
	public static JButton makeButton(String imageURL, String actionCommand, String toolTipText, String altText,
			ActionListener l) {

		JButton resultButton = new JButton();
		resultButton.setActionCommand(actionCommand);
		resultButton.setToolTipText(toolTipText);
		resultButton.setToolTipText(toolTipText);

		resultButton.addActionListener(l);

		URL url = JCodeREPL.class.getResource(imageURL);
		if (url != null) {
			resultButton.setIcon(new ImageIcon(url, altText));
		} else {
			resultButton.setText(altText);
			System.err.println("Resource was missing.  Attempted to created button with image at " + imageURL
					+ ".  Using altText '" + altText + "' as image instead.");
		}

		return resultButton;
	}

	/*
	 * ================================================================
	 * JCodeREPL APPEARANCE MEMBERS
	 * ================================================================
	 */

	private AttributeSet _EchoMessageStyle = null;
	private AttributeSet _SystemMessageStyle = null;
	private AttributeSet _ErrorMessageStyle = null;

	private AttributeSet createSimpleAttributeSet(Color foreground, Color background, boolean bold) {
		SimpleAttributeSet result = new SimpleAttributeSet();
		StyleConstants.setForeground(result, foreground);
		StyleConstants.setBackground(result, background);
		StyleConstants.setBold(result, bold);
		return result;
	}

	/*
	 * ================================================================
	 * JCodeREPL CODING INTERFACE MEMBERS
	 * ================================================================
	 */

	
	private class ScriptExecutionWorker extends SwingWorker<Object, String> {

		public String executingCode = "";
		private long _milliseconds = MaxExecutionTime;

		public ScriptExecutionWorker(String code, long executionTime) {
			executingCode = code;
			_milliseconds = executionTime;
		}

		// Cannot override execute()

		/** Does the work in the calling thread. Useful for testing purposes. */
		public Object doSynchronized() {
			return doInBackground();
		}

		@Override
		protected Object doInBackground() {

			PrintStream originalOut = System.out;
			try  {
				
				LuaScript script = _Sandbox.script(executingCode);
				script.start(); 
				script.join(_milliseconds);
				
				if (script.getError() != null)
					return script.getError();

				switch (script.getStatus()) {
				case READY:
					throw new Exception("Script did not execute.");
				case COMPLETE:
					return Interpret(script.getResults());
				case RUNNING:
					throw new Exception("Script is still running.");
				case TIMEOUT:
					throw new Exception("Script timed out.");
				case STOPPED:
					throw new Exception("Script has been stopped.");
				case LUA_ERROR:
					throw new Exception("Lua error.");
				case ERROR:
					throw new Exception("Threading error.");
				case PAUSED:
					throw new Exception("Script has been paused.");
				default:
					throw new Exception("Unrecognized status: " + script.getStatus() + ".");
				}
			} catch (Exception ex) {
				return ex;
			}finally{
				System.setOut(originalOut);
			}
		}

		@Override
		protected void done() {
			try {
				Object result = get();
				onExecutionComplete(this, result);
			} catch (Exception e) {
				onExecutionComplete(this, e);
			}
		}
	}

	/**
	 * Starts execution of the code contained in the code editor, on a
	 * background thread.
	 */
	public void execute(long executionTime) {
		setIsExecuting(true);
		String code = getCode();
		message(">>> " + code, _EchoMessageStyle);
		ScriptExecutionWorker worker = new ScriptExecutionWorker(code, executionTime);
		worker.execute();
	}

	/**
	 * Executes the contents of the code editor, on the main thread. Useful for
	 * testing purposes.
	 */
	public void executeSynchronized(long executionTime) {
		setIsExecuting(true);
		ScriptExecutionWorker worker = new ScriptExecutionWorker(getCode(), executionTime);
		onExecutionComplete(worker, worker.doSynchronized());
	}

	private void onExecutionComplete(ScriptExecutionWorker sender, Object result) {
		_LastResult = result;

		// Send a message indicating the results.
		if (result == null)
			message("Ok", _SystemMessageStyle);
		else if (result instanceof Exception)
			message(((Exception) result).getMessage(), _ErrorMessageStyle);
		else
			message(result.toString(), _SystemMessageStyle);

		// It's possible for some other entity to sneak some code in while the
		// thread is in the
		// background. Only clear out the code editor if the executed code is
		// still in the editor.
		if (sender.executingCode.equals(getCode()))
			setCode("");

		setIsExecuting(false);
	}

	/** Sets the GUI to correctly reflect the execution status. */
	private void setIsExecuting(boolean value) {
		if (_IsExecuting == value)
			throw new IllegalStateException("Call to setIsExecuting(boolean) must CHANGE state.");
		_IsExecuting = value;
		_CancelBttn.setEnabled(value);
		_ExecuteBttn.setEnabled(!value);

	}

	/**
	 * Interprets the result of a LuaScript execution and converts it into a
	 * suitable Java object.
	 */
	public static Object Interpret(Optional<Varargs> rawResult) {
		if (rawResult == null)
			return null;
		Varargs unpackedResult = rawResult.get();
		if (unpackedResult instanceof LuaNil)
			return null;
		if (unpackedResult instanceof LuaInteger)
			return ((LuaInteger) unpackedResult).toint();
		if (unpackedResult instanceof LuaValue) {
			//
			LuaValue lv = (LuaValue) unpackedResult;
			if (lv.tojstring() == "none") // Void result.
				return lv;

		}
		throw new ClassCastException(
				"Have not implemented Lua-to-Java interpretation of type " + unpackedResult.getClass().getName() + ".");
	}

	/** Returns the code contents being edited in this REPL. */
	public String getCode() {
		try {
			Document doc = _EditorPane.getDocument();
			return doc.getText(0, doc.getLength());
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Sets the code as indicated. If running the code is intended, don't forget
	 * to call execute(long).
	 */
	public void setCode(String code) {
		try {
			Document doc = _EditorPane.getDocument();
			doc.remove(0, doc.getLength());
			doc.insertString(0, code, _EchoMessageStyle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Returns the last result that came from execution of the REPL. */
	public Object getLastResult() {
		return _LastResult;
	}

	/*
	 * ================================================================
	 * JCodeREPL MESSAGING MEMBERS
	 * ================================================================
	 */
	
	/**
	 * Posts a message to the message pane.
	 */
	public void message(String message){
		message(message, null);
	}
	
	/**
	 * Posts a message to the message pane. The identity of the sender will
	 * compel the style with which the message will appear.
	 */
	public void message(String message, Object sender) {
		if (sender == this)
			message(message, _EchoMessageStyle);
		else
			message(message, _SystemMessageStyle);
	}

	/** Posts a message to the message pane, with the indicated style. */
	protected void message(String message, AttributeSet attribs) {

		if (_MessagePane == null || attribs == null)
			return;

		StyledDocument doc = _MessagePane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), message + "\n\n", attribs);
			doc.remove(0, Math.max(0, doc.getLength() - _MessageMax));
			pageEnd(_MessageScroller);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void pageHome(JScrollPane scroller) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				JScrollBar vert = scroller.getVerticalScrollBar();
				JScrollBar horiz = scroller.getHorizontalScrollBar();
				vert.setValue(vert.getMinimum());
				horiz.setValue(horiz.getMinimum());
			}
		});
	}

	private static void pageEnd(JScrollPane scroller) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JScrollBar vert = scroller.getVerticalScrollBar();
					JScrollBar horiz = scroller.getHorizontalScrollBar();
					if (vert != null)
						vert.setValue(vert.getMaximum());
					if (horiz != null)
						horiz.setValue(horiz.getMinimum());
				} catch (Exception e) {
				}

			}
		});
	}

	/** Scrolls the message pane to the end of the messages. */
	public void pageEnd() {
		pageEnd(_MessageScroller);
	}

	/** Scrolls the message pane to the beginning of the messages. */
	public void pageHome() {
		pageHome(_MessageScroller);
	}

	/** Returns all the messages contained in this REPL. */
	public String getMessages() {

		try {
			if (_MessagePane == null)
				return "";
			StyledDocument doc = _MessagePane.getStyledDocument();
			return doc.getText(0, doc.getLength());
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public void actionPerformed(ActionEvent action) {

		switch (action.getActionCommand()) {
		case "EXECUTE":
			execute(3000);
			break;
		case "HELP":
			System.out.println("Don't panic!");
			break;
		default:
			System.out.println("Unimplemented action received by " + this.getClass().getName() + " object: "
					+ action.getActionCommand());
			break;
		}
		// TODO Auto-generated method stub

	}

}
