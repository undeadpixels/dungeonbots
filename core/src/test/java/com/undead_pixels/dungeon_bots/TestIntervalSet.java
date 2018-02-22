package com.undead_pixels.dungeon_bots;

import static org.junit.Assert.*;

import org.junit.Test;

import com.undead_pixels.dungeon_bots.math.IntegerSet;
import com.undead_pixels.dungeon_bots.math.IntervalSet;

public class TestIntervalSet {

	@Test
	public void testInclude_nonInfinite() {
		IntervalSet<Integer> set = new IntegerSet();
		assertTrue(set.getIntervalsCount() == 0);
		for (int i = 0; i <= 10; i++)
			assertFalse(set.includes(i));
		set.add(0, 10);
		assertFalse(set.includes(-1));
		for (int i = 0; i <= 10; i++)
			assertTrue(set.includes(i));
		assertFalse(set.includes(11));
		assertTrue(set.getIntervalsCount() == 1);

		assertFalse(set.add(8));
		assertTrue(set.includes(8));
		assertTrue(set.getIntervalsCount() == 1);

		assertTrue(set.add(12));
		assertTrue(set.includes(12));
		assertFalse(set.includes(11));
		assertFalse(set.includes(13));
		assertTrue(set.getIntervalsCount() == 2);

		set.add(11, 15);
		for (int i = 0; i <= 15; i++)
			assertTrue(set.includes(i));
		assertFalse(set.includes(-1));
		assertFalse(set.includes(16));
		assertTrue(set.getIntervalsCount() == 1);

		set.add(20, 25);
		for (int i = 0; i <= 15; i++)
			assertTrue(set.includes(i));
		for (int i = 20; i <= 25; i++)
			assertTrue(set.includes(i));
		for (int i = 16; i <= 19; i++)
			assertFalse(set.includes(i));
		assertFalse(set.includes(-1));
		assertFalse(set.includes(26));
		assertTrue(set.getIntervalsCount() == 2);

		set.add(30);
		set.add(-5);
		assertTrue(set.includes(30));
		assertTrue(set.includes(-5));
		assertTrue(set.getIntervalsCount() == 4);

		set.add(35, 40);
		for (int i = 35; i <= 40; i++)
			assertTrue(set.includes(i));
		set.add(0, 25);
		for (int i = 0; i <= 25; i++)
			assertTrue(set.includes(i));
		for (int i = 26; i <= 29; i++)
			assertFalse(set.includes(i));
		assertTrue(set.getIntervalsCount() == 4);

		set.add(25, 35);
		for (int i = 0; i <= 40; i++)
			assertTrue(set.includes(i));
		assertTrue(set.getIntervalsCount() == 2);

		set.add(-10, -15);
		for (int i = -15; i <= -10; i++)
			assertTrue(set.includes(i));
		assertFalse(set.includes(-16));
		assertFalse(set.includes(-9));
		assertTrue(set.getIntervalsCount() == 3);

		set.add(-10, -4);
		for (int i = -15; i <= -4; i++)
			assertTrue(set.includes(i));
		assertFalse(set.includes(-16));
		assertFalse(set.includes(-3));
		assertTrue(set.getIntervalsCount() == 2);

		set.add(-25, -20);
		for (int i = -25; i <= -20; i++)
			assertTrue(set.includes(i));
		assertFalse(set.includes(-26));
		assertFalse(set.includes(-19));
		assertTrue(set.getIntervalsCount()==3);
		
		set.add(-30,-22);
		for (int i = -30; i<=-20; i++)
			assertTrue(set.includes(i));
		assertFalse(set.includes(-31));
		assertFalse(set.includes(-19));
		assertTrue(set.getIntervalsCount()==3);
		
		set.add(-100,100);
		for (int i = -100; i <=100; i++) assertTrue(set.includes(i));
		assertTrue(set.getIntervalsCount()==1);

	
	}

	
	@Test
	public void testInclude_infinite(){
		
	}
	
	@Test
	public void testRemove_finite(){
		System.err.println("Not implemented yet.");		
	}
}
