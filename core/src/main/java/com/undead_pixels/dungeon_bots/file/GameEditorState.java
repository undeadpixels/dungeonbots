package com.undead_pixels.dungeon_bots.file;

import java.text.ParseException;
import java.util.LinkedHashMap;

public class GameEditorState {
	
	private LinkedHashMap<String, GameEditorStateSection> sections = new LinkedHashMap<>();

	public GameEditorState() {
		sections.put("world size",
				new GameEditorStateSection.Fake("world.setSize(16, 16)"));
		sections.put("load custom assets",
				new GameEditorStateSection.Fake(""));
		sections.put("register tiles",
				new GameEditorStateSection.Fake(""));
		sections.put("tiles",
				new GameEditorStateSection.Fake(
						"for i in 1,16 do\n" + 
						"        for j in 1,16 do\n" + 
						"            if i == 1 or i == 16 or j == 1 or j == 16 then\n" + 
						"                world:setTile(j, i, tileTypes.wall)\n" + 
						"            else\n" + 
						"                world:setTile(j, i, tileTypes.floor)\n" + 
						"            end\n" + 
						"        end\n" + 
						"    end"));
		sections.put("player init",
				new GameEditorStateSection.Fake(
						"world.player = world.newPlayer(2, 2)\n" + 
						"world.player:setCode(\"autobind()\")"));
		sections.put("bots init",
				new GameEditorStateSection.Fake(""));
		sections.put("enemies init",
				new GameEditorStateSection.Fake(""));
		sections.put("whitelist",
				new GameEditorStateSection.Fake(""));
		sections.put("settings",
				new GameEditorStateSection.Fake(""));
	}

	public GameEditorState(String luaCode) {
		this(); // init the defaults
		
		
		// TODO - parse
		
	}

	public String toLua() {
		StringBuilder sb = new StringBuilder();
		
		for(String name: sections.keySet()) {
			GameEditorStateSection s = sections.get(name);
			
			String sectionText = s.toLua();
			
			if(!sectionText.isEmpty()) {
				sb.append("-- Editor Section: "+name);
				sb.append("\n");
				sb.append(sectionText);
				sb.append("\n\n");
			}
			
		}
		
		return sb.toString();
	}
	
	public static abstract class GameEditorStateSection {
		public abstract String toLua();
		public abstract void updateFromLuaString(String luaCode) throws ParseException;
		
		
		/**
		 * A fake GameEditorSection, just used for planning
		 */
		public static class Fake extends GameEditorStateSection {
			private String str;

			public Fake(String str) {
				super();
				this.str = str;
			}

			@Override
			public String toLua() {
				return str;
			}

			@Override
			public void updateFromLuaString(String luaCode) throws ParseException {
				throw new ParseException("Fake class cannot update itself. It's a fake.", 0);
			}
			
			
		}
	}
}
