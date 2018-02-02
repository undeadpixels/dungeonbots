package com.undead_pixels.dungeon_bots.scene;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Stream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.scene.entities.actions.ActionGroupings;
import com.undead_pixels.dungeon_bots.scene.entities.actions.ActionQueue;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaSandbox;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.script.ScriptStatus;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * The World of the game.
 * Controls pretty much everything in the entire level, but could get reset/rebuilt if the level is restarted.
 * 
 * TODO - some parts of this should persist between the resets/rebuilds, but some parts shouldn't.
 * Need to figure out what parts.
 */
public class World implements GetLuaFacade, GetLuaSandbox {
	
    /**
     * The script that defines this world
     */
    private LuaScript levelScript;
    
    /**
     * A lazy-loaded LuaValue representation of this world
     */
    private LuaValue luaBinding;
    
	/**
	 * The LuaFunction to call on every update
	 */
	private LuaFunction mapUpdateFunc;
	
	/**
	 * The sandbox that the levelScript runs inside of
	 */
	private LuaSandbox mapSandbox;

    /**
     * The of this world (may be user-readable)
     */
    private String name = "world";

	/**
	 * A background image for this world
	 */
	private Texture backgroundImage;
	
	/**
	 * An array of tiles, in the bottom layer of this world
	 * This array is generated from the tileTypes array.
	 * 
	 * TODO - probably fix that eventually.
	 */
	private Tile[][] tiles;
	
	/**
	 * An array of TileType's. Used to generate the array of tiles.
	 */
	private TileType[][] tileTypes;
	
	/**
	 * The collection of available TileType's
	 */
	private TileTypes tileTypesCollection;
	
	/**
	 * Indication of if the tile array needs to be refreshed
	 */
	private boolean tilesAreStale = false;
	
    /**
     * Collection of all entities in this world
     */
    private ArrayList<Entity> entities = new ArrayList<>();
    
    /**
     * The player object
     */
    private Player player;
    
    /**
     * An id counter, used to hand out id's to entities
     * TODO - see if this conflicts with anything Stewart is doing
     */
    private int idCounter = 0;
    
    /**
     * The playstyle of this world
     * TODO - add a lua binding to be able to configure this from the level script
     */
    private ActionGroupings playstyle = new ActionGroupings.RTSGrouping();

	/**
	 *
	 */
	private String defaultScript;

	/**
	 * Simple constructor
	 */
	public World() {
		this(null, "world");
	}

	/**
	 * Constructs this world from a lua script
	 * 
	 * @param luaScriptFile	The level script
	 */
	public World(File luaScriptFile) {
		this(luaScriptFile, "world");
	}

	/**
	 * Constructs this world with a name
	 * 
	 * @param name	The name
	 */
	public World(String name) {
			this(null, name);
	}
	
	/**
	 * Constructs a world
	 * 
	 * @param luaScriptFile	The level script
	 * @param name	The name
	 */
	public World(File luaScriptFile, String name) {
		super();
		
		this.name = name;
   	 	backgroundImage = null;
   	 	tiles = new Tile[0][0];
		
		if(luaScriptFile != null) {
			tileTypesCollection = new TileTypes();
			
			AssetManager.loadAsset(AssetManager.AssetSrc.Player, Texture.class);
			AssetManager.finishLoading();
			
			mapSandbox = new LuaSandbox(SecurityLevel.DEBUG);
			mapSandbox.addBindable(this, tileTypesCollection, this.getWhitelist()).addBindableClass(Player.class);
			levelScript = mapSandbox.script(luaScriptFile).start().join();
			assert levelScript.getStatus() == ScriptStatus.COMPLETE && levelScript.getResults().isPresent();
			LuaTable tbl = levelScript.getResults().get().checktable(1);
			LuaFunction init = tbl.get("init").checkfunction();
			LuaFunction mapUpdate = tbl.get("update").checkfunction();
			
			mapUpdateFunc = mapUpdate;
			
			init.invoke();
		}
	}

	@Bind @BindTo("new")
    public static LuaValue newWorld() {
    		World w = new World();
    		SecurityContext.getWhitelist().add(w);
		return LuaProxyFactory.getLuaValue(w);
	}

	@Bind
	public void win() {
		System.out.println("A winner is you");
	}

