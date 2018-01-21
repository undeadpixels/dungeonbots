package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaBinding;
import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.utils.annotations.ScriptAPI;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

/**
 * An actor is a general entity that is solid and capable of doing stuff.
 * Examples include players, bots, and enemies.
 */
public class Actor extends SpriteEntity {

	/**
	 * Relative directions (although effectively cardinal directions since the screen doesn't rotate)
	 */
	public enum Direction {
		UP, DOWN, LEFT, RIGHT
	}

	/**
	 * @param world		The world to contain this Actor
	 * @param tex		A texture for this Actor
	 */
	public Actor(World world, TextureRegion tex) {
		super(world, tex);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param world		The world to contain this Actor
	 * @param script		A user script that is run on this object
	 * @param tex		A texture for this Actor
	 */
	public Actor(World world, LuaScript script, TextureRegion tex) {
		super(world, script, tex);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param world		The world to contain this Actor
	 * @param sprite		A texture for this Actor
	 */
	public Actor(World world, Sprite sprite) {
		super(world, sprite);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param world		The world to contain this Actor
	 * @param script		A user script that is run on this object 
	 * @param sprite		A texture for this Actor
	 */
	public Actor(World world, LuaScript script, Sprite sprite) {
		super(world, script, sprite);
		// TODO Auto-generated constructor stub
	}

	@Override
	public float getZ() {
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	public boolean isSolid() {
		return true;
	}
	
	/**
	 * TODO - DELETEME
	 * @param dir
	 * @param dist
	 */
	public void moveInstantly(Direction dir, int dist) {
		switch (dir) {
			case UP:
				sprite.setY(sprite.getY() - dist);
				break;
			case DOWN:
				sprite.setY(sprite.getY() + dist);
				break;
			case LEFT:
				sprite.setX(sprite.getX() - dist);
				break;
			case RIGHT:
				sprite.setX(sprite.getX() + dist);
				break;
		}
	}

	@ScriptAPI(SecurityLevel.BASIC)
	public LuaBinding actorUp() {
		return genZeroArg("up", () -> moveInstantly(Direction.UP, 1));
	}

	/*
	@ScriptAPI(SecurityLevel.BASIC)
	public LuaBinding actorDown() {
		return genZeroArg("down", () -> moveInstantly(Direction.DOWN, 1));
	}

	@ScriptAPI(SecurityLevel.BASIC)
	public LuaBinding actorLeft() {
		return genZeroArg("left", () -> moveInstantly(Direction.LEFT, 1));
	}

	@ScriptAPI(SecurityLevel.BASIC)
	public LuaBinding actorRight() {
		return genZeroArg("right", () -> moveInstantly(Direction.RIGHT, 1));
	}
	*/
}
