package com.undead_pixels.dungeon_bots.file.editor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

import com.undead_pixels.dungeon_bots.scene.World;

/**
 * A state object for the game editor.
 * Backed by both a world and some serializable/deserializable pieces.
 */
public class GameEditorState {
	
	private LinkedHashMap<String, LevelScriptSection> sections = new LinkedHashMap<>();
	
	
	public final WorldSizeSection worldSizeSection; // TODO - make private?
	public final TileRegionSection tileRegionSection; // TODO - make private?
	public final PlayerInitSection playerInitSection; // TODO - make private?
	public final World world;

	public GameEditorState(World world) {
		this.world = world;
		
		sections.put("world size",
				worldSizeSection = new WorldSizeSection(world));
		sections.put("load custom assets",
				new FakeEditorStateSection(""));
		sections.put("register tiles",
				new FakeEditorStateSection(""));
		sections.put("tiles",
				tileRegionSection = new TileRegionSection(world));
		sections.put("player init",
				playerInitSection = new PlayerInitSection(world));
		sections.put("bots init",
				new FakeEditorStateSection(""));
		sections.put("enemies init",
				new FakeEditorStateSection(""));
		sections.put("whitelist",
				new FakeEditorStateSection(""));
		sections.put("settings",
				new FakeEditorStateSection(""));
	}

	public GameEditorState(World world, String luaCode) throws ParseException {
		this(world); // init the defaults
		
		Scanner sc = new Scanner(luaCode);
		
		LevelScriptSection currentSection = new FakeEditorStateSection("");
		String prevLine = "";
		boolean hadIndent = false;
		ArrayList<String> currentLineList = new ArrayList<>();
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			
			if(line.startsWith("-- Editor Section: ")) {
				String sectionName = line.split("\\: ", 2)[1];
				
				LevelScriptSection newSection = sections.get(sectionName);
				
				if(newSection == null) {
					sc.close();
					throw new ParseException("Could not understand section header: "+line, 0);
				}
				
				prevLine = "";
				currentSection = newSection;
				currentLineList.clear();
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
						//System.out.println("Updating from list of length "+s.length+"\n "+Arrays.toString(s));
						currentSection.updateFromLuaStrings(s);
					}
					currentLineList.clear();
					
					hadIndent = false;
				} else if(!line.isEmpty()) {
					if(hadIndent) {
						prevLine += "\n"+line;
					}
					if(!prevLine.isEmpty()) {
						currentLineList.add(prevLine);
					}

					if(!hadIndent) {
						prevLine = line;
					} else {
						prevLine = "";
					}
					
					hadIndent = false;
					
				}
			}
			
		}
		
		sc.close();
		
	}

	public String toLua() {
		StringBuilder sb = new StringBuilder();
		
		for(String name: sections.keySet()) {
			LevelScriptSection s = sections.get(name);
			
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
