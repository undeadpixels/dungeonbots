package com.undead_pixels.dungeon_bots.scene.entities;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import com.undead_pixels.dungeon_bots.scene.*;
import com.undead_pixels.dungeon_bots.scene.entities.actions.ActionQueue;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.CanUseItem;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.events.StringBasedLuaInvocationCoalescer;
import com.undead_pixels.dungeon_bots.script.events.UpdateCoalescer;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaSandbox;
import com.undead_pixels.dungeon_bots.script.interfaces.HasEntity;
import com.undead_pixels.dungeon_bots.script.interfaces.HasTeam;
import com.undead_pixels.dungeon_bots.script.proxy.LuaReflection;
import org.luaj.vm2.LuaValue;

/**
 * @author Kevin Parker
 * @version 1.0 Pretty much everything visible/usable within a regular game.
 *          Does not include UI elements.
 */
public abstract class Entity implements BatchRenderable, GetLuaSandbox, GetLuaFacade, Serializable, CanUseItem,
		HasEntity, HasTeam, HasImage, Inspectable {

	public static float MIN_IDLE_THRESHOLD = 5.0f;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The scripts associated with this entity.
	 */
	private final UserScriptCollection scripts;

	/**
	 * A user sandbox that is run on this object
	 * 
	 * Lazy-loaded
	 */
	private transient LuaSandbox sandbox;

	/**
	 * The queue of actions this Entity is going to take
	 */
	protected transient ActionQueue actionQueue = new ActionQueue(this);

	/**
	 * The world of which this Entity is a part
	 */
	protected final World world;

	/**
	 * Some simple int that can uniquely identify this entity
	 */
	protected final int id;

	/**
	 * A name for this entity that can potentially be user-facing
	 */
	protected String name;

	/**The instructions associated with an entity.  These are what is shown in 
	 * the Entity Editor in the instruction pane.  The value can be null or any 
	 * string.*/
	private String help = "Try the 'help(this)' command to learn about this entity.";


	/**
	 * Constructor for this entity
	 * 
	 * @param world		The world
	 * @param name		This entity's name
	 */
	public Entity(World world, String name, UserScriptCollection scripts) {
		this.world = world;
		this.name = name;
		this.scripts = scripts;
		standardizeResources();
		freshenPermissions();
		if (world != null) {
			this.id = world.makeID();
		} else {
			this.id = -1;
		}
	}


	public LuaSandbox createSandbox() {
		sandbox = new LuaSandbox(this);
		sandbox.addBindable("this", this);
		sandbox.addBindable("world", world);
		sandbox.addBindableClasses(LuaReflection.getAllBindableClasses());
		sandbox.registerEventType("UPDATE", "Called on every frame", "deltaTime");
		sandbox.registerEventType("IDLE", "Called when this entity has been idle for a while (usually about 60 seconds).");
		sandbox.registerEventType("CLICKED", "Called whenever this entity has been clicked.");
		sandbox.registerEventType("KEY_PRESSED", "Called when a key is pressed on the keyboard", "key");
		sandbox.registerEventType("KEY_RELEASED", "Called when a key is released on the keyboard", "key");
		sandbox.registerEventType("ON_TURN", "Called when it is this entity's turn to move");
		final int[] releasedCounter = {0};
		world.listenTo(World.StringEventType.KEY_PRESSED, this, (s) -> {
			sandbox.fireEvent("KEY_PRESSED", new StringBasedLuaInvocationCoalescer(s, releasedCounter[0]), LuaValue.valueOf(s));
		});
		world.listenTo(World.StringEventType.KEY_RELEASED, this, (s) -> {
			sandbox.fireEvent("KEY_RELEASED", LuaValue.valueOf(s));
			releasedCounter[0]++;
		});

		if (idleThreshold < MIN_IDLE_THRESHOLD)
			idleThreshold = 60f;


		return this.sandbox;
	}


	/**
	 * Returns the Lua sandbox wherein this entity's scripts will execute.
	 */
	@Override
	public LuaSandbox getSandbox() {
		if (sandbox == null) {
			sandbox = createSandbox();
		}
		return sandbox;
	}


	public void sandboxInit() {
		if (this.scripts != null && this.scripts.get("init") != null) {
			getSandbox().init();
			System.out.println("Running entity init for " + this);
		} else {
			System.out.println("Skipping entity init (script does not exist for " + this + ")");
		}
	}


	/**
	 * Should only ever be called by the world, in its addEntity
	 * @param world
	 */
	public void onAddedToWorld(World world) {
	}


	/**Accumulated idle time.*/
	private transient float idle = 0f;
	private float idleThreshold = 60f;


	/** Called during the game loop to update the entity's status. */
	@Override
	public void update(float dt) {

		// Next item on the queue acts.
		actionQueue.act(dt);

		// Enqueue an idle call, if enough time has elapsed.
		boolean isIdle = actionQueue.isEmpty();
		if (!isIdle) {
			idle = 0f;
		} else {
			idle += dt;
			if (idle > idleThreshold) {
				idle = 0;
				getSandbox().fireEvent("IDLE");
				getWorld().getSandbox().fireEvent("ENTITY_IDLE", this.getLuaValue());
			}
		}

		// Update the sandbox
		if (sandbox != null) {
			sandbox.update(dt);
			getSandbox().fireEvent("UPDATE", UpdateCoalescer.instance, LuaValue.valueOf(dt));
		}
	}


	/**
	 * @return This Entity's position in tile space
	 */
	public abstract Point2D.Float getPosition();


	public void setPosition(float x, float y) {
		throw new RuntimeException("Cannot set position on Entity of type " + this.getClass().getName());
	}


	/**
	 * Derived classes must implement.
	 *
	 * @return If this object disallows movement through it.
	 */
	public abstract boolean isSolid();


	/**
	 * NOTE - this probably shouldn't be messed with,
	 * but Java doesn't have "friend" classes,
	 * and the World needs to access it.
	 * 
	 * @return This Entity's action queue
	 */
	public ActionQueue getActionQueue() {
		return actionQueue;
	}


	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.NONE;
	}


	@Override
	public Entity getEntity() {
		return this;
	}


	/**
	 * @return The world this entity belongs to
	 */
	public World getWorld() {
		return world;
	}


	/**
	 * Returns an ID number associated with this entity. The ID number should
	 * not be user-facing.
	 */
	@Bind(value = SecurityLevel.AUTHOR, doc = "The Unique ID of the Entity")
	public final int getId() {
		return this.id;
	}


	/** Returns the name of this entity. */
	@Bind(value = SecurityLevel.NONE, doc = "Get the Name of the Entity in it's world")
	public final String getName() {
		return this.name;
	}


	@Bind(value = SecurityLevel.AUTHOR, doc = "Set the Name of the Entity")
	public final void setName(LuaValue name) {
		this.name = name.checkjstring();
	}

	public final void setName(String name) {
		this.name = name;
	}


	@Bind(value = SecurityLevel.NONE, doc = "Gets the threshold determining when an entity is deemed to be idle (in seconds).")
	public final float getIdleThreshold() {
		return this.idleThreshold;
	}


	@Bind(value = SecurityLevel.NONE, doc = "Sets the threshold determining when an entity is deemed to be idle (in seconds).")
	public final void setIdleThreshold(LuaValue threshold) {
		float f = (float) threshold.checkdouble();
		f = Math.max(MIN_IDLE_THRESHOLD, f);
		this.idleThreshold = f;
	}


	@Bind(value = SecurityLevel.NONE, doc = "Set the Help associated with this entity.")
	public final void setHelp(LuaValue help) {
		this.help = help.checkjstring();
	}


	public final void setHelp(String help) {
		this.help = help;
	}


	@Bind(value = SecurityLevel.NONE, doc = "Gets the help associated with this Entity.")
	public final String getHelp() {
		return this.help();
	}

	public abstract float getScale();


	/**
	 * Called upon deserialization to load additional variables in a less-traditional manner
	 * 
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		actionQueue = new ActionQueue(this);
		standardizeResources();
		freshenPermissions();
	}


	/**This method will ensure that this Entity contains all the necessary scripts, etc., for a new
	 * instantiated Entity or for an Entity that is deserialized from an earlier source that lacked
	 * those resources.*/
	protected void standardizeResources(){
		// TODO - this might be a good place to add the init script
	}


	/**
	 * @return	The collection of scripts that this entity can run.  CAUTION: this returns a reference to the scripts themselves.
	 */
	public UserScriptCollection getScripts() {
		return this.scripts;
	}


	/**Sets the entity's collection of scripts as indicated.*/
	public void setScripts(UserScript[] newScripts) {
		synchronized (this) {
			this.scripts.clear();
			for (UserScript is : newScripts)
				this.scripts.add(is);
		}
	}


	protected Point2D.Float add(final Point2D.Float toAdd, float x, float y) {
		return new Point2D.Float(toAdd.x + x, toAdd.y + y);
	}


	/**
	 * Get the position left relative to the player
	 * @return
	 */
	protected Point2D.Float left() {
		return add(this.getPosition(), -1f, 0f);
	}


	/**
	 * Get the position right relative to the player
	 * @return
	 */
	protected Point2D.Float right() {
		return add(this.getPosition(), 1f, 0f);
	}


	/**
	 * Get the position up relative to the player
	 * @return
	 */
	protected Point2D.Float up() {
		return add(this.getPosition(), 0f, 1f);
	}


	/**
	 * Get the position down relative to the player
	 * @return
	 */
	protected Point2D.Float down() {
		return add(this.getPosition(), 0f, -1f);
	}


	/**
	 * Convenience function for extracting a Userdata class of the specified type from
	 * a LuaValue argument.
	 * @param clz The UserData type to return
	 * @param lv The LuaValue that contains the user data type
	 * @param <T>
	 * @return
	 */
	public static <T> T userDataOf(Class<T> clz, LuaValue lv) {
		return clz.cast(lv.checktable().get("this").checkuserdata(clz));
	}

	@Bind(value = SecurityLevel.AUTHOR, doc = "Set's the specified value as a global in the target entities sandbox")
	public Entity setGlobal(
			@Doc("The name the value is bound to") LuaValue name,
			@Doc("The Lua Value to bind in the entities sandbox") LuaValue val) {
		getSandbox().getGlobals().set(name.checkjstring(), val);
		return this;
	}


	// =======================================================
	// ====== Entity PERMISSION STUFF ========================
	// =======================================================


	// These permissions can be used to call, i.e.,
	// getPermission(PERMISSION_SELECTION), to find out the level of security
	// for the given function.
	public static final transient String PERMISSION_SCRIPT_EDITOR = "Script editor";
	public static final transient String PERMISSION_ENTITY_EDITOR = "Entity editor";
	public static final transient String PERMISSION_COMMAND_LINE = "Command line";
	public static final transient String PERMISSION_SELECTION = "Selection";
	public static final transient String PERMISSION_PROPERTIES_EDITOR = "Properties editor";
	public static final transient String PERMISSION_ADD_REMOVE_SCRIPTS = "Add/remove scripts";
	public static final transient String PERMISSION_EDIT_HELP = "Edit help";

	private HashMap<String, SecurityLevel> permissions = new HashMap<String, SecurityLevel>();

	private void freshenPermission(String oldName, String newName, SecurityLevel defaultValue) {
		SecurityLevel oldUntransitionedValue = permissions.get(oldName);
		if(oldName == null || oldUntransitionedValue == null) {
			SecurityLevel currentValue = permissions.get(newName);
			if(currentValue == null) {
				permissions.put(newName, defaultValue);
			} else {
				// already ok
			}
		} else {
			permissions.put(newName, oldUntransitionedValue);
			permissions.remove(oldName);
		}
	}
	private void freshenPermissions() {
		if(permissions == null)
			permissions = new HashMap<>();

		SecurityLevel editLevel = SecurityLevel.NONE;
		if(getTeam() == TeamFlavor.AUTHOR) {
			editLevel = SecurityLevel.AUTHOR;
		} else if(getTeam() == TeamFlavor.NONE) {
			// ?
		}

		// set defaults and copy old permissions over
		freshenPermission("ENTITY_EDITOR", PERMISSION_ENTITY_EDITOR, SecurityLevel.NONE);
		freshenPermission("SCRIPT_EDITOR", PERMISSION_SCRIPT_EDITOR, editLevel);
		freshenPermission("PROPERTIES", PERMISSION_PROPERTIES_EDITOR, SecurityLevel.AUTHOR);
		freshenPermission("REPL", PERMISSION_COMMAND_LINE, editLevel);
		freshenPermission("SELECTION", PERMISSION_SELECTION, SecurityLevel.NONE);
		freshenPermission(null, PERMISSION_ADD_REMOVE_SCRIPTS, editLevel);

	}


	/**Returns the permissions associated with this Entity.  Does not reference the whitelist, but
	 * references things like:  can the REPL be accessed through this entity?  Etc*/
	public SecurityLevel getPermission(String name) {
		if (permissions == null)
			permissions = new HashMap<String, SecurityLevel>();
		SecurityLevel s = permissions.get(name);
		if (s == null)
			return SecurityLevel.NONE;
		return s;
	}


	/**Returns a COPY hash map containing all the permissions that have been set for this Entity.*/
	public HashMap<String, SecurityLevel> getPermissions() {
		return new HashMap<String, SecurityLevel>(permissions);
	}


	public void setPermission(String permission, SecurityLevel level) {
		if (permissions == null)
			permissions = new HashMap<String, SecurityLevel>();
		permissions.put(permission, level);
	}


	public void setPermissions(HashMap<String, SecurityLevel> newPermissions) {
		if (permissions == null)
			permissions = new HashMap<String, SecurityLevel>();
		else
			permissions.clear();
		for (Entry<String, SecurityLevel> entry : newPermissions.entrySet())
			permissions.put(entry.getKey(), entry.getValue());

	}


	public Iterable<String> listPermissionNames() {
		return permissions.keySet();
	}
}
