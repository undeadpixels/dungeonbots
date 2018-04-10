package com.undead_pixels.dungeon_bots.ui;

import com.undead_pixels.dungeon_bots.math.Cartesian;
import com.undead_pixels.dungeon_bots.nogdx.OrthographicCamera;
import com.undead_pixels.dungeon_bots.nogdx.RenderingContext;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.World.StringEventType;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.ui.screens.Tool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * The screen for the regular game
 */
public class WorldView extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private OrthographicCamera cam;
	private boolean didInitCam = false;
	private boolean showGrid = true;

	private World world;

	private long lastTime;
	private transient Tile[] selectedTiles = null;
	private transient Entity[] selectedEntities = null;
	private Tool renderingTool = null;
	private boolean isPlaying = false;
	private final Timer timer;
	private final Consumer<World> winAction;

	public WorldView(World world, Consumer<World> winAction) {
		this.world = world;
		this.winAction = winAction;

		lastTime = System.nanoTime(); // warning: this can overflow after 292
										// years of runtime

		this.setPreferredSize(new Dimension(9999, 9999));

		timer = new Timer(16, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (WorldView.this.world != null) {
					long nowTime = System.nanoTime();
					float dt = (nowTime - lastTime) / 1_000_000_000.0f;

					if (dt > 1_000_000_000.0f) {
						dt = 1_000_000_000; // cap dt at 1 second
					}

					lastTime = nowTime;
					WorldView.this.world.update(dt);

					if (WorldView.this.world.isWon()) {
						timer.stop();
						winAction.accept(world);
					}
				}

				repaint();
			}

		});

		if (world.isPlayOnStart()) {
			setPlaying(true);
		}

		timer.start();

		this.setFocusable(true);
		this.requestFocusInWindow();
		
		this.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					if(WorldView.this.isShowing()) {
						WorldView.this.requestFocusInWindow();
						timer.start();
					} else {
						timer.stop();
					}
				}
			}
		});
		this.addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded (AncestorEvent event) {
				timer.start();
			}

			@Override
			public void ancestorRemoved (AncestorEvent event) {
				timer.stop();
				System.out.println(requestFocusInWindow());
			}

			@Override
			public void ancestorMoved (AncestorEvent event) {
				// TODO Auto-generated method stub
				
			}
			
		});

		this.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped (KeyEvent e) {
			}
			
			String lookupKeycode(KeyEvent e) {

				switch(e.getKeyCode()) {
					case KeyEvent.VK_UP:
						return "up";
					case KeyEvent.VK_DOWN:
						return "down";
					case KeyEvent.VK_LEFT:
						return "left";
					case KeyEvent.VK_RIGHT:
						return "right";
					default:
						char c = e.getKeyChar();
						if(c == KeyEvent.CHAR_UNDEFINED) {
							return null;
						} else {
							return ""+c;
						}
				}
			}

			@Override
			public void keyPressed (KeyEvent e) {
				String key = lookupKeycode(e);
				if(key != null) {
					world.fire(StringEventType.KEY_PRESSED, key);
				}
			}

			@Override
			public void keyReleased (KeyEvent e) {
				String key = lookupKeycode(e);
				if(key != null) {
					world.fire(StringEventType.KEY_RELEASED, key);
				}
			}
			
		});
	}


	/**
	 * Renders the world using the camera transform specific to this WorldView
	 */
	@Override
	public void paintComponent(Graphics g) {

		try {
			Graphics2D g2d = (Graphics2D) g;

			float w = this.getWidth();
			float h = this.getHeight();

			RenderingContext batch = new RenderingContext(g2d, w, h);

			if (!didInitCam) {
				cam = new OrthographicCamera(w, h);

				cam.zoomFor(world.getSize());

				didInitCam = true;
			}

			cam.setViewportSize(w, h);

			// cam.update(); //Nothing in this function call.
			batch.setProjectionMatrix(cam);

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

			// Render selection stuff.
			renderSelectedTiles(g2d, batch);
			renderSelectedEntities(g2d, batch);
			if (renderingTool != null)
				renderingTool.render(g2d, batch);

		} catch (ClassCastException ex) {
			ex.printStackTrace();
		}
	}


	/** Returns the camera being used to view the world. */
	public OrthographicCamera getCamera() {
		return cam;
	}


	/** Returns the given screen coordinates, translated into game space. */
	public Point2D.Float getScreenToGameCoords(double screenX, double screenY) {
		return cam.unproject((float) screenX, (float) screenY);
	}


	/** Returns the given screen coordinates, translated into game space. */
	public Point2D.Float getScreenToGameCoords(int screenX, int screenY) {
		return cam.unproject((float) screenX, (float) screenY);
	}


	/**
	 * Returns a rectangle defined by the given corner points, translated into
	 * game space.
	 */
	public Rectangle2D.Float getScreenToGameRect(int x1, int y1, int x2, int y2) {
		Point2D.Float pt1 = cam.unproject((float) x1, (float) y1);
		Point2D.Float pt2 = cam.unproject((float) x2, (float) y2);

		return Cartesian.makeRectangle(pt1, pt2);

	}


	/** Returns the world currently being viewed. */
	public World getWorld() {
		return world;
	}


	/** Sets the world to be viewed. */
	public void setWorld(World world) {
		this.world = world;

		setPlaying(world.isPlayOnStart());
	}


	/**Returns whether the grid is being displayed.*/
	public boolean getShowGrid() {
		return this.showGrid;
	}


	/**Sets whether to display the grid.*/
	public void setShowGrid(boolean value) {
		this.showGrid = value;
	}


	// ==================================================
	// ======= WorldView SELECTION STUFF ================
	// ==================================================

	/** Returns a copied array of the selected tiles positions. */
	public Tile[] getSelectedTiles() {
		return selectedTiles.clone();
	}


	/** Sets the selected tiles to an array copy of the indicated collection. */
	public void setSelectedTiles(Tile[] newTiles) {
		selectedTiles = newTiles;
	}


	private void renderSelectedTiles(Graphics2D g2d, RenderingContext batch) {
		Tile[] st = this.selectedTiles;
		if (st != null && st.length > 0) {
			g2d.setColor(new Color(1.0f, 0.3f, 0.0f, 0.4f));
			for (Tile tile : st) {
				Point2D.Float pt = tile.getPosition();
				batch.fillRect(pt.x, pt.y + 1f, 1f, 1f);
			}
			g2d.setColor(new Color(1.0f, 0.0f, 0.0f, 0.8f));
			g2d.setStroke(new BasicStroke(3));
			for (Tile tile : st) {
				Point2D.Float pt = tile.getPosition();
				batch.drawRect(pt.x, pt.y + 1f, 1f, 1f);
			}
		}
	}


	/** Returns a copied array of the selected entities. */
	public Entity[] getSelectedEntities() {
		return selectedEntities.clone();
	}


	/**
	 * Sets the selected entities to an array copy of the indicated collection.
	 */
	public void setSelectedEntities(Entity[] newEntities) {
		selectedEntities = newEntities;
	}


	private void renderSelectedEntities(Graphics2D g2d, RenderingContext batch) {
		Entity[] se = this.selectedEntities;
		if (se != null && se.length > 0) {
			g2d.setColor(new Color(1.0f, 1.0f, 0.0f, 0.4f));
			for (Entity e : se) {
				Point2D.Float pt = e.getPosition();
				batch.fillRect(pt.x, pt.y + 1f, 1f, 1f);
			}
			g2d.setStroke(new BasicStroke(3));
			g2d.setColor(new Color(1.0f, 1.0f, 0.0f, 0.8f));
			for (Entity e : se) {
				Point2D.Float pt = e.getPosition();
				batch.drawRect(pt.x, pt.y + 1f, 1f, 1f);
			}
		}
	}


	/** Returns the scribbling render tool. */
	public Tool getAdornmentTool() {
		return renderingTool;
	}


	/** Sets the scribbling render tool. */
	public void setAdornmentTool(Tool tool) {
		this.renderingTool = tool;
	}


	/** Returns whether the given entity is selected. */
	public boolean isSelectedEntity(Entity e) {
		Entity[] selected = selectedEntities;
		if (selected == null)
			return false;
		for (int i = 0; i < selected.length; i++) {
			if (e == selected[i])
				return true;
		}
		return false;
	}


	/** Returns whether the given tile is selected. */
	public boolean isSelectedTile(Tile tile) {
		Tile[] selected = selectedTiles;
		if (selected == null)
			return false;
		for (Tile t : selected)
			if (t == tile)
				return true;
		return false;
	}


	/**
	 * Returns whether the tile at the given game space location is selected.
	 */
	public boolean isSelectedTile(float x, float y) {
		Tile[] selected = selectedTiles;
		if (selected == null)
			return false;
		int xi = (int) x, yi = (int) y;
		for (int i = 0; i < selected.length; i++) {
			Tile t = selected[i];
			int tx = (int) t.getPosition().getX();
			if (tx != xi)
				continue;
			int ty = (int) t.getPosition().getY();
			if (ty == yi)
				return true;
		}
		return false;
	}


	/**
	 * @param isPlaying
	 */
	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;

		if (isPlaying) {
			world.runInitScripts();
		}
	}


	/**Returns whether this WorldView is current playing.*/
	public boolean getPlaying() {
		return this.isPlaying;
	}

}
