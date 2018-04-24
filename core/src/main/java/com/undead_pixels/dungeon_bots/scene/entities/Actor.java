package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.LoggingLevel;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.actions.*;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.*;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons.Weapon;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons.WeaponStats;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
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
	 */
	private FloatingText floatingText;

	/**
	 * Relative directions (although effectively cardinal directions since the screen doesn't rotate)
	 */
	public enum Direction {
		UP, DOWN, LEFT, RIGHT;

		/**
		 * @param dx
		 * @param dy
		 * @return
		 */
		public static Direction byDelta (float dx, float dy) {
			if(Math.abs(dx) > Math.abs(dy)) {
				if(dx > 0) {
					return RIGHT;
				} else {
					return LEFT;
				}
			} else {
				if(dy > 0) {
					return UP;
				} else {
					return DOWN;
				}
			}
		}
	}

	/**
	 * @param world		The world to contain this Actor
	 * @param tex		A texture for this Actor
	 */
	public Actor(World world, String name, TextureRegion tex, UserScriptCollection scripts) {
		super(world, name, tex, scripts);
		floatingText = new FloatingText(this, name+"-text");
	}
	
	/**
	 * @param world		The world to contain this Actor
	 * @param tex		A texture for this Actor
	 */
	public Actor(World world, String name, TextureRegion tex, UserScriptCollection scripts, float x, float y) {
		super(world, name, tex, scripts, x, y);
		floatingText = new FloatingText(this, name+"-text");
	}
	
	/**
	 * Should only ever be called by the world, in its addEntity
	 * @param world
	 */
	@Override
	public void onAddedToWorld(World world) {
		world.addEntity(floatingText);
	}

	@Override
	public LuaSandbox createSandbox() {
		LuaSandbox sandbox = super.createSandbox();
		
		sandbox.registerEventType("ITEM_GIVEN", "Called when this entity is given an item", "item");
	
		return sandbox;
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
	 * Enqueues an action to the action queue that directs the Actor to
	 * move in the provided direction
	 * @param dir The direction to move
	 * @param blocking 
	 */
	public void queueMoveSlowly(Direction dir, boolean blocking) {
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
		final HasImage imgSrc = this;
		SpriteAnimatedAction tryMoveAction = new SpriteAnimatedAction(sprite, getMoveDuration()) {
			
			public boolean preAct() {
				initialPos[0] = Math.round(e.getPosition().x);
				initialPos[1] = Math.round(e.getPosition().y);
				boolean canMove = world.requestMoveToNewTile(e, _dx + initialPos[0], _dy + initialPos[1]);
				if(canMove) {
					steps++;

					world.message(imgSrc,
									"Moving to ("+(_dx + initialPos[0])+", "+(_dy + initialPos[1])+")",
									LoggingLevel.DEBUG);
				} else {
					bumps++;
					world.message(imgSrc,
							"Bumped at ("+initialPos[0]+", "+initialPos[1]+")",
							LoggingLevel.DEBUG);
				}
				
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
		if(this.floatingText == null) {
			this.floatingText = new FloatingText(this, getName()+"-text");
			world.addEntity(floatingText);
		}
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
		this.world.message(
				this,
				String.format("Invokes Move %s", direction.name()),
				LoggingLevel.DEBUG);
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

	@Bind(value = SecurityLevel.NONE, doc = "Ask the bot to wait the specified number of turns")
	@BindTo("wait")
	public Actor waitFor(@Doc("The number of turns to wait") Varargs args) {
		int SIZE = args.narg();
		int n;
		if (SIZE < 2)
			n = args.arg1().isint() ? args.arg1().checkint() : 1;
		else
			n = args.arg(2).checkint();
		this.queueWait(n);
		return this;
	}

	private void queueWait(int n) {
		final SpriteAnimatedAction waitAnimation = new SpriteAnimatedAction(sprite, getMoveDuration()) {

			public boolean preAct() {
				world.message(Actor.this, String.format("%s waits...", getName()), LoggingLevel.DEBUG);
				return true;
			}
		};
		for(int i = 0; i < n; i ++) {
			actionQueue.enqueue(waitAnimation);
		}
	}

	/**
	 * Moves the player a given direction and distance
	 * @author Stewart Charles
	 * @since 1.0
	 * @return The invoked Actor
	 */
	@Bind(SecurityLevel.DEFAULT)
	@Doc("Moves the player a given direction")
	final public Actor move(@Doc("The direction and number of spaces to move") Varargs dirAmt) {
		Direction direction;
		int n;
		try {
			direction = Direction.valueOf(dirAmt.checkjstring(2).toUpperCase());
			n = dirAmt.optint(3, 1);
		} catch(LuaError e) {
			direction = Direction.valueOf(dirAmt.checkjstring(1).toUpperCase());
			n = dirAmt.optint(2, 1);
		}

		for(int i = 0; i < n; i++)
			this.queueMoveSlowly(direction, true);
		
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
	@Bind(value=SecurityLevel.DEFAULT, doc="Get the position of this Actor as an x,y varargs pair")
	final public Varargs position() {
		final Point2D.Float pos = this.getPosition();
		return varargsOf(new LuaValue[] { valueOf(pos.x + 1), valueOf(pos.y + 1)});
	}

	@Bind(value=SecurityLevel.DEFAULT, doc="Get the X position of this Actor")
	final public LuaValue x() {
		final Point2D.Float pos = this.getPosition();
		return LuaValue.valueOf(pos.x + 1);
	}

	@Bind(value=SecurityLevel.DEFAULT, doc="Get the Y position of this Actor")
	final public LuaValue y() {
		final Point2D.Float pos = this.getPosition();
		return LuaValue.valueOf(pos.x + 1);
	}

	/**
	 * @param args
	 */
	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Prints the argument text above the player")
	public void say(@Doc("The text for the player to say") Varargs args) {
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
	public Integer take(
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
				return -1;
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
		if(dir.istable() && dir.checktable().get("this").isuserdata() || dir.isnil())
			return world.tryUse(this, getPosition());
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
				return world.tryUse(this, getPosition());
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
			doc = "Actor inspects objects or entities in the specified direction")
	public String look(
			@Doc("A Varargs of either the Dir to look. Nil if Looking at the players position") LuaValue v) {
		if(v.isstring()) {
			switch (v.checkjstring().toLowerCase()) {
				case "up":
					return lookUp();
				case "down":
					return lookDown();
				case "left":
					return lookLeft();
				case "right":
					return lookRight();
				default:
					return "Nothing...";
			}
		}
		else return world.tryLook(getPosition());
	}

	@Bind(value = SecurityLevel.NONE, doc = "")
	public Boolean give(
			@Doc("A Varargs of the direction to give, the index from your inventory to give from, and optionally the location in the target inventory to give to")
					Varargs args) {
		final int start = args.arg1().isstring() ? 1 : 2;
		final String dir = args.arg(start).checkjstring();
		switch (dir.toLowerCase()) {
			case "up":
				return giveUp(args.subargs(start + 1));
			case "down":
				return giveDown(args.subargs(start + 1));
			case "left":
				return giveLeft(args.subargs(start + 1));
			case "right":
				return giveRight(args.subargs(start + 1));
			default:
				return false;
		}
	}

	@Bind(value = SecurityLevel.NONE, doc = "Give the item specified at the actors inventory index to the entity RIGHT from the actor")
	public Boolean giveRight(@Doc("A varargs of the index from the source Actors inventory, and optionally the destination index of the target inventory") Varargs args) {
		final int start = args.arg1().isnumber() ? 0 : 1;
		final int srcIndex = args.arg(start + 1).toint() - 1;
		final int dstIndex = args.arg(start + 2).isnil() ? -1 : args.arg(start + 2).toint() - 1;
		if(dstIndex < 0)
			return world.tryGive(this.inventory.peek(srcIndex), right());
		else {
			return world.tryGive(this.inventory.peek(srcIndex),dstIndex, right());
		}
	}

	@Bind(value = SecurityLevel.NONE, doc = "Give the item specified at the actors inventory index to the entity LEFT from the actor")
	public Boolean giveLeft(@Doc("A varargs of the index from the source Actors inventory, and optionally the destination index of the target inventory") Varargs args) {
		final int start = args.arg1().isnumber() ? 0 : 1;
		final int srcIndex = args.arg(start + 1).toint() - 1;
		final int dstIndex = args.arg(start + 2).isnil() ? -1 : args.arg(start + 2).toint() - 1;
		if(dstIndex < 0)
			return world.tryGive(this.inventory.peek(srcIndex), left());
		else {
			return world.tryGive(this.inventory.peek(srcIndex),dstIndex, left());
		}
	}

	@Bind(value = SecurityLevel.NONE, doc = "Give the item specified at the actors inventory index to the entity DOWN from the actor")
	public Boolean giveDown(@Doc("A varargs of the index from the source Actors inventory, and optionally the destination index of the target inventory") Varargs args) {
		final int start = args.arg1().isnumber() ? 0 : 1;
		final int srcIndex = args.arg(start + 1).toint() - 1;
		final int dstIndex = args.arg(start + 2).isnil() ? -1 : args.arg(start + 2).toint() - 1;
		if(dstIndex < 0)
			return world.tryGive(this.inventory.peek(srcIndex), down());
		else {
			return world.tryGive(this.inventory.peek(srcIndex),dstIndex, down());
		}
	}


	@Bind(value = SecurityLevel.NONE, doc = "Give the item specified at the actors inventory index to the entity UP from the actor")
	public Boolean giveUp(@Doc("A varargs of the index from the source Actors inventory, and optionally the destination index of the target inventory") Varargs args) {
		final int start = args.arg1().isnumber() ? 0 : 1;
		final int srcIndex = args.arg(start + 1).toint() - 1;
		final int dstIndex = args.arg(start + 2).isnil() ? -1 : args.arg(start + 2).toint() - 1;
		if(dstIndex < 0)
			return world.tryGive(this.inventory.peek(srcIndex), up());
		else {
			return world.tryGive(this.inventory.peek(srcIndex),dstIndex, up());
		}
	}


	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Actor inspects objects or entities UP relative to their position")
	public String lookUp() {
		return world.tryLook(up());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Actor inspects objects or entities DOWN relative to their position")
	public String lookDown() {
		return world.tryLook(down());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Actor inspects objects or entities LEFT relative to their position")
	public String lookLeft() {
		return world.tryLook(left());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Actor inspects objects or entities RIGHT relative to their position")
	public String lookRight() {
		return world.tryLook(right());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Push objects UP relative to the Actor")
	public Actor pushUp() {
		world.tryPush(this, up(), Direction.UP);
		return this;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Push objects DOWN relative to the Actor")
	public Actor pushDown() {
		world.tryPush(this, down(), Direction.DOWN);
		return this;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Push objects LEFT relative to the Actor")
	public Actor pushLeft() {
		world.tryPush(this, left(), Direction.LEFT);
		return this;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Push objects RIGHT relative to the Actor")
	public Actor pushRight() {
		world.tryPush(this, right(), Direction.RIGHT);
		return this;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Peek at the inventory of any entity UP relative to the actor")
	public LuaValue peekUp() {
		return world.tryPeek(this, up());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Peek at the inventory of any entity DOWN relative to the actor")
	public LuaValue peekDown() {
		return world.tryPeek(this, down());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Peek at the inventory of any entity LEFT relative to the actor")
	public LuaValue peekLeft() {
		return world.tryPeek(this, left());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Peek at the inventory of any entity RIGHT relative to the actor")
	public LuaValue peekRight() {
		return world.tryPeek(this, right());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Take an item from the inventory of any entity found UP relative to the Actor")
	private Integer takeUp(@Doc("The Index of the item in the owners inventory") LuaValue index) {
		return world.tryTake(this,up(), index.checkint() - 1) + 1;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Take an item from the inventory of any entity found DOWN relative to the Actor")
	private Integer takeDown(@Doc("The Index of the item in the owners inventory") LuaValue index) {
		return world.tryTake(this, down(), index.checkint() - 1) + 1;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Take an item from the inventory of any entity found LEFT relative to the Actor")
	private Integer takeLeft(@Doc("The Index of the item in the owners inventory") LuaValue index) {
		return world.tryTake(this, left(), index.checkint() - 1) + 1;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Take an item from the inventory of any entity found RIGHT relative to the Actor")
	private Integer takeRight(@Doc("The Index of the item in the owners inventory") LuaValue index) {
		return world.tryTake(this, right(), index.checkint() - 1) + 1;
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Contextually use an object or entity RIGHT relative to the actor")
	private Boolean useRight() {
		return world.tryUse(this, right());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Contextually use an object or entity LEFT relative to the actor")
	private Boolean useLeft() {
		return world.tryUse(this, left());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Contextually use an object or entity DOWN relative to the actor")
	private Boolean useDown() {
		return world.tryUse(this, down());
	}

	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Contextually use an object or entity UP relative to the actor")
	private Boolean useUp() {
		return world.tryUse(this, up());
	}

	@Bind(value = SecurityLevel.NONE, doc = "Get a table that identifies if adjacent tiles are blocking (i.e. walls or doors)"
			+ "\nblocking = {up = <boolean>, down = <boolean>, left = <boolean>, right = <boolean>}")
	public LuaValue nearbyBlocking() {
		final LuaTable tbl = new LuaTable();
		tbl.set("up", LuaValue.valueOf(isBlocking(up())));
		tbl.set("down", LuaValue.valueOf(isBlocking(down())));
		tbl.set("left", LuaValue.valueOf(isBlocking(left())));
		tbl.set("right", LuaValue.valueOf(isBlocking(right())));
		return tbl;
	}

	@Bind(value = SecurityLevel.NONE, doc = "Get a table that identifies if adjacent tiles are open (i.e. floor)"
			+ "\nblocking = {up = <boolean>, down = <boolean>, left = <boolean>, right = <boolean>}")
	public LuaValue nearbyOpen() {
		final LuaTable tbl = new LuaTable();
		tbl.set("up", LuaValue.valueOf(!isBlocking(up())));
		tbl.set("down", LuaValue.valueOf(!isBlocking(down())));
		tbl.set("left", LuaValue.valueOf(!isBlocking(left())));
		tbl.set("right", LuaValue.valueOf(!isBlocking(right())));
		return tbl;
	}

	private boolean isBlocking(final Point2D.Float pos) {
		return world.isBlocking(Math.round(pos.x), Math.round(pos.y));
	}
}
