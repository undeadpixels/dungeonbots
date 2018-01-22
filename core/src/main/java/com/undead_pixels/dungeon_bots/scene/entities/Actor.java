package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.utils.annotations.BindTo;
import com.undead_pixels.dungeon_bots.utils.annotations.ScriptAPI;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;
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

	@ScriptAPI(SecurityLevel.DEBUG)
	public void print() {
		System.out.println(String.format("Position: {%.2f, %.2f}", this.getPosition().x, this.getPosition().y));
	}

	@ScriptAPI
	public void up() {
		moveInstantly(Direction.UP, 1);
	}

	@ScriptAPI
	public void down() {
		moveInstantly(Direction.DOWN, 1);
	}

	@ScriptAPI
	public void left() {
		moveInstantly(Direction.LEFT, 1);
	}

	@ScriptAPI
	public void right() {
		moveInstantly(Direction.RIGHT, 1);
	}

	@ScriptAPI(SecurityLevel.DEBUG) @BindTo("greet")
	public LuaValue debugName(LuaValue luaValue) {
		String greet = luaValue.checkjstring();
		return CoerceJavaToLua.coerce(greet + " " + this.name);
	}
}
