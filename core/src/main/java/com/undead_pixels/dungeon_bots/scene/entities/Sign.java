package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.Inspectable;
import com.undead_pixels.dungeon_bots.scene.LoggingLevel;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

@Doc("An Entity type that can be inspected ")
public class Sign extends Actor implements Inspectable, HasImage {

	private static final long serialVersionUID = 1L;
	
	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Objects/Decor0.png", 1, 5);
	private String message;
	private FloatingText floatingText;

	public Sign(World world, String message, float x, float y) {
		super(world, "sign", DEFAULT_TEXTURE, new UserScriptCollection(), x, y);
		this.message = message;
		floatingText = new FloatingText(this, name+"-text");
		registerListener();
		
		this.getScripts().add(new UserScript("init",
				"registerEnteredListener(function(e)\n" +
				"  e:say(this.inspect())\n" +
				"end)"));
	}

	
	/**
	 * Should only ever be called by the world, in its addEntity
	 * @param world
	 */
	@Override
	public void onAddedToWorld(World world) {
		world.addEntity(floatingText);
	}
	
	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		registerListener();
	}
	
	private void registerListener() {
		world.listenTo(World.EntityEventType.ENTITY_MOVED, this, (e) -> {
			if(e.getPosition().distanceSq(this.getPosition()) < .1) {
				//world.showAlert(this.inspect(), "Sign");
				//this.floatingText.addLine(this.inspect());
				getSandbox().fireEvent("ENTERED", e.getLuaValue());
			}
		});
	}

	@Override
	public LuaSandbox createSandbox() {
		LuaSandbox sandbox = super.createSandbox();
		sandbox.registerEventType("ENTERED", "Called when another entity moves to the same tile as this", "entity");
		sandbox.registerEventType("READ", "Called when something accesses the messsage of this sign");
	
		return sandbox;
	}

	@BindTo("new")
	@Bind(value = SecurityLevel.AUTHOR, doc = "Create a new Sign")
	public static Sign create(
			@Doc("The World the Sign belongs to") LuaValue world,
			@Doc("The Message the Sign should display") LuaValue message,
			@Doc("The X position of the Sign") LuaValue x,
			@Doc("The Y position of the Sign") LuaValue y) {
		return new Sign(userDataOf(World.class, world), message.checkjstring(), x.tofloat(), y.tofloat());
	}

	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.AUTHOR;
	}

	@Override
	public boolean isSolid() {
		return false;
	}

	@Override
	public float getZ() {
		return 1f;
	}

	@Override
	@Bind(SecurityLevel.NONE)
	public String inspect() {
		getSandbox().fireEvent("READ");
		world.message(this, this.message, LoggingLevel.QUEST);
		return message;
	}

	@Bind(value = SecurityLevel.ENTITY, doc = "Change the message that the sign displays")
	public void setMessage(@Doc("The new message that should display") LuaValue message) {
		this.message = message.checkjstring();
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public Image getImage() {
		return DEFAULT_TEXTURE.toImage();
	}
}
