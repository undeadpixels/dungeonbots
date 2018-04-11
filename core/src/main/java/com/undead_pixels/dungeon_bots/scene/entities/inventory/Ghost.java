package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.RpgActor;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

public class Ghost extends RpgActor  {
	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Characters/Undead0.png", 2, 4);

	public Ghost(World world, float x, float y) {
		super(world, "ghost", DEFAULT_TEXTURE, initUserScripts(), x, y);
	}

	@Override
	public boolean isSolid() {
		return true;
	}

	@Override
	public LuaSandbox getSandbox() {
		final LuaSandbox sandbox =  super.getSandbox();
		sandbox.registerEventType("ENTER", "called when an entity enters the Ghosts space", "entity");
		world.listenTo(World.EntityEventType.ENTITY_MOVED, this, (e) -> {
			if(e.getPosition().distance(this.getPosition()) < 0.1) {
				getSandbox().fireEvent("ENTER", e.getLuaValue());
			}
		});
		return sandbox;
	}

	public static UserScriptCollection initUserScripts() {
		return new UserScriptCollection();
	}

	@Override
	public String inspect() {
		return this.getClass().getSimpleName();
	}
}
