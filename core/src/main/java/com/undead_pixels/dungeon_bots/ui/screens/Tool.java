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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

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
import com.undead_pixels.dungeon_bots.ui.undo.UndoStack;
import com.undead_pixels.dungeon_bots.ui.undo.Undoable;

/** A tool is a class which determines how input is handled. */
public abstract class Tool implements MouseInputListener, KeyListener, MouseWheelListener {


	public final String name;
	public final Image image;


	public Tool(String name, Image image) {
		this.name = name;
		this.image = image;
	}


	/** The editor state */
	public static class SelectionModel {

		public TileType tileType = null;
		public EntityType entityType = null;

		/** The current Tool. */
		public Tool tool = null;
	}


	// ===============================================
	// ========== Tool UNDO STUFF ===================
	// ===============================================

	private static final HashMap<World, UndoStack> undoStacks = new HashMap<World, UndoStack>();


	public static void pushUndo(World world, Undoable u) {
		if (!undoStacks.containsKey(world))
			undoStacks.put(world, new UndoStack());
		UndoStack stack = undoStacks.get(world);
		stack.push(u);
	}


	/**Un-does the most recent change with respect to the given world.  Returns 
	 * true or false based on whether undo occurred.*/
	public static boolean undo(World world) {
		UndoStack stack = undoStacks.get(world);
		if (stack == null)
			return false;
		Undoable<?> u = stack.nextUndo();
		if (u == null)
			return false;
		try {
			u.undo();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}


	/**Re-does the most recent change with respect to the given world.  Returns true 
	 * or false based on whether redo occurred.*/
	public static boolean redo(World world) {
		UndoStack stack = undoStacks.get(world);
		if (stack == null)
			return false;
		Undoable<?> r = stack.nextRedo();
		if (r == null)
			return false;
		try {
			r.redo();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}


	// ===============================================
	// ========== Tool UI HANDLING ===================
	// ===============================================

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	}


	/**A click is a full press-and-release of a mouse button.*/
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


	// ===============================================
	// ========== Tool GRAPHICS ======================
	// ===============================================
	public void render(Graphics2D g) {
	}


	// ===============================================
	// ========== Tool TOOL IMPLEMENTATIONS===========
	// ===============================================


	/** A view grabber allows user to right-click-and-drag to move a view around.*/
	public static class ViewControl extends Tool {

		private final WorldView view;

		private Point2D.Float gameCenterOrigin = null;
		private Point screenOrigin = null;
		private Point screenCurrent = null;


		public ViewControl(WorldView view) {
			super("ViewGrabber", null);
			this.view = view;
		}


		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			adjustZoom(e.getWheelRotation());
		}


		public void adjustZoom(int delta) {
			float newZoom = (view.getCamera().getZoom() * 100f) - (3 * delta);
			newZoom /= 100f;
			setZoom(newZoom);
		}


		@Override
		public void mousePressed(MouseEvent e) {
			if (screenOrigin != null)
				return;
			screenOrigin = new Point(e.getX(), e.getY());
			gameCenterOrigin = view.getCamera().getPosition();
			e.consume();
		}


		@Override
		public void mouseReleased(MouseEvent e) {
			if (screenOrigin == null)
				return;
			screenOrigin = null;
			gameCenterOrigin = null;
			screenCurrent = null;
			e.consume();
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			if (screenOrigin == null)
				return;

			screenCurrent = new Point(e.getX(), e.getY());
			Point2D.Float gameWorldA = view.getScreenToGameCoords(screenOrigin.x, screenOrigin.y);
			Point2D.Float gameWorldB = view.getScreenToGameCoords(screenCurrent.x, screenCurrent.y);
			float movedX = -(gameWorldB.x - gameWorldA.x);
			float movedY = -(gameWorldB.y - gameWorldA.y);
			Point2D.Float newGameCenter = new Point2D.Float(gameCenterOrigin.x + movedX, gameCenterOrigin.y + movedY);
			setCenter(newGameCenter);

			e.consume();

		}


		/**Sets the zoom where 'f' is the distance between 0 and 1, 0 representing the min zoom and 1 representing the max zoom.*/
		public void setZoomAsPercentage(float newZoom) {
			view.getCamera().setZoomOnMinMaxRange(newZoom);
		}


		/**Sets the zoom to the absolute value given.*/
		public void setZoom(float newZoom) {
			view.getCamera().setZoom(newZoom);
		}


		public void setCenter(Point2D.Float newCenter) {
			view.getCamera().setPosition(newCenter.x, newCenter.y);
		}

	}


	public static class Selector extends Tool {

		private final ViewControl viewControl;
		private final WorldView view;
		private final Window owner;
		private final World world;
		private Point cornerA = null;
		private Point cornerB = null;
		private final SecurityLevel securityLevel;
		private boolean selectsEntities = true;
		private boolean selectsTiles = true;


