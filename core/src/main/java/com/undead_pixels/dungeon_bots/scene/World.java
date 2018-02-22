package com.undead_pixels.dungeon_bots.scene;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.nogdx.SpriteBatch;
import com.undead_pixels.dungeon_bots.nogdx.Texture;
import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.scene.entities.actions.ActionGrouping;
import com.undead_pixels.dungeon_bots.scene.entities.actions.ActionQueue;
import com.undead_pixels.dungeon_bots.scene.level.Level;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.annotations.UserScript;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaSandbox;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.ui.screens.ResultsScreen;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.*;

/**
 * The World of the game. Controls pretty much everything in the entire level,
 * but could get reset/rebuilt if the level is restarted.
 * 
 * TODO - some parts of this should persist between the resets/rebuilds, but
 * some parts shouldn't. Need to figure out what parts.
 */
public class World implements GetLuaFacade, GetLuaSandbox, GetState, Serializable {

	private transient ReentrantLock updateLock = new ReentrantLock();

	/**
	 * The script that defines this world
	 */
	// private String levelScript;
	private UserScript levelScript;

	/** TODO: a script to be called when win() is invoked. */
	private UserScript onWinScript;

	/** The LuaBindings to the World Lazy initialized */
	private transient LuaValue luaValue;

	/** The sandbox that the levelScript runs inside of */
	private transient LuaSandbox mapSandbox;

	/** The level pack of which this World is a part. */
	private transient LevelPack levelPack = null;

	/** The of this world (may be user-readable) */
	private String name = "world";

	// =============================================
	// ====== World CTOR AND STARTUP STUFF
	// =============================================

	// =============================================
	// ====== World TILE MANAGEMENT STUFF
	// =============================================

	/** A background image for this world */
	private TextureRegion backgroundImage;

	/**
	 * An array of tiles, in the bottom layer of this world. This array is
	 * generated from the tileTypes array.
	 */
	private Tile[][] tiles;

	/** The collection of available TileType's */
	private TileTypes tileTypesCollection;

	/** Indication of if the tile array needs to be refreshed */
	private transient boolean tilesAreStale = true;

	/** Collection of all entities in this world */
	private ArrayList<Entity> entities = new ArrayList<>();

	// TODO: Is it worthwhile to have a ref to player object? Isn't it just an
	// entity among the other entities?
	/** The player object */
	@State
	private Player player;

	// TODO: specify the goal position with a goal entity?
	private Integer[] goalPosition = new Integer[] {};

	/** WO: for performance stats reporting? */
	@State
	private int timesReset = 0;

	/**
	 * An id counter, used to hand out id's to entities TODO - see if this
	 * conflicts with anything Stewart is doing. WO: for now, this can be
	 * transient?
	 */
	private transient int idCounter = 0;

	/**
	 * The playstyle of this world TODO - add a lua binding to be able to
	 * configure this from the level script WO: I think this is generated during
	 * play, so no need to serialize.
	 */
	private transient ActionGrouping playstyle = new ActionGrouping.RTSGrouping();

	/**
	 *
	 */
	private transient Level level;

	/**
	 * Simple constructor
	 */
	public World() {
		this(null, "world");
		tileTypesCollection = new TileTypes();
	}

	/**
	 * Constructs this world from a lua script
	 * 
	 * @param luaScriptFile
	 *            The level script
	 */
	public World(File luaScriptFile) {
		this(luaScriptFile, "world");
		tileTypesCollection = new TileTypes();
	}

	/**
	 * Constructs this world with a name
	 * 
	 * @param name
	 *            The name
	 */
	public World(String name) {
		this(null, name);
		tileTypesCollection = new TileTypes();
	}

