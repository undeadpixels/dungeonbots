package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.luaj.vm2.LuaNil;
import org.luaj.vm2.Varargs;

import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.builders.UIBuilder;

public class JCodeREPL extends JPanel implements ActionListener {

	private LuaSandbox _Sandbox;
	private JScrollPane _MessageScroller;
	public final long MAX_EXECUTION_TIME = 3000;
	private final int MAX_HISTORY_COUNT = 100;
	final private int _MessageMax = 3000;
	private JTextPane _MessagePane;
	private JEditorPane _EditorPane;
	private Object _LastResult = null;
	private JButton _CancelBttn;
	private JButton _ExecuteBttn;
	private boolean _IsExecuting;

	private LuaScript _RunningScript = null;

	private ArrayList<String> _CommandHistory = new ArrayList<String>();
	private int _CommandHistoryIndex = 0;

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

		if (sandbox == null)
			sandbox = new LuaSandbox(SecurityLevel.DEBUG);

		_Sandbox = sandbox;

		_EchoMessageStyle = putSimpleAttributeSet(Color.WHITE, Color.BLACK, false);
		_SystemMessageStyle = putSimpleAttributeSet(Color.GREEN, Color.BLACK, true);
		_ErrorMessageStyle = putSimpleAttributeSet(Color.RED, Color.BLACK, true);

		addComponents();

		_MessagePane.setText("");
		_EditorPane.setText("");