		public Selector(WorldView view, Window owner, SecurityLevel securityLevel, ViewControl viewControl) {
			super("Selector", UIBuilder.getImage("selector.gif"));
			this.view = view;
			this.owner = owner;
			this.world = view.getWorld();
			this.securityLevel = securityLevel;
			this.viewControl = viewControl;
		}


		/**Sets whether this Selector can select entities.*/
		public Selector setSelectsEntities(boolean value) {
			selectsEntities = value;
			view.setSelectedEntities(null);
			return this;
		}


		/**Returns whether this tool can select entities.  Default value is true.*/
		public boolean getSelectsEntities() {
			return selectsEntities;
		}


		/**Sets whether this Selector can select tiles.*/
		public Selector setSelectsTiles(boolean value) {
			selectsTiles = value;
			view.setSelectedTiles(null);
			return this;
		}


		/**Returns whether this tool can select tiles.  Default value is true.*/
		public boolean getSelectsTiles() {
			return selectsTiles;
		}


		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			this.viewControl.mouseWheelMoved(e);
		}


		@Override
		public void mousePressed(MouseEvent e) {


			if (e.getButton() == MouseEvent.BUTTON1) {
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
			} else if (e.getButton() == MouseEvent.BUTTON3 && this.viewControl != null) {
				this.viewControl.mousePressed(e);
				return;
			}
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			// If drawing isn't happening, just return.
			if (cornerA == null && this.viewControl != null) {
				this.viewControl.mouseDragged(e);
				return;
			}

			assert (view.getRenderingTool() == this); // Sanity check.

			cornerB = new Point(e.getX(), e.getY());

			e.consume();
		}


