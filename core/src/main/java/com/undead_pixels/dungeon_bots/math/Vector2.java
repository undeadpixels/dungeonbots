package com.undead_pixels.dungeon_bots.math;

public class Vector2 {
	public final float x, y;

	public Vector2(float x, float y) {
		super();
		this.x = x;
		this.y = y;
	}

	public Vector2() {
		this(0, 0);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}
	
	@Override
	public String toString(){
		return x + ", " + y;
	}
	
}
