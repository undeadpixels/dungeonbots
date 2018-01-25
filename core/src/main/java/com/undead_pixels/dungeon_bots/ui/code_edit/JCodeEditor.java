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

import org.luaj.vm2.Varargs;

import com.undead_pixels.dungeon_bots.script.LuaScript;

public class JCodeEditor extends JPanel implements ActionListener {

	private Mode _Mode;
	private LuaScript _Script;
	final private int _MessageMax = 3000;
	private JTextPane _MessagePane;
	private JEditorPane _EditorPane;
	private boolean _IsExecuting = false;

	public enum Mode {
		CUSTOM, REPL, SIMPLE
	}

	/*
	 * ================================================================
	 * JCodeEditor CONSTRUCTION MEMBERS
	 * ================================================================
	 */

	public JCodeEditor(Mode mode) {
		super(new BorderLayout());
		_Mode = mode;
		addComponents();
	}

	public JCodeEditor() {
		this(Mode.REPL);
	}

	private void addComponents() {
		if (!(getLayout() instanceof BorderLayout)) {
			add(new JLabel("Wrong layout - must be a BorderLayout."));
			return;
		}
		setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
		_EditorPane = new JEditorPane();
		_EditorPane.setFocusable(true);

		switch (_Mode) {
		case REPL:
			_EchoMessageStyle = new SimpleAttributeSet();
			StyleConstants.setForeground(_EchoMessageStyle, Color.WHITE);
			StyleConstants.setBackground(_EchoMessageStyle, Color.BLACK);
			StyleConstants.setBold(_EchoMessageStyle, false);

			_SystemMessageStyle = new SimpleAttributeSet();
			StyleConstants.setForeground(_SystemMessageStyle, Color.GREEN);
			StyleConstants.setBackground(_SystemMessageStyle, Color.BLACK);
			StyleConstants.setBold(_SystemMessageStyle, true);
			
			_ErrorMessageStyle = new SimpleAttributeSet();
			StyleConstants.setForeground(_ErrorMessageStyle, Color.RED);
			StyleConstants.setBackground(_ErrorMessageStyle, Color.BLACK);
			StyleConstants.setBold(_ErrorMessageStyle,  true);

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

			break;
		case SIMPLE:

		default:
			throw new IllegalStateException("Not implemented yet.");

		}

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

		URL url = JCodeEditor.class.getResource(imageURL);
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

	private SimpleAttributeSet _EchoMessageStyle = null;
	private SimpleAttributeSet _SystemMessageStyle = null;
	private SimpleAttributeSet _ErrorMessageStyle = null;

	/**
	 * Sets the echo message appearance to the given attribute set. If set to
	 * null, no echo messages will appear. *
	 * 
	 * @return Returns the attributes set.
	 */
	public SimpleAttributeSet setEchoMessageAppearance(SimpleAttributeSet appearance) {
		return _EchoMessageStyle = appearance;
	}

	

	/**
	 * Sets the system message appearance to the given attribute set. If set to
	 * null, no system messages will appear. *
	 * 
	 * @return Returns the attributes set.
	 */
	public SimpleAttributeSet setSystemMessageAppearance(SimpleAttributeSet appearance) {
		return _SystemMessageStyle = appearance;
	}

	/*
	 * ================================================================
	 * JCodeEditor CODING INTERFACE MEMBERS
	 * ================================================================
	 */

	/**
	 * @param milliseconds The maximum amount of time allowed for execution to complete.
	 * @return
	 */
	public boolean execute(long milliseconds) {
		if (_Script == null) return false;		
		_Script.start();
		_Script.join(milliseconds);
		
		if (_Script.getError() != null){
			message(_Script.getError().getMessage(), this);
		}else{
			switch (_Script.getStatus()){
			case READY:
				Optional<Varargs> results = _Script.getResults();
				message("TODO:  present results correctly, " + results.get().toString(), _SystemMessageStyle);
				break;
			case RUNNING:
				message("Error - the script is still running.", _ErrorMessageStyle);
				break;
			case TIMEOUT:
				message("Script interrupted without completion.", _ErrorMessageStyle);
				break;			
			default:
			}
			
			
		}
		
		
		/*READY,
	    RUNNING,
	    STOPPED,
	    LUA_ERROR,
	    ERROR,
	    TIMEOUT,
	    PAUSED,
	    COMPLETE*/
		
		//TODO:  wait until script execution completes.
		/*
		 * if (_CanExecute) { _Script.start(); _Script.join(milliseconds);
		 * 
		 * return true; }
		 */
		return false;
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

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		System.out.println("Action received by " + this.getClass().getName() + " object: " + arg0.getActionCommand());
		// TODO Auto-generated method stub

	}

}
