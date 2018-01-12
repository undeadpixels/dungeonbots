package com.undead_pixels.dungeon_bots.scene;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Texture;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.script.LuaScript;

public class World implements Renderable {
    private LuaScript levelScript;

	private Texture backgroundImage;
	private Tile[][] tiles;
    private ArrayList<Actor> dynamicObjects = new ArrayList<>();
    
    
	@Override
	public void update(double dt) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void render() {
		// TODO Auto-generated method stub
		
	}
    
}
