package com.undead_pixels.dungeon_bots;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.script.LuaScriptEnvironment;
import com.undead_pixels.dungeon_bots.script.ScriptStatus;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class ScriptApiTest {

    @Test
    public void testGetBindings() {
        Actor player = new Actor(new World(), "player", null);
        LuaScriptEnvironment se = player.getScriptEnvironment(SecurityLevel.DEBUG);
        LuaScript luaScript = se.scriptFromString("player.up();");
        luaScript.start().join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue(Math.abs(player.getPosition().y + 1.0) < 0.01);
    }
}
