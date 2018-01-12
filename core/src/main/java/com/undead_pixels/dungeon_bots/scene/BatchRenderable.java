package com.undead_pixels.dungeon_bots.scene;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface BatchRenderable {

    public void update(double dt);
    public void render(SpriteBatch batch);
    public float getZ();
    
}
