package com.undead_pixels.dungeon_bots.script.security;

import java.lang.reflect.Method;
import java.util.Optional;

import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.HasImage;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.HasEntity;
import com.undead_pixels.dungeon_bots.script.interfaces.HasTeam;

public class SecurityContext {

    private Whitelist whitelist;
	private SecurityLevel securityLevel = SecurityLevel.NONE;
	private World world;
	private Entity entity;
	private TeamFlavor team = TeamFlavor.NONE;
	

	public SecurityContext(Whitelist whitelist, SecurityLevel securityLevel, World world, Entity entity,
			TeamFlavor team) {
		super();
		this.whitelist = whitelist;
		this.securityLevel = securityLevel;
		this.world = world;
		this.entity = entity;
		this.team = team;
	}

	/**
	 * A player-level or author-level security context for a given entity.
	 * 
	 * The actual level is determined by the entity's team.
	 *
	 * @param entity
	 */
	public SecurityContext(Entity entity) {
		super();
		this.world = entity.getWorld();
		this.whitelist = world.getWhitelist();
		switch(entity.getTeam()) {
		case AUTHOR:
			this.securityLevel = SecurityLevel.AUTHOR;
			break;
		case PLAYER:
			this.securityLevel = SecurityLevel.TEAM;
			break;
		case NONE:
		default:
			this.securityLevel = SecurityLevel.NONE;
			break;
		
		}
		this.entity = entity;
		this.team = entity.getTeam();
	}

	/**
	 * An author-level security context for the world
	 * @param world
	 */
	public SecurityContext(World world) {
		super();
		this.whitelist = world.getWhitelist();
		this.securityLevel = SecurityLevel.AUTHOR;
		this.world = world;
		this.entity = null;
		this.team = TeamFlavor.AUTHOR;
	}
	
	public boolean canExecute(Object o, Method method) {
		SecurityLevel level = whitelist.getLevel(method);
		
		TeamFlavor oTeam = TeamFlavor.NONE;
		
		if(o != null && o instanceof HasEntity) {
			Entity e = ((HasEntity)o).getEntity();
			if(e != null) {
				oTeam = e.getTeam();
			}
		}
		if(o == null) {
			oTeam = TeamFlavor.PLAYER;
		} else if(o instanceof HasTeam) {
			oTeam = ((HasTeam)o).getTeam();
		}
		
		switch(level) {
		case DEBUG:
			return securityLevel == SecurityLevel.DEBUG;
		case AUTHOR:
			return securityLevel.level >= SecurityLevel.AUTHOR.level
				|| team == TeamFlavor.AUTHOR;
		case ENTITY:
			return securityLevel.level >= SecurityLevel.AUTHOR.level
				|| team == TeamFlavor.AUTHOR
				|| o == null
				|| entity == o;
		case TEAM:
			return securityLevel.level >= SecurityLevel.AUTHOR.level
				|| team == TeamFlavor.AUTHOR
				|| oTeam == team;
		case NONE:
		case DEFAULT:
			return true;
		
		default:
			return false;
		}
	}

	public Whitelist getWhitelist() {
		return whitelist;
	}

	public SecurityLevel getSecurityLevel() {
		return securityLevel;
	}

	/**
	 * 
	 */
	public Entity getEntity () {
		return entity;
	}

	/**
	 * @return
	 */
	public String getOwnerName () {
		if(entity != null) {
			return entity.getName();
		}
		return "world";
	}
	
	public HasImage getOwner() {
		if(entity != null) {
			return entity;
		}
		return world;
	}

public Optional<World> getWorld() {
		return Optional.ofNullable(world);
	}
}