		addKeyBindings();
	}

	private void addComponents() {
		this.setLayout(new BorderLayout());
		setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);

		JToolBar toolBar = makeREPLToolBar();
		add(toolBar, BorderLayout.PAGE_START);
		toolBar.setFocusable(false);

		_MessagePane = new JTextPane();
		_MessagePane.setFocusable(false);
		_MessagePane.setText("");
		_MessageScroller = new JScrollPane(_MessagePane);

		_EditorPane = new JEditorPane();
		_EditorPane.setFocusable(true);
		JScrollPane editorScroller = new JScrollPane(_EditorPane);
		_EditorPane.setContentType("text/lua");

		JPanel startStopPanel = new JPanel();
		startStopPanel.setLayout(new BoxLayout(startStopPanel, BoxLayout.PAGE_AXIS));
		_ExecuteBttn = UIBuilder.makeButton("executeButton.gif", "Click to execute", "EXECUTE", this);
		_ExecuteBttn.setFocusable(false);
		_ExecuteBttn.setMinimumSize(new Dimension(50, 80));
		_CancelBttn = UIBuilder.makeButton("cancelButton.gif", "Click to cancel", "CANCEL", this);
		_CancelBttn.setFocusable(false);
		_CancelBttn.setEnabled(false);
		_CancelBttn.setPreferredSize(new Dimension(30, 40));
		JButton helpBttn = UIBuilder.makeButton("", "Get help", "HELP", this);
		startStopPanel.add(_ExecuteBttn);
		startStopPanel.add(_CancelBttn);

		JPanel executePanel = new JPanel(new BorderLayout());
		executePanel.add(new JScrollPane(editorScroller), BorderLayout.CENTER);
		executePanel.add(startStopPanel, BorderLayout.LINE_END);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, _MessageScroller, executePanel);
		splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(false);
		splitPane.setDividerLocation(200);
		splitPane.setDividerSize(20);
		this.add(splitPane);

		_EditorPane.requestFocusInWindow();
	}

	/** Binds the CTRL+Enter key to execute the code. */
	@SuppressWarnings("serial")
	private void addKeyBindings() {

		InputMap inputMap = _EditorPane.getInputMap();
		ActionMap actionMap = _EditorPane.getActionMap();

		// On CTRL+ENTER, should execute.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK), "EXECUTE");
		actionMap.put("EXECUTE", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				execute(MAX_EXECUTION_TIME);
			}
		});

		// On ESC, should clear the command line.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CLEAR");
		actionMap.put("CLEAR", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_EditorPane.setText("");
			}
		});

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "STOP");
		actionMap.put("STOP", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				messageError("^C");
				stop();
			}

		});

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), "CLOSE");
		actionMap.put("CLOSE", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				messageError("^Q");
				Component parent = _EditorPane.getParent();
				while (parent != null) {
					// System.out.println(parent.getClass().toString());
					parent = parent.getParent();
					if (parent instanceof JDialog) {
						JDialog dialog = (JDialog) parent;
						dialog.setVisible(false);
					}
				}
			}

		});

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK), "RECALL_COMMAND_UP");
		actionMap.put("RECALL_COMMAND_UP", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (--_CommandHistoryIndex >= 0)
					_EditorPane.setText(_CommandHistory.get(_CommandHistoryIndex));
				else {
					_CommandHistoryIndex = 0;
					_EditorPane.setText("");
				}
			}

		});

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK), "RECALL_COMMAND_DOWN");
		actionMap.put("RECALL_COMMAND_DOWN", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (++_CommandHistoryIndex < _CommandHistory.size())
					_EditorPane.setText(_CommandHistory.get(_CommandHistoryIndex));
				else {
					_CommandHistoryIndex = _CommandHistory.size();
					_EditorPane.setText("");
				}
			}

		});

		// TODO: handle code completion, code run on ENTER (when well-formed),
		// code suggestion.
	}

	/**
	 * Just trying to keep the code a little organized. This is called by
	 * addComponents().
	 */
	private JToolBar makeREPLToolBar() {

		JToolBar result = new JToolBar();
		JButton cutBttn = UIBuilder.makeButton("cutBttn.gif",
				"Cut from the command line and move the text to the clipboard.", "CUT", this);
		JButton copyBttn = UIBuilder.makeButton("copyBttn.gif", "Copy from the command line to the clipboard.", "COPY",
				this);
		JButton pasteBttn = UIBuilder.makeButton("pasteBttn.gif", "Paste from the clipboard to the command line.",
				"PASTE", this);
		JButton helpBttn = UIBuilder.makeButton("helpBttn.gif", "Get help with the command line.", "HELP", this);

		cutBttn.setFocusable(false);
		copyBttn.setFocusable(false);
		pasteBttn.setFocusable(false);
		helpBttn.setFocusable(false);

		result.add(cutBttn);
		result.add(copyBttn);
		result.add(pasteBttn);
		result.add(helpBttn);

		return result;
	}

	/*
	 * public static JButton makeButton(String imageURL, String actionCommand,
	 * String toolTipText, String altText, ActionListener l) {
	 * 
	 * JButton resultButton = new JButton();
	 * resultButton.setActionCommand(actionCommand);
	 * resultButton.setToolTipText(toolTipText);
	 * resultButton.setToolTipText(toolTipText);
	 * 
	 * resultButton.addActionListener(l);
	 * 
	 * URL url = JCodeREPL.class.getResource(imageURL); if (url != null) {
	 * resultButton.setIcon(new ImageIcon(url, altText)); } else {
	 * resultButton.setText(altText); System.err.
	 * println("Resource was missing.  Attempted to created button with image at "
	 * + imageURL + ".  Using altText '" + altText + "' as image instead."); }
	 * 
	 * return resultButton; }
	 */

	/*
	 * ================================================================
	 * JCodeREPL APPEARANCE MEMBERS
	 * ================================================================
	 */

	private AttributeSet _EchoMessageStyle = null;
	private AttributeSet _SystemMessageStyle = null;
	private AttributeSet _ErrorMessageStyle = null;

	public static AttributeSet putSimpleAttributeSet(Color foreground, Color background, boolean bold) {

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

	/** Stops any running script by forcing an interrupt. */
	public void stop() {
		LuaScript toBeCancelled = _RunningScript;
		if (toBeCancelled == null)
			return;
		toBeCancelled.stop();
	}

	/**
	 * The reason for the ScriptExecutionWorker is to make the Lua script run to
	 * its completion but not lock up the GUI.
	 */
	private class ScriptExecutionWorker extends SwingWorker<Object, String> {

		public String executingCode = "";
		private long _milliseconds = MAX_EXECUTION_TIME;

		public ScriptExecutionWorker(String code, long executionTime) {
			executingCode = code;
			_milliseconds = executionTime;
		}

		// Cannot override execute()

		/** Does the work in the calling thread. Useful for testing purposes. */
		public Object doSynchronized() {
			return doInBackground();
		}

		/** Returns the currently operating script, if there is one. */
		public LuaScript getScript() {
			return _RunningScript;
		}

		@Override
		protected Object doInBackground() {

			PrintStream originalOut = System.out;
			try {

				_RunningScript = _Sandbox.script(executingCode);
				_RunningScript.start();
				_RunningScript.join(_milliseconds);

				if (_RunningScript.getError() != null)
					return _RunningScript.getError();

				switch (_RunningScript.getStatus()) {
				case READY:
					throw new Exception("Script did not execute.");
				case COMPLETE:
					return _RunningScript.getResults().map(result -> interpret(result)).orElse("Ok");
				// return interpret(_RunningScript.getResults());
				case RUNNING:
					throw new Exception("Script is still running.");
				case TIMEOUT:
					throw new Exception("Script timed out.");
				case STOPPED:
					throw new Exception("Script has been interrupted.");
				case LUA_ERROR:
					throw new Exception("Lua error.");
				case ERROR:
					throw new Exception("Threading error.");
				case PAUSED:
					throw new Exception("Script has been paused.");
				default:
					throw new Exception("Unrecognized status: " + _RunningScript.getStatus() + ".");
				}
			} catch (Exception ex) {
				return ex;
			} finally {
				System.setOut(originalOut);
			}
		}

		@Override
		protected void done() {
			try {
				Object result = get();
				_RunningScript = null;
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

		if (_RunningScript != null) {
			messageError("Error - a script is already running.  NOTE:  this shouldn't actually be possible.");
			return;
		}

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
		if (_RunningScript != null) {
			messageError("Error - a script is already running.  NOTE:  this shouldn't actually be possible.");
			return;
		}
		setIsExecuting(true);
		ScriptExecutionWorker worker = new ScriptExecutionWorker(getCode(), executionTime);
		_RunningScript = null;
		onExecutionComplete(worker, worker.doSynchronized());
	}

	private void onExecutionComplete(ScriptExecutionWorker sender, Object result) {
		_LastResult = result;

		// Update the command history records.
		if (_CommandHistory.size() == 0
				|| !_CommandHistory.get(_CommandHistory.size() - 1).equals(sender.executingCode))
			_CommandHistory.add(sender.executingCode);
		_CommandHistoryIndex = _CommandHistory.size();
		_EditorPane.setText("");

		// Send a message indicating the results.
		if (result == null)
			message("null", _SystemMessageStyle);
		else if (result instanceof Exception)
			message(((Exception) result).getMessage(), _ErrorMessageStyle);
		else
			message(result.toString(), _SystemMessageStyle);

		// Update flag
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
	protected static Object interpret(Varargs result) {
		return result instanceof LuaNil ? "Ok" : result.tojstring();
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
	public void message(String message) {
		message(message, _SystemMessageStyle);
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

	/** Cause a message to display as an error in the REPL. */
	public void messageError(String message) {
		message(message, _ErrorMessageStyle);
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

	/*
	 * ================================================================
	 * JCodeREPL ACTION HANDLING MEMBERS
	 * ================================================================
	 */

	@Override
	public void actionPerformed(ActionEvent action) {

		switch (action.getActionCommand()) {
		case "CUT":
			_EditorPane.cut();
			break;
		case "COPY":
			_EditorPane.copy();
			break;
		case "PASTE":
			_EditorPane.paste();
			break;
		case "EXECUTE":
			execute(3000);
			break;
		case "HELP":
			System.out.println("Don't panic!");
			break;
		case "CANCEL":
			stop();
			break;
		default:
			System.out.println("Unimplemented action received by " + this.getClass().getName() + " object: "
					+ action.getActionCommand());
			break;
		}
		// TODO Auto-generated method stub

	}

	/** Called when there is a new visual parent for this REPL. */
	@Override
	public void addNotify() {
		super.addNotify();
		_EditorPane.requestFocusInWindow();
	}

}
