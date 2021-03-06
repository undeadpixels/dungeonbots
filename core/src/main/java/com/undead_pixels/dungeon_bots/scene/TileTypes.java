package com.undead_pixels.dungeon_bots.scene;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import com.undead_pixels.dungeon_bots.nogdx.Texture;
import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import org.luaj.vm2.LuaValue;

/**
 * A collection of TileType's
 */
@Doc("A collection of Tile Types of the following varieties.\nfloor,grass,wall,pit,goal")
public class TileTypes implements GetLuaFacade, Iterable<TileType>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Internal storage
	 */
	private HashMap<String, TileType> typeMap = new HashMap<>();
	
	/**
	 * Lazily-loaded luaValue for this object
	 */
	private transient LuaValue luaValue;
	
	/**
	 * Creates some default tiles (such as walls and floors)
	 */
	public TileTypes() {
		super();


		Point2D.Float[] offsetsWalls = new Point2D.Float[] {
				new Point2D.Float(0, 1), // 0 default
				new Point2D.Float(1, 0), // 1 only left
				new Point2D.Float(1, 0), // 2 only right
				new Point2D.Float(1, 0), // 3 only left+right
				new Point2D.Float(1, 1), // 4 only up
				new Point2D.Float(2, 2), // 5 only up+left
				new Point2D.Float(0, 2), // 6 only up+right
				new Point2D.Float(4, 2), // 7 no down
				new Point2D.Float(0, 1), // 8 only down
				new Point2D.Float(2, 0), // 9 only down+left
				new Point2D.Float(0, 0), // A only down+right
				new Point2D.Float(4, 0), // B no up
				new Point2D.Float(0, 1), // C only down+up
				new Point2D.Float(5, 1), // D no right
				new Point2D.Float(3, 1), // E no left
				new Point2D.Float(4, 1), // F all
		};

		Point2D.Float[] offsetsFloors = new Point2D.Float[] {
				new Point2D.Float(5, 0), // 0 default
				new Point2D.Float(6, 1), // 1 only left
				new Point2D.Float(4, 1), // 2 only right
				new Point2D.Float(5, 1), // 3 only left+right
				new Point2D.Float(3, 2), // 4 only up
				new Point2D.Float(2, 2), // 5 only up+left
				new Point2D.Float(0, 2), // 6 only up+right
				new Point2D.Float(1, 2), // 7 no down
				new Point2D.Float(3, 0), // 8 only down
				new Point2D.Float(2, 0), // 9 only down+left
				new Point2D.Float(0, 0), // A only down+right
				new Point2D.Float(1, 0), // B no up
				new Point2D.Float(3, 1), // C only down+up
				new Point2D.Float(2, 1), // D no right
				new Point2D.Float(0, 1), // E no left
				new Point2D.Float(1, 1), // F all
		};
		
		final int TILESIZE = 16;
		
		// register some default tile types

		registerTile("floor", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 0, 6, offsetsFloors, false, false);
		registerTile("floor2", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 0, 3, offsetsFloors, false, false);
		registerTile("floor3", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 0, 9, offsetsFloors, false, false);
		registerTile("floor4", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 0, 12, offsetsFloors, false, false);

		registerTile("sand1", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 0, 15, offsetsFloors, false, false);
		registerTile("sand2", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 0, 18, offsetsFloors, false, false);
		registerTile("sand3", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 0, 21, offsetsFloors, false, false);

		registerTile("wood-floor1", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 7, 15, offsetsFloors, false, false);
		registerTile("wood-floor2", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 7, 18, offsetsFloors, false, false);
		registerTile("wood-floor3", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 7, 21, offsetsFloors, false, false);
		registerTile("wood-floor4", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 7, 24, offsetsFloors, false, false);


		registerTile("snow1", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 14, 15, offsetsFloors, false, false);
		registerTile("snow2", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 14, 18, offsetsFloors, false, false);
		registerTile("snow3", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 14, 21, offsetsFloors, false, false);

		registerTile("rock1", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 14, 3, offsetsFloors, false, false);
		registerTile("rock2", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 14, 6, offsetsFloors, false, false);
		registerTile("rock3", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 14, 9, offsetsFloors, false, false);

		registerTile("grass", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 7, 6, offsetsFloors, false, false);
		registerTile("grass2", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 7, 3, offsetsFloors, false, false);
		registerTile("grass3", AssetManager.getTexture("DawnLike/Objects/Floor.png"), TILESIZE, 7, 9, offsetsFloors, false, false);

		registerTile("tiles_big", AssetManager.getTexture("DawnLike/Objects/Tile.png"), TILESIZE, 5, 2, null, false, false);
		registerTile("tiles_small", AssetManager.getTexture("DawnLike/Objects/Tile.png"), TILESIZE, 6, 2, null, false, false);
		registerTile("tiles_diamond", AssetManager.getTexture("DawnLike/Objects/Tile.png"), TILESIZE, 7, 2, null, false, false);
		registerTile("fillpit", AssetManager.getTexture("DawnLike/Objects/Tile.png"), TILESIZE, 7, 0, null, false, false);

		registerTile("wall", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 6, offsetsWalls, false, true);
		registerTile("wall2", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 3, offsetsWalls, false, true);
		registerTile("wall3", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 9, offsetsWalls, false, true);
		registerTile("wall4", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 12, offsetsWalls, false, true);

		registerTile("stone-wall1", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 15, offsetsWalls, false, true);
		registerTile("stone-wall2", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 18, offsetsWalls, false, true);
		registerTile("stone-wall3", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 21, offsetsWalls, false, true);
		registerTile("stone-wall4", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 24, offsetsWalls, false, true);

		registerTile("ice-wall1", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 27, offsetsWalls, false, true);
		registerTile("ice-wall2", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 30, offsetsWalls, false, true);
		registerTile("ice-wall3", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 33, offsetsWalls, false, true);
		registerTile("ice-wall4", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 36, offsetsWalls, false, true);

		registerTile("solid-wall1", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 39, offsetsWalls, false, true);
		registerTile("solid-wall2", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 42, offsetsWalls, false, true);
		registerTile("solid-wall3", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 45, offsetsWalls, false, true);
		registerTile("solid-wall4", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 48, offsetsWalls, false, true);

		registerTile("alien-wall1", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 14, 27, offsetsWalls, false, true);
		registerTile("alien-wall2", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 14, 30, offsetsWalls, false, true);
		registerTile("alien-wall3", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 14, 33, offsetsWalls, false, true);
		registerTile("alien-wall4", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 14, 36, offsetsWalls, false, true);

		registerTile("wood-wall1", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 7, 3, offsetsWalls, false, true);
		registerTile("wood-wall2", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 7, 6, offsetsWalls, false, true);
		registerTile("wood-wall3", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 7, 9, offsetsWalls, false, true);
		registerTile("wood-wall4", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 7, 12, offsetsWalls, false, true);

		registerTile("mine-wall1", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 14, 3, offsetsWalls, false, true);
		registerTile("mine-wall2", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 14, 6, offsetsWalls, false, true);
		registerTile("mine-wall3", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 14, 9, offsetsWalls, false, true);
		registerTile("mine-wall4", AssetManager.getTexture("DawnLike/Objects/Wall.png"), TILESIZE, 14, 12, offsetsWalls, false, true);

		registerTile("pit", AssetManager.getTexture("DawnLike/Objects/Trap1.png"), TILESIZE, 5, 2, null, false, true);

	}

	@Bind @BindTo("new")
	public static LuaValue generate() {
		TileTypes tileTypes = new TileTypes();
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
		registerTile(name, texture, tilesize, x, y, new Point2D.Float[] {new Point2D.Float()}, true, solid);
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
	public void registerTile(String name, Texture texture, int tilesize, int x, int y, Point2D.Float[] variations, boolean random, boolean solid) {
		
		int len = 1;
		if(variations != null) {
			len = variations.length;
		}
		TextureRegion[] regions = new TextureRegion[len];
		for(int i = 0; i < len; i++) {
			//regions[i] = new TextureRegion(new Texture("DawnLike/Objects/Floor.png"), ts*1, ts*4, ts, ts);
			if(texture == null) {
				regions[i] = null;
			} else if(variations == null) {
				regions[i] = new TextureRegion(texture, (int)(tilesize*x), (int)(tilesize*y), tilesize, tilesize);
			} else {
				regions[i] = new TextureRegion(texture, (int)(tilesize*(x+variations[i].x)), (int)(tilesize*(y+variations[i].y)), tilesize, tilesize);
			}
		}
		typeMap.put(name, new TileType(regions, name, random, solid));
	}

	@Bind(SecurityLevel.AUTHOR)
	@Doc("Returns a TileType of the specific type")
	public TileType getTile(@Doc("A TileType of the specified Type") LuaValue luaValue) {
		return getTile(luaValue.checkjstring());
	}

	public TileType getTile(String name) {
		return typeMap.get(name);
	}

	@Override
	public LuaValue getLuaValue() {
		if(this.luaValue == null)
			this.luaValue = LuaProxyFactory.getLuaValue(this);
		return this.luaValue;
	}

	@Override
	public Iterator<TileType> iterator() {
		return typeMap.values().iterator();
	}
	
	public TileType[] toArray(){
		return typeMap.values().toArray(new TileType[typeMap.values().size()]);
	}
}
