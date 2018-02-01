package com.undead_pixels.dungeon_bots.file.editor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.undead_pixels.dungeon_bots.scene.World;

/**
 * A section of a level script indicating the tile map
 */
public class TileRegionSection extends LevelScriptSection {

	/**
	 * The world
	 */
	private World world;
	
	/**
	 * A list of each tile (or region of tiles)
	 */
	private ArrayList<TileRegion> regions = new ArrayList<>();

	public TileRegionSection(World world) {
		this.world = world;
	}

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
	public void updateFromLuaStrings(String[] luaCode) throws ParseException {
		for(String line : luaCode) {
			regions.add(new TileRegion(line));
		}
	}

	/**
	 * @param tileRegion		The region to add to this section
	 */
	public void add(TileRegion tileRegion) {
		regions.add(tileRegion);
		
		for(int i = tileRegion.x0; i <= tileRegion.x1; i++) {
			for(int j = tileRegion.y0; j <= tileRegion.y1; j++) {
				world.setTile(i, j, world.getTileTypes().getTile(tileRegion.tileName));
			}
		}
	}
	
	
	/**
	 * A region of tiles (always axis-aligned rectangular)
	 * 
	 * Can also represent a single tile
	 */
	public static class TileRegion {
		/**
		 * Position of this region
		 */
		final int x0, x1, y0, y1;
		
		/**
		 * Name of the tile used in this region
		 */
		final String tileName;

		/**
		 * Constructor
		 * 
		 * @param x0		Left side
		 * @param x1		Right side
		 * @param y0		Bottom side
		 * @param y1		Top side
		 * @param tileName
		 */
		public TileRegion(int x0, int x1, int y0, int y1, String tileName) {
			super();
			this.x0 = x0;
			this.x1 = x1;
			this.y0 = y0;
			this.y1 = y1;
			this.tileName = tileName;
		}
		
		/**
		 * Constructor
		 * 
		 * @param luaCode		Lua code that can generate a region
		 * @throws ParseException	If this code doesn't look like this class's definition of a region
		 */
		public TileRegion(String luaCode) throws ParseException {

			Pattern noFor = Pattern.compile(
					"world:setTile\\((\\d+), (\\d+), tileTypes\\.getTile\\(\\\"([A-Za-z0-9_]+)\\\"\\)\\)");

			//Pattern oneFor = Pattern.compile(
			//		"for (x|y)=(\\d+),(\\d+) do\n" +
			//		"  world:setTile\\((\\d+|x), (\\d+|y), tileTypes\\.([A-Za-z0-9_]+))\n" +
			//		"end");
			Pattern twoFor = Pattern.compile(
					"\\s*for x=(\\d+),(\\d+) do\n" +
					"\\s*for y=(\\d+),(\\d+) do\n" +
					"\\s*world:setTile\\(x, y, tileTypes\\.getTile\\(\\\"([A-Za-z0-9_]+)\\\"\\)\\)\n" +
					"\\s*end\n" +
					"\\s*end");

			Matcher noForMatcher = noFor.matcher(luaCode);
			//Matcher oneForMatcher = oneFor.matcher(luaCode);
			Matcher twoForMatcher = twoFor.matcher(luaCode);

			if(noForMatcher.matches()) {
				String[] strs = extractGroupsFromText(noForMatcher);

				x0 = intAt(strs, 0, 0);
				y0 = intAt(strs, 1, 0);
				x1 = x0;
				y1 = y0;
				tileName = stringAt(strs, 2, "");
				
			//} else if(oneForMatcher.matches()) {
			//	String[] strs = extract(oneForMatcher);
			} else if(twoForMatcher.matches()) {
				String[] strs = extractGroupsFromText(twoForMatcher);

				x0 = intAt(strs, 0, 0);
				x1 = intAt(strs, 1, 0);
				y0 = intAt(strs, 2, 0);
				y1 = intAt(strs, 3, 0);
				tileName = stringAt(strs, 4, "");
			} else {
				throw new ParseException("Unable to parse Tile Region:\n"+luaCode, 0);
			}
			
		}

		/**
		 * @return	This TileRegion, converted to Lua code
		 */
		public String toLua() {
			String ret = "";
			int indent = 0;
			String X = ""+x0;
			String Y = ""+y0;
			
			boolean useFors = (x0 != x1) || (y0 != y1);
			
			if(useFors) {
				ret += "for x="+x0+","+x1+" do\n";
				X = "x";
				indent++;
			}
			if(useFors) {
				ret += tab(indent)+"for y="+y0+","+y1+" do\n";
				Y = "y";
				indent++;
			}

			ret += tab(indent)+"world:setTile("+X+", "+Y+", tileTypes.getTile(\""+tileName+"\"))";
			
			if(useFors) {
				ret += "\n";
				indent--;
				ret += tab(indent)+"end\n";
			}
			if(useFors) {
				indent--;
				ret += tab(indent)+"end";
			}

			return ret;
		}
	}
	
}
