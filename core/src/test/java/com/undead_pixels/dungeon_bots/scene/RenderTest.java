package com.undead_pixels.dungeon_bots.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

import com.undead_pixels.dungeon_bots.nogdx.OrthographicCamera;
import com.undead_pixels.dungeon_bots.nogdx.RenderingContext;

import org.junit.Assert;

public class RenderTest {
	
	public class ColorBuckets {
		private ArrayList<ColorBucket> sortedBuckets;
		private int total = 0;
		private HashMap<Integer, Integer> colorCounts = new HashMap<>();
		
		/**
		 * Does a histogram
		 * 
		 * @param img
		 */
		public ColorBuckets(BufferedImage img) {
			for(int i = 0; i < img.getHeight(); i++) {
				for(int j = 0; j < img.getWidth(); j++) {
					int rgb = img.getRGB(j, i);
					add(rgb);
				}
			}
		}
		
		public void add(int color) {
			Integer count = colorCounts.get(color);
			if(count == null) {
				count = 1;
			} else {
				count += 1;
			}
			
			colorCounts.put(color, count);
			sortedBuckets = null;
			total += 1;
		}
		public int count(int color) {
			Integer ret = colorCounts.get(color);
			
			if(ret == null) {
				return 0;
			}
			
			return ret;
		}
		public int count(Color color) {
			return count(color.getRGB());
		}
		public float fraction(Color color) {
			return count(color) / (float)total;
		}
		
		public int numColors() {
			return colorCounts.size();
		}
		
		private void sort() {
			if(sortedBuckets != null)
				return;
			
			sortedBuckets = new ArrayList<>();
			for(Integer c : colorCounts.keySet()) {
				sortedBuckets.add(new ColorBucket(c, colorCounts.get(c)));
			}
			Collections.sort(sortedBuckets);
		}
		public Color colorAtRank(int rank) {
			sort();
			return new Color(sortedBuckets.get(rank).color);
		}
		public int countAtRank(int rank) {
			sort();
			return sortedBuckets.get(rank).count;
		}
		public float fractionAtRank(int rank) {
			sort();
			return sortedBuckets.get(rank).count / (float)total;
		}
		
		private class ColorBucket implements Comparable<ColorBucket> {
			int color, count;

			public ColorBucket(int color, int count) {
				super();
				this.color = color;
				this.count = count;
			}

			@Override
			public int compareTo(ColorBucket o) {
				if(count < o.count) {
					return 1;
				} else if(count > o.count) {
					return -1;
				}
				return 0;
			}
			
		}
		
		public void printHistogram() {
			System.out.println("Colors visible:");
			for(int i = 0; i < numColors(); i++) {
				System.out.println(i+": "+fractionAtRank(i) + " ("+colorAtRank(i)+")");
			}
		}
	}
	
	
	
	
	
	final Color transparent = new Color(0,0,0,0);
	final Color black = new Color(0,0,0,255);
	
	
	private BufferedImage createAndRenderWorld(int w, int h, String levelName) {

		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		World world = new World(new File(levelName));
		RenderingContext batch = new RenderingContext(img.createGraphics(), img.getWidth(), img.getHeight());
		OrthographicCamera cam = new OrthographicCamera(w, h);
		
		cam.zoomFor(world.getSize());
		cam.setViewportSize(w, h);
		batch.setProjectionMatrix(cam);
		
		world.render(batch);
		
		return img;
	}
	private ColorBuckets createAndHistoWorld(int w, int h, String levelName) {
		return new ColorBuckets(createAndRenderWorld(w, h, levelName));
	}
	
	@Test
	public void drawLevel1() {
		ColorBuckets histo = createAndHistoWorld(800, 600, "level1.lua");
		histo.printHistogram();


		// now check if the render looks decent...
		Assert.assertEquals("No transparent", 0.0f, histo.fraction(transparent), .001f);
		Assert.assertEquals("1/4 black", 0.25f, histo.fraction(black), .001f); // level1 is a square level, and 600/800=.75, so .25 should be background-colored
		Assert.assertTrue("Not bland", histo.numColors() > 8);
		Assert.assertTrue("Not too many colors", histo.numColors() < 1000);
	}
	
	@Test
	public void drawMaze1() {
		ColorBuckets histo = createAndHistoWorld(800, 600, "maze1.lua");
		histo.printHistogram();


		// now check if the render looks decent...
		Assert.assertEquals("No transparent", 0.0f, histo.fraction(transparent), .001f);
		Assert.assertEquals("1/5 black", 0.2f, histo.fraction(black), .001f); // look for background
		Assert.assertTrue("Not bland", histo.numColors() > 8);
		Assert.assertTrue("Not too many colors", histo.numColors() < 1000);
	}
	
	@Test
	public void drawMaze2() {
		ColorBuckets histo = createAndHistoWorld(800, 600, "maze2.lua");
		histo.printHistogram();


		// now check if the render looks decent...
		Assert.assertEquals("No transparent", 0.0f, histo.fraction(transparent), .001f);
		Assert.assertEquals("1/4 black", 0.25f, histo.fraction(black), .001f); // look for background
		Assert.assertTrue("Not bland", histo.numColors() > 8);
		Assert.assertTrue("Not too many colors", histo.numColors() < 1000);
	}
}
