package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.MouseInputListener;

import com.undead_pixels.dungeon_bots.math.Cartesian;
import com.undead_pixels.dungeon_bots.scene.EntityType;
import com.undead_pixels.dungeon_bots.scene.TileType;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.RpgActor;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.JEntityEditor;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;
import com.undead_pixels.dungeon_bots.ui.WorldView;

/** A tool is a class which determines how input is handled. */
public abstract class Tool implements MouseInputListener, KeyListener {

	public final String name;
	public final Image image;


	public Tool(String name, Image image) {
		this.name = name;
		this.image = image;
	}


	public Tool(String name, Image image, World world, WorldView view, Window owner) {
		this.name = name;
		this.image = image;
	}


	public Tool(String name, Image image, World world) {
		this(name, image, world, null, null);
	}


	@Override
	public void mouseClicked(MouseEvent e) {
	}


	@Override
	public void mouseEntered(MouseEvent e) {
	}


	@Override
	public void mouseExited(MouseEvent e) {
	}


	@Override
	public void mousePressed(MouseEvent e) {
	}


	@Override
	public void mouseReleased(MouseEvent e) {
	}


	@Override
	public void mouseDragged(MouseEvent e) {

	}


	@Override
	public void mouseMoved(MouseEvent e) {
	}


	@Override
	public void keyPressed(KeyEvent e) {
	}


	@Override
	public void keyReleased(KeyEvent e) {
	}


	@Override
	public void keyTyped(KeyEvent e) {
	}


	public void render(Graphics2D g) {
	}


	/** The editor state? */
	public static class SelectionModel {

		public TileType tileType = null;
		public EntityType entityType = null;

		/** The current Tool. */
		public Tool tool = null;

	}


	public static class Selector extends Tool {

		private final WorldView view;
		private final Window owner;
		private final World world;
		private Point cornerA = null;
		private Point cornerB = null;
		private final SecurityLevel securityLevel;


		public Selector(WorldView view, Window owner, SecurityLevel securityLevel) {
			super("Selector", UIBuilder.getImage("selector.gif"));
			this.view = view;
			this.owner = owner;
			this.world = view.getWorld();
			this.securityLevel = securityLevel;
		}


		@Override
		public void mousePressed(MouseEvent e) {

			// If there is a cornerA, it means selection has started
			// already.
			if (cornerA != null)
				return;

			assert (view.getRenderingTool() == null); // sanity check.

			// Set the selection corner in screen coordinates.
			cornerB = cornerA = new Point(e.getX(), e.getY());

			// The view should start rendering the lasso.
			view.setRenderingTool(this);
			e.consume();
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			// If drawing isn't happening, just return.
			if (cornerA == null)
				return;

			assert (view.getRenderingTool() == this); // Sanity check.

			cornerB = new Point(e.getX(), e.getY());

			e.consume();
		}


		@Override
		public void mouseReleased(MouseEvent e) {

			// If there is no cornerA, it means selection hasn't started
			// yet.
			if (cornerA == null)
				return;

			// What is the current lasso in game space?
			Rectangle2D.Float rect = view.getScreenToGameRect(cornerA.x, cornerA.y, cornerB.x, cornerB.y);
			if (rect.width == 0.0f)
				rect.width = 0.01f;
			if (rect.height == 0.0f)
				rect.height = 0.01f;

			// Find the entities (first) or tiles (second) that are lassoed.
			// If only one entity is lassoed and that entity is already
			// selected, open its editor. Otherwise if any entities are
			// lassoed, select them. But if no entities are lassoed, look to
			// tiles. If selecting tiles that are already part of the
			// selection, just update the tile selection. Otherwise, nothing
			// is selected.

			List<Actor> se = world.getActorsUnderLocation(rect);
			List<Tile> st = world.getTilesUnderLocation(rect);
			if (st.size() == 1 && se.size() == 1 && view.isSelectedEntity(se.get(0))) {
				// Clicked on an entity. Open its editor.
				view.setSelectedEntities(new Entity[] { se.get(0) });
				JEntityEditor.create(owner, se.get(0), securityLevel, "Entity Editor");
				view.setSelectedTiles(null);
			} else if (se.size() > 0) {
				// One or more unselected entities are lassoed. Select them
				// all.
				view.setSelectedEntities(se.toArray(new Entity[se.size()]));
				view.setSelectedTiles(null);
			} else if (st.size() == 1 && !view.isSelectedTile(st.get(0))) {
				// Clicked on an unselected tile. Clear the tile selection.
				view.setSelectedTiles(null);
				view.setSelectedEntities(null);
			} else if (st.size() > 0) {
				// More than one tile lassoed. Select them all.
				view.setSelectedTiles(st.toArray(new Tile[st.size()]));
				view.setSelectedEntities(null);
			} else {
				// Neither tile nor entity selected.
				view.setSelectedTiles(null);
				view.setSelectedEntities(null);
			}

			// The view should no longer render the lasso.
			view.setRenderingTool(null);

			// Show that selection is complete, and re-selection hasn't
			// started.
			cornerB = cornerA = null;
			e.consume();
		}


		@Override
		public void render(Graphics2D g) {
			g.setStroke(new BasicStroke(2));
			g.setColor(Color.red);
			Rectangle rect = Cartesian.makeRectangle(cornerA, cornerB);
			// System.out.println(rect.toString());
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		}

	}


	public static class TilePen extends Tool {

		private final WorldView view;
		private final World world;

		public final SelectionModel selection;


		public TilePen(WorldView view, SelectionModel selection) {
			super("Tile Pen", null);
			this.view = view;
			this.world = view.getWorld();
			this.selection = selection;
		}


		@Override
		public void mouseClicked(MouseEvent e) {
			if (selection.tileType != null)
				drawTile(e.getX(), e.getY(), selection.tileType);
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			if (selection.tileType != null)
				drawTile(e.getX(), e.getY(), selection.tileType);
		}


		public void drawTile(int screenX, int screenY, TileType tileType) {
			Point2D.Float gamePos = view.getScreenToGameCoords(screenX, screenY);
			Tile existingTile = world.getTile(gamePos);
			if (existingTile == null)
				throw new RuntimeException("Have not implemented drawing a tile where one does not already exist.");
			assert (tileType != null);
			Point2D.Float existingPoint = existingTile.getPosition();
			world.setTile((int) existingPoint.getX(), (int) existingPoint.getY(), tileType);
		}

	}


	public static class EntityPlacer extends Tool {

		private final WorldView view;
		private final World world;
		private final SelectionModel selection;
		private final Window owner;
		private final SecurityLevel securityLevel;


		public EntityPlacer(WorldView view, SelectionModel selection, Window owner, SecurityLevel securityLevel) {
			super("EntityPlacer", UIBuilder.getImage("entity_placer.gif"));
			this.view = view;
			this.world = view.getWorld();
			this.selection = selection;
			this.owner = owner;
			this.securityLevel = securityLevel;
		}


		@Override
		public void mouseClicked(MouseEvent e) {
			Point2D.Float gamePos = view.getScreenToGameCoords(e.getX(), e.getY());
			EntityType type = selection.entityType;
			assert (type != null);
			Actor actor = new Actor(world, name, null, new UserScriptCollection(), (int) gamePos.x, (int) gamePos.y);
			world.addEntity(actor);
			view.setSelectedEntities(new Entity[] { actor });
			JEntityEditor.create(owner, actor, securityLevel, "Entity Editor");
		}

	}
}
