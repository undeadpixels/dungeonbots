package com.undead_pixels.dungeon_bots.scene.level;

import java.io.Serializable;
import java.util.ArrayList;

import com.undead_pixels.dungeon_bots.scene.World;

/**
 * This is a class wrapping a list of Worlds whose exclusive purpose is to make
 * serialization work correctly. It probably won't even have any members.
 * <p>
 * The problem this class solves is that types of generics are erased in Java at
 * runtime, so the serializers can't tell an ArrayList&ltWorld&gt from an
 * ArrayList&ltInteger&gt. This is a problem because serialization has to give
 * the list of worlds special treatment. However, the serializer can tell a
 * WorldList from an ArrayList&ltWorld&gt. Hence, dummy class. Yes, it's
 * hackish, but Java's decision to use type erasure gives us no choice.
 */

public class WorldList extends ArrayList<World> implements Serializable {
	
}
