package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor.Direction;
import com.undead_pixels.dungeon_bots.script.LuaScript;

public class Actor extends SpriteEntity {

	public enum Direction {
		UP, DOWN, LEFT, RIGHT
	}

	public Actor(World world, TextureRegion tex) {
		super(world, tex);
		// TODO Auto-generated constructor stub
	}
	public Actor(World world, LuaScript script, TextureRegion tex) {
		super(world, script, tex);
		// TODO Auto-generated constructor stub
	}
	public Actor(World world, Sprite sprite) {
		super(world, sprite);
		// TODO Auto-generated constructor stub
	}
	public Actor(World world, LuaScript script, Sprite sprite) {
		super(world, script, sprite);
		// TODO Auto-generated constructor stub
	}

	@Override
	public float getZ() {
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	public boolean isSolid() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void moveInstantly(Direction dir, int dist) {
		// TODO - DELETEME
		if(dir == Direction.UP) {
			sprite.setY(sprite.getY() - dist);
		} else if(dir == Direction.DOWN) {
			sprite.setY(sprite.getY() + dist);
		} else if(dir == Direction.LEFT) {
			sprite.setX(sprite.getX() - dist);
		} else if(dir == Direction.RIGHT) {
			sprite.setX(sprite.getX() + dist);
		}
	}

}
