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
public class Switch extends SpriteEntity implements Useable , HasImage {

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
		super(world, "switch", DISABLED_TEXTURE, new UserScriptCollection(), x, y);
	}

	@Override
	public boolean isSolid() {
		return true;
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
		sandbox.registerEventType("USE");
		return sandbox;
	}

	@Override
	public String inspect() {
		return this.getClass().getSimpleName();
	}

	public Image getImage() {
		return getTexture().toImage();
	}
}
