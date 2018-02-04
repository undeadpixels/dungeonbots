package com.undead_pixels.dungeon_bots.ui;

import com.undead_pixels.dungeon_bots.math.Vector2;
import com.undead_pixels.dungeon_bots.nogdx.OrthographicCamera;
import com.undead_pixels.dungeon_bots.nogdx.SpriteBatch;
import com.undead_pixels.dungeon_bots.nogdx.Texture;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * The screen for the regular game
 */
public class WorldView extends JComponent {
	
	private OrthographicCamera cam;
	private boolean didInitCam = false;
	
	private World world;
	
	private long lastTime;
	
	public WorldView() {
		world = new World(new File("sample-level-packs/sample-pack-1/levels/level1.lua"));
		
		this.setPreferredSize(new Dimension(9999, 9999));

		this.setFocusable(true);
		this.requestFocusInWindow();
	}
		

	public WorldView(World world) {
		AssetManager.loadAsset(AssetManager.AssetSrc.Player, Texture.class);
		AssetManager.finishLoading();
		this.world = world;
		
		lastTime = System.nanoTime(); // warning: this can overflow after 292 years of runtime
		
		this.setPreferredSize(new Dimension(9999, 9999));
	}
	
	/**
	 * Renders the world using the camera transform specific to this WorldView 
	 */
	@Override
	public void paint(Graphics g) {
		
		long nowTime = System.nanoTime();
		float dt = (nowTime - lastTime) / 1_000_000_000.0f;
		lastTime = nowTime;
		
		// TODO - move this update() thing elsewhere. Pretty please.
		// TODO
		// TODO
		// TODO
		if(world != null) {
			world.update(dt);
		}
		
		try {
			Graphics2D g2d = (Graphics2D) g;
			
			
			float w = this.getWidth();
			float h = this.getHeight();
			
			SpriteBatch batch = new SpriteBatch(g2d, w, h);
			
			if(!didInitCam) {
				cam = new OrthographicCamera(w, h);
				
				cam.zoomFor(world.getSize());
				
	    			didInitCam = true;
			}

			cam.viewportWidth = w;
			cam.viewportHeight = h;
			
			cam.update();
			batch.setProjectionMatrix(cam);
			//batch.setTransformMatrix(cam.view);
			
			if(world != null) {
				world.render(batch);
			}
		} catch(ClassCastException ex) {
			ex.printStackTrace();
		}
		
		
		// TODO - this should live elsewhere, but it'll at least help cap fps for now.
		long spareTime = 15_000_000 - (System.nanoTime() - lastTime);
		int sleepTime = (int)(spareTime/1000_000);
		System.out.println("DT = "+dt+",  sleep = "+sleepTime);
		if(sleepTime > 0) {
			Timer t = new Timer(sleepTime, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					repaint();
				}
			});
			t.setRepeats(false);
			t.start();
		} else {
			repaint();
		}
	}

	public OrthographicCamera getCamera() {
		return cam;
	}

	public Vector2 getScreenToGameCoords(int screenX, int screenY) {
		cam.update();
		Vector2 gameSpace = cam.unproject(new Vector2(screenX, screenY));
		
		return new Vector2(gameSpace.x, gameSpace.y);
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}
}
