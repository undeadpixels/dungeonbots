package com.undead_pixels.dungeon_bots.scene;

import java.util.HashMap;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.script.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.LuaReflection;
import com.undead_pixels.dungeon_bots.script.SecurityContext;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetBindable;
import com.undead_pixels.dungeon_bots.script.interfaces.Scriptable;
import org.luaj.vm2.LuaValue;

public class TileTypes implements Scriptable, GetBindable {
	
	private HashMap<String, TileType> typeMap = new HashMap<>();

	@Bind @BindTo("new")
	public static LuaValue generate() {
		TileTypes tileTypes = new TileTypes();
		if(SecurityContext.getActiveSecurityLevel() == SecurityLevel.DEBUG)
			SecurityContext.getWhitelist().addWhitelist(tileTypes.permissiveWhitelist());
		return LuaProxyFactory.getLuaValue(tileTypes, SecurityContext.getActiveSecurityLevel());
	}

	/**
	 * @param name			The tile's name
	 * @param texture		A texture
	 * @param tilesize		Size of each tile
	 * @param x				X coordinate (in tile space) of the main variation of this tile
	 * @param y				Y coordinate (in tile space) of the main variation of this tile
	 * @param variationsX	An array indicating relative X variations of this tile
	 * 							(other ways this same tile might be rendered based on circumstances)
	 * @param variationsY	An array indicating relative Y variations of this tile
	 * 							(other ways this same tile might be rendered based on circumstances)
	 * @param random		If the variations are random or should be based on nearby tiles. "Nearby" (false)
	 * 							means that the array should have 16 indices, each referring to bitwise-or of
	 * 							Left=1, Right=2, Up=4, Down=8, where a 1 for each bit indicates that the given
	 * 							tile is the same.
	 */
	public void registerTile(String name, Texture texture, int tilesize, int x, int y) {
		registerTile(name, texture, tilesize, x, y, new Vector2[] {new Vector2()}, true);
	}

	/**
	 * @param name			The tile's name
	 * @param texture		A texture
	 * @param tilesize		Size of each tile
	 * @param x				X coordinate (in tile space) of the main variation of this tile
	 * @param y				Y coordinate (in tile space) of the main variation of this tile
	 * @param variations		An array indicating relative variations of this tile
	 * 							(other ways this same tile might be rendered based on circumstances)
	 * @param random		If the variations are random or should be based on nearby tiles. "Nearby" (false)
	 * 							means that the array should have 16 indices, each referring to bitwise-or of
	 * 							Left=1, Right=2, Up=4, Down=8, where a 1 for each bit indicates that the given
	 * 							tile is the same.
	 */
	public void registerTile(String name, Texture texture, int tilesize, int x, int y, Vector2[] variations, boolean random) {
		int len = variations.length;
		TextureRegion[] regions = new TextureRegion[len];
		
		for(int i = 0; i < len; i++) {
			//regions[i] = new TextureRegion(new Texture("DawnLike/Objects/Floor.png"), ts*1, ts*4, ts, ts);
			regions[i] = new TextureRegion(texture, (int)(tilesize*(x+variations[i].x)), (int)(tilesize*(y+variations[i].y)), tilesize, tilesize);
		}
		
		
		typeMap.put(name, new TileType(regions, name, random));
	}

	@Bind
	public LuaValue getTile(LuaValue luaValue) {
		return LuaProxyFactory.getLuaValue(getTile(luaValue.checkjstring()), SecurityContext.getActiveSecurityLevel());
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
		return this.getClass().getName();
	}
}
