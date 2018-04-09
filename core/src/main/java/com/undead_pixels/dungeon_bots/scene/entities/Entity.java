package com.undead_pixels.dungeon_bots.scene.entities;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;

import com.undead_pixels.dungeon_bots.scene.*;
import com.undead_pixels.dungeon_bots.scene.entities.actions.ActionQueue;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.CanUseItem;
import com.undead_pixels.dungeon_bots.script.*;
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
public abstract class Entity
		implements BatchRenderable, GetLuaSandbox, GetLuaFacade, Serializable, CanUseItem, HasEntity, HasTeam, HasImage, Inspectable {


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
	protected final String name;

	/**The instructions associated with an entity.  These are what is shown in 
	 * the Entity Editor in the instruction pane.  The value can be null or any 
	 * string.*/
	public String help = "This is some example text instructions associated with an entity.";


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
		sandbox.addBindableClasses(LuaReflection.getItemClasses())
				.addBindableClasses(LuaReflection.getEntityClasses());
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
			System.out.println("Skipping entity init (script does not exist for "+this+")");
		}
	}


	/**
	 * Should only ever be called by the world, in its addEntity
	 * @param world
	 */
	public void onAddedToWorld(World world) {
	}


	/** Called during the game loop to update the entity's status. */
	@Override
	public void update(float dt) {
		// TODO - sandbox.resume();
		actionQueue.act(dt);
		if (sandbox != null) {
			sandbox.update(dt);
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
	 * TODO - should this be private?
	 * 
	 * @return This Entity's action queue
	 */
	public ActionQueue getActionQueue() {
		return actionQueue;
	}


	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.NONE; // TODO - store info on the actual team, maybe
								// (or just have overrides do this right)
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
	public final int getId() {
		return this.id;
	}


	/** Returns the name of this entity. */
	public final String getName() {
		return this.name;
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
	}


	/**
	 * @return	The collection of scripts that this entity can run.  CAUTION: this returns a reference to the scripts themselves.
	 */
	public UserScriptCollection getScripts() {
		return this.scripts;
	}


	/**Sets the entity's collection of scripts as indicated.*/
	public void setScripts(UserScript[] newScripts) {
		this.scripts.clear();
		for (UserScript is : newScripts)
			this.scripts.add(is);
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


	private HashMap<String, SecurityLevel> permissions = new HashMap<String, SecurityLevel>();


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


	/**Sets the permissions associated with this Entity.  Does not reference the whitelist, but 
	 * references things like:  can the REPL be accessed through this entity?  Etc*/
	public void setSecurityLevel(String name, SecurityLevel level) {
		if (permissions == null)
			permissions = new HashMap<String, SecurityLevel>();
		permissions.put(name, level);
	}
}
