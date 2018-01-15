package com.undead_pixels.dungeon_bots.ui.code_edit;

import javax.swing.JComponent;

/**
 * An abstract place where users can enter code, somehow
 */
public abstract class AbstractCodeEditorController {
	/**
	 * @return	The current text in this editor
	 */
	public abstract String getAllText();
	
	/**
	 * @return	Something that can be added to the UI for the user to interact with
	 */
	public abstract JComponent getGuiElement();
}
