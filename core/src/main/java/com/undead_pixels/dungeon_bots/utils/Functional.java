package com.undead_pixels.dungeon_bots.utils;

import com.undead_pixels.dungeon_bots.utils.generic.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Functional {
	public static <T> Stream<Pair<T,Integer>> iterStream(T[] arr) {
		List<Pair<T, Integer>> ans = new ArrayList<>();
		for(int i = 0; i < arr.length; i++)
			ans.add(new Pair<>(arr[i], i));
		return ans.stream();
	}
}
