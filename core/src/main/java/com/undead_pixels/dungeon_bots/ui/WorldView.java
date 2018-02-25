package com.undead_pixels.dungeon_bots.ui;

import com.undead_pixels.dungeon_bots.nogdx.OrthographicCamera;
import com.undead_pixels.dungeon_bots.nogdx.SpriteBatch;
import com.undead_pixels.dungeon_bots.nogdx.Texture;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.ui.screens.Tool;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * The screen for the regular game
 */
public class WorldView extends JComponent {

	private OrthographicCamera cam;
	private boolean didInitCam = false;
	private boolean showGrid = true;

	private World world;

	private long lastTime;
	private Point[] selectedTiles = null;
	private Tool renderingTool = null;

	/*
	 * @Deprecated public WorldView() { // world = new World(new //
	 * File("sample-level-packs/sample-pack-1/levels/level1.lua")); world = new
	 * World(new File("sample-level-packs/sample-pack-1/levels/level2.lua"));
	 * 
	 * this.setPreferredSize(new Dimension(9999, 9999));
	 * 
	 * this.setFocusable(true); this.requestFocusInWindow(); }
	 */

	public WorldView(World world) {
		AssetManager.loadAsset(AssetManager.AssetSrc.Player, Texture.class);
		AssetManager.finishLoading();
		this.world = world;

		lastTime = System.nanoTime(); // warning: this can overflow after 292
										// years of runtime

		this.setPreferredSize(new Dimension(9999, 9999));
		selectedTiles = new Point[0];
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
		if (world != null) {
			world.update(dt);
		}

		try {
			Graphics2D g2d = (Graphics2D) g;

			float w = this.getWidth();
			float h = this.getHeight();

			SpriteBatch batch = new SpriteBatch(g2d, w, h);

			if (!didInitCam) {
				cam = new OrthographicCamera(w, h);

				cam.zoomFor(world.getSize());

				didInitCam = true;
			}

			cam.setViewportSize(w, h);

			// cam.update(); //Nothing in this function call.
			batch.setProjectionMatrix(cam);
			// batch.setTransformMatrix(cam.view);

			if (world != null) {
				world.render(batch);
			}

			// Render the grid.
			if (showGrid) {
				g2d.setStroke(new BasicStroke(1));
				g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
				Point2D.Float size = world.getSize();
				// draw Y lines
				for (int i = 0; i <= size.y + .5f; i++) {
					batch.drawLine(0, i, size.x, i);
				}
				// draw X lines
				for (int j = 0; j <= size.x + .5f; j++) {
					batch.drawLine(j, 0, j, size.y);
				}
			}

			// Render selections.
			Point[] selections = this.selectedTiles;
			if (selections != null && selections.length > 0) {
				g2d.setStroke(new BasicStroke(3));
				g.setColor(new Color(1.0f, 0.3f, 0.0f, 0.4f));
				for (int i = 0; i < selections.length; i++) {
					Point pt = selections[i];
					int x = pt.x, y = pt.y;
					batch.fillRect(x, y + 1, 1, 1);
				}
				g.setColor(new Color(1.0f, 0.0f, 0.0f, 0.8f));
				for (int i = 0; i < selections.length; i++) {
					Point pt = selections[i];
					int x = pt.x, y = pt.y;
					batch.drawRect(x, y + 1, 1, 1);
				}
			}

			// Render scribbles.
			if (renderingTool != null)
				renderingTool.render(g2d);

		} catch (ClassCastException ex) {
			ex.printStackTrace();
		}

		// TODO - this should live elsewhere, but it'll at least help cap fps
		// for now.
		long spareTime = 15_000_000 - (System.nanoTime() - lastTime);
		int sleepTime = (int) (spareTime / 1000_000);
		// System.out.println("DT = "+dt+", sleep = "+sleepTime);
		if (sleepTime > 0) {
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

	/** Returns the camera being used to view the world. */
	public OrthographicCamera getCamera() {
		return cam;
	}

	/** Returns the given screen coordinates, translated into game space. */
	public Point2D.Float getScreenToGameCoords(int screenX, int screenY) {
		return cam.unproject((float) screenX, (float) screenY);
	}

	/** Returns the world currently being viewed. */
	public World getWorld() {
		return world;
	}

	/** Sets the world to be viewed. */
	public void setWorld(World world) {
		this.world = world;
	}

	/** Returns a copied array of the selection positions. */
	public Point[] getSelectedTiles() {
		return selectedTiles.clone();
	}

	public void setSelectedTiles(Point[] newSelections) {
		selectedTiles = newSelections.clone();
	}

	public Tool getRenderingTool() {
		return renderingTool;
	}

	public void setRenderingTool(Tool tool) {
		this.renderingTool = tool;
	}
}
