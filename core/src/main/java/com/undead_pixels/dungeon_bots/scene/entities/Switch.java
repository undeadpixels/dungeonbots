package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.LuaValue;

import java.awt.*;

@Doc("A Switch is an Entity that's contextual use function invokes a event")
public class Switch extends Actor implements Useable , HasImage {

	public final static TextureRegion DISABLED_TEXTURE =
			AssetManager.getTextureRegion("tiny16/things.png", 4, 4);
	public final static TextureRegion ENABLED_TEXTURE =
			AssetManager.getTextureRegion("tiny16/things.png", 3, 4);


	private boolean enabled = false;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Switch(World world, float x, float y) {
		super(world, "switch", DISABLED_TEXTURE, initScripts(), x, y);
	}

	@Override
	public boolean isSolid() {
		return false;
	}

	@Override
	public float getZ() {
		return 0;
	}

	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.AUTHOR;
	}

	@Override
	@Bind(value=SecurityLevel.ENTITY, doc = "Use the given switch")
	public Boolean use() {
		// Signal appropriate observer to invoke onUse script if present
		return toggleEnabled();
	}

	@Bind(value = SecurityLevel.ENTITY, doc = "Sets the current state of the switch")
	public Switch setEnabled(@Doc("A boolean value") LuaValue enabled) {
		return setEnabled(enabled.checkboolean());
	}

	private Switch setEnabled(boolean b) {
		this.enabled = b;
		sprite.setTexture(getTexture());
		getSandbox().fireEvent("USE");
		world.updateEntity(this);
		return this;
	}

	@Bind(value = SecurityLevel.DEFAULT, doc = "Get the Switch's enabled flag")
	public Boolean getEnabled() {
		return enabled;
	}

	private boolean toggleEnabled() {
		setEnabled(!enabled);
		return enabled;
	}

	public TextureRegion getTexture() {
		return  enabled ? ENABLED_TEXTURE : DISABLED_TEXTURE;
	}

	@Override
	public LuaSandbox createSandbox() {
		LuaSandbox sandbox = super.createSandbox();
		sandbox.registerEventType("USE", "Called when the Switch is invoked with use<dir> from an entity");
		return sandbox;
	}

	@Override
	public String inspect() {
		return this.getClass().getSimpleName();
	}

	public Image getImage() {
		return getTexture().toImage();
	}

	private static UserScriptCollection initScripts() {
		UserScriptCollection scripts = new UserScriptCollection();
		scripts.add(new UserScript("init", "registerUseListener(function()\n" +
				"    -- Have the door do something\n" +
				"    -- Example:\n" +
				"    -- local door = world:findEntity('door1')\n" +
				"    -- if door then\n" +
				"    --   door:unlock()\n" +
				"    -- end\n" +
				"end)"));
		return scripts;
	}
}
