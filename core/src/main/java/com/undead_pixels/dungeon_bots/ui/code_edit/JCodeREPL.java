package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.luaj.vm2.LuaNil;
import org.luaj.vm2.Varargs;

import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.ScriptEventStatusListener;
import com.undead_pixels.dungeon_bots.script.ScriptStatus;
import com.undead_pixels.dungeon_bots.script.LuaInvocation;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaSandbox;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;

public class JCodeREPL extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private LuaSandbox _Sandbox;
	private JScrollPane _MessageScroller;
	public final long MAX_EXECUTION_TIME = 3000;
	final private int _MessageMax = 10000;
	private JTextPane _MessagePane;
	private JEditorPane _EditorPane;
	private Object _LastResult = null;
	private JButton _CancelBttn;
	private JButton _ExecuteBttn;
	
	private LuaInvocation _RunningScript = null;

	private ArrayList<String> _CommandHistory = new ArrayList<String>();
	private int _CommandHistoryIndex = 0;


	/* ================================================================
	 * JCodeREPL CONSTRUCTION MEMBERS
	 * ================================================================ */

	/** Creates a new REPL. All code will execute in the given sandbox. */
	public JCodeREPL(LuaSandbox sandbox) {

		if (sandbox == null)
			sandbox = new LuaSandbox(SecurityLevel.DEBUG);

		_Sandbox = sandbox;

		_Sandbox.addOutputEventListener((str) -> this.message(str));

		_EchoMessageStyle = putSimpleAttributeSet(Color.WHITE, Color.BLACK, false);
		_SystemMessageStyle = putSimpleAttributeSet(Color.GREEN, Color.BLACK, true);
		_ErrorMessageStyle = putSimpleAttributeSet(Color.RED, Color.BLACK, true);

		addComponents();

		_MessagePane.setText("");
		_EditorPane.setText("");

		addKeyBindings();
	}


	/**
	 * @param entity
	 */
	public JCodeREPL(GetLuaSandbox sandboxable) {
		this(sandboxable.getSandbox());
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
		_ExecuteBttn = UIBuilder.buildButton().image("icons/play.png").minSize(40, 40).toolTip("Click to execute.")
				.action("EXECUTE", this).focusable(false).preferredSize(40, 40).create();
		_CancelBttn = UIBuilder.buildButton().image("icons/abort.png", true).toolTip("Click to cancel.")
				.action("CANCEL", this).focusable(false).enabled(false).create();
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
	private void addKeyBindings() {

		InputMap inputMap = _EditorPane.getInputMap();
		ActionMap actionMap = _EditorPane.getActionMap();

		// On CTRL+ENTER, should execute.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK), "EXECUTE");
		actionMap.put("EXECUTE", new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;


			@Override
			public void actionPerformed(ActionEvent e) {
				execute();
			}
		});

		// On ESC, should clear the command line.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CLEAR");
		actionMap.put("CLEAR", new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;


			@Override
			public void actionPerformed(ActionEvent e) {
				_EditorPane.setText("");
			}
		});

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "STOP");
		actionMap.put("STOP", new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;


			@Override
			public void actionPerformed(ActionEvent e) {
				messageError("^C");
				stop();
			}

		});

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), "CLOSE");
		actionMap.put("CLOSE", new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;


			@Override
			public void actionPerformed(ActionEvent e) {
				messageError("^Q");
				Component parent = _EditorPane.getParent();
				while (parent != null) {
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

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;


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

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;


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
		JButton bttnCut = UIBuilder.buildButton().image("icons/cut.png").toolTip("Cut a selected section.")
				.action("CUT", this).focusable(false).preferredSize(30, 30).create();
		JButton bttnCopy = UIBuilder.buildButton().image("icons/copy.png").toolTip("Copy a selected section.")
				.action("COPY", this).focusable(false).preferredSize(30, 30).create();
		JButton bttnPaste = UIBuilder.buildButton().image("icons/paste.png").toolTip("Paste at the cursor.")
				.action("PASTE", this).focusable(false).preferredSize(30, 30).create();
		JButton bttnHelp = UIBuilder.buildButton().image("icons/question.png")
				.toolTip("Get help with the command line.").action("HELP", this).focusable(false).preferredSize(30, 30)
				.create();

		result.add(bttnCut);
		result.add(bttnCopy);
		result.add(bttnPaste);
		result.add(bttnHelp);

		return result;
	}


	/* ================================================================
	 * JCodeREPL APPEARANCE MEMBERS
	 * ================================================================ */

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


	/* ================================================================
	 * JCodeREPL CODING INTERFACE MEMBERS
	 * ================================================================ */

	/** Stops any running script by forcing an interrupt. */
	public void stop() {
		LuaInvocation toBeCancelled = _RunningScript;
		if (toBeCancelled == null)
			return;
		toBeCancelled.stop();
	}


	/**
	 * Starts execution of the code contained in the code editor, on a
	 * background thread.
	 */
	public LuaInvocation execute() {

		setIsExecuting(true);
		ScriptEventStatusListener listener = new ScriptEventStatusListener() {

			@Override
			public void scriptEventFinished(LuaInvocation script, ScriptStatus status) {
				if (_RunningScript == script) {
					_RunningScript = null;
				}

				if (script.getError() != null) {
					onExecutionComplete(script, script.getError());
				} else {
					switch (script.getStatus()) {
					case READY:
						onExecutionComplete(script, new Exception("Script did not execute."));
						break;
					case COMPLETE:
						onExecutionComplete(script, script.getResults().map(result -> interpret(result)).orElse("Ok"));
						break;
					case RUNNING:
						onExecutionComplete(script, new Exception("Script is still running."));
						break;
					case TIMEOUT:
						onExecutionComplete(script, new Exception("Script timed out."));
						break;
					case STOPPED:
						onExecutionComplete(script, new Exception("Script has been interrupted."));
						break;
					case LUA_ERROR:
						onExecutionComplete(script, new Exception("Lua error."));
						break;
					case ERROR:
						onExecutionComplete(script, new Exception("Threading error."));
						break;
					case PAUSED:
						onExecutionComplete(script, new Exception("Script has been paused."));
						break;
					default:
					}
				}
			}


			@Override
			public void scriptEventStarted(LuaInvocation script, ScriptStatus status) {
			}

		};

		String code = getCode();
		message(">>> " + code, _EchoMessageStyle);
		_RunningScript = _Sandbox.enqueueCodeBlock(getCode(), listener);
		_EditorPane.setText("");

		// Update the command history records.
		if (_CommandHistory.size() == 0 || !_CommandHistory.get(_CommandHistory.size() - 1).equals(code))
			_CommandHistory.add(code);
		_CommandHistoryIndex = _CommandHistory.size();

		return _RunningScript;
	}

	private void onExecutionComplete(LuaInvocation sender, Object result) {
		_LastResult = result;
		System.out.println("Result = "+result);

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


	/* ================================================================
	 * JCodeREPL MESSAGING MEMBERS
	 * ================================================================ */

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


	/* ================================================================
	 * JCodeREPL ACTION HANDLING MEMBERS
	 * ================================================================ */

	private final HashSet<ActionListener> _ActionListeners = new HashSet<ActionListener>();


	// This class fires action events. Add a listener to receive those events.
	public void addActionListener(ActionListener l) {
		_ActionListeners.add(l);
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		switch (e.getActionCommand()) {
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
			execute();
			break;
		case "HELP":
			// Pass on the event to every listener.
			e = new ActionEvent(this, e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers());
			for (ActionListener l : _ActionListeners) l.actionPerformed(e);
			break;
		case "CANCEL":
			stop();
			break;
		default:
			System.out.println("Unimplemented action received by " + this.getClass().getName() + " object: "
					+ e.getActionCommand());
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
