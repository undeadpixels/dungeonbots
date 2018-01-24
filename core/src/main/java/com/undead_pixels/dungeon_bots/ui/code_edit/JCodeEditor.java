package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.text.JTextComponent;

import com.undead_pixels.dungeon_bots.script.LuaScript;

public class JCodeEditor extends JPanel implements ActionListener {

	private Mode _Mode;
	private LuaScript _Script;

	public enum Mode {
		CUSTOM, REPL, SIMPLE
	}

	public JCodeEditor(Mode mode) {
		super(new BorderLayout());
		_Mode = mode;
		addComponents();
	}

	public JCodeEditor() {
		this(Mode.REPL);
	}

	private void addComponents()  {
		if (!(getLayout() instanceof BorderLayout)) {
			add(new JLabel("Wrong layout - must be a BorderLayout."));
			return;
		}
		setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);

		switch (_Mode){
		case REPL:
			setPreferredSize(new Dimension(300,500));
			add(makeREPLToolBar(), BorderLayout.PAGE_START);
			add(new JEditorPane(), BorderLayout.CENTER);
			break;
		default:
			throw new IllegalStateException("Not implemented yet.");
			
			
		}
		
	}
	
	private JToolBar makeREPLToolBar(){
		JToolBar result = new JToolBar();
		result.add(makeButton("testing.gif", "CLICK", "ToolTipA", "AltTextA", this));
		result.add(makeButton("testing.gif", "CLICK", "ToolTipB", "AltTextB", this));
		return result;
	}
	
	/**
	 * Creates a button with the appearance described.  Don't forget to set a listener.
	 * @param imageURL The image resource that will appear in the button.
	 * @param actionCommand The command that the button will issue.
	 * @param toolTipText The tip presented when hovering the mouse over the button.
	 * @param altText Something-or-other...
	 * @param l The listener.
	 * @return Returns a JButton.
	 */
	public static JButton makeButton(String imageURL, String actionCommand, String toolTipText, String altText, ActionListener l){
		
		JButton resultButton = new JButton();
		resultButton.setActionCommand(actionCommand);
		resultButton.setToolTipText(toolTipText);
		resultButton.setToolTipText(toolTipText);
		
		resultButton.addActionListener(l);
		
		URL url = JCodeEditor.class.getResource(imageURL);
		if (url != null){
			resultButton.setIcon(new ImageIcon(url, altText));
		} else{
			resultButton.setText(altText);
			System.err.println("Resource was missing.  Attempted to created button with image at " + imageURL + ".");
		}
		
		return resultButton;
	}

	
	
	public boolean execute(long milliseconds) {
		/*if (_CanExecute) {
			_Script.start();
			_Script.join(milliseconds);

			return true;
		}*/
		return false;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		System.out.println("action received. " + arg0.getActionCommand());
		// TODO Auto-generated method stub
		
	}

}
