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
	public Actor(World world, String name, TextureRegion tex) {
		super(world, name, tex);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param world		The world to contain this Actor
	 * @param script		A user script that is run on this object
	 * @param tex		A texture for this Actor
	 */
	public Actor(World world, String name, LuaScript script, TextureRegion tex) {
		super(world, name, script, tex);
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
	public void up() {
		moveInstantly(Direction.UP, 1);
	}

	@ScriptAPI(SecurityLevel.BASIC)
	public void down() {
		moveInstantly(Direction.DOWN, 1);
	}

	@ScriptAPI(SecurityLevel.BASIC)
	public void left() {
		moveInstantly(Direction.LEFT, 1);
	}

	@ScriptAPI(SecurityLevel.BASIC)
	public void right() {
		moveInstantly(Direction.RIGHT, 1);
	}
}
