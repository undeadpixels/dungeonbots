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

/** A tool is a class which determines how input is handled. */
public abstract class Tool implements MouseInputListener, KeyListener, MouseWheelListener {

	private final HashMap<Object, Stack<Undoable>> undoables = new HashMap<Object, Stack<Undoable>>();
	private final HashMap<Object, Stack<Undoable>> redoables = new HashMap<Object, Stack<Undoable>>();
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
	public static class EntityEditorTool extends Tool {

		World world;


		public EntityEditorTool(World world) {
			super("EntityEditor", null);
		}


	}


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
			float newZoom = (view.getCamera().getZoom() * 100f) - (3 * e.getWheelRotation());
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


		public Selector(WorldView view, Window owner, SecurityLevel securityLevel, ViewControl viewControl) {
			super("Selector", UIBuilder.getImage("selector.gif"));
			this.view = view;
			this.owner = owner;
			this.world = view.getWorld();
			this.securityLevel = securityLevel;
			this.viewControl = viewControl;
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
			} else if (e.getButton() == MouseEvent.BUTTON3) {
				this.viewControl.mousePressed(e);
			}
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			// If drawing isn't happening, just return.
			if (cornerA == null) {
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
			} else if (e.getButton() == MouseEvent.BUTTON3) {
				viewControl.mouseReleased(e);
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

		public final SelectionModel selection;


		public TilePen(WorldView view, SelectionModel selection, ViewControl viewControl) {
			super("Tile Pen", null);
			this.view = view;
			this.world = view.getWorld();
			this.selection = selection;
			this.viewControl = viewControl;
		}


		@Override
		public void mouseClicked(MouseEvent e) {
			if (selection.tileType != null) {
				drawTile(e.getX(), e.getY(), selection.tileType);
				e.consume();
			}
		}


		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isConsumed())
				return;
			else if (e.getButton() == MouseEvent.BUTTON3)
				viewControl.mousePressed(e);
		}


		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isConsumed())
				return;
			else if (e.getButton() == MouseEvent.BUTTON3)
				viewControl.mouseReleased(e);
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3)
				viewControl.mouseDragged(e);
			else if (selection.tileType != null) {
				drawTile(e.getX(), e.getY(), selection.tileType);
				e.consume();
			}
		}


		public void drawTile(int screenX, int screenY, TileType tileType) {
			Point2D.Float gamePos = view.getScreenToGameCoords(screenX, screenY);
			Tile existingTile = world.getTile(gamePos);
			if (existingTile == null)
				throw new RuntimeException("Have not implemented drawing a tile where one does not already exist.");
			assert (tileType != null);
			Point2D.Float existingPoint = existingTile.getPosition();
			world.setTile((int) existingPoint.getX(), (int) existingPoint.getY(), tileType);

			Undoable u = new Undoable(existingTile.getType(), tileType) {

				@Override
				public void Undo() {
					if (!existingTile.getType().equals(after))
						error();
					world.setTile((int) existingPoint.getX(), (int) existingPoint.getY(), (TileType) before);
				}


				@Override
				public void Redo() {
					if (!existingTile.getType().equals(before))
						error();
					world.setTile((int) existingPoint.getX(), (int) existingPoint.getY(), (TileType) after);
				}
			};
			addUndo(world, u);
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

			Undoable u = new Undoable(null, actor) {

				@Override
				public void Undo() {
					if (!world.containsEntity((Entity) after))
						error();
					world.removeEntity(actor);
				}


				@Override
				public void Redo() {
					if (world.containsEntity((Entity) after))
						error();
					world.addEntity(actor);
				}
			};
			addUndo(world, u);

			view.setSelectedEntities(new Entity[] { actor });
			JEntityEditor.create(owner, actor, securityLevel, "Entity Editor");
		}

	}


	// ===============================================
	// ========== Tool UNDO/REDO HANDLING ============
	// ===============================================

	/**Implement this to specify an undo process.*/
	protected abstract class Undoable {

		protected final Object before;
		protected final Object after;


		/**Stores the given 'before' and 'after' value within the Undoable object.*/
		public Undoable(Object before, Object after) {
			this.before = before;
			this.after = after;
		}


		/**Creates a stateless Undoable, or an Undoable that relies on values supplied by a closure.*/
		public Undoable() {
			this.before = null;
			this.after = null;
		}


		/**Throws an exception indicating possible undo stack corruption.*/
		protected void error() {
			throw new RuntimeException("Possible undo stack corruption.");
		}


		public abstract void Undo();


		public abstract void Redo();
	}


	/**Adds the undo item to the undo stack related to the given context, and clears the associated redo stack.*/
	public void addUndo(Object context, Undoable undoable) {

		// Add the undoable to the stack.
		Stack<Undoable> stack;
		if (undoables.containsKey(context))
			stack = undoables.get(context);
		else
			undoables.put(context, stack = new Stack<Undoable>());
		stack.push(undoable);

		// Putting something on the undoable stack means the redoable stack is
		// now cleared.
		if (redoables.containsKey(context))
			redoables.get(context).clear();
		else
			redoables.put(context, new Stack<Undoable>());
	}


	/**Causes the last action associated with the given context to undo.*/
	public void undo(Object context) {
		// Add the undoable to the stack.
		Stack<Undoable> stack;
		if (undoables.containsKey(context))
			stack = undoables.get(context);
		else
			throw new RuntimeException("No undo stack is associated with the context: " + context.toString());

		Undoable u = stack.pop();
		u.Undo();
		redoables.get(context).push(u);
	}


	/**Causes the last redone action associated with the given context to redo. */
	public void redo(Object context) {
		Stack<Undoable> stack;
		if (redoables.containsKey(context))
			stack = redoables.get(context);
		else
			throw new RuntimeException("No redo stack is associated with the context: " + context.toString());

		Undoable r = stack.pop();
		r.Redo();
		undoables.get(context).push(r);
	}

}
