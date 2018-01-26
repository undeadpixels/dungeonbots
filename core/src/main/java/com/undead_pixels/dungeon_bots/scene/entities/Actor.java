package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.actions.SpriteAnimatedAction;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import org.luaj.vm2.*;
import org.luaj.vm2.ast.Exp;
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
	 * @param script		A user scriptEnv that is run on this object
	 * @param tex		A texture for this Actor
	 */
	public Actor(World world, String name, LuaSandbox script, TextureRegion tex) {
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
				sprite.setY(sprite.getY() + dist);
				break;
			case DOWN:
				sprite.setY(sprite.getY() - dist);
				break;
			case LEFT:
				sprite.setX(sprite.getX() - dist);
				break;
			case RIGHT:
				sprite.setX(sprite.getX() + dist);
				break;
		}
	}
	
	public void queueMoveSlowly(Direction dir) {
		int dx = 0, dy = 0;

		switch (dir) {
			case UP:
				dy = 1;
				break;
			case DOWN:
				dy = -1;
				break;
			case LEFT:
				dx = -1;
				break;
			case RIGHT:
				dx = 1;
				break;
		}
		
		Entity e = this;
		final int _dx = dx, _dy = dy;
		
		actionQueue.enqueue(new SpriteAnimatedAction(sprite, getMoveDuration()) {
			int initialX, initialY;
			
			public boolean preAct() {
				initialX = Math.round(e.getPosition().x);
				initialY = Math.round(e.getPosition().y);
				boolean canMove = world.requestMoveToNewTile(e, _dx + initialX, _dy + initialY);
				
				this.setFinalPosition(_dx + initialX, _dy + initialY);
				
				return canMove;
				
			}
			
			public void postAct() {
				world.didLeaveTile(e, initialX, initialY);
			}
			
		});
	}

	
	public float getMoveDuration() {
		return 0.5f;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getId() {
		return this.id;
	}

	// Lua ScriptApi methods that are collected and bound to a LuaSandbox using runtime reflection

	/**
	 * Prints debug info pertaining to the player to the console
	 */
	@Bind(SecurityLevel.DEBUG) @BindTo("debug")
	public void print() {
		System.out.println(String.format("Position: {%.2f, %.2f}", this.getPosition().x, this.getPosition().y));
	}

	/**
	 * Moves the player UP
	 */
	@Bind
	public void up() {
		moveInstantly(Direction.UP, 1);
	}

	/**
	 * Moves the player DOWN
	 */
	@Bind
	public void down() {
		moveInstantly(Direction.DOWN, 1);
	}

	/**
	 * Moves the player LEFT
	 */
	@Bind
	public void left() {
		moveInstantly(Direction.LEFT, 1);
	}

	/**
	 * Moves the player RIGHT
	 */
	@Bind
	public void right() {
		moveInstantly(Direction.RIGHT, 1);
	}

	/**
	 * Returns a Varargs of the players position
	 * <code>
	 *     x, y = actor.position()
	 * </code>
	 * @param v An empty Varargs of the players position. <br> Will throw a runtime error if the Varargs parameter arg count is > 0
	 * @return A Varargs of the players position
	 */
	@Bind(SecurityLevel.DEFAULT)
	public Varargs position(Varargs v) {
		assert v.narg() == 0;
		Vector2 pos = this.getPosition();
		return LuaValue.varargsOf(LuaValue.valueOf(pos.x), LuaValue.valueOf(pos.y));
	}
}