	/**
	 * Constructs a world
	 * 
	 * @param luaScriptFile
	 *            The level script
	 * @param name
	 *            The name
	 */
	public World(File luaScriptFile, String name) {
		this.name = name;
		backgroundImage = null;
		tiles = new Tile[0][0];

		if (luaScriptFile != null) {
			tileTypesCollection = new TileTypes();

			AssetManager.loadAsset(AssetManager.AssetSrc.Player, Texture.class);
			AssetManager.finishLoading();

			mapSandbox = new LuaSandbox(SecurityLevel.DEBUG);
			mapSandbox.addBindable(this, tileTypesCollection, this.getWhitelist()).addBindableClass(Player.class);
			LuaInvocation initScript = mapSandbox.init(luaScriptFile).join();
			levelScript = new UserScript("init", initScript.getScript());
			onWinScript = new UserScript("onWin", "--do nothing.", SecurityLevel.AUTHOR);
			// levelScript = initScript.getScript();
			assert initScript.getStatus() == ScriptStatus.COMPLETE && initScript.getResults().isPresent();
			level = new Level(initScript.getResults().get(), mapSandbox);
			level.init();
			assert player != null;
			player.getSandbox().addBindable(this, player.getSandbox().getWhitelist(), tileTypesCollection);
		}
	}

	private void worldSomewhatInit() {
		// TODO: "somewhat" init?
		AssetManager.loadAsset(AssetManager.AssetSrc.Player, Texture.class);
		AssetManager.finishLoading();

		mapSandbox = new LuaSandbox(SecurityLevel.DEBUG);
		System.out.println(this + ",     " + tileTypesCollection + ",     " + this.getWhitelist());
		mapSandbox.addBindable(this, tileTypesCollection, this.getWhitelist()).addBindableClass(Player.class);
		LuaInvocation initScript = mapSandbox.init(levelScript.code).join();
		System.out.println(initScript.getStatus());
		assert initScript.getStatus() == ScriptStatus.COMPLETE;
		assert initScript.getResults().isPresent();
		level = new Level(initScript.getResults().get(), mapSandbox);
		level.init();
		assert player != null;
		player.getSandbox().addBindable(this, player.getSandbox().getWhitelist(), tileTypesCollection);
	}

	// =============================================
	// ====== World BINDABLE METHODS
	// =============================================

	@Bind(SecurityLevel.AUTHOR)
	@BindTo("new")
	public static LuaValue newWorld() {
		World w = new World();
		SecurityContext.getWhitelist().add(w);
		return LuaProxyFactory.getLuaValue(w);
	}

	/**
	 * Instantly causes win to occur.
	 */
	@Bind(SecurityLevel.AUTHOR)
	public void win() {
		DungeonBotsMain.instance.setCurrentScreen(DungeonBotsMain.ScreenType.RESULTS);
	}

	public void setPlayer(Player p) {
		player = p;
		// entities.add(p);
	}

	/**
	 * Gets the object of class Player that exists in the Lua sandbox and sets
	 * the World's player reference to that.
	 * 
	 * @param luaPlayer
	 */
	@Bind(SecurityLevel.AUTHOR)
	public void setPlayer(LuaValue luaPlayer) {
		Player p = (Player) luaPlayer.checktable().get("this").checkuserdata(Player.class);
		setPlayer(p);
	}

	// =============================================
	// ====== World GAME LOOP
	// =============================================

	// TODO - another constructor for specific resource paths?