		@Override
		public void mouseReleased(MouseEvent e) {

			if (e.getButton() == MouseEvent.BUTTON1) {
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

				// If only one tile is selected, and one entity is selected, and
				// it is the entity that would be selected by this lasso, then
				// this is a double-click. Bring up the editor for the selected
				// entity.
				if (st.size() == 1 && se.size() == 1 && view.isSelectedEntity(se.get(0))) {
					view.setSelectedEntities(new Entity[] { se.get(0) });
					JEntityEditor.create(owner, se.get(0), securityLevel, "Entity Editor", new Undoable.Listener() {

						@Override
						public void pushUndoable(Undoable<?> u) {
							pushUndo(world, u);
						}
					});
					view.setSelectedTiles(null);
				}
				// If some number of entities are lassoed, select them (if
				// allowed).
				else if (se.size() > 0 && selectsEntities) {
					view.setSelectedEntities(se.toArray(new Entity[se.size()]));
					view.setSelectedTiles(null);
				}
				// If an unselected tile is lassoed, but a selection exists,
				// this counts to clear the selections.
				else if (st.size() == 1 && !view.isSelectedTile(st.get(0))) {
					view.setSelectedTiles(null);
					view.setSelectedEntities(null);
				}
				// If one or more tiles is lassoed, and tile select is allowed,
				// select them.
				else if (st.size() > 0 && selectsTiles) {

					view.setSelectedTiles(st.toArray(new Tile[st.size()]));
					view.setSelectedEntities(null);
				}
				// In all other cases, neither tile nor entity may be selected.
				// Clear the selections.
				else {
					view.setSelectedTiles(null);
					view.setSelectedEntities(null);
				}

				// The view should no longer render the lasso.
				view.setRenderingTool(null);

				// Show that selection is complete, and re-selection hasn't
				// started.
				cornerB = cornerA = null;
				e.consume();
			} else if (e.getButton() == MouseEvent.BUTTON3 && this.viewControl != null) {
				viewControl.mouseReleased(e);
				return;
			}

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

		private final ViewControl viewControl;
		private final WorldView view;
		private final World world;
		private HashMap<Point, TileType> oldTileTypes = null;
		private HashMap<Point, TileType> newTileTypes = null;

		public final SelectionModel selection;
		private TileType drawingTileType;


		public TilePen(WorldView view, SelectionModel selection, ViewControl viewControl) {
			super("Tile Pen", null);
			this.view = view;
			this.world = view.getWorld();
			this.selection = selection;
			this.viewControl = viewControl;
		}


		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isConsumed() || oldTileTypes != null)
				return;
			else if (e.getButton() == MouseEvent.BUTTON3)
				viewControl.mousePressed(e);
			else if (selection.tileType == null)
				return;
			else {
				drawingTileType = selection.tileType;
				oldTileTypes = new HashMap<Point, TileType>();
				newTileTypes = new HashMap<Point, TileType>();
				drawTile(e.getX(), e.getY(), drawingTileType);
				e.consume();
			}
		}


		@Override
		public void mouseExited(MouseEvent e) {
			// TODO: pushing 'ESC' should also cancel the draw.
			if (e.getSource() == view && oldTileTypes != null) {
				cancelDraw();
				e.consume();
			}
		}


		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isConsumed() || oldTileTypes == null)
				return;
			else if (e.getButton() == MouseEvent.BUTTON3)
				viewControl.mouseReleased(e);
			else {
				Undoable<HashMap<Point, TileType>> u = new Undoable<HashMap<Point, TileType>>(oldTileTypes,
						newTileTypes) {

					@Override
					protected boolean validateUndo() {
						for (Point p : after.keySet()) {
							Tile existingTile = world.getTile(p.x, p.y);
							if (existingTile == null)
								return false;
							TileType existingTileType = existingTile.getType();
							if (!after.get(p).equals(existingTileType))
								return false;
						}
						return true;
					}


					@Override
					protected boolean validateRedo() {
						for (Point p : before.keySet()) {
							Tile existingTile = world.getTile(p.x, p.y);
							if (existingTile == null)
								return false;
							TileType existingTileType = existingTile.getType();
							if (!before.get(p).equals(existingTileType))
								return false;
						}
						return true;
					}


					@Override
					protected void undoValidated() {
						for (Point p : before.keySet()) {
							TileType t = before.get(p);
							world.setTile(p.x, p.y, t);
						}
					}


					@Override
					protected void redoValidated() {
						for (Point p : after.keySet()) {
							TileType t = after.get(p);
							world.setTile(p.x, p.y, t);
						}
					}

				};

				pushUndo(world, u);

				oldTileTypes = null;
				newTileTypes = null;
				drawingTileType = null;
				e.consume();
			}


		}


		@Override
		public void mouseDragged(MouseEvent e) {
			if (e.isConsumed() || oldTileTypes == null)
				return;
			else if (e.getButton() == MouseEvent.BUTTON3)
				viewControl.mouseDragged(e);
			else {
				assert drawingTileType != null;
				drawTile(e.getX(), e.getY(), drawingTileType);
				e.consume();
			}
		}


		private void cancelDraw() {

			// Restore all the old tile types.
			for (Point p : oldTileTypes.keySet()) {
				world.setTile(p.x, p.y, oldTileTypes.get(p));
			}

			// Null the tile type caches to signal that drawing is done.
			oldTileTypes = null;
			newTileTypes = null;
			drawingTileType = null;
		}


		private void drawTile(int screenX, int screenY, TileType tileType) {

			// Find the position in game space.
			Point2D.Float gamePos = view.getScreenToGameCoords(screenX, screenY);
			Tile existingTile = world.getTile(gamePos);

			// Sanity checks.
			assert existingTile != null;
			assert (tileType != null);

			// Find the existing location and tile type.
			TileType oldTileType = existingTile.getType();
			if (oldTileType == tileType)
				return;
			Point2D.Float existingPoint = existingTile.getPosition();
			int x = (int) existingPoint.getX(), y = (int) existingPoint.getY();

			// Cache the old and new tile types, then draw to the world;
			oldTileTypes.put(new Point(x, y), oldTileType);
			newTileTypes.put(new Point(x, y), tileType);
			world.setTile(x, y, tileType);
		}

	}


	public static class EntityPlacer extends Tool {

		private final WorldView view;
		private final World world;
		private final SelectionModel selection;
		private final Window owner;
		private final SecurityLevel securityLevel;
		private final ViewControl viewControl;


		public EntityPlacer(WorldView view, SelectionModel selection, Window owner, SecurityLevel securityLevel,
				ViewControl viewControl) {
			super("EntityPlacer", UIBuilder.getImage("entity_placer.gif"));
			this.view = view;
			this.world = view.getWorld();
			this.selection = selection;
			this.owner = owner;
			this.securityLevel = securityLevel;
			this.viewControl = viewControl;
		}


		@Override
		public void mouseClicked(MouseEvent e) {
			Point2D.Float gamePos = view.getScreenToGameCoords(e.getX(), e.getY());
			EntityType type = selection.entityType;
			assert (type != null);
			Actor actor = new Actor(world, name, null, new UserScriptCollection(), (int) gamePos.x, (int) gamePos.y);
			world.addEntity(actor);

			Undoable<Entity> u = new Undoable<Entity>(null, actor) {

				@Override
				protected boolean validateUndo() {
					return world.containsEntity(actor);
				}


				@Override
				protected boolean validateRedo() {
					return !world.containsEntity(actor);
				}


				@Override
				protected void undoValidated() {
					world.removeEntity(actor);

				}


				@Override
				protected void redoValidated() {
					world.addEntity(actor);
				}


			};

			view.setSelectedEntities(new Entity[] { actor });
			JEntityEditor.create(owner, actor, securityLevel, "Entity Editor", new Undoable.Listener() {

				@Override
				public void pushUndoable(Undoable<?> u) {
					pushUndo(world, u);
				}
			});
		}

	}


}
