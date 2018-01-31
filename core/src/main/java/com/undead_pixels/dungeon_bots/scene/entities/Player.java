package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import org.luaj.vm2.LuaValue;

public class Player extends Actor {

	public static TextureRegion PLAYER_TEXTURE;
	private static int TILESIZE = 16;

	public Player(World world, String name) {
		super(world, name, PLAYER_TEXTURE);
	}

	@Bind @BindTo("new")
	public static Player newPlayer(LuaValue world, LuaValue x, LuaValue y) {
		World w = (World)world.checktable().get("this").checkuserdata(World.class);
		Player p = new Player(w, "player");
		SecurityContext.getWhitelist().add(p);
		p.sprite.setX((float)x.checkdouble());
		p.sprite.setY((float)y.checkdouble());
		return p;
	}



}
