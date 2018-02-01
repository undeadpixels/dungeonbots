package com.undead_pixels.dungeon_bots.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
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
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

public class World implements GetLuaFacade, GetLuaSandbox {
    private LuaScript levelScript;
    private LuaValue luaBinding;
	private LuaFunction mapUpdateFunc;
	private LuaSandbox mapSandbox = new LuaSandbox(SecurityLevel.DEBUG);

    private String name = "world";

	private Texture backgroundImage;
	private Tile[][] tiles;
	private TileType[][] tileTypes;
	private boolean tilesAreStale = false;
    private ArrayList<Entity> entities = new ArrayList<>();
    private Player player;
    
    private Vector2 offset = new Vector2();
    
    private int idCounter = 0;
    
    private ActionGroupings playstyle = new ActionGroupings.RTSGrouping();

    public World() {
   	 	backgroundImage = null;
   	 	tiles = new Tile[0][0];
    }

    public World(String name) {
    	super();
    	this.name = name;
	}

    @Bind @BindTo("new")
    public static LuaValue newWorld() {
    	World w = new World();
    	SecurityContext.getWhitelist().add(w);
		return LuaProxyFactory.getLuaValue(w);
	}

	public void addMapUpdate(LuaFunction luaFunction) {
    	mapUpdateFunc = luaFunction;
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
    
    
	public void update(float dt) {
		refreshTiles();

		for(Tile[] ts : tiles) {
			for(Tile t : ts) {
				if(t != null) {
					t.update(dt);
				}
			}
		}
		
		for(Entity e : entities) {
			ActionQueue aq = e.getActionQueue();
			playstyle.dequeueIfAllowed(aq);
			
			e.update(dt);
		}
		playstyle.update();

		Whitelist temp = SecurityContext.getWhitelist();
		SecurityContext.set(this.mapSandbox);
		if(mapUpdateFunc != null)
			mapUpdateFunc.invoke(LuaValue.valueOf(dt));
	}
	
	public void render(SpriteBatch batch) {
		refreshTiles();
		//System.out.println("Rendering world");
		
		//cam.translate(w/2, h/2);
		
		Gdx.gl.glClearColor(.65f, .2f, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		if(backgroundImage != null) {
			batch.draw(backgroundImage, 0, 0);
		}

		for(Tile[] ts : tiles) {
			for(Tile t : ts) {
				if(t != null) {
					t.render(batch);
				}
			}
		}

		for(Layer layer : toLayers()) {
			for(Entity e : layer.entities) {
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

	public void addEntity(Entity e) {
		entities.add(e);
	}


	@Bind
	public void setSize(LuaValue w, LuaValue h) {
    	setSize(w.checkint(), h.checkint());
	}

	public void setSize(int w, int h) {
		// TODO - copy old tiles?
		tiles = new Tile[w][h];
		tileTypes = new TileType[w][h];
	}

	public Vector2 getSize() {
		// TODO - copy old tiles?
		return new Vector2(tiles.length, tiles[0].length);
	}

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

						Tile t = new Tile(this, current.getName(), current.getTexture(l, r, u, d), i, j);
						tiles[i][j] = t;
					}
				}
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

	public void setTile(int x, int y, TileType tileType) {
		// TODO - bounds checking
		// TODO - more stuff here
		tilesAreStale = true;
		tileTypes[x][y] = tileType;
	}
	
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


	private static class Layer implements Comparable<Layer> {
		private final float z;
		public Layer(float z) {
			super();
			this.z = z;
		}

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
		
		public void add(Entity e) {
			entities.add(e);
		}
		
		public ArrayList<Entity> getEntities() {
			return entities;
		}
		
	}

	public int makeID() {
		return idCounter++;
	}
	
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
	public void didLeaveTile(Entity e, int x, int y) {
		// TODO
	}
	
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
}
