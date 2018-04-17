package com.undead_pixels.dungeon_bots.scene;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.*;

import com.undead_pixels.dungeon_bots.nogdx.RenderingContext;
import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.entities.*;
import com.undead_pixels.dungeon_bots.scene.entities.Bot;
import com.undead_pixels.dungeon_bots.scene.entities.ChildEntity;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.FloatingText;
import com.undead_pixels.dungeon_bots.scene.entities.Goal;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.scene.entities.actions.ActionGrouping;
import com.undead_pixels.dungeon_bots.scene.entities.actions.ActionQueue;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Ghost;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.HasInventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.script.events.UpdateCoalescer;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaSandbox;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.script.proxy.LuaReflection;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;
import com.undead_pixels.dungeon_bots.ui.screens.GameplayScreen;
import com.undead_pixels.dungeon_bots.ui.screens.GameplayScreen.Poptart;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.*;

/**
 * The World of the game. Controls pretty much everything in the entire level,
 * but could get reset/rebuilt if the level is restarted.
 */
@Doc("The current map can be interfaced with via the 'world'")
public class World implements GetLuaFacade, GetLuaSandbox, GetState, Serializable, HasImage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The script that defines this world
	 */
	private final UserScriptCollection levelScripts = new UserScriptCollection();

	/**
	 * The scripts the players entities all own
	 */
	private final UserScriptCollection botScripts = new UserScriptCollection();

	/**
	 * The LuaBindings to the World Lazy initialized
	 */
	private transient LuaValue luaValue;

	/**
	 * The sandbox that the levelScript runs inside of.
	 *
	 * Lazy-loaded
	 */
	private transient LuaSandbox mapSandbox;

	private transient ConcurrentLinkedDeque<Entity> toRemove = new ConcurrentLinkedDeque<>();


	/**
	 * The of this world (may be user-readable)
	 */
	private String name = "world";

	/**
	 * The whitelist governing what functions are accessible by whom
	 */
	private Whitelist sharedWhitelist = new Whitelist();

	/**
	 * If the gameplay view should not automagically begin play when the world is loaded
	 * (Should wait until the user hits "play" to run init)
	 */
	private boolean autoPlay = false;

	/**
	 * If this world has been won
	 */
	private transient boolean isWon = false;

	/**
	 * If the init scripts were already run
	 */
	private transient boolean didInit = false;

	/**
	 * A background image for this world Not currently used.
	 */
	private TextureRegion backgroundImage;

	/**
	 * An array of tiles, in the bottom layer of this world. This array is
	 * generated from the tileTypes array.
	 */
	private Tile[][] tiles;

	/**
	 * The collection of available TileType's
	 */
	private TileTypes tileTypesCollection;

	/**
	 * Indication of if the tile array needs to be refreshed
	 */
	private transient boolean tilesAreStale = true;

	/**
	 * Collection of all entities in this world
	 */
	private ArrayList<Entity> entities = new ArrayList<>();

	/**
	 * The number of times the "reset" button was pressed
	 */
	@State
	private int timesReset = 0;

	/**
	 * An id counter, used to hand out id's to entities.  The id is needed when a game is rewinded, so entities with 
	 * a particular ID in the old world can have their scripts copied to entities with a matching ID in the newly 
	 * deserialized world. 
	 */
	private int idCounter = 0;

	/**
	 * The playstyle of this world
	 *
	 * TODO - add a knob for this in the level editor
	 */
	private ActionGrouping playstyle = new ActionGrouping.RTSGrouping();


	/**
	 * Function interface for Logging Events
	 */
	public interface MessageListener {

		/**
		 * Listener that will be invoked upon certain types of events.
		 * @param src The Entity or Object that is the source of the Message
		 * @param message The message to log
		 * @param level The LoggineLevel of the message
		 */
		void message(HasImage src, String message, LoggingLevel level);
	}


	private transient MessageListener messageListener;
	private transient Consumer<Poptart> poptartListener;
	// =============================================
	// ====== Events and stuff
	// =============================================


	private static interface EventType {
	}


	public static enum EntityEventType implements EventType {
		ENTITY_ADDED, ENTITY_REMOVED, ENTITY_MOVED
	}


	public static enum WorldEventType implements EventType {
		WIN
	}


	public static enum StringEventType implements EventType {
		KEY_PRESSED, KEY_RELEASED
	}


	private transient WorldEvent<World> winEvent;
	private transient WorldEvent<Entity> entityAddedEvent;
	private transient WorldEvent<Entity> entityRemovedEvent;
	private transient WorldEvent<Entity> entityMovedEvent;
	private transient WorldEvent<String> keyPressedEvent;
	private transient WorldEvent<String> keyReleasedEvent;


	private WorldEvent<Entity> getEntityEvent(EntityEventType t) {
		switch (t) {
		case ENTITY_ADDED:
			if (entityAddedEvent == null)
				entityAddedEvent = new WorldEvent<>();
			return entityAddedEvent;
		case ENTITY_REMOVED:
			if (entityRemovedEvent == null)
				entityRemovedEvent = new WorldEvent<>();
			return entityRemovedEvent;
		case ENTITY_MOVED:
			if (entityMovedEvent == null)
				entityMovedEvent = new WorldEvent<>();
			return entityMovedEvent;
		}
		return null;
	}


	private WorldEvent<World> getWorldEvent(WorldEventType t) {
		switch (t) {
		case WIN:
			if (winEvent == null)
				winEvent = new WorldEvent<>();
			return winEvent;
		}
		return null;
	}


	private synchronized WorldEvent<String> getStringEvent(StringEventType t) {
		switch (t) {
		case KEY_PRESSED:
			if (keyPressedEvent == null)
				keyPressedEvent = new WorldEvent<>();
			return keyPressedEvent;
		case KEY_RELEASED:
			if (keyReleasedEvent == null)
				keyReleasedEvent = new WorldEvent<>();
			return keyReleasedEvent;
		}
		return null;
	}


	public synchronized void listenTo(EntityEventType t, Object owner, Consumer<Entity> func) {
		WorldEvent<Entity> ev = getEntityEvent(t);
		ev.addListener(owner, func);
	}


	public synchronized void fire(EntityEventType t, Entity e) {
		WorldEvent<Entity> ev = getEntityEvent(t);
		ev.fire(e);
	}


	public synchronized void listenTo(WorldEventType t, Object owner, Consumer<World> func) {
		WorldEvent<World> ev = getWorldEvent(t);
		ev.addListener(owner, func);
	}


	public synchronized void fire(WorldEventType t, World w) {
		WorldEvent<World> ev = getWorldEvent(t);
		ev.fire(w);
	}


	public synchronized void listenTo(StringEventType t, Object owner, Consumer<String> func) {
		WorldEvent<String> ev = getStringEvent(t);
		ev.addListener(owner, func);
	}


	public synchronized void fire(StringEventType t, String s) {
		WorldEvent<String> ev = getStringEvent(t);
		ev.fire(s);
	}


	public synchronized void removeEventListenersByOwner(Object owner) {
		for (EntityEventType t : EntityEventType.values()) {
			getEntityEvent(t).removeListenerFamily(owner);
		}
		for (WorldEventType t : WorldEventType.values()) {
			getWorldEvent(t).removeListenerFamily(owner);
		}
	}


	// =============================================
	// ====== World CONSTRUCTOR AND STARTUP STUFF
	// =============================================


	/**
	 * Simple constructor
	 */
	public World() {
		this(null, "world", true);
		tileTypesCollection = new TileTypes();

		this.setSize(16, 16);
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				if (x == 0 || y == 0 || x == 15 || y == 15) {
					this.setTile(x, y, tileTypesCollection.getTile("wall"));
				} else {
					this.setTile(x, y, tileTypesCollection.getTile("floor"));
				}
			}
		}
	}


	/**
	 * Constructs this world from a lua script
	 * 
	 * @param luaScriptFile
	 *            The level script
	 */
	@Deprecated
	public World(File luaScriptFile) {
		this(luaScriptFile, "world", true);
		tileTypesCollection = new TileTypes();
	}


	/**
	 * Constructs this world with a name
	 * 
	 * @param name
	 *            The name
	 */
	public World(String name) {
		this(null, name, false);
		tileTypesCollection = new TileTypes();

		backgroundImage = null;
		tiles = new Tile[0][0];
	}


	/**
	 * Constructs a world
	 * 
	 * @param luaScriptFile
	 *            The level script
	 * @param name
	 *            The name
	 */
	@Deprecated
	private World(File luaScriptFile, String name, boolean autoPlay) {
		this.name = name;

		tileTypesCollection = new TileTypes();

		backgroundImage = null;
		tiles = new Tile[0][0];

		if (luaScriptFile != null) {
			this.levelScripts.add(new UserScript("init", luaScriptFile));
		} else {
			String defaultInitScript =
					"--[[\n" +
					"    The world's init script.\n" +
					"    In the command line, run help() for more info.\n" +
					"]]\n" +
					"\n" +
					"registerUpdateListener(function(dt)\n" +
					"  -- put any code you want to run every frame in here\n" +
					"end)";
			this.levelScripts.add(new UserScript("init", defaultInitScript));
		}

		botScripts.add(new UserScript("init", "--TODO"));

		this.autoPlay = autoPlay;
	}


	/**
	 * Perform some initializations that need to be done upon deserialization
	 */
	public void runInitScripts() {
		if (!didInit) {
			System.out.println("Init'ing world");
			LuaInvocation initScript = getSandbox().init().join(2000);

			assert initScript.getStatus() == ScriptStatus.COMPLETE;

			for (Entity e : entities) {
				System.out.println("init: " + e);
				e.sandboxInit();
			}
			this.didInit = true;
			
			this.message(this, "World Initialization finished", LoggingLevel.DEBUG);
		}
	}


	public void onBecomingVisibleInGameplay() {
		if (!didInit && autoPlay) {
			runInitScripts();
		}
	}


	// =============================================
	// ====== World BINDABLE METHODS
	// =============================================

	/**
	 * @return A new world
	 */
	@Bind(SecurityLevel.AUTHOR)
	@BindTo("new")
	@Deprecated
	public static LuaValue newWorld() {
		World w = new World();
		return LuaProxyFactory.getLuaValue(w);
	}


	/**
	 * Instantly causes win to occur.
	 */
	@Bind(SecurityLevel.AUTHOR)
	public void win() {
		isWon = true;

		this.message(this, "World has been won!", LoggingLevel.DEBUG);
		fire(WorldEventType.WIN, this);
	}


	public boolean isWon() {
		return isWon;
	}


	// =============================================
	// ====== World GAME LOOP
	// =============================================

	/**
	 * Updates this world and all children.
	 * 
	 * @param dt
	 *            Delta time
	 */
	public void update(float dt) {
		// update tiles from tileTypes, if dirty
			// Remove all entities that have been asynchronously
			// queued to be removed

		final Entity[] remove = getToRemove().toArray(new Entity[]{});
		synchronized (this) {
			for (Entity e : remove) {
				removeEntity(e);
				getToRemove().remove(e);
			}

			refreshTiles();

			// update tiles
			for (Tile[] ts : tiles) {
				for (Tile t : ts) {
					if (t==null) continue;
					t.update(dt);
				}
			}

			// update entities
			for (Entity e : entities) {
				ActionQueue aq = e.getActionQueue();
				playstyle.dequeueIfAllowed(aq);
				e.update(dt);
			}
			playstyle.update();


			// update level script
			if (this.didInit) {
				getSandbox().fireEvent("UPDATE", UpdateCoalescer.instance, LuaValue.valueOf(dt));

				checkIfWon();
			}
		}

	}


	/**
	 * Render this world and all children
	 * 
	 * @param batch
	 *            a SpriteBatch
	 */
	public void render(RenderingContext batch) {
		refreshTiles();

		// cam.translate(w/2, h/2);

		// clear to black
		batch.setClearColor(new Color(.0f, .0f, .0f, 1));
		batch.clearContext();

		// draw background image
		if (backgroundImage != null) {
			batch.draw(backgroundImage, 0, 0);
		}

		// draw tiles
		for (Tile[] ts : tiles) {
			for (Tile t : ts) {
				if (t==null) continue;
				t.render(batch);
			}
		}

		// draw each layer of entities
		for (Layer layer : toLayers()) {
			for (Entity e : layer.getEntities()) {
				e.render(batch);
			}
		}
	}


	private void checkIfWon() {
		int numGoals = 0;
		int numGoalsMet = 0;

		ArrayList<Goal> goals = new ArrayList<>();
		ArrayList<Actor> actors = new ArrayList<>();

		// extract all goals and actors
		for (Entity e : entities) {
			if (e instanceof Goal) {
				goals.add((Goal) e);
				numGoals++;
			} else if (e instanceof Actor) {
				actors.add((Actor) e);
			}
		}

		// check if each goal has been met
		for (Goal g : goals) {
			for (Actor a : actors) {
				if (a.getPosition().distance(g.getPosition()) < .1f) {
					numGoalsMet++;
					continue;
				}
			}
		}
		if (numGoals > 0 && numGoals == numGoalsMet) {
			this.win();
		}
	}


	@Bind(value = SecurityLevel.AUTHOR, doc = "Add and Entity to the World")
	public World addEntity(@Doc("The Argument Entity to add to the World") LuaValue v) {
		Entity e = (Entity) v.checktable().get("this").checkuserdata(Entity.class);
		addEntity(e);
		return this;
	}


	/**
	 * Adds an entity
	 * 
	 * @param e
	 *            The entity to add
	 */
	public void addEntity(Entity e) {

		if (entitiesAtPos(e.getPosition()).anyMatch(entity -> e.isSolid() && e.getClass().equals(entity.getClass()))) {
			return;
		}

		if (entities.contains(e)) {
			return;
		}

		Tile tile = this.getTile(e.getPosition());

		if (tile == null || (tile.isOccupied() && e.isSolid())) {
			return;
		}

		this.message(this, "Adding entity "+e.getName()+" to the world", LoggingLevel.DEBUG);
		entities.add(e);
		e.onAddedToWorld(this);
		fire(EntityEventType.ENTITY_ADDED, e);


		if (e.isSolid()) {
			if (tile != null) {
				tile.setOccupiedBy(e);
			}
		}
	}


	public void updateEntity(Entity e) {
		if (entities.contains(e)) {
			Tile tile = this.getTile(e.getPosition());
			if (tile != null)
				tile.setOccupiedBy(e.isSolid() ? e : null);
		}
	}


	/**This is necessary because a game that is rewinded will have to copy all the entity's scripts 
	 * into the newly deserialized versions of those entities.  If they are removed from the entities 
	 * list, then that copy couldn't happen.*/
	private transient ArrayList<Entity> removedEntities = new ArrayList<Entity>();


	@Bind(value = SecurityLevel.AUTHOR, doc = "Removes the entity from the world")
	public Boolean removeEntity(LuaValue entity) {
		Entity e = (Entity) entity.checktable().get("this").checkuserdata(Entity.class);
		return removeEntity(e);
	}


	/**Removes the entity from the world.  Returns 'true' if the items was removed, or 'false' if
	 * it was never in the world to begin with.  Stashes removed entities into a private list so 
	 * any changes to their scripts will not be lost in the event of a game reset.*/
	public boolean removeEntity(Entity e) {
		if (!entities.remove(e))
			return false;
		if (removedEntities==null) removedEntities = new ArrayList<Entity>();
		removedEntities.add(e);
		if (e.isSolid()) {
			Tile tile = this.getTile(e.getPosition());
			if (tile != null && tile.getOccupiedBy() == e)
				tile.setOccupiedBy(null);
		}
		removeEventListenersByOwner(e);
		fire(EntityEventType.ENTITY_REMOVED, e);
		message(this, String.format("%s was removed", e.getName()), LoggingLevel.GENERAL);
		return true;
	}


	/**
	 * Place a bot with a given name at (x,y)
	 * 
	 * @param name
	 * @param x
	 * @param y
	 */
	public Bot makeBot(String name, float x, float y) {
		// TODO - change the way names work, perhaps
		for (Entity e : entities) {
			if (e instanceof Bot && e.getPosition().x == x && e.getPosition().y == y) {
				return (Bot) e;
			}
		}

		Bot b = new Bot(this, name);
		b.setPosition(new Point2D.Float(x, y));
		this.addEntity(b);

		return b;
	}


	/**
	 * Place a bot with a given name at (x,y)
	 *
	 * @param x
	 * @param y
	 */
	@Bind
	public void makeBot(LuaValue x, LuaValue y) {
		makeBot("bot", x.tofloat(), y.tofloat());
	}


	public boolean isPlayOnStart() {
		return this.autoPlay;
	}


	public void setPlayOnStart(boolean autoPlay) {
		this.autoPlay = autoPlay;
	}


	@Bind(value = SecurityLevel.AUTHOR,
			doc = "Set Game flags for the following\n" +
					"'autoPlay:boolean', 'name:string', 'playstyle:[EntityTurns,RTS,TeamTurns]'")
	public void setFlag(@Doc("The Flag Name [autoPlay,name,playstyle, timesReset]") LuaValue flagName,
						@Doc("The Value of the flag") LuaValue flagVal) {
		switch (flagName.checkjstring()) {
		case "autoPlay":
			this.autoPlay = flagVal.checkboolean();
			break;
		case "name":
			this.name = flagVal.checkjstring();
			break;
		case "playstyle":
			if (flagVal.checkjstring().equals("EntityTurns")) {
				this.playstyle = new ActionGrouping.EntityTurnsGrouping();
			} else if (flagVal.checkjstring().equals("RTS")) {
				this.playstyle = new ActionGrouping.RTSGrouping();
			} else if (flagVal.checkjstring().equals("TeamTurns")) {
				this.playstyle = new ActionGrouping.TeamTurnsGrouping();
			} else {

			}
			break;
		case "timesReset":
			this.timesReset = flagVal.checkint();
			break;
		default:
			throw new LuaError("Bad flag name");
		}
	}


	/**
	 * Set the size of the world
	 * 
	 * @param w
	 * @param h
	 */
	@Bind
	public void setSize(LuaValue w, LuaValue h) {
		setSize(w.checkint(), h.checkint());
	}


	/**
	 * Sets this world's size Calls to set tiles outside of the world's size (or
	 * before the world's size is set) may cause issues.
	 * 
	 * @param w
	 *            the width, in tiles
	 * @param h
	 *            the height, in tiles
	 */
	public void setSize(int w, int h) {
		setSize(w, h, 0, 0);
		/* Tile[][] oldTiles = tiles; tiles = new Tile[w][h];
		 *
		 * int copyW = Math.min(w, oldTiles.length); int copyH = Math.min(h,
		 * oldTiles.length == 0 ? 0 : oldTiles[0].length);
		 *
		 * for (int i = 0; i < h; i++) { for (int j = 0; j < w; j++) { if (j <
		 * copyW && i < copyH) { tiles[j][i] = oldTiles[j][i]; } else {
		 * tiles[j][i] = new Tile(this, null, j, i); } } }
		 *
		 * ArrayList<Entity> displaced = new ArrayList<Entity>(); for (Entity e
		 * : entities) { Tile t = this.getTile(e.getPosition()); if
		 * (e.isSolid()) { if (t == null) { displaced.add(e); } else {
		 * t.setOccupiedBy(e); } } } entities.removeAll(displaced); */
	}


	public void setSize(int w, int h, int offsetX, int offsetY) {
		this.message(this, "Changing world size to "+w+"x"+h+".", LoggingLevel.DEBUG);

		// Copy all tiles into a new array of tiles.
		Tile[][] newTiles = new Tile[w][h];
		Rectangle oldRect = (tiles != null && tiles.length > 0) ? new Rectangle(0, 0, tiles.length, tiles[0].length)
				: new Rectangle(0, 0, 0, 0);
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				newTiles[x][y] = new Tile(this, null, x, y);
		Rectangle newRect = new Rectangle(offsetX, offsetY, w, h);

		// Move tiles from the intersecting rectangle.
		Rectangle interRect = oldRect.intersection(newRect);
		for (int x = 0; x < interRect.width; x++) {
			int interX = x + interRect.x;
			for (int y = 0; y < interRect.height; y++) {
				int interY = y + interRect.y;
				Tile t = tiles[interX][interY];
				int newX = (offsetX < 0 ? x - offsetX : x), newY = (offsetY < 0 ? y - offsetY : y);
				t.setPosition(newX, newY);
				newTiles[newX][newY] = t;
				Entity e = t.getOccupiedBy();
				if (e == null)
					continue;
				//Point2D.Float ePos = e.getPosition();
				e.setPosition(newX, newY);
			}
		}
		tiles = newTiles;
		// Remove all entities that have now been displaced.
		entities.removeIf((e) -> getTile(e.getPosition()) == null);


	}


	/**
	 * @return The size of this world, in tiles
	 */
	public Point2D.Float getSize() {
		if (tiles.length == 0) {
			return new Point2D.Float(0, 0);
		}
		return new Point2D.Float(tiles.length, tiles[0].length);
	}


	/**
	 * Update tile sprites, if they're stale
	 */
	public void refreshTiles() {
		if (tilesAreStale) {

			int w = tiles.length;
			if (tiles.length == 0)
				return;

			int h = tiles[0].length;
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					Tile current = tiles[i][j];
					if (current==null) continue;
					current.setPosition(i, j);

					Tile l = i >= 1 ? tiles[i - 1][j] : null;
					Tile r = i < w - 1 ? tiles[i + 1][j] : null;
					Tile u = j < h - 1 ? tiles[i][j + 1] : null;
					Tile d = j >= 1 ? tiles[i][j - 1] : null;

					current.updateTexture(l, r, u, d);
				}
			}

			tilesAreStale = false;
		}
	}


	/**
	 *
	 * @param x
	 * @param y
	 * @param tiletype
	 */
	@Bind(value=SecurityLevel.AUTHOR,doc="Sets the Tile at the argument positions")
	public void setTile(
			@Doc("The X position of the tile") LuaValue x,
			@Doc("The Y position of the tile") LuaValue y,
			@Doc("The TileType of the tile ['floor', 'tile', etc...]") LuaValue tiletype) {
		setTile(x.checkint() - 1, y.checkint() - 1, tileTypesCollection.getTile(tiletype.checkjstring()));
	}


	/**
	 *
	 * @return
	 */
	@Bind
	public Player getPlayer() {
		for (Entity e : entities) {
			if (e instanceof Player) {
				return (Player) e;
			}
		}
		return null;
	}

	public boolean isBlocking(int x, int y) {
		final int w = tiles.length;
		final int h = tiles[0].length;
		return x >= 0 && x <= w - 1 && y >= 0 && y <= h - 1 && tiles[x][y].isSolid();
	}

	@Bind(SecurityLevel.DEFAULT)
	public Boolean isBlocking(LuaValue lx, LuaValue ly) {
		final int x = lx.checkint() - 1;
		final int y = ly.checkint() - 1;
		return isBlocking(x,y);
	}


	/**
	 * Sets a specific tile
	 * 
	 * @param x
	 *            The x location, in tiles
	 * @param y
	 *            The y location, in tiles
	 * @param tileType
	 *            The type of the tile
	 */
	public void setTile(int x, int y, TileType tileType) {
		if (!isInBounds(x, y))
			return;
		
		this.message(this, "Setting tile at ("+x+", "+y+") to "+tileType.getName(), LoggingLevel.DEBUG);

		tilesAreStale = true;
		if (tiles[x][y] == null)
			tiles[x][y] = new Tile(this, tileType, x, y);
		else
			tiles[x][y].setType(tileType);
	}


	/**Sets the tile as indicated.  If out-of-bounds, does nothing.  Can set to null.*/
	public void setTile(int x, int y, Tile tile) {
		if (!isInBounds(x, y))
			return;
		this.message(this, "Setting tile at ("+x+", "+y+") to "+tile.getType().getName(), LoggingLevel.DEBUG);
		tilesAreStale = true;
		tiles[x][y] = tile;
	}

	@Bind(value=SecurityLevel.AUTHOR,doc = "Set's a range of Tiles")
	public void setTiles(LuaValue lx, LuaValue ly, LuaValue width, LuaValue height, LuaValue tileType) {
		final int x = lx.toint() - 1;
		final int y = ly.toint() - 1;
		assert x >= 0 || x < tiles.length;
		assert y >= 0 || y < tiles[0].length;
		assert x + width.checkint() > 0 || (x + width.checkint()) < tiles.length;
		assert y + width.checkint() > 0 || (y + width.checkint()) < tiles[0].length;

		for(int i = x; i < width.checkint(); i++) {
			for(int j = y; j < height.checkint(); j++) {
				setTile(i,j,tileTypesCollection.getTile(tileType.checkjstring()));
			}
		}
	}


	/**Returns whether the given x,y is within the world's bounds.*/
	public boolean isInBounds(int x, int y) {
		if (x < 0 || y < 0 || x >= tiles.length || y >= tiles[x].length) {
			return false;
		}
		return true;
	}


	/**
	 * Returns the tile at the given tile location. If tiles reference is null,
	 * or the (x,y) is outside the world boundaries, returns null.
	 * 
	 * @param x
	 *            The x position, in game space.
	 * @param y
	 *            The y position, in game space.
	 */
	@Deprecated
	public Tile getTileUnderLocation(double x, double y) {
		return getTileUnderLocation((int) x, (int) y);
	}


	/**
	 * Returns the tile at the given tile location. If tiles reference is null,
	 * or the (x,y) is outside the world boundaries, returns null.
	 * 
	 * @param x
	 *            The x position, in game space.
	 * @param y
	 *            The y position, in game space.
	 */
	public Tile getTileUnderLocation(int x, int y) {
		Tile[][] tiles = this.tiles;
		if (tiles == null)
			return null;
		if (!isInBounds(x, y))
			return null;
		return tiles[x][y];
	}


	/**
	 * @param x
	 * @param y
	 * @return The Tile at a given position, or null, if the tile at the given location
	 * is beyond the world's boundaries or non-existent.
	 */
	public Tile getTile(float x, float y) {
		return getTileUnderLocation((int) Math.floor(x), (int) Math.floor(y));
	}


	/**
	 * @param pos
	 * @return The Tile at a given position
	 */
	public Tile getTile(Point2D.Float pos) {
		return getTile(pos.x, pos.y);
	}


	/**
	 * Returns all tiles that are encompassed by, or intersect, the given
	 * rectangle.
	 */
	public List<Tile> getTilesUnderLocation(Rectangle2D.Float rect) {
		ArrayList<Tile> result = new ArrayList<Tile>();
		Tile[][] tiles = this.tiles;
		assert (tiles != null); // Sanity check
		int maxX = (int) (rect.x + rect.width);
		int maxY = (int) (rect.y + rect.height);
		for (int x = Math.max(0, (int) rect.x); x <= maxX && x < tiles.length; x++) {
			Tile[] tilesAtX = tiles[x];
			assert (tilesAtX != null); // Sanity check.
			for (int y = Math.max(0, (int) rect.y); y <= maxY && y < tilesAtX.length; y++) {
				Tile t = tilesAtX[y];
				if (t != null)
					result.add(t);
			}
		}
		return result;
	}


	/**
	 * All the entities are assigned to a layer during the render loop,
	 * depending on their Z-value.
	 *
	 * @return A list of layers, representing all actors
	 */
	private ArrayList<Layer> toLayers() {
		HashMap<Float, Layer> layers = new HashMap<>();

		for (Entity e : entities) {
			float z = e.getZ();

			Layer l = layers.get(z);
			if (l == null) {
				l = new Layer(z);
				layers.put(z, l);
			}

			l.add(e);
		}

		ArrayList<Layer> ret = new ArrayList<Layer>(layers.values());
		Collections.sort(ret);

		return ret;
	}


	public String getName() {
		return name;
	}


	public void setName(String newName) {
		name = newName;
	}


	@Override
	public LuaValue getLuaValue() {
		if (this.luaValue == null) {
			this.luaValue = LuaProxyFactory.getLuaValue(this);
		}
		return this.luaValue;
	}


	/**
	 * Generates an id
	 * 
	 * @return a new id
	 */
	public int makeID() {
		return idCounter++;
	}


	/**
	 * Asks if an entity is allowed to move to a given tile. Locks that tile to
	 * be owned by the given entity if it is allowed.
	 * 
	 * @param e
	 *            The entity asking
	 * @param x
	 *            Location X, in tiles
	 * @param y
	 *            Location Y, in tiles
	 * @return True if the entity is allowed to move to this location
	 */
	public boolean requestMoveToNewTile(Entity e, int x, int y) {
		if (x < 0 || y < 0) {
			// System.out.println("Unable to move: x/y too small");
			return false;
		}
		if (x >= tiles.length || y >= tiles[0].length) {
			// System.out.println("Unable to move: x/y too big");
			return false;
		}

		if(Block.class.isAssignableFrom(e.getClass()) && tiles[x][y].getType().getName().equals("pit")) {
			return true;
		}

		Tile t = tiles[x][y];
		if (t == null)
			return false;
		if (t.isSolid()) {
			// System.out.println("Unable to move: tile solid");
			return false;
		}
		if (t.isOccupied()) {
			Entity o = t.getOccupiedBy();
			if (o != null && o instanceof Pushable) {
				((Pushable) o).bumpedInto(e, Actor.Direction.byDelta(x - e.getPosition().x, y - e.getPosition().y));
			}
			// System.out.println("Unable to move: tile occupied");
			return false;
		}

		// System.out.println("Ok to move");
		if (e.isSolid()) {
			t.setOccupiedBy(e);
		}

		return true;

	}


	/**
	 * Used to release the lock that this entity previously owned on a tile
	 * 
	 * @param e
	 *            The entity releasing the tile
	 * @param x
	 *            Location X, in tiles
	 * @param y
	 *            Location Y, in tiles
	 */
	public void didLeaveTile(Entity e, int x, int y) {
		Tile tile = getTile(x, y);
		tile.setOccupiedBy(null);
		this.message(e, "Left tile at ("+x+", "+y+")", LoggingLevel.DEBUG);


		fire(EntityEventType.ENTITY_MOVED, e);
	}


	/**
	 * Gets what entity is occupying a given tile, or null if there is no such
	 * entity.
	 * 
	 * @param x
	 *            Location X, in tiles
	 * @param y
	 *            Location Y, in tiles
	 * @return The entity under the given location. Returns null if there is no
	 *         such entity.
	 */
	public Entity getEntityUnderLocation(float x, float y) {
		for (Entity e : entities) {
			Point2D.Float p = e.getPosition();

			if (x < p.x || x >= p.x + 1) {
				continue;
			}

			if (y < p.y || y >= p.y + 1) {
				continue;
			}

			if (e instanceof ChildEntity) {
				continue;
			}

			return e;
		}
		return null;
	}

	public boolean containsEntity(Entity e) {
		return entities.contains(e);
	}


	/**
	 * Gets all Entity objects intersecting the given rectangle.
	 *
	 * @return The list of intersecting entities. An entity is "intersecting" if
	 *         any part of it would be within the given rectangle.
	 */
	public List<Entity> getEntitiesUnderLocation(Rectangle2D.Float rect) {
		ArrayList<Entity> existingEntities = entities;
		ArrayList<Entity> result = new ArrayList<Entity>();

		for (Entity e : existingEntities) {
			if (e instanceof FloatingText)
				continue;
			Point2D.Float pt = e.getPosition();
			Rectangle2D.Float rectEntity = new Rectangle2D.Float(pt.x, pt.y, 1f, 1f);
			if (rectEntity.intersects(rect))
				result.add(e);
		}

		return result;
	}

	/**
	 * For people who don't want to use floor()
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Point getTileLocation(float x, float y) {
		return new Point((int) x, (int) y);
	}


	@Override
	/**
	 * The returns the sandbox associated with calls to the world. The init
	 * script runs here.
	 */
	public LuaSandbox getSandbox() {
		if (this.mapSandbox != null) {
			return this.mapSandbox;
		} else {
			mapSandbox = new LuaSandbox(this);
			mapSandbox.registerEventType("UPDATE", "Called on every frame", "deltaTime");
			mapSandbox.addBindable("this", this);
			mapSandbox.addBindable("world", this);
			mapSandbox.addBindable("tileTypes", tileTypesCollection);
			mapSandbox.addBindable("whitelist", this.getWhitelist());
			mapSandbox.addBindableClass(Player.class);
			mapSandbox.addBindableClasses(LuaReflection.getEntityClasses())
					.addBindableClasses(LuaReflection.getItemClasses());

			return mapSandbox;
		}
	}


	/**
	 * @return The types of tiles available
	 */
	public TileTypes getTileTypes() {
		if (tileTypesCollection == null) {
			tileTypesCollection = new TileTypes();
		}
		return tileTypesCollection;
	}


	/**
	 * Copies the scripts associated with bots from a prior world into the bots with the same 
	 * ID numbers in this world.  This method is necessary after a rewind because the world 
	 * is re-serialized from bytes, and any changes a player has made to scripts would be 
	 * lost in this process.
	 */
	public synchronized void persistScriptsFrom(World dirty) {
		synchronized (this) {
			HashMap<Integer, Entity> dirtyEntities = new HashMap<Integer, Entity>(),
					cleanEntities = new HashMap<Integer, Entity>();

			// The "dirty" entities include those entities in the old World,
			// plus those entities removed from the old World.
			for (Entity e : dirty.entities)
				dirtyEntities.put(e.getId(), e);
			if (dirty.removedEntities != null)
			for (Entity e : dirty.removedEntities)
				dirtyEntities.put(e.getId(), e);

			// The "clean" entities are those entities that are in the
			// newly-deserialized World (ie, this World).
			for (Entity e : this.entities)
				cleanEntities.put(e.getId(), e);

			// Every dirty entity should copy its scripts into the matching clean entities.
			for (Entity dirty_e : dirty.entities) {
				Entity clean_e = cleanEntities.remove(dirty_e.getId());
				// This can happen if an Entity was added during the game.
				if (clean_e == null)
					this.message(dirty_e,
							"An entity from before the rewind does not exist.  Its scripts cannot be copied.",
							LoggingLevel.STDOUT);
				// Sanity check
				else if (clean_e.getScripts() == null && dirty_e.getScripts() != null)
					this.message(clean_e,
							"New version of entity has no scripts while old versions of entity does have scripts.",
							LoggingLevel.ERROR);
				//Sanity check
				else if (clean_e.getScripts() != null && dirty_e.getScripts() == null)
					this.message(clean_e,
							"Old version of entity has no scripts while new versions of entity does have scripts.",
							LoggingLevel.ERROR);
				//This can happen for non-scriptable entities like FloatingText
				else if (clean_e.getScripts() == null && dirty_e.getScripts() == null)
					continue;
				else
					clean_e.getScripts().setTo(dirty_e.getScripts());
			}

			// This is a sanity check.  There should be no clean entities left (all were removed when scripts were copied into them.  That was the point of maintaining World.removedEntities.
			for (Entity new_e : cleanEntities.values()) {
				this.message(new_e,
						"An entity from after the rewind did not exist before the rewind.  Its scripts will be restarting.",
						LoggingLevel.ERROR);
			}
		}
	}


	/**
	 * @param oldWorld
	 */
	public void resetFrom(World oldWorld) {
		persistScriptsFrom(oldWorld);
		this.timesReset = oldWorld.timesReset + 1;

		// TODO - should we pass the old world to the init script maybe?
	}


	/**
	 *
	 * @return The state of this World
	 */
	@Override
	public Map<String, Object> getState() {
		final Map<String, Object> state = new HashMap<>();
		state.put("Times Reset", timesReset);
		// TODO - this should involve bots and stuff, too...
		// also TODO - we should clean up stuff we ended up not using like health
		/*
		Player player = getPlayer();
		if (player != null) {
			state.put("Steps", player.steps());
			state.put("Bumps", player.bumps());
			state.put("Player Treasure", player.getInventory().getTotalValue());
		}*/
		state.put("Steps", getPlayerEntities()
				.map(e -> e.steps())
				.reduce(0, (a,b) -> a + b));
		state.put("Bumps", getPlayerEntities()
				.map(e -> e.bumps())
				.reduce(0, (a,b) -> a + b));
		state.put("Player Treasure", getPlayerEntities()
				.map(e -> e.getInventory().getTotalValue())
				.reduce(0, (a, b) -> a + b));
		state.put("Total Treasure", getTotalValue());
		return state;
	}

	private Stream<Actor> getPlayerEntities() {
		return  entities.stream().filter(e ->
						Player.class.isAssignableFrom(e.getClass())
								|| Bot.class.isAssignableFrom(e.getClass())
								|| Ghost.class.isAssignableFrom(e.getClass()))
						.map(Actor.class::cast);
	}

	/**
	 *
	 * @return
	 */
	@Bind(SecurityLevel.DEFAULT)
	@Doc("Returns the location of the Goal in the world.")
	public Varargs getGoal() {
		Point2D.Float searchPos = this.getSize();
		searchPos.x /= 2;
		searchPos.y /= 2;

		LuaSandbox currentSandbox = SandboxManager.getCurrentSandbox();

		if (currentSandbox != null) {
			Entity e = currentSandbox.getSecurityContext().getEntity();

			if (e != null) {
				searchPos = e.getPosition();
			}
		}

		Goal closest = null;
		double dist = Float.POSITIVE_INFINITY;
		for (Entity e : entities) {
			if (e instanceof Goal) {
				double d = e.getPosition().distance(searchPos);

				if (d < dist) {
					dist = d;
					closest = (Goal) e;
				}
			}
		}

		if (closest == null) {
			return LuaValue.NIL;
		} else {
			Point2D.Float p = closest.getPosition();
			return LuaValue.varargsOf(new LuaValue[] { LuaValue.valueOf(p.x + 1), LuaValue.valueOf(p.y + 1) });
		}
	}


	@Bind(SecurityLevel.DEFAULT)
	@Doc("Creates and displays an Alert window.")
	public void alert(@Doc("The Alert message") LuaValue alert, @Doc("The Title of the Alert Window") LuaValue title) {
		showAlert(alert.checkjstring(),
				title.isnil() ? "" : title.optjstring(""));
	}


	/**
	 *
	 * @param alert
	 * @param title
	 */
	public void showAlert(String alert, String title) {
		this.message(this, title + (title.length()>=1?"\n":"") + alert, LoggingLevel.QUEST);
		Thread t = new Thread(() -> {
			if(poptartListener != null) {
				poptartListener.accept(new GameplayScreen.Poptart(this, title, alert));
			} else {
				JOptionPane.showMessageDialog(null, alert, title, JOptionPane.INFORMATION_MESSAGE);
			}
		});
		t.start();
	}

	/**
	 * Opens a browser to a given url
	 * 
	 * @param lurl
	 *            A url to open
	 */
	@Bind(SecurityLevel.AUTHOR)
	@Doc("Opens a Browser Window using the argument URL string")
	public void openBrowser(@Doc("The desired URL") LuaValue lurl) {
		// TODO - now that we're doing url security, should we just change this
		// to "NONE" security level?
		try {
			String urlString = lurl.checkjstring();
			String urlNoProtocol = urlString.replace("http://", "");
			urlNoProtocol = urlNoProtocol.replace("https://", "");

			String[] allowedURLs = { "youtube.com", "dungeonbots.herokuapp.com", "en.wikipedia.org",
					"stackoverflow.com" };

			for (String allow : allowedURLs) {
				if (urlNoProtocol.startsWith(allow + "/") || urlNoProtocol.startsWith("www." + allow + "/")
						|| urlNoProtocol.equals(allow) || urlNoProtocol.equals("www." + allow)) {
					this.message(this, "Opening browser to "+urlString, LoggingLevel.DEBUG);
					java.awt.Desktop.getDesktop().browse(new URI(urlString));
					return;
				}
			}

			throw new Exception("URL not allowed: " + urlNoProtocol);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new LuaError("Invalid URL!");
		}
	}


	/**
	 * Shows a popup box'
	 * 
	 * @param alert
	 * @param title
	 */
	@Bind(value = SecurityLevel.AUTHOR, doc = "Creates an alert message")
	public void showAlert(@Doc("The Content of the Alert Message") LuaValue alert,
			@Doc("The Title of the Alert window") LuaValue title) {
		showAlert(alert.tojstring(), title.tojstring());
	}

	private Stream<Entity> entitiesAtPos(final Point2D.Float pos) {
		return entities.stream().filter(e -> e.getPosition().distance(pos) < 0.1);
	}

	/**
	 * Specialized form of typeAtPos that statically requires that the argument type is derived from Entity
	 * @param pos
	 * @param type
	 * @param <T>
	 * @return
	 */
	private <T extends Entity> Stream<T> entityTypeAtPos(final Point2D.Float pos, final Class<T> type) {
		return typeAtPos(pos, type);
	}


	/**
	 * Returns a Stream of the specified type of entities that are assignable from the argument type
	 * @param pos The Position to
	 * @param type
	 * @param <T>
	 * @return
	 */
	private <T> Stream<T> typeAtPos(final Point2D.Float pos, final Class<T> type) {
		return entitiesAtPos(pos).filter(e -> type.isAssignableFrom(e.getClass())).map(type::cast);
	}


	/**
	 * Invokes the peekInventory method on any valid entities found at the specified position.
	 * @param pos The position to try to peek at an inventory
	 * @return A table/array of item names and descriptions of items in the inventory
	 */
	public LuaValue tryPeek(final Entity src, final Point2D.Float pos) {
		return typeAtPos(pos, HasInventory.class)
				.findFirst()
				.filter(e -> e.canTake())
				.map(e -> e.peekInventory())
				.orElse(LuaValue.NIL);
	}


	/**
	 * Takes the item from the entity found at the specified index from the entities inventory at the specified index
	 * @param src The Entity with the inventory
	 * @param pos The position to find an entity with an inventory to take an item from
	 * @param index The index into the inventory of the entity to take the item
	 * @return True if taking the item succeeded, false otherwise.
	 */
	public Integer tryTake(final Actor src, final Point2D.Float pos, final int index) {
		for(Actor a : typeAtPos(pos, Actor.class)
				.filter(HasInventory::canTake).collect(Collectors.toList())) {
			final ItemReference ir = a.getInventory().peek(index);
			final String itemName = ir.getName();
			final Integer taken = src.getInventory().tryTakeItem(ir);
			if(taken >= 0) {
				message(src,
						String.format("%s took %s from %s",
								src.getName(),
								itemName,
								a.getName()),
						LoggingLevel.GENERAL);
			}
			return taken;
		}
		return -1;
	}


	/**
	 * Try to use the Item provided with the ItemReference on any entities found at the specified location
	 * @param itemReference The Item to use
	 * @param location The location to find entities to use the associated Item with
	 * @return True If any entity/ies successfully used the Item
	 */
	public Boolean tryUse(final ItemReference itemReference, final Point2D.Float location) {
		return entitiesAtPos(location)
				.anyMatch(e -> {
					final Entity owner = itemReference.inventory.getOwner();
					final String name = itemReference.getName();
					final boolean used = e.useItem(itemReference);
					if(used) {
						message(owner,
								String.format("%s used %s on %s",
										owner.getName(),
										name,
										e.getName()),
								LoggingLevel.GENERAL);
					}
					return used;
				});
	}


	/**
	 * Try to use Entities that implement the Useable interface at the specified location
	 * @param location
	 * @return
	 */
	public Boolean tryUse(final Actor src, final Point2D.Float location) {
		return entitiesAtPos(location)
				.filter(e -> Useable.class.isAssignableFrom(e.getClass()))
				.anyMatch(e -> {
					final boolean used = Useable.class.cast(e).use();
					if(used){
						message(e,
								String.format("%s used %s", src.getName(), e.getName()),
								LoggingLevel.GENERAL);
					}
					return used;
				});
	}


	/**
	 * Try to get the Item specified with the ItemReference at the specified lcoation
	 * @param itemReference
	 * @param location
	 * @return
	 */
	public Boolean tryGive(final ItemReference itemReference, final Point2D.Float location) {
		return typeAtPos(location, Actor.class)
				.filter(e -> e.canTake())
				.anyMatch(e -> {
					final String name = itemReference.getName();
					final int gives = e.getInventory().tryTakeItem(itemReference);
					if(gives > 0) {
						message(e,
								String.format("%s gives %s to %s",
										itemReference.inventory.getOwner().getName(),
										name,
										e.getClass().getSimpleName()),
								LoggingLevel.GENERAL);
					}
					return gives > 0;
				});
	}

	/**
	 * Try to get the Item specified with the ItemReference at the specified lcoation
	 * @param itemReference
	 * @param index
	 * @param location
	 * @return
	 */
	public Boolean tryGive(final ItemReference itemReference, final int index, final Point2D.Float location) {
		return typeAtPos(location, Actor.class)
				.filter(e -> e.canTake())
				.anyMatch(e -> {
					final String name = itemReference.getName();
					final int gives = e.getInventory().tryTakeItem(itemReference, index);
					if(gives > 0) {
						message(e,
								String.format("%s gives %s to %s",
										itemReference.inventory.getOwner().getName(),
										name,
										e.getClass().getSimpleName()),
								LoggingLevel.GENERAL);
					}
					return gives > 0;
				});
	}


	/**
	 * Try to grab an ItemEntity in the space currently occupied by the argument entity.
	 * @param dst The Entity that will receive the item
	 * @return True if an Item was successfully grabed and placed into the dst entity's inventory
	 */
	public Boolean tryGrab(final Actor dst) {
		return entityTypeAtPos(dst.getPosition(), ItemEntity.class)
				.filter(e -> !e.equals(dst))
				.findFirst()
				.map(e -> {
					final String name = e.getItem().getName();
					final boolean grabbed = e.pickUp(dst);
					message(e,
							String.format("%s %s grabbed %s",
									dst.getEntity().getClass().getSimpleName(),
									grabbed ? "Sucessfully" : "Unsucessfully",
									name),
							LoggingLevel.GENERAL);
					return grabbed;
				})
				.orElse(false);
	}


	/**
	 *
	 * @param src
	 * @param pos
	 * @param dir
	 */
	public void tryPush(final Actor src, final Point2D.Float pos, final Actor.Direction dir) {
		typeAtPos(pos, Pushable.class)
				.forEach(e -> {
					message(src, "pushes " + e.getClass().getSimpleName(), LoggingLevel.DEBUG);
					e.push(dir);
				});
	}


	public String tryLook(final Point2D.Float dir) {
		return entitiesAtPos(dir)
				.filter(e -> Inspectable.class.isAssignableFrom(e.getClass()))
				.filter(e -> !ChildEntity.class.isAssignableFrom(e.getClass()))
				.map(e -> Inspectable.class.cast(e).inspect())
				.reduce("", (a,b) -> a + "\n" + b);
	}


	/**
	 * Deserialization helper
	 * 
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		this.tilesAreStale = true;
	}


	/**
	 * @return The scripts specifically associated with this level
	 */
	public UserScriptCollection getScripts() {
		return levelScripts;
	}


	/**
	 * @return The magical whitelist governing all lua bindings to java code
	 */
	public Whitelist getWhitelist() {
		return sharedWhitelist;
	}


	public UserScriptCollection getLevelScripts() {
		return levelScripts;
	}


	public UserScriptCollection getBotScripts() {
		return botScripts;
	}


	/**Sets the world's collection of level scripts as indicated.*/
	public void setScripts(UserScript[] newScripts) {
		this.levelScripts.clear();
		for (UserScript is : newScripts)
			this.levelScripts.add(is);
	}


	@BindTo("totalValue")
	@Bind(value = SecurityLevel.NONE, doc = "Query the total value of Treasure and Items found in the World")
	public Integer getTotalValue() {
		return entities.parallelStream()
				.filter(e -> HasInventory.class.isAssignableFrom(e.getClass()))
				.map(e -> HasInventory.class.cast(e).getInventory().getTotalValue())
				.reduce(0, (a,b) -> a + b);
	}

	@Bind(value = SecurityLevel.AUTHOR,
			doc = "Finds and returns the first entity with the specified name or id")
	public Entity findEntity(@Doc("Name is a String, ID is a number") LuaValue nameOrId) {
		return entities.stream()
				.filter(nameOrId.isnumber() ?
						(Entity e) -> e.getId() == nameOrId.checkint() :
						(Entity e) -> e.getName().equals(nameOrId.checkjstring()))
				.findFirst()
				.orElse(null);
	}

	public void registerMessageListener(final MessageListener messageListener) {
		this.messageListener = messageListener;
	}


	/**
	 * @param object
	 */
	public void registerPoptartListener (Consumer<GameplayScreen.Poptart> p) {
		this.poptartListener = p;
		
	}

	public void message(HasImage src, String message, LoggingLevel level) {
		if(messageListener != null) {
			// Catch any and all exceptions that may be thrown when attempting to log information
			try {
				messageListener.message(src, message, level);
			}
			catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}

	@Bind(value = SecurityLevel.AUTHOR, doc = "Logs a message with a Quest logging level")
	public void logQuest(@Doc("The message to Log") LuaValue message) {
		message(this, message.checkjstring(), LoggingLevel.QUEST);
	}

	private static final Image WORLD_TEXTURE =
			AssetManager.getTextureRegion("DawnLike/Items/Scroll.png", 1, 0).toImage();

	@Override
	public Image getImage() {
		return WORLD_TEXTURE;
	}

	public boolean fillIfPit(int x, int y) {
		if(tiles[x][y].getType().getName().equals("pit")) {
			setTile(x,y, tileTypesCollection.getTile("fillpit"));
			return true;
		}
		return false;
	}

	private ConcurrentLinkedDeque<Entity> getToRemove() {
		if(toRemove == null) {
			toRemove = new ConcurrentLinkedDeque<>();
		}
		return toRemove;
	}

	public void queueRemove(final Entity e) {
		getToRemove().add(e);
	}



	private HashMap<String, SecurityLevel> permissions = new HashMap<String, SecurityLevel>();


	/**Returns the permissions associated with this World.  Does not reference the whitelist, but
	 * references things like:  can the REPL be accessed through this entity?  Etc*/
	public SecurityLevel getPermission(String name) {
		if (permissions == null)
			permissions = new HashMap<String, SecurityLevel>();
		SecurityLevel s = permissions.get(name);
		if (s == null)
			return SecurityLevel.NONE;
		return s;
	}


	/**Sets the permissions associated with this World.  Does not reference the whitelist, but
	 * references things like:  can the REPL be accessed through this entity?  Etc*/
	public void setSecurityLevel(String name, SecurityLevel level) {
		if (permissions == null)
			permissions = new HashMap<String, SecurityLevel>();
		this.message(this, "Changing security level of  "+name+" to "+level.name(), LoggingLevel.DEBUG);
		permissions.put(name, level);
	}
}
