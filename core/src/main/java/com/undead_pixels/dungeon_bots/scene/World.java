package com.undead_pixels.dungeon_bots.scene;

import java.util.ArrayList;

import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.script.LuaScript;

public class World {
    private LuaScript levelScript;
    
    private Map map;
    private ArrayList<Actor> dynamicObjects = new ArrayList<>();
}
