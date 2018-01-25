package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.SecurityContext;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;

public class Player extends Actor {

	public Player(World world, String name, TextureRegion tex) {
		super(world, name, tex);
	}

	public Player(World world, String name, LuaSandbox script, TextureRegion tex) {
		super(world, name, script, tex);
	}

	@Bind @BindTo("new")
	public static LuaValue generate(LuaValue world, LuaValue x, LuaValue y) {
		World w = (World)world.checktable().get("this").checkuserdata(World.class);
		Player p = new Player(w, "player", new LuaSandbox(), null);
		if(SecurityContext.getActiveSecurityLevel() == SecurityLevel.DEBUG)
			SecurityContext.getWhitelist().addWhitelist(p.permissiveWhitelist());
		p.sprite.setX((float)x.checkdouble());
		p.sprite.setY((float)y.checkdouble());
		return LuaProxyFactory.getLuaValue(p, SecurityContext.getActiveSecurityLevel());
	}
}
