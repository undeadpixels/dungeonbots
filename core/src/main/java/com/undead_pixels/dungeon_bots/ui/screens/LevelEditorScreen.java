/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.undead_pixels.dungeon_bots.file.editor.GameEditorState;
import com.undead_pixels.dungeon_bots.file.editor.TileRegionSection;
import com.undead_pixels.dungeon_bots.math.Vector2;
import com.undead_pixels.dungeon_bots.ui.WorldView;

/**
 * The screen for the level editor
 * @author Wesley
 *
 */
public class LevelEditorScreen extends GDXandSwingScreen implements MouseListener, KeyListener, MouseMotionListener {

	/**
	 * The view
	 */
	private WorldView view;
	
	/**
	 * Current state. Used to update the world and write to file.
	 */
	private GameEditorState state;

	/**
	 * Constructor
	 */
	public LevelEditorScreen() {
		state = new GameEditorState();
		view = new WorldView(state.world);
		
		// TODO - these are 0-based, but some lua things are 1-based. Needs to be figured out.
		state.tileRegionSection.add(new TileRegionSection.TileRegion(0, 15, 0, 15, "floor"));

		view.addKeyListener(this);
		view.addMouseListener(this);
		view.addMouseMotionListener(this);
		
		this.addPane(view, BorderLayout.CENTER);
		
		

		// super-simple swing gui on side
		Box b = new Box(BoxLayout.Y_AXIS);
		JButton printButton = new JButton("Print to lua");
		printButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(state.toLua());
			}
			
		});
		b.add(printButton);
		b.add(new JLabel("click/drag with the mouse to place walls"));
		this.addPane(b, BorderLayout.WEST);
	}


	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		int screenX = e.getX();
		int screenY = e.getY();
		Vector2 gameSpace = view.getScreenToGameCoords(screenX, screenY);
		
		int x = (int)gameSpace.x;
		int y = (int)gameSpace.y;
		
		state.tileRegionSection.add(new TileRegionSection.TileRegion(x, x, y, y, "wall"));
		e.consume();
	}


	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseDragged(MouseEvent e) {
		int screenX = e.getX();
		int screenY = e.getY();
		Vector2 gameSpace = view.getScreenToGameCoords(screenX, screenY);
		
		int x = (int)gameSpace.x;
		int y = (int)gameSpace.y;
		
		state.tileRegionSection.add(new TileRegionSection.TileRegion(x, x, y, y, "wall"));
		e.consume();
	}


	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
