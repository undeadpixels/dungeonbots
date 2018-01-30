package com.undead_pixels.dungeon_bots.file;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TileRegionSection extends GameEditorStateSection {
	
	private ArrayList<TileRegion> regions = new ArrayList<>();

	@Override
	public String toLua() {
		String ret = "";
		for(TileRegion r: regions) {
			if(!ret.isEmpty()) {
				ret += "\n";
			}
			ret += r.toLua();
		}
		return ret;
	}

	@Override
	public void updateFromLuaString(String[] luaCode) throws ParseException {
		for(String line : luaCode) {
			regions.add(new TileRegion(line));
		}
	}

	public void add(TileRegion tileRegion) {
		regions.add(tileRegion);
	}
	
	
	public static class TileRegion {
		final int x0, x1, y0, y1;
		final String tileName;

		public TileRegion(int x0, int x1, int y0, int y1, String tileName) {
			super();
			this.x0 = x0;
			this.x1 = x1;
			this.y0 = y0;
			this.y1 = y1;
			this.tileName = tileName;
		}
		
		public TileRegion(String luaCode) throws ParseException {

			Pattern noFor = Pattern.compile(
					"world:setTile\\((\\d+), (\\d+), tileTypes\\.getTile\\(\\\"([A-Za-z0-9_]+)\\\"\\)\\)\n");

			//Pattern oneFor = Pattern.compile(
			//		"for (x|y)=(\\d+),(\\d+) do\n" +
			//		"  world:setTile\\((\\d+|x), (\\d+|y), tileTypes\\.([A-Za-z0-9_]+))\n" +
			//		"end");
			Pattern twoFor = Pattern.compile(
					"for x=(\\d+),(\\d+) do\n" +
					"  for y=(\\d+),(\\d+) do\n" +
					"    world:setTile\\(x, y, tileTypes\\.getTile\\(\\\"([A-Za-z0-9_]+)\\\"\\)\\)\n" +
					"  end" +
					"end");

			Matcher noForMatcher = noFor.matcher(luaCode);
			//Matcher oneForMatcher = oneFor.matcher(luaCode);
			Matcher twoForMatcher = twoFor.matcher(luaCode);

			if(noForMatcher.matches()) {
				String[] strs = extract(noForMatcher);

				x0 = intAt(strs, 0, 0);
				y0 = intAt(strs, 1, 0);
				x1 = x0;
				y1 = y0;
				tileName = stringAt(strs, 2, "");
				
			//} else if(oneForMatcher.matches()) {
			//	String[] strs = extract(noForMatcher);
			} else if(twoForMatcher.matches()) {
				String[] strs = extract(noForMatcher);

				x0 = intAt(strs, 0, 0);
				x1 = intAt(strs, 1, 0);
				y0 = intAt(strs, 2, 0);
				y1 = intAt(strs, 3, 0);
				tileName = stringAt(strs, 4, "");
			} else {
				throw new ParseException("Unable to parse Tile Region", 0);
			}
			
		}

		public String toLua() {
			String ret = "";
			int indent = 0;
			String X = ""+x0;
			String Y = ""+y0;
			
			boolean useFors = x0 != x1 || y0 != y1;
			
			if(useFors) {
				ret += "for x in "+x0+","+x1+" do\n";
				X = "x";
				indent++;
			}
			if(useFors) {
				ret += tab(indent)+"for y in "+y0+","+y1+" do\n";
				Y = "y";
				indent++;
			}
			
			ret += indent+"world:setTile("+X+", "+Y+", tileTypes."+tileName+")\n";
			
			if(useFors) {
				indent--;
				ret += tab(indent)+"end\n";
			}
			if(useFors) {
				indent--;
				ret += tab(indent)+"end\n";
			}

			return ret;
		}
	}
	
}
