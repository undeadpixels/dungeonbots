package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.actions.*;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.*;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons.Weapon;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons.WeaponStats;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.SandboxManager;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import org.luaj.vm2.*;
import java.awt.geom.Point2D;

import static org.luaj.vm2.LuaValue.*;


/**
 * @author Kevin Parker
 * @version 1.0
 * An actor is a general entity that is solid and capable of doing stuff.
 * Examples include players, bots, and enemies.
 */
@Doc("The base type for Bot and Player entities")
public abstract class Actor extends SpriteEntity implements HasInventory {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Number of steps taken
	 */
	protected int steps = 0;
	
	/**
	 * Number of times bumped into a wall
	 */
	protected int bumps = 0;

	/**
	 * Inventory
	 */
	protected Inventory inventory = new Inventory(this, 10);
	
	/**
	 * Lazy-loaded lua value
	 */
	private transient LuaValue luaBinding;
	
	/**
	 * The text associated with this actor
	 * 
	 * TODO - this might want to be refactored eventually
	 */
	private FloatingText floatingText;

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
	public Actor(World world, String name, TextureRegion tex, UserScriptCollection scripts) {
		super(world, name, tex, scripts);
		floatingText = new FloatingText(this, name+"-text");
		world.addEntity(floatingText);
	}
	
	/**
	 * @param world		The world to contain this Actor
	 * @param tex		A texture for this Actor
	 */
	public Actor(World world, String name, TextureRegion tex, UserScriptCollection scripts, float x, float y) {
		super(world, name, tex, scripts, x, y);
		floatingText = new FloatingText(this, name+"-text");
		world.addEntity(floatingText);
	}

	@Override
	public float getZ() {
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
	 * @param blocking 
	 */
	protected void queueMoveSlowly(Direction dir, boolean blocking) {
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
				if(canMove) steps++; else bumps++;
				this.setFinalPosition(_dx + initialPos[0], _dy + initialPos[1]);
				return canMove;
			}
			
			public void postAct() {
				world.didLeaveTile(e, initialPos[0], initialPos[1]);
			}
			
		};

		Action fail1 = new SpriteAnimatedAction(sprite, .2f) {
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

		if(blocking) {
			SandboxManager.getCurrentSandbox().safeWaitUntil(() -> actionQueue.isEmpty());
		}
		
		actionQueue.enqueue(new OnlyOneOfActions(tryMoveAction, moveFailAction));

		if(blocking) {
			SandboxManager.getCurrentSandbox().safeWaitUntil(() -> actionQueue.isEmpty());
		}
	}
	
	/**
	 * Put some text above this entity's head
	 * 
	 * @param text
	 */
	public void addText(String text) {
		this.floatingText.addLine(text);
	}

	
	/**
	 * @return	How quickly this Actor moves, in seconds
	 */
	public float getMoveDuration() {
		return 0.5f;
	}

