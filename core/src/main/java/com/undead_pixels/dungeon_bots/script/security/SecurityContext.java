package com.undead_pixels.dungeon_bots.script.security;

import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

public class SecurityContext {

    private Whitelist whitelist = new Whitelist();
	private SecurityLevel securityLevel = SecurityLevel.NONE;
	private World world;
	private Entity entity;
	private TeamFlavor team = TeamFlavor.NONE;
}
