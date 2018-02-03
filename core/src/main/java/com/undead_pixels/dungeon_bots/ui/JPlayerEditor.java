package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeEditor;
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeREPL;

public class JPlayerEditor extends JPanel {

	public JPlayerEditor(Player player) {
		super(new BorderLayout());
		
		this.setPreferredSize(new Dimension(400,600));

		//JCodeREPL repl = new JCodeREPL(player.getSandbox());				
		//this.add(repl, BorderLayout.LINE_START);
		
		JCodeEditor editor = new JCodeEditor();
		this.add(editor, BorderLayout.LINE_START);
		
	}

	

	

}