	@Override
	public LuaValue getLuaValue() {
		if(this.luaBinding == null)
			this.luaBinding = LuaProxyFactory.getLuaValue(this);
		return this.luaBinding;
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
	 * Move a given amount
	 * 
	 * @param amt
	 * @param direction
	 * @param blocking		Whether this call should block until the movement has finished
	 * @return
	 */
	protected Actor moveAmt(Varargs amt, Direction direction, boolean blocking) {
		int SIZE = amt.narg();
		int n;
		if (SIZE < 2)
			n = amt.arg1().isint() ? amt.arg1().checkint() : 1;
		else
			n = amt.arg(2).checkint();
		for(int i = 0; i < n; i++)
			this.queueMoveSlowly(direction, blocking);
		return this;
	}

	/**
	 * Moves the player UP
	 * @author Stewart Charles
	 * @since 1.0
	 * @return The invoked Actor
	 */
	@Bind(SecurityLevel.DEFAULT)
	@Doc("Moves the player UP")
	final public Actor up(@Doc("The number of spaces to move") Varargs amt) {
		return moveAmt(amt, Direction.UP, true);
	}

	/**
	 * Moves the player DOWN
	 * @author Stewart Charles
	 * @since 1.0
	 * @return The invoked Actor
	 */
	@Bind(SecurityLevel.DEFAULT)
	@Doc("Moves the player DOWN")
	final public Actor down(@Doc("The number of spaces to move") Varargs amt) {
		return moveAmt(amt, Direction.DOWN, true);
	}

	/**
	 * Moves the player LEFT
	 * @author Stewart Charles
	 * @since 1.0
	 * @return The invoked Actor
	 */
	@Bind(SecurityLevel.DEFAULT)
	@Doc("Moves the player LEFT")
	final public Actor left(@Doc("The number of spaces to move") Varargs amt) {
		return moveAmt(amt, Direction.LEFT, true);
	}

	/**
	 * Moves the player RIGHT
	 * @author Stewart Charles
	 * @since 1.0
	 * @return The invoked Actor
	 */
	@Bind(SecurityLevel.DEFAULT)
	@Doc("Moves the player RIGHT")
	final public Actor right(@Doc("The number of spaces to move") Varargs amt) {
		return moveAmt(amt, Direction.RIGHT, true);
	}


	/**
	 * Moves the player UP
	 * @author Stewart Charles
	 * @since 1.0
	 * @return The invoked Actor
	 */
	@Bind(SecurityLevel.DEFAULT)
	final public Actor queueUp(Varargs amt) {
		return moveAmt(amt, Direction.UP, false);
	}

	/**
	 * Moves the player DOWN
	 * @author Stewart Charles
	 * @since 1.0
	 * @return The invoked Actor
	 */
	@Bind(SecurityLevel.DEFAULT)
	final public Actor queueDown(Varargs amt) {
		return moveAmt(amt, Direction.DOWN, false);
	}

	/**
	 * Moves the player LEFT
	 * @author Stewart Charles
	 * @since 1.0
	 * @return The invoked Actor
	 */
	@Bind(SecurityLevel.DEFAULT)
	final public Actor queueLeft(Varargs amt) {
		return moveAmt(amt, Direction.LEFT, false);
	}

	/**
	 * Moves the player RIGHT
	 * @author Stewart Charles
	 * @since 1.0
	 * @return The invoked Actor
	 */
	@Bind(SecurityLevel.DEFAULT)
	final public Actor queueRight(Varargs amt) {
		return moveAmt(amt, Direction.RIGHT, false);
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
	@Bind(value=SecurityLevel.DEFAULT, doc="Get the position of the player as an x,y varargs pair")
	final public Varargs position() {
		final Point2D.Float pos = this.getPosition();
		return varargsOf(new LuaValue[] { valueOf(pos.x + 1), valueOf(pos.y + 1)});
	}

	/**
	 * @param args
	 */
	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Prints the argument text above the player")
	final public void say(@Doc("The text for the player to say") Varargs args) {
		final StringBuilder text = new StringBuilder();
		for(int i = 2; i <= args.narg(); i++) {
			if(i > 2)
				text.append(" ");
			text.append(args.tojstring(i));
		}
		this.addText(text.toString());
	}

	/**
	 * Remove an item from this Actor's inventory
	 * 
	 * @param i
	 */
	public void removeItem(Item i) {
		// TODO
	}

	@Override
	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Get the Inventory of the Player")
	@BindTo("inventory")
	public Inventory getInventory() {
		return inventory;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Get the Number of Steps taken by the Actor")
	public int steps() {
		return steps;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Get the Number of Collisions made by the Actor with walls")
	public int bumps() {
		return bumps;
	}

	@Override
	public Boolean giveItem(ItemReference ir) {
		final Item item = ir.derefItem();
		return this.inventory.addItem(item) || ir.inventory.addItem(item);
	}

	/**
	 * Peek at the inventory of the first entity found int he target direction relative to the Actor
	 * @param dir The Direction of the target entity
	 * @return A LuaTable that is an array of tables containing the name and description of items.
	 */
	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Peek at an entities inventory in the specified direction\nrelative to the actor.")
	public LuaValue peek(@Doc("The direction to peek [up,down,left,right]") LuaValue dir) {
		switch (dir.checkjstring().toLowerCase()) {
			case "up":
				return peekUp();
			case "down":
				return peekDown();
			case "left":
				return peekLeft();
			case "right":
				return peekRight();
			default:
				return LuaValue.NIL;
		}
	}

	/**
	 * Takes an item from an entity found in the specified direction at the argument index
	 * @param dir The direction of the target entity
	 * @param index The index into the inventory of the target entity
	 * @return True if taking the item succeeded, False otherwise
	 */
	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Take an item from the inventory of any entity found in the specified direction if possible")
	public Boolean take(
			@Doc("The Direction of the entity to take the item from") LuaValue dir,
			@Doc("The Index of the Item") LuaValue index) {
		switch (dir.checkjstring().toLowerCase()) {
			case "up":
				return takeUp(index);
			case "down":
				return takeDown(index);
			case "left":
				return takeLeft(index);
			case "right":
				return takeRight(index);
			default:
				return false;
		}
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Grabs any Item Entities that exist in the tile the player is in")
	public Boolean grab() {
		return world.tryGrab(this);
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Contextually use an object/entity in the specified direction relative to the actor")
	public Boolean use(@Doc("The direction of the entity or object to Use") LuaValue dir) {
		switch (dir.checkjstring().toLowerCase()) {
			case "up":
				return useUp();
			case "down":
				return useDown();
			case "left":
				return useLeft();
			case "right":
				return useRight();
			default:
				return false;
		}
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Pushes any pushable objects in the associated direction")
	public Actor push(@Doc("The Direction to try to push an object") LuaValue dir) {
		switch (dir.checkjstring().toLowerCase()) {
			case "up":
				return pushUp();
			case "down":
				return pushDown();
			case "left":
				return pushLeft();
			case "right":
				return pushRight();
			default:
				return this;
		}
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Push objects UP relative to the Actor")
	public Actor pushUp() {
		world.tryPush(up(), Direction.UP);
		return this;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Push objects DOWN relative to the Actor")
	public Actor pushDown() {
		world.tryPush(down(), Direction.DOWN);
		return this;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Push objects LEFT relative to the Actor")
	public Actor pushLeft() {
		world.tryPush(left(), Direction.LEFT);
		return this;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Push objects RIGHT relative to the Actor")
	public Actor pushRight() {
		world.tryPush(right(), Direction.RIGHT);
		return this;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Peek at the inventory of any entity UP relative to the actor")
	public LuaValue peekUp() {
		return world.tryPeek(up());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Peek at the inventory of any entity DOWN relative to the actor")
	public LuaValue peekDown() {
		return world.tryPeek(down());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Peek at the inventory of any entity LEFT relative to the actor")
	public LuaValue peekLeft() {
		return world.tryPeek(left());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Peek at the inventory of any entity RIGHT relative to the actor")
	public LuaValue peekRight() {
		return world.tryPeek(right());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Take an item from the inventory of any entity found UP relative to the Actor")
	private Boolean takeUp(@Doc("The Index of the item in the owners inventory") LuaValue index) {
		return world.tryTake(up(), index.checkint() - 1, this.inventory);
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Take an item from the inventory of any entity found DOWN relative to the Actor")
	private Boolean takeDown(@Doc("The Index of the item in the owners inventory") LuaValue index) {
		return world.tryTake(down(), index.checkint() - 1, this.inventory);
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Take an item from the inventory of any entity found LEFT relative to the Actor")
	private Boolean takeLeft(@Doc("The Index of the item in the owners inventory") LuaValue index) {
		return world.tryTake(left(), index.checkint() - 1, this.inventory);
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Take an item from the inventory of any entity found RIGHT relative to the Actor")
	private Boolean takeRight(@Doc("The Index of the item in the owners inventory") LuaValue index) {
		return world.tryTake(right(), index.checkint() - 1, this.inventory);
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Contextually use an object or entity RIGHT relative to the actor")
	private Boolean useRight() {
		return world.tryUse(right());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Contextually use an object or entity LEFT relative to the actor")
	private Boolean useLeft() {
		return world.tryUse(left());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Contextually use an object or entity DOWN relative to the actor")
	private Boolean useDown() {
		return world.tryUse(down());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Contextually use an object or entity UP relative to the actor")
	private Boolean useUp() {
		return world.tryUse(up());
	}
}
