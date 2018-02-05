/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.EventListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JWindow;

import com.undead_pixels.dungeon_bots.file.editor.GameEditorState;
import com.undead_pixels.dungeon_bots.file.editor.TileRegionSection;
import com.undead_pixels.dungeon_bots.math.Vector2;
import com.undead_pixels.dungeon_bots.ui.WorldView;

/**
 * The screen for the level editor
 * @author Wesley
 *
 */
public class LevelEditorScreen extends Screen {

	
	

	/**
	 * The view
	 */
	private WorldView view;
	
	/**
	 * Current state. Used to update the world and write to file.
	 */
	private GameEditorState state;

	@Override
	protected ScreenController makeController() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void addComponents(Container pane) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setDefaultLayout() {
		// TODO Auto-generated method stub
		
	}


	
}
