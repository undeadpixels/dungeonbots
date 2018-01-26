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
import com.undead_pixels.dungeon_bots.script.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.script.SecurityContext;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.Scriptable;
import com.undead_pixels.dungeon_bots.script.interfaces.GetBindable;
import org.luaj.vm2.LuaValue;

public class World implements Scriptable, GetBindable {
    private LuaScript levelScript;
    private String name = "world";

	private Texture backgroundImage;
	private Tile[][] tiles;
	private TileType[][] tileTypes;
	private boolean tilesAreStale = false;
    private ArrayList<Entity> entities = new ArrayList<>();
    private Player player;
    
    private Vector2 offset = new Vector2();
    
    private int idCounter = 0;

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
    	if(SecurityContext.getActiveSecurityLevel() == SecurityLevel.DEBUG) {
    		SecurityContext.getWhitelist().addWhitelist(w.permissiveWhitelist());
		}
		return LuaProxyFactory.getLuaValue(w, SecurityContext.getActiveSecurityLevel());
	}

	@Bind
	public void setPlayer(LuaValue luaPlayer) {
    	Player p = (Player) luaPlayer.checktable().get("this").checkuserdata(Player.class);
    	player = p;
	}

	@Bind
	public void setTile(LuaValue luaTile) {

	}

    // TODO - another constructor for specific resource paths
    
    
	public void update(float dt) {
		refreshTiles();

		for(Tile[] ts : tiles) {
			for(Tile t : ts) {
				t.update(dt);
			}
		}
		
		for(Entity e : entities) {
			e.update(dt);
		}
		
		// TODO - tell the levelScript that a new frame happened
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
				t.render(batch);
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
			System.out.println("Refreshing tiles");
			
			int w = tiles.length;
			int h = tiles[0].length;
			for(int i = 0; i < tiles.length; i++) {
				for(int j = 0; j < tiles.length; j++) {
					System.out.println(w+" "+h+": "+i+" "+j);
					TileType l = i >= 1   ? tileTypes[i-1][j] : null;
					TileType r = i <  w-1 ? tileTypes[i+1][j] : null;
					TileType u = j <  h-1 ? tileTypes[i][j+1] : null;
					TileType d = j >= 1   ? tileTypes[i][j-1] : null;
					
					TileType current = tileTypes[i][j];
					Tile t = new Tile(this, current.getName(), null, current.getTexture(l, r, u, d), i, j);
					tiles[i][j] = t;
				}
			}
			
			tilesAreStale = false;
		}
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
	
	public boolean canMoveToNewTile(Entity e, int x, int y) {
		// TODO
		return true;
	}
	public void didLeaveTile(Entity e, int x, int y) {
		
	}
}
