package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Optional;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.script.LuaScriptEnvironment;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;

public class JCodeREPL extends JPanel implements ActionListener {

	private LuaScript _Script;
	final private int _MessageMax = 3000;
	private JTextPane _MessagePane;
	private JEditorPane _EditorPane;
	private Object _LastResult = null;
	// private boolean _IsExecuting = false;

	/*
	 * ================================================================
	 * JCodeEditor CONSTRUCTION MEMBERS
	 * ================================================================
	 */

	public JCodeREPL() {
		this(new LuaScriptEnvironment(SecurityLevel.DEBUG));
	}

	public JCodeREPL(LuaScriptEnvironment sandbox) {
		super(new BorderLayout());

		_Script = sandbox.script("");

		_EchoMessageStyle = createSimpleAttributeSet(Color.WHITE, Color.BLACK, false);
		_SystemMessageStyle = createSimpleAttributeSet(Color.GREEN, Color.BLACK, true);
		_ErrorMessageStyle = createSimpleAttributeSet(Color.RED, Color.BLACK, true);

		addComponents();
	}

	private void addComponents() {
		if (!(getLayout() instanceof BorderLayout)) {
			add(new JLabel("Wrong layout - must be a BorderLayout."));
			return;
		}
		setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
		_EditorPane = new JEditorPane();
		_EditorPane.setFocusable(true);

		setPreferredSize(new Dimension(300, 500));
		add(makeREPLToolBar(), BorderLayout.PAGE_START);

		_MessagePane = new JTextPane();
		JScrollPane messageScroller = new JScrollPane(_MessagePane);
		add(messageScroller, BorderLayout.CENTER);
		_MessagePane.setFocusable(false);
		_MessagePane.setText("");

		JPanel executePanel = new JPanel(new BorderLayout());
		_EditorPane.setPreferredSize(new Dimension(this.getPreferredSize().width, 100));
		this.add(executePanel, BorderLayout.PAGE_END);
		executePanel.add(new JScrollPane(_EditorPane), BorderLayout.CENTER);
		executePanel.add(makeButton("tbd.gif", "EXECUTE", "Click to execute", ">", this), BorderLayout.LINE_END);

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
	 * JCodeEditor APPEARANCE MEMBERS
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
	 * JCodeEditor CODING INTERFACE MEMBERS
	 * ================================================================
	 */

	/**
	 * @param milliseconds
	 *            The maximum amount of time allowed for execution to complete.
	 * @return
	 */
	public boolean execute(long milliseconds) {
		if (_Script == null)
			return false;
		_Script.setScript(getCode());
		message(">>> " + getCode(), _EchoMessageStyle);
		_Script.start();
		_Script.join(milliseconds);

		if (_Script.getError() != null) {
			message(_Script.getError().getMessage(), _ErrorMessageStyle);
		} else {
			switch (_Script.getStatus()) {
			case COMPLETE:
			case READY:
				_LastResult = Interpret(_Script.getResults());
				if (_LastResult == null) message ("null", _SystemMessageStyle);
				else if (_LastResult instanceof LuaValue) message ("ok", _SystemMessageStyle);  //Void.  TODO:  would this always indicate void?
				else message(_LastResult.toString(), _SystemMessageStyle);				
				break;
			case RUNNING:
				message("Error - the script is still running.", _ErrorMessageStyle);
				break;
			case TIMEOUT:
				message("Script interrupted without completion.", _ErrorMessageStyle);
				break;
			case STOPPED:
				message("Script has been stopped.", _ErrorMessageStyle);
				break;
			case LUA_ERROR:
				message("Error: " + _Script.getError().getMessage(), _ErrorMessageStyle);
				break;
			case ERROR:
				message("Threading error: " + _Script.getError().toString(), _ErrorMessageStyle);
				break;
			case PAUSED:
				message("Script has been paused.", _ErrorMessageStyle);
				break;
			default:
				message("Unrecognized script status: " + _Script.getStatus(), _ErrorMessageStyle);
				break;
			}

		}

		return true;
	}

	/**
	 * Interprets the result of a LuaScript execution and converts it into a
	 * suitable Java object.
	 */
	public static Object Interpret(Optional<Varargs> rawResult) {
		if (rawResult == null)
			return null;
		Varargs unpackedResult = rawResult.get();
		if (unpackedResult instanceof LuaInteger)
			return ((LuaInteger) unpackedResult).toint();
		if (unpackedResult instanceof LuaValue) {
			// 
			LuaValue lv = (LuaValue) unpackedResult;
			if (lv.tojstring() == "none") //Void result.
				return lv;

		}
		throw new ClassCastException(
				"Have not implemented Lua-to-Java interpretation of type " + unpackedResult.getClass().getName() + ".");
	}

	public void message(String message, Object sender) {
		if (sender == this)
			message(message, _EchoMessageStyle);
		else
			message(message, _SystemMessageStyle);
	}

	protected void message(String message, AttributeSet attribs) {

		if (_MessagePane == null || attribs == null)
			return;

		StyledDocument doc = _MessagePane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), message + "\n\n", attribs);
			doc.remove(0, Math.max(0, doc.getLength() - _MessageMax));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Returns all the messages contained in this editor. */
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

	/** Returns the code contents being edited in this editor. */
	public String getCode() {
		try {
			Document doc = _EditorPane.getDocument();
			return doc.getText(0, doc.getLength());
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public void setCode(String code) {
		try {
			Document doc = _EditorPane.getDocument();
			doc.remove(0, doc.getLength());
			doc.insertString(0, code, _EchoMessageStyle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent action) {

		switch (action.getActionCommand()) {
		case "EXECUTE":
			execute(100);
			break;
		default:
			System.out.println("Unimplemented action received by " + this.getClass().getName() + " object: "
					+ action.getActionCommand());
			break;
		}
		// TODO Auto-generated method stub

	}

}
