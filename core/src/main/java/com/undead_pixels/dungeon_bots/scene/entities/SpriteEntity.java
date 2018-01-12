package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaScript;

public abstract class SpriteEntity extends Entity {
	
	protected Sprite sprite;

	public SpriteEntity(World world, Sprite sprite) {
		super(world);
		if(sprite == null)
			sprite = new Sprite();
		this.sprite = sprite;
		// TODO Auto-generated constructor stub
	}

	public SpriteEntity(World world, LuaScript script, Sprite sprite) {
		super(world, script);
		if(sprite == null)
			sprite = new Sprite();
		this.sprite = sprite;
		// TODO Auto-generated constructor stub
	}
	public SpriteEntity(World world, TextureRegion tex) {
		this(world, new Sprite(tex));
	}

	public SpriteEntity(World world, LuaScript script, TextureRegion tex) {
		this(world, script, new Sprite(tex));
	}

	@Override
	public void update(double dt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(SpriteBatch batch) {
		if(sprite != null)
			sprite.draw(batch);
	}

	@Override
	public Vector2 getPosition() {
		return new Vector2(sprite.getX(), sprite.getY());
	}

}
