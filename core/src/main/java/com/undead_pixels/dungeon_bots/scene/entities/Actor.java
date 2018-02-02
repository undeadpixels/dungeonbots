package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.Gson;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.actions.Action;
import com.undead_pixels.dungeon_bots.scene.entities.actions.OnlyOneOfActions;
import com.undead_pixels.dungeon_bots.scene.entities.actions.SequentialActions;
import com.undead_pixels.dungeon_bots.scene.entities.actions.SpriteAnimatedAction;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import org.luaj.vm2.*;

import static org.luaj.vm2.LuaValue.*;

/**
 * @author Kevin Parker
 * @version 1.0
 * An actor is a general entity that is solid and capable of doing stuff.
 * Examples include players, bots, and enemies.
 */
public class Actor extends SpriteEntity {

	private LuaValue luaBinding;

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
		this.world.addEntity(this);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param world		The world to contain this Actor
	 * @param script		A user sandbox that is run on this object
	 * @param tex		A texture for this Actor
	 */
	@Deprecated
	public Actor(World world, String name, LuaSandbox script, TextureRegion tex) {
		super(world, name, tex);
		this.world.addEntity(this);
		// TODO Auto-generated constructor stub
		/// XXX
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
	 * @param dir
	 * @param dist
	 */
	@Deprecated
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

	/**
	 * Enqueues an action to the action queue that directs the Actor to
	 * move in the provided direction
	 * @param dir The direction to move
	 */
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
		final int[] initialPos = {0, 0};
		
		SpriteAnimatedAction tryMoveAction = new SpriteAnimatedAction(sprite, getMoveDuration()) {
			
			public boolean preAct() {
				initialPos[0] = Math.round(e.getPosition().x);
				initialPos[1] = Math.round(e.getPosition().y);
				boolean canMove = world.requestMoveToNewTile(e, _dx + initialPos[0], _dy + initialPos[1]);
				
				this.setFinalPosition(_dx + initialPos[0], _dy + initialPos[1]);
				
				return canMove;
				
			}
			
			public void postAct() {
				world.didLeaveTile(e, initialPos[0], initialPos[1]);
			}
			
		};

		Action fail1 = new SpriteAnimatedAction(sprite, .1f) {
			public boolean preAct() {
				this.setFinalPosition(_dx*.2f + initialPos[0], _dy*.2f + initialPos[1]);
				return true;
			}
		};
		Action fail2 = new SpriteAnimatedAction(sprite, .1f) {
			public boolean preAct() {
				this.setFinalPosition(initialPos[0], initialPos[1]);
				return true;
			}
		};
		
		Action moveFailAction = new SequentialActions(fail1, fail2);
		
		actionQueue.enqueue(new OnlyOneOfActions(tryMoveAction, moveFailAction));
	}

	
	/**
	 * @return	How quickly this Actor moves, in seconds
	 */
	public float getMoveDuration() {
		return 0.5f;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public LuaValue getLuaValue() {
		if(this.luaBinding == null)
			this.luaBinding = LuaProxyFactory.getLuaValue(this);
		return this.luaBinding;
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

	private Actor moveAmt(Varargs amt, Direction direction) {
		int SIZE = amt.narg();
		int n;
		if (SIZE < 2)
			n = amt.arg1().isint() ? amt.arg1().checkint() : 1;
		else
			n = amt.arg(2).checkint();
		for(int i = 0; i < n; i++)
			this.queueMoveSlowly(direction);
		return this;
	}

	/**
	 * Moves the player UP
	 * @author Stewart Charles
	 * @since 1.0
	 * @return The invoked Actor
	 */
	@Bind
	final public Actor up(Varargs amt) {
		return moveAmt(amt, Direction.UP);
	}

	/**
	 * Moves the player DOWN
	 * @author Stewart Charles
	 * @since 1.0
	 * @return The invoked Actor
	 */
	@Bind
	final public Actor down(Varargs amt) {
		return moveAmt(amt, Direction.DOWN);
	}

	/**
	 * Moves the player LEFT
	 * @author Stewart Charles
	 * @since 1.0
	 * @return The invoked Actor
	 */
	@Bind
	final public Actor left(Varargs amt) {
		return moveAmt(amt, Direction.LEFT);
	}

	/**
	 * Moves the player RIGHT
	 * @author Stewart Charles
	 * @since 1.0
	 * @return The invoked Actor
	 */
	@Bind
	final public Actor right(Varargs amt) {
		return moveAmt(amt, Direction.RIGHT);
	}

	/**
	 * @author Stewart Charles
	 * @since 1.0
	 * Returns a Varargs of the players position
	 * <pre>{@code
	 * x, y = actor:position()
	 * }</pre>
	 * @return A Varargs of the players position
	 */
	@Bind
	final public Varargs position() {
		Vector2 pos = this.getPosition();
		return varargsOf(new LuaValue[] { valueOf(pos.x), valueOf(pos.y)});
	}
}
