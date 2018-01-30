package com.undead_pixels.dungeon_bots.scene.entities.actions;

import com.undead_pixels.dungeon_bots.queueing.Taskable;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;

/**
 * An action that can be performed on an Entity
 */
public interface Action extends Taskable<Entity> {
}
