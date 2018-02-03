package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.undead_pixels.dungeon_bots.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.ui.JPlayerEditor;
import com.undead_pixels.dungeon_bots.ui.WorldView;

/**
 * A screen for gameplay
 */
public class GameplayScreen extends GDXandSwingScreen implements KeyListener, MouseListener, MouseMotionListener {
	
	/**
	 * The view for this screen
	 */
	private WorldView view;



	public GameplayScreen() {
		super();
		
		view = new WorldView();

		view.addKeyListener(this);
		view.addMouseListener(this);
		view.addMouseMotionListener(this);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int screenX = e.getX();
		int screenY = e.getY();
		Vector2 gameSpace = view.getScreenToGameCoords(screenX, screenY);
		Entity ent = view.getWorld().getEntityUnderLocation(gameSpace.x, gameSpace.y);
		
		if(ent instanceof Player) {
			
			
			JPlayerEditor jpe = new JPlayerEditor((Player)ent);
			this.addWindowFor(jpe,  "Player Editor");
			
		}

		System.out.println("Clicked entity "+ent+" at "+ gameSpace.x+", "+gameSpace.y+" (screen "+screenX+", "+screenY+")");
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

}