	@Bind
	public void setPlayer(LuaValue luaPlayer) {
		Player p = (Player) luaPlayer.checktable().get("this").checkuserdata(Player.class);
		player = p;
	}

    // TODO - another constructor for specific resource paths
    
    
	/**
	 * Updates this world and all children
	 * 
	 * @param dt	Delta time
	 */
	public void update(float dt) {
		
		// update tiles from tileTypes, if dirty
		refreshTiles();

		// update tiles
		for(Tile[] ts : tiles) {
			for(Tile t : ts) {
				if(t != null) {
					t.update(dt);
				}
			}
		}
		
		// update entities
		for(Entity e : entities) {
			ActionQueue aq = e.getActionQueue();
			playstyle.dequeueIfAllowed(aq);
			e.update(dt);
		}
		playstyle.update();

		// update level script
		Whitelist temp = SecurityContext.getWhitelist();
		if(mapUpdateFunc != null) {
			SecurityContext.set(this.mapSandbox);
			mapUpdateFunc.invoke(LuaValue.valueOf(dt));
		}
	}
	
	/**
	 * Render this world and all children
	 * 
	 * @param batch	a SpriteBatch
	 */
	public void render(SpriteBatch batch) {
		refreshTiles();
		//System.out.println("Rendering world");
		
		//cam.translate(w/2, h/2);
		
		// TODO - probably use a better background color once we have things stable
		Gdx.gl.glClearColor(.65f, .2f, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// draw background image
		batch.begin();
		if(backgroundImage != null) {
			batch.draw(backgroundImage, 0, 0);
		}

		// draw tiles
		for(Tile[] ts : tiles) {
			for(Tile t : ts) {
				if(t != null) {
					t.render(batch);
				}
			}
		}

		// draw each layer of entities
		for(Layer layer : toLayers()) {
			for(Entity e : layer.getEntities()) {
				e.render(batch);
			}
		}
		batch.end();
	}

	@Bind
	public void addEntity(LuaValue v) {
    	Entity e = (Entity) v.checktable().get("this").checkuserdata(Entity.class);
    	addEntity(e);
	}

	/**
	 * Adds an entity
	 * 
	 * @param e	The entity to add
	 */
	public void addEntity(Entity e) {
		entities.add(e);
	}


	@Bind
	public void setSize(LuaValue w, LuaValue h) {
    	setSize(w.checkint(), h.checkint());
	}

	/**
	 * Sets this world's size
	 * Calls to set tiles outside of the world's size (or before the world's size is set) may cause issues.
	 * 
	 * @param w	the width, in tiles
	 * @param h	the height, in tiles
	 */
	public void setSize(int w, int h) {
		// TODO - copy old tiles?
		tiles = new Tile[w][h];
		tileTypes = new TileType[w][h];
	}

	/**
	 * @return	The size of this world, in tiles
	 */
	public Vector2 getSize() {
		return new Vector2(tiles.length, tiles[0].length);
	}

	/**
	 * Update tile sprites, if they're stale
	 */
	public void refreshTiles() {
		if(tilesAreStale) {
			
			int w = tiles.length;
			int h = tiles[0].length;
			for(int i = 0; i < tiles.length; i++) {
				for(int j = 0; j < tiles.length; j++) {
					TileType current = tileTypes[i][j];
					
					if(current != null) {
						TileType l = i >= 1   ? tileTypes[i-1][j] : null;
						TileType r = i <  w-1 ? tileTypes[i+1][j] : null;
						TileType u = j <  h-1 ? tileTypes[i][j+1] : null;
						TileType d = j >= 1   ? tileTypes[i][j-1] : null;

						Tile t = new Tile(this, current.getName(), current.getTexture(l, r, u, d), i, j, current.isSolid());
						
						System.out.print(current.isSolid() ? "#" : ".");
						tiles[i][j] = t;
					}
				}
				
				System.out.println();
			}
			
			tilesAreStale = false;
		}
	}

	@Bind
	public void setTile(LuaValue x, LuaValue y, LuaValue tt) {
    	TileType tileType = (TileType) tt.checktable().get("this").checkuserdata(TileType.class);
    	setTile(x.checkint() - 1, y.checkint() - 1, tileType);
	}

	@Bind
	public Player getPlayer() {
    	SecurityContext.getWhitelist().add(this.player);
    	return this.player;
	}

	/**
	 * Sets a specific tile
	 * 
	 * @param x	The x location, in tiles
	 * @param y	The y location, in tiles
	 * @param tileType	The type of the tile
	 */
	public void setTile(int x, int y, TileType tileType) {
		// TODO - bounds checking
		// TODO - more stuff here
		tilesAreStale = true;
		tileTypes[x][y] = tileType;
	}
	
	/**
	 * @return	A list of layers, representing all actors
	 */
	private ArrayList<Layer> toLayers() {
		HashMap<Float, Layer> layers = new HashMap<>();
		
		for(Entity e : entities) {
			float z = e.getZ();
			
			Layer l = layers.get(z);
			if(l == null) {
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
		if(this.luaBinding == null) {
			this.luaBinding = LuaProxyFactory.getLuaValue(this);
		}
		return this.luaBinding;
	}

	@Override
	public int getId() {
		return this.hashCode();
	}


	/**
	 * A class to represent a collection of actors at a given Z-value
	 * Used to draw some things on top of other things.
	 * 
	 * TODO - refactor this somewhere better
	 */
	private static class Layer implements Comparable<Layer> {
		/**
		 * The z value
		 */
		private final float z;
		
		/**
		 * Constructor
		 * @param z
		 */
		public Layer(float z) {
			super();
			this.z = z;
		}

		/**
		 * Internal storage
		 */
		private ArrayList<Entity> entities = new ArrayList<Entity>();

		@Override
		public int compareTo(Layer o) {
			if(z == o.z) {
				return 0;
			} else if(z < o.z) {
				return -1;
			} else {
				return 1;
			}
		}
		
		/**
		 * @param e	The entity to add
		 */
		public void add(Entity e) {
			entities.add(e);
		}
		
		/**
		 * @return	A list of all entities in this layer
		 */
		public ArrayList<Entity> getEntities() {
			return entities;
		}
		
	}

	/**
	 * Generates an id
	 * @return	a new id
	 */
	public int makeID() {
		return idCounter++;
	}
	
	/**
	 * Asks if an entity is allowed to move to a given tile.
	 * Locks that tile to be owned by the given entity if it is allowed.
	 * 
	 * @param e	The entity asking
	 * @param x	Location X, in tiles
	 * @param y	Location Y, in tiles
	 * @return	True if the entity is allowed to move to this location
	 */
	public boolean requestMoveToNewTile(Entity e, int x, int y) {
		if(x < 0 || y < 0) {
			System.out.println("Unable to move: x/y too small");
			return false;
		}
		if(x >= tiles.length || y >= tiles[0].length) {
			System.out.println("Unable to move: x/y too big");
			return false;
		}

		if(tiles[x][y] != null && tiles[x][y].isSolid()) {
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
	 * @param e	The entity releasing the tile
	 * @param x	Location X, in tiles
	 * @param y	Location Y, in tiles
	 */
	public void didLeaveTile(Entity e, int x, int y) {
		// TODO
	}
	
	/**
	 * Gets what entity is occupying a given tile
	 * 
	 * @param x	Location X, in tiles
	 * @param y	Location Y, in tiles
	 * @return	The entity under the given location
	 */
	public Entity getEntityUnderLocation(float x, float y) {
		for(Entity e : entities) {
			Vector2 p = e.getPosition();
			
			if(x < p.x || x > p.x+1) {
				continue;
			}

			if(y < p.y || y > p.y+1) {
				continue;
			}
			
			return e;
		}
		
		return null;
	}

	@Override
	public LuaSandbox getSandbox() {
		return this.mapSandbox;
	}

	/**
	 * @return	The types of tiles available
	 */
	public TileTypes getTileTypes() {
		if(tileTypesCollection == null) {
			tileTypesCollection = new TileTypes();
		}
		return tileTypesCollection;
	}

	public String getDefaultScript() {
		return defaultScript != null ? defaultScript : "";
	}

	public void setDefaultScript(String defaultScript) {
		this.defaultScript = defaultScript;
	}

	@Bind
	public void setLevelScript(LuaValue luaValue) {
		setDefaultScript(luaValue.checkjstring());
	}
}
