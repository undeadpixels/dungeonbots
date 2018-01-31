package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetBindable;
import com.undead_pixels.dungeon_bots.script.interfaces.Scriptable;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import org.luaj.vm2.LuaValue;

import java.util.Optional;

@BindTo("Debug")
public class LuaDebug implements GetBindable, Scriptable {
	private String stdout;
	private LuaSandbox luaSandbox;
	private LuaValue luaValue;

	public LuaDebug(LuaSandbox luaSandbox) {
		this.luaSandbox = luaSandbox;
	}

	public void reset() {
		stdout = null;
	}

	@Bind(SecurityLevel.DEFAULT)
	public void print(LuaValue str) {
		String val = str.checkjstring();
		if(stdout != null)
			stdout += ("\n" + val);
		else
			stdout = val;
	}

	public Optional<String> getPrint() {
		return Optional.ofNullable(stdout);
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public String getName() {
		return "debug";
	}

	@Override
	public LuaSandbox getSandbox() {
		return this.luaSandbox;
	}

	@Override
	public LuaValue getLuaValue() {
		if(luaValue == null) {
			luaValue = LuaProxyFactory.getLuaValue(this);
		}
		return luaValue;
	}
}
