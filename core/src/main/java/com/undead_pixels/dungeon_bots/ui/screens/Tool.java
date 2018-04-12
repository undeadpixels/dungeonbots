package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Event;
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
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;

import com.undead_pixels.dungeon_bots.math.Cartesian;
import com.undead_pixels.dungeon_bots.nogdx.RenderingContext;
import com.undead_pixels.dungeon_bots.scene.EntityType;
import com.undead_pixels.dungeon_bots.scene.LoggingLevel;
import com.undead_pixels.dungeon_bots.scene.TileType;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.HasImage;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
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


	/**Pushes an undo associated with the given world onto the stack.*/
	public static void pushUndo(World world, Undoable<?> u) {
		if (!undoStacks.containsKey(world))
			undoStacks.put(world, new UndoStack());
		UndoStack stack = undoStacks.get(world);
		stack.push(u);
	}


	/**Clears the undo stack associated with the given world.*/
	public static void clearUndo(World world) {
		UndoStack stack = undoStacks.get(world);
		if (stack == null)
			return;
		stack.clear();
	}


	/**Un-does the most recent change with respect to the given world.  Returns 
	 * true or false based on whether undo occurred.*/
	public static boolean undo(World world) {
		UndoStack stack = undoStacks.get(world);
		if (stack == null)
			return false;
		Undoable<?> u = stack.popUndo();
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
		Undoable<?> r = stack.popRedo();
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


	/**This method is called when the tool is first activated as the selected tool.*/
	public void onActivated(){
		
	}
	/**This method is called when the tool is no longer the activated, selected tool.*/
	public void onDeactivated(){
		
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
	public void render(Graphics2D g, RenderingContext batch) {
	}


	// ===============================================
	// ========== Tool TOOL IMPLEMENTATIONS===========
	// ===============================================


	/** A view grabber allows user to right-click-and-drag to move a view around.*/
	public static class ViewControl extends Tool {

		private final ArrayList<ViewChangedListener> _ViewChangedListeners = new ArrayList<ViewChangedListener>();

		/**The maximum zoom-in.  The minimum is defined by the size of the world.*/
		public static float MAX_ZOOM = 4f;

		/**A scalar defining the highest zoom-out.  The higher the number the further the view can 
		 * zoom out.*/
		public static float MIN_ZOOM_SCALAR = 4f;

		/**How fast zooming occurs.*/
		private static final float ZOOM_INCREMENT = 1f / 64f;

		private final WorldView view;
		private ViewPreset[] presets = new ViewPreset[10];

		// Temp state variables to allow grab-and-move
		private Point2D.Float gameCenterOrigin = null;
		private Point screenOrigin = null;
		private Point screenCurrent = null;
		private Cursor originalCursor = null;


		public ViewControl(WorldView view) {
			super("ViewGrabber", null);
			this.view = view;
		}


		/**Returns the minimum zoom value for this control, based on the world's size.*/
		public float getMinZoom() {
			Point2D.Float size = view.getWorld().getSize();
			float min = Math.min(size.x, size.y);
			float max = Math.max(size.x, size.y);
			float aspect = Math.min(min, max);
			return MIN_ZOOM_SCALAR / aspect;
		}


		/**Returns the maximum zoom value for this control, which is a constant value.*/
		public float getMaxZoom() {
			return MAX_ZOOM;
		}


		/**Sets a preset as indicated.*/
		public void setPreset(int index, float zoom, Point2D.Float center) {
			if (presets[index] == null)
				presets[index] = new ViewPreset(zoom, center);
			else {
				presets[index].zoom = zoom;
				presets[index].center = center;
			}
		}


		/**Applies the preset at the given index to the view.*/
		public void applyPreset(int index) {
			if (presets[index] != null)
				presets[index].apply(this);
		}


		/**Adds a listener for changes in the view.*/
		public void addViewChangedListener(ViewChangedListener l) {
			_ViewChangedListeners.add(l);
		}


		/**Removes a listener for changes in the view.*/
		public void removeViewChangedListener(ViewChangedListener l) {
			_ViewChangedListeners.remove(l);
		}


		/**Fires the view changed event.*/
		public void fireViewChanged(float priorZoom, Point2D.Float priorPosition, float newZoom,
				Point2D.Float newPosition) {
			if (_ViewChangedListeners.size() == 0)
				return;
			ViewChangedEvent e = new ViewChangedEvent(this, priorZoom, priorPosition, newZoom, newPosition);
			if (e.getID() == 0)
				return;
			for (ViewChangedListener l : _ViewChangedListeners)
				l.viewChanged(e);
		}


		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			adjustZoom(e.getWheelRotation());
		}


		@Override
		public void mousePressed(MouseEvent e) {
			if (screenOrigin != null)
				return;
			screenOrigin = new Point(e.getX(), e.getY());
			gameCenterOrigin = view.getCamera().getPosition();
			originalCursor = view.getCursor();
			view.setCursor(new Cursor(Cursor.HAND_CURSOR));
			// e.consume();
		}


		@Override
		public void mouseReleased(MouseEvent e) {
			if (screenOrigin == null)
				return;
			screenOrigin = null;
			gameCenterOrigin = null;
			screenCurrent = null;
			view.setCursor(originalCursor != null ? originalCursor : new Cursor(Cursor.DEFAULT_CURSOR));
			originalCursor = null;
			// e.consume();
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			if (screenOrigin == null)
				return;
			if (screenCurrent != null && screenCurrent.x == e.getX() && screenCurrent.y == e.getY())
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


		/**Adjust the zoom an increment.*/
		public void adjustZoom(int delta) {
			float priorZoom = view.getCamera().getZoom();
			double dz = Math.signum(delta) * Math.exp(priorZoom) * ZOOM_INCREMENT;

			float newZoom = (float) (priorZoom - dz);

			// The limit on zoom-out depends on the size of the world.
			newZoom = Math.max(newZoom, getMinZoom());
			newZoom = Math.min(newZoom, MAX_ZOOM); // Limit on zoom in

			view.getCamera().setZoom(newZoom);
			this.fireViewChanged(priorZoom, view.getCamera().getPosition(), newZoom, view.getCamera().getPosition());
		}


		/**Sets the zoom as an exponential function between 0 and 1, 0 representing the min zoom and 1 
		 * representing the max zoom.*/
		public void setZoomAsPercentage(float percentage) {
			float priorZoom = view.getCamera().getZoom();
			view.getCamera().setZoomInRange(getMinZoom(), percentage, MAX_ZOOM);
			float newZoom = view.getCamera().getZoom();
			this.fireViewChanged(priorZoom, view.getCamera().getPosition(), newZoom, view.getCamera().getPosition());
		}


		/**Sets the view center.*/
		public void setCenter(Point2D.Float newCenter) {
			Point2D.Float oldPosition = view.getCamera().getPosition();
			view.getCamera().setPosition(newCenter.x, newCenter.y);
			this.fireViewChanged(view.getCamera().getZoom(), oldPosition, view.getCamera().getZoom(), newCenter);
		}


		void setZoomAndCenter(float zoom, Point2D.Float center) {
			float priorZoom = view.getCamera().getZoom();
			Point2D.Float priorCenter = view.getCamera().getPosition();
			view.getCamera().setZoom(zoom);
			view.getCamera().setPosition(center.x, center.y);
			this.fireViewChanged(priorZoom, priorCenter, zoom, center);
		}


		public void setMapView() {
			view.getCamera().zoomFor(view.getWorld().getSize());
		}


		/**Embodies view state for a quick return to that view.*/
		public static final class ViewPreset {

			public float zoom;
			public Point2D.Float center;


			public ViewPreset(float zoom, Point2D.Float center) {
				this.zoom = zoom;
				this.center = center;
			}


			public void apply(ViewControl control) {
				control.setZoomAndCenter(zoom, center);
			}
		}


		public abstract static class ViewChangedListener implements EventListener {

			public abstract void viewChanged(ViewChangedEvent e);
		}


		@SuppressWarnings("serial")
		public static final class ViewChangedEvent extends AWTEvent {

			public static final int POSITION_CHANGED = 1;
			public static final int ZOOM_CHANGED = 2;
			public static final int BOTH_CHANGED = 3;

			public final float priorZoom;
			public final float priorPositionX;
			public final float priorPositionY;

			public final float newZoom;
			public final float newPositionX;
			public final float newPositionY;


			ViewChangedEvent(ViewControl src, float priorZoom, Point2D.Float priorPosition, float newZoom,
					Point2D.Float newPosition) {
				super(src, 0);

				this.priorZoom = priorZoom;
				this.newZoom = newZoom;
				if (priorZoom != newZoom)
					id |= ZOOM_CHANGED;

				this.priorPositionX = priorPosition.x;
				this.priorPositionY = priorPosition.y;
				this.newPositionX = newPosition.x;
				this.newPositionY = newPosition.y;

				if (priorPositionX != newPositionX || priorPositionY != newPositionY)
					id |= POSITION_CHANGED;
			}
		}


	}


	public static class Eraser extends Tool {

		private final World world;
		private final WorldView view;


		public Eraser(WorldView view, World world) {
			super("Eraser", UIBuilder.getImage("icons/cross_48x48.png"));
			this.view = view;
			this.world = world;
		}

		public void onDeactivated(){
			view.setSelectedEntities(null);
			view.setSelectedTiles(null);
		}

		public Undoable<Entity> removeEntity(Entity e) {
			if (world.removeEntity(e)) {
				return new Undoable<Entity>(e, null) {

					@Override
					protected void undoValidated() {
						if (world.containsEntity(e))
							error();
						world.addEntity(e);
					}


					@Override
					protected void redoValidated() {
						if (!world.containsEntity(e))
							error();
						world.removeEntity(e);
					}
				};
			} else
				return null;
		}


		public Undoable<Tile> removeTile(Integer x, Integer y) {
			Tile oldTile = world.getTile(x, y);
			world.setTile(x, y, (Tile) null);
			return new Undoable<Tile>(oldTile, null) {

				@Override
				protected void undoValidated() {
					if (world.getTile(x, y) != null)
						error();
					world.setTile(x, y, oldTile);
				}


				@Override
				protected void redoValidated() {
					if (world.getTile(x, y) != oldTile)
						error();
					world.setTile(x, y, (Tile) null);
				}

			};
		}


		public void mouseMoved(MouseEvent e) {
			if (e.getButton() != MouseEvent.NOBUTTON)
				return;
			Point2D.Float pt = view.getScreenToGameCoords(e.getX(), e.getY());
			Entity en = world.getEntityUnderLocation(pt.x, pt.y);
			if (en != null) {
				view.setSelectedEntities(new Entity[] { en });
				e.consume();
				return;
			}
			Tile t = world.getTile((int) pt.x, (int) pt.y);
			if (t != null) {
				view.setSelectedTiles(new Tile[] { t });
				e.consume();
				return;
			}
		}


		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				Point2D.Float pt = view.getScreenToGameCoords(e.getX(), e.getY());
				Entity en = world.getEntityUnderLocation(pt.x, pt.y);
				if (en != null) {
					Undoable<?> u = removeEntity(en);
					if (u != null)
						pushUndo(world, u);
					view.setSelectedEntities(null);
					e.consume();
					return;
				}
				Tile t = world.getTile(pt.x, pt.y);
				if (t != null) {
					Undoable<?> u = removeTile((int) pt.x, (int) pt.y);
					if (u != null)
						pushUndo(world, u);
					view.setSelectedTiles(null);
					e.consume();
					return;
				}

			}
		}
	}


	public static class Selector extends Tool {

		private final WorldView view;
		private final Window owner;
		private World world;
		private Point cornerA = null;
		private Point cornerB = null;
		private final SecurityLevel securityLevel;
		private boolean selectsEntities = true;
		private boolean selectsTiles = true;


		public Selector(WorldView view, Window owner, SecurityLevel securityLevel) {
			super("Selector", UIBuilder.getImage("icons/crosshair_48x48.png"));
			this.view = view;
			this.owner = owner;
			this.world = view.getWorld();
			this.securityLevel = securityLevel;
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
		public void keyTyped(KeyEvent e) {
			if (view.getSelectedEntities() == null || view.getSelectedEntities().length == 0)
				return;
			System.out.println(e.getKeyChar());
			e.consume();
		}


		@Override
		public void mousePressed(MouseEvent e) {


			if (e.getButton() == MouseEvent.BUTTON1) {
				view.requestFocusInWindow();

				// If there is a cornerA, it means selection has started
				// already.
				if (cornerA != null)
					return;

				assert (view.getAdornmentTool() == null); // sanity check.

				// Set the selection corner in screen coordinates.
				cornerB = cornerA = new Point(e.getX(), e.getY());

				// The view should start rendering the lasso.
				view.setAdornmentTool(this);
				e.consume();
			}
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			if (cornerA == null)
				return;
			cornerB = new Point(e.getX(), e.getY());
			e.consume();
		}


		@Override
		public void mouseReleased(MouseEvent e) {

			if (e.getButton() != MouseEvent.BUTTON1)
				return;
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
			List<Entity> se = world.getEntitiesUnderLocation(rect);
			se.removeIf((ent) -> ent.getPermission(Entity.PERMISSION_SELECTION).level > securityLevel.level);
			List<Tile> st = world.getTilesUnderLocation(rect);

			System.out.println(se);

			// If only one tile is selected, and one entity is selected, and
			// it is the entity that would be selected by this lasso, then
			// this is a double-click. Bring up the editor for the selected
			// entity.
			if (st.size() == 1 && se.size() == 1 && view.isSelectedEntity(se.get(0))) {
				view.setSelectedEntities(new Entity[] { se.get(0) });
				JEntityEditor jee = null;
				if (se.get(0).getPermission(Entity.PERMISSION_ENTITY_EDITOR).level <= securityLevel.level) {
					jee = JEntityEditor.createDialog(owner, se.get(0), se.get(0).getName(), securityLevel, view);

				}
				if (jee == null) {
					world.message((se.get(0) instanceof HasImage) ? (HasImage) se.get(0) : null,
							"This entity cannot be edited.", LoggingLevel.GENERAL);
					return;
				} else {
					jee.setVisible(true);
					view.setSelectedTiles(null);
				}

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
			view.setAdornmentTool(null);

			// Show that selection is complete, and re-selection hasn't
			// started.
			cornerB = cornerA = null;
			e.consume();

		}


		@Override
		public void render(Graphics2D g, RenderingContext batch) {
			// Don't use the batch, because it's in terms of actual screen
			// coordinates
			g.setStroke(new BasicStroke(2));
			g.setColor(Color.red);
			Rectangle rect = Cartesian.makeRectangle(cornerA, cornerB);
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		}


		/**
		 * @param world
		 */
		public void setWorld(World world) {
			this.world = world;
		}

	}


	public static class TilePen extends Tool {

		private final WorldView view;
		private final World world;
		private HashMap<Point, Tile> oldTiles = null;
		private HashMap<Point, Tile> newTiles = null;

		public final SelectionModel selection;
		private TileType drawingTileType;


		public TilePen(WorldView view, SelectionModel selection) {
			super("Tile Pen", UIBuilder.getImage("icons/draw_48x48.png"));
			this.view = view;
			this.world = view.getWorld();
			this.selection = selection;
		}


		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isConsumed() || oldTiles != null || selection.tileType == null)
				return;
			drawingTileType = selection.tileType;
			oldTiles = new HashMap<Point, Tile>();
			newTiles = new HashMap<Point, Tile>();
			drawTile(e.getX(), e.getY(), drawingTileType);
			e.consume();
		}


		@Override
		public void mouseExited(MouseEvent e) {
			// TODO: pushing 'ESC' should also cancel the draw.
			if (e.getSource() == view && oldTiles != null) {
				cancelDraw();
				e.consume();
			}
		}


		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isConsumed() || oldTiles == null)
				return;
			if (newTiles.size() == 0)
				return;
			Undoable<HashMap<Point, Tile>> u = new Undoable<HashMap<Point, Tile>>(oldTiles, newTiles) {

				@Override
				protected void undoValidated() {
					for (Point p : before.keySet()) {
						Tile existingTile = world.getTile(p.x, p.y);
						if (existingTile == null) {
							if (after.get(p) != null)
								error();
						} else if (!existingTile.equals(after.get(p)))
							error();
						Tile t = before.get(p);
						world.setTile(p.x, p.y, t);
					}
				}


				@Override
				protected void redoValidated() {
					for (Point p : after.keySet()) {
						Tile existingTile = world.getTile(p.x, p.y);
						if (existingTile == null) {
							if (before.get(p) != null)
								error();
						} else if (!existingTile.equals(before.get(p)))
							error();
						Tile t = after.get(p);
						world.setTile(p.x, p.y, t);
					}
				}

			};

			pushUndo(world, u);

			oldTiles = null;
			newTiles = null;
			drawingTileType = null;
			e.consume();

		}


		@Override
		public void mouseDragged(MouseEvent e) {
			if (e.isConsumed() || oldTiles == null || selection.tileType == null)
				return;
			assert drawingTileType != null;
			drawTile(e.getX(), e.getY(), drawingTileType);
			e.consume();
		}


		private void cancelDraw() {

			// Restore all the old tile types.
			for (Point p : oldTiles.keySet()) {
				world.setTile(p.x, p.y, oldTiles.get(p));
			}

			// Null the tile type caches to signal that drawing is done.
			oldTiles = null;
			newTiles = null;
			drawingTileType = null;
		}


		/**Returns the new tile created.*/
		private Tile drawTile(int screenX, int screenY, TileType tileType) {

			// Find the position and existing tile in game space.
			Point2D.Float gamePos = view.getScreenToGameCoords(screenX, screenY);
			int x = (int) gamePos.x, y = (int) gamePos.y;
			if (!world.isInBounds(x, y))
				return null;
			Tile existingTile = world.getTile(x, y);

			// If the tile type isn't changing, shouldn't change the change
			// buffer.
			if (existingTile != null && existingTile.getType() != null) {
				if (existingTile.getType().equals(tileType))
					return null;
				if (existingTile.getType().getName().equals(tileType.getName()))
					return null;
			}

			// Cache the old and new tile types, then draw to the world;
			Tile newTile = new Tile(world, tileType, x, y);
			oldTiles.put(new Point(x, y), existingTile);
			world.setTile(x, y, newTile);
			newTiles.put(new Point(x, y), newTile);
			return newTile;
		}

	}


	public static class EntityPlacer extends Tool {

		private final WorldView view;
		private final World world;
		private final SelectionModel selection;
		private final Window owner;
		private final SecurityLevel securityLevel;


		public EntityPlacer(WorldView view, SelectionModel selection, Window owner, SecurityLevel securityLevel) {
			super("Entity Placer", UIBuilder.getImage("icons/diamond_48x48.png"));
			this.view = view;
			this.world = view.getWorld();
			this.selection = selection;
			this.owner = owner;
			this.securityLevel = securityLevel;
		}


		@Override
		public void mouseClicked(MouseEvent e) {

			// What is the world location we're dealing with?
			Point2D.Float gamePos = view.getScreenToGameCoords(e.getX(), e.getY());

			// What if an entity already exists at this spot?
			Entity alreadyThere = world.getEntityUnderLocation(gamePos.x, gamePos.y);
			if (alreadyThere != null) {
				if (alreadyThere.getPermission(Entity.PERMISSION_SELECTION).level <= securityLevel.level) {
					view.setSelectedEntities(new Entity[] { alreadyThere });

					JEntityEditor jee = null;
					if (alreadyThere.getPermission(Entity.PERMISSION_ENTITY_EDITOR).level <= securityLevel.level) {
						jee = JEntityEditor.createDialog(owner, alreadyThere, alreadyThere.getName(), securityLevel,
								view);

					}
					if (jee == null) {
						world.message((alreadyThere instanceof HasImage) ? (HasImage) alreadyThere : null,
								"This entity cannot be edited.", LoggingLevel.GENERAL);
						return;
					} else {
						jee.setVisible(true);
						view.setSelectedTiles(null);
					}

				}
				return;
			} else {
				int x = (int) gamePos.x, y = (int) gamePos.y;
				if (!world.isInBounds(x, y))
					return;
				Tile tile = world.getTile(x, y);
				if (tile != null && (tile.isOccupied() || tile.isSolid()))
					return;

				EntityType type = selection.entityType;
				if (type == null)
					return;
				Entity newEntity = type.get(x, y);
				world.addEntity(newEntity);

				Undoable<Entity> placeUndoable = new Undoable<Entity>(null, newEntity) {

					@Override
					protected void undoValidated() {
						if (!world.containsEntity(newEntity))
							error();
						view.setSelectedEntities(null);
						world.removeEntity(newEntity);
					}


					@Override
					protected void redoValidated() {
						if (world.containsEntity(newEntity))
							error();
						view.setSelectedEntities(new Entity[] { newEntity });
						world.addEntity(newEntity);
					}
				};
				pushUndo(world, placeUndoable);


				JEntityEditor jee = JEntityEditor.createDialog(owner, newEntity, newEntity.getName(), securityLevel,
						view);
				jee.setVisible(true);
				view.setSelectedEntities(new Entity[] { newEntity });
			}
		}
	}


}
