package com.undead_pixels.dungeon_bots.scene;

import java.util.HashMap;

import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import com.undead_pixels.dungeon_bots.math.Vector2;
import com.undead_pixels.dungeon_bots.nogdx.Texture;
import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import org.luaj.vm2.LuaValue;

/**
 * A collection of TileType's
 */
public class TileTypes implements GetLuaFacade {

	/**
	 * Internal storage
	 */
	private HashMap<String, TileType> typeMap = new HashMap<>();
	
	/**
	 * Lazily-loaded luaValue for this object
	 */
	private LuaValue luaValue;
	
	/**
	 * Creates some default tiles (such as walls and floors)
	 */
	public TileTypes() {
		super();


		// TODO - visually test these all at some point
		Vector2[] offsetsWalls = new Vector2[] {
				new Vector2(1, 1), // 0 default
				new Vector2(1, 0), // 1 only left
				new Vector2(1, 0), // 2 only right
				new Vector2(1, 0), // 3 only left+right
				new Vector2(0, 1), // 4 only up
				new Vector2(2, 2), // 5 only up+left
				new Vector2(0, 2), // 6 only up+right
				new Vector2(4, 2), // 7 no down
				new Vector2(0, 1), // 8 only down
				new Vector2(2, 0), // 9 only down+left
				new Vector2(0, 0), // A only down+right
				new Vector2(4, 0), // B no up
				new Vector2(0, 1), // C only down+up
				new Vector2(5, 1), // D no right
				new Vector2(3, 1), // E no left
				new Vector2(4, 1), // F all
		};

		Vector2[] offsetsFloors = new Vector2[] {
				new Vector2(5, 0), // 0 default
				new Vector2(6, 1), // 1 only left
				new Vector2(4, 1), // 2 only right
				new Vector2(5, 1), // 3 only left+right
				new Vector2(3, 2), // 4 only up
				new Vector2(2, 2), // 5 only up+left
				new Vector2(0, 2), // 6 only up+right
				new Vector2(1, 2), // 7 no down
				new Vector2(3, 0), // 8 only down
				new Vector2(2, 0), // 9 only down+left
				new Vector2(0, 0), // A only down+right
				new Vector2(1, 0), // B no up
				new Vector2(3, 1), // C only down+up
				new Vector2(2, 1), // D no right
				new Vector2(0, 1), // E no left
				new Vector2(1, 1), // F all
		};
		
		final int TILESIZE = 16;
		
		// register some default tile types
		// TODO - how do we handle this if we're running 'headless' (for testing)

		registerTile("floor", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 0, 3, offsetsFloors, false, false);
		registerTile("wall", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 3, offsetsWalls, false, true);
	}

	@Bind @BindTo("new")
	public static LuaValue generate() {
		TileTypes tileTypes = new TileTypes();
		SecurityContext.getWhitelist().add(tileTypes);
		return LuaProxyFactory.getLuaValue(tileTypes);
	}

	/**
	 * @param name			The tile's name
	 * @param texture		A texture
	 * @param tilesize		Size of each tile
	 * @param x				X coordinate (in tile space) of the main variation of this tile
	 * @param y				Y coordinate (in tile space) of the main variation of this tile
	 * @param solid			True if this tile cannot be walked through
	 */
	public void registerTile(String name, Texture texture, int tilesize, int x, int y, boolean solid) {
		registerTile(name, texture, tilesize, x, y, new Vector2[] {new Vector2()}, true, solid);
	}

	/**
	 * @param name			The tile's name
	 * @param texture		A texture
	 * @param tilesize		Size of each tile
	 * @param x				X coordinate (in tile space) of the main variation of this tile
	 * @param y				Y coordinate (in tile space) of the main variation of this tile
	 * @param variations		An array indicating relative variations of this tile
	 * 							(other ways this same tile might be rendered based on circumstances)
	 * @param random			If the variations are random or should be based on nearby tiles. "Nearby" (false)
	 * 							means that the array should have 16 indices, each referring to bitwise-or of
	 * 							Left=1, Right=2, Up=4, Down=8, where a 1 for each bit indicates that the given
	 * 							tile is the same.
	 * @param solid			True if this tile cannot be walked through
	 */
	public void registerTile(String name, Texture texture, int tilesize, int x, int y, Vector2[] variations, boolean random, boolean solid) {
		int len = variations.length;
		TextureRegion[] regions = new TextureRegion[len];
		
		for(int i = 0; i < len; i++) {
			//regions[i] = new TextureRegion(new Texture("DawnLike/Objects/Floor.png"), ts*1, ts*4, ts, ts);
			if(texture == null) {
				regions[i] = null;
			} else {
				regions[i] = new TextureRegion(texture, (int)(tilesize*(x+variations[i].x)), (int)(tilesize*(y+variations[i].y)), tilesize, tilesize);
			}
		}
		
		
		typeMap.put(name, new TileType(regions, name, random, solid));
	}

	@Bind
	public TileType getTile(LuaValue luaValue) {
		return getTile(luaValue.checkjstring());
	}

	public TileType getTile(String name) {
		return typeMap.get(name);
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public String getName() {
		return "tileTypes";
	}

	@Override
	public LuaValue getLuaValue() {
		if(this.luaValue == null) {
			this.luaValue = LuaProxyFactory.getLuaValue(this);
		}
		return this.luaValue;
	}
}
