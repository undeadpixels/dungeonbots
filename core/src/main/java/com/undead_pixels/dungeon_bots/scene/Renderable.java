package com.undead_pixels.dungeon_bots.scene;

/**
 * An interface for objects that should be rendered (but not batched)
 */
public interface Renderable {

    /**
     * Updates this Renderable
     * @param dt		delta time
     */
    public void update(double dt);
    
    /**
     * Renders this Renderable
     */
    public void render();
    
}