	/**
	 * Updates this world and all children. Update means.... ?
	 * 
	 * @param dt
	 *            Delta time
	 */
	public void update(float dt) {
		updateLock.lock();
		try {
			// update tiles from tileTypes, if dirty
			refreshTiles();

			// update tiles
			for (Tile[] ts : tiles) {
				for (Tile t : ts) {
					if (t != null) {
						t.update(dt);
					}
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
			if (level != null)
				level.update();
		} finally {
			updateLock.unlock();
		}
	}

	/**
	 * Render this world and all children
	 * 
	 * @param batch
	 *            a SpriteBatch
	 */
	public void render(SpriteBatch batch) {
		refreshTiles();

		// System.out.println("Rendering world");

		// cam.translate(w/2, h/2);

		// clear to black
		batch.setClearColor(new Color(.0f, .0f, .0f, 1));
		batch.clearContext();

		// draw background image
		batch.begin();
		if (backgroundImage != null) {
			batch.draw(backgroundImage, 0, 0);
		}

		// draw tiles
		for (Tile[] ts : tiles) {
			for (Tile t : ts) {
				if (t != null) {
					t.render(batch);
				}
			}
		}
		batch.end();

		// draw each layer of entities
		for (Layer layer : toLayers()) {
			batch.begin();
			for (Entity e : layer.getEntities()) {
				e.render(batch);
			}
			batch.end();
		}
	}

	@Bind
	public void addEntity(LuaValue v) {
		Entity e = (Entity) v.checktable().get("this").checkuserdata(Entity.class);
		addEntity(e);
	}

	/**
	 * Adds an entity
	 * 
	 * @param e
	 *            The entity to add
	 */
	public void addEntity(Entity e) {
		entities.add(e);
	}

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
		// TODO - copy old tiles?
		tiles = new Tile[w][h];
	}

	/**
	 * @return The size of this world, in tiles
	 */
	public Point2D.Float getSize() {
		return new Point2D.Float(tiles.length, tiles[0].length);
	}

	/**
	 * Update tile sprites, if they're stale
	 */
	public void refreshTiles() {
		if (tilesAreStale) {

			int w = tiles.length;
			int h = tiles[0].length;
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					Tile current = tiles[i][j];

					if (current != null) {
						Tile l = i >= 1 ? tiles[i - 1][j] : null;
						Tile r = i < w - 1 ? tiles[i + 1][j] : null;
						Tile u = j < h - 1 ? tiles[i][j + 1] : null;
						Tile d = j >= 1 ? tiles[i][j - 1] : null;

						current.updateTexture(l, r, u, d);
					}
				}

				// System.out.println();
			}

			tilesAreStale = false;
		}
	}

	/**
	 *
	 * @param x
	 * @param y
	 * @param tt
	 */
	@Bind(SecurityLevel.AUTHOR)
	public void setTile(LuaValue x, LuaValue y, LuaValue tt) {
		TileType tileType = (TileType) tt.checktable().get("this").checkuserdata(TileType.class);
		setTile(x.checkint() - 1, y.checkint() - 1, tileType);
	}

	/**
	 *
	 * @return
	 */
	@Bind
	public Player getPlayer() {
		return this.player != null ? this.player : new Player(this, "player");
	}

	@Bind(SecurityLevel.DEFAULT)
	public Boolean isBlocking(LuaValue lx, LuaValue ly) {
		final int x = lx.checkint() - 1;
		final int y = ly.checkint() - 1;
		final int w = tiles.length;
		final int h = tiles[0].length;
		return x >= 0 && x <= w - 1 && y >= 0 && y <= h - 1 && tiles[x][y].isSolid();
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
		// TODO - more stuff here

		if (x < 0 || y < 0 || x >= tiles.length || y >= tiles[0].length) {
			return; // out of bounds; TODO - should something else happen?
		}

		if (tileType.getName().equals("goal"))
			setGoal(x, y);
		tilesAreStale = true;

		if (tiles[x][y] == null) {
			tiles[x][y] = new Tile(this, tileType, x, y);
		} else {
			tiles[x][y].setType(tileType);
		}
	}

	/**
	 * Returns the tile at the given location. If outside the world boundaries,
	 * returns null.
	 */
	public Tile getTile(int x, int y) {
		if (x < 0 || x >= tiles.length)
			return null;
		if (y < 0 || y >= tiles[x].length)
			return null;
		return tiles[x][y];
	}

	/**
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

	@Override
	public String getName() {
		return name;
	}

	@Override
	public LuaValue getLuaValue() {
		if (this.luaValue == null) {
			this.luaValue = LuaProxyFactory.getLuaValue(this);
		}
		return this.luaValue;
	}

	@Override
	public int getId() {
		return this.hashCode();
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
			System.out.println("Unable to move: x/y too small");
			return false;
		}
		if (x >= tiles.length || y >= tiles[0].length) {
			System.out.println("Unable to move: x/y too big");
			return false;
		}

		if (tiles[x][y] != null && tiles[x][y].isSolid()) {
			System.out.println("Unable to move: tile solid");
			return false;
		}

		// TODO - check if other entities own that spot
		// TODO - tell the world that this entity owns that spot

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
		// TODO: what is this?
	}

	/**
	 * Gets what entity is occupying a given tile
	 * 
	 * @param x
	 *            Location X, in tiles
	 * @param y
	 *            Location Y, in tiles
	 * @return The entity under the given location
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

			return e;
		}

		return null;
	}

	@Override
	/**
	 * The returns the sandbox associated with calls to the world. The init
	 * script runs here.
	 */
	public LuaSandbox getSandbox() {
		return this.mapSandbox;
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
	 *
	 */
	public synchronized void reset() {
		updateLock.lock();
		try {
			timesReset++;
			// levelScript = null;
			// tiles = new Tile[0][0];
			// entities.clear();
			// backgroundImage = null;
			level.init();
		} finally {
			updateLock.unlock();
		}
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Map<String, Object> getState() {
		final Map<String, Object> state = new HashMap<>();
		state.put("Times Reset", timesReset);
		state.put("Steps", player.steps());
		state.put("Bumps", player.bumps());
		state.put("Health", player.getHealth());
		state.put("Mana", player.getMana());
		state.put("Stamina", player.getStamina());
		return state;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String getMapScript() {
		String script = "tbl = {}\n" + "tbl.init = function()\n%s\n\tend\n" + "tbl.update = function(dt)\n%s\n\tend\n"
				+ "return tbl";
		return String.format(script, createInit(), createUpdate());
	}

	private String put(String... a) {
		return Stream.of(a).reduce("", (c, d) -> c + "\n" + d);
	}

	private String createUpdate() {
		StringBuilder ans = new StringBuilder();
		ans.append(put("\t\tlocal x, y = world:getPlayer():position()",
				String.format("" + "\t\tif x == %d and y == %d then", goalPosition[0] + 1, goalPosition[1] + 1),
				"\t\t\tworld.win()", "\t\tend"));
		return ans.toString();
	}

	private String createInit() {
		final StringBuilder ans = new StringBuilder();
		final int width = tiles.length;
		final int height = tiles[0].length;
		ans.append(put(String.format("\t\tworld:setSize(%d,%d)", width, height)));
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				Tile t = tiles[i][j];
				ans.append(put(String.format("\t\tworld:setTile(%d, %d, tileTypes:getTile(\"%s\"))", i + 1, j + 1,
						t.getName())));
			}
		}
		Point2D.Float pos = player.getPosition();
		ans.append(String.format("local player = Player.new(world, %d, %d)", (int) pos.x + 1, (int) pos.y + 1));
		ans.append(String.format("player.setDefaultCode(\"%s\")", player.getDefaultCode()));
		ans.append(put("\t\tworld:setPlayer(player)"));
		return ans.toString();
	}

	/**
	 *
	 * @return
	 */
	public Integer[] goal() {
		return goalPosition;
	}

	/**
	 *
	 * @param lx
	 * @param ly
	 */
	@Bind(SecurityLevel.AUTHOR)
	public void setGoal(LuaValue lx, LuaValue ly) {
		setGoal(lx.checkint() - 1, ly.checkint() - 1);
	}

	/**
	 *
	 * @return
	 */
	@Bind(SecurityLevel.DEFAULT)
	public Varargs getGoal() {
		Integer[] goal = goal();
		return LuaValue.varargsOf(new LuaValue[] { LuaValue.valueOf(goal[0] + 1), LuaValue.valueOf(goal[1] + 1) });
	}

	/**
	 *
	 * @return
	 */
	public Integer[] getGoalPosition() {
		return goalPosition;
	}

	/**
	 *
	 * @param x
	 * @param y
	 */
	public void setGoal(int x, int y) {
		Integer[] newGoal = new Integer[] { x, y };
		if (!Arrays.equals(newGoal, goalPosition) && goalPosition.length == 2) {
			setTile(goalPosition[0], goalPosition[1], tileTypesCollection.getTile("floor"));
		}
		goalPosition = newGoal;

	}

	@Bind(SecurityLevel.DEFAULT)
	public void alert(LuaValue alert, LuaValue title) {
		showAlert(alert.checkjstring(), title.checkjstring());
	}

	/**
	 *
	 * @param alert
	 * @param title
	 */
	public void showAlert(String alert, String title) {
		Thread t = new Thread(() -> JOptionPane.showMessageDialog(null, alert, title, JOptionPane.INFORMATION_MESSAGE));
		t.start();
	}

	@Bind(SecurityLevel.AUTHOR)
	public void openBrowser(LuaValue lurl) {
		try {
			java.awt.Desktop.getDesktop().browse(new URI(lurl.checkjstring()));
		} catch (Exception e1) {
			throw new LuaError("Invalid URL!");
		}
	}

	@Bind(SecurityLevel.AUTHOR)
	public void showAlert(LuaValue alert, LuaValue title) {
		showAlert(alert.tojstring(), title.tojstring());
	}

	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		this.worldSomewhatInit();
	}
}
