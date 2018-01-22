package com.undead_pixels.dungeon_bots.scene;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Something with both render(SpriteBatch) and update(dt) functions.
 * Basically anything that's renderable but should be batched with other similar things.
 */
public interface BatchRenderable {

    /**
     * Updates this Renderable. Called at some point before render().
     * @param dt		delta time
     */
    public void update(float dt);
    
    /**
     * Renders this Renderable.
     * All rendering should be performed in tile-space, and a camera transform will automagically turn things into screen-space
     * 
     * @param batch		The batch to insert this renderable into
     */
    public void render(SpriteBatch batch);
    
    
    /**
     * @return	the requested z (layer) value
     */
    public float getZ();
    
}
