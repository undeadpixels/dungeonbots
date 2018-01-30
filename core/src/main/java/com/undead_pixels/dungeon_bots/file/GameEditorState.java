package com.undead_pixels.dungeon_bots.file;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class GameEditorState {
	
	private LinkedHashMap<String, GameEditorStateSection> sections = new LinkedHashMap<>();
	public WorldSizeSection worldSizeSection; // TODO - make private
	public TileRegionSection tileRegionSection; // TODO - make private
	public PlayerInitSection playerInitSection; // TODO - make private

	public GameEditorState() {
		sections.put("world size",
				worldSizeSection = new WorldSizeSection());
		sections.put("load custom assets",
				new FakeSection(""));
		sections.put("register tiles",
				new FakeSection(""));
		sections.put("tiles",
				tileRegionSection = new TileRegionSection());
		sections.put("player init",
				playerInitSection = new PlayerInitSection());
		sections.put("bots init",
				new FakeSection(""));
		sections.put("enemies init",
				new FakeSection(""));
		sections.put("whitelist",
				new FakeSection(""));
		sections.put("settings",
				new FakeSection(""));
	}

	public GameEditorState(String luaCode) throws ParseException {
		this(); // init the defaults
		
		Scanner sc = new Scanner(luaCode);
		
		GameEditorStateSection currentSection = new FakeSection("");
		String prevLine = "";
		boolean hadIndent = false;
		ArrayList<String> currentLineList = new ArrayList<>();
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			
			if(line.startsWith("-- Editor Section: ")) {
				String sectionName = line.split("\\: ", 2)[1];
				
				GameEditorStateSection newSection = sections.get(sectionName);
				
				if(newSection == null) {
					sc.close();
					throw new ParseException("Could not understand section header: "+line, 0);
				}
			} else {
				if(line.startsWith(" ")) { // indented section
					hadIndent = true;
					prevLine += "\n" + line;
				} else if(line.isEmpty()) { // end of section
					if(!prevLine.isEmpty()) {
						currentLineList.add(prevLine);
					}
					prevLine = "";
					String[] s = currentLineList.toArray(new String[0]);
					if(s.length > 0) {
						currentSection.updateFromLuaString(s);
					}
					currentLineList.clear();
				} else if(!line.isEmpty()) {
					if(hadIndent) {
						prevLine += line;
					}
					if(!prevLine.isEmpty()) {
						currentLineList.add(prevLine);
					}

					if(!hadIndent) {
						prevLine = line;
					} else {
						prevLine = "";
					}
					
				}
			}
			
		}
		
		sc.close();
		
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
}
