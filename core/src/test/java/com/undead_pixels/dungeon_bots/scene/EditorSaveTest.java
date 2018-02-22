package com.undead_pixels.dungeon_bots.scene;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.undead_pixels.dungeon_bots.file.editor.GameEditorState;
import com.undead_pixels.dungeon_bots.file.editor.TileRegionSection;

public class EditorSaveTest {

	@Test
	public void simpleTest() {
		GameEditorState s = new GameEditorState(new World());
		Assert.assertNotEquals("", s.toLua());
		// System.out.println(s.toLua());
	}

	@Test
	public void slightlyLessSimpleTest() {
		GameEditorState s = new GameEditorState(new World());
		s.worldSizeSection.width = 50;
		s.worldSizeSection.height = 51;
		s.tileRegionSection.add(new TileRegionSection.TileRegion(5, 15, 5, 15, "floor"));
		s.tileRegionSection.add(new TileRegionSection.TileRegion(2, 2, 3, 3, "wall"));
		s.playerInitSection.px = 8;
		s.playerInitSection.py = 9;

		Assert.assertNotEquals("", s.toLua());
		// System.out.println(s.toLua());
	}

	@Test
	public void actualSerializeDeserializeTest() throws ParseException {
		GameEditorState s = new GameEditorState(new World());
		s.worldSizeSection.width = 50;
		s.worldSizeSection.height = 51;
		s.tileRegionSection.add(new TileRegionSection.TileRegion(5, 15, 5, 15, "floor"));
		s.tileRegionSection.add(new TileRegionSection.TileRegion(2, 2, 3, 3, "wall"));
		s.playerInitSection.px = 8;
		s.playerInitSection.py = 9;

		String luaOut = s.toLua();
		System.out.println("1.\n" + luaOut);

		GameEditorState s2 = new GameEditorState(new World(), luaOut);

		String luaOut2 = s2.toLua();
		System.out.println("2.\n" + luaOut2);

		Assert.assertEquals("Check if serialize->deserialize->serialize is consistent", s.toLua(), luaOut2);
	}

	@Test
	public void testSerializeWorld() throws Exception {
		testWorldMadeFromScript("level1.lua", false);
		testWorldMadeFromScript("maze1.lua", false);
		testWorldMadeFromScript("maze2.lua", false);
	}

	private static void testWorldMadeFromScript(String filename, boolean printResults) throws Exception {

		World w1 = new World(new File(filename));

		PipedInputStream in_ = new PipedInputStream();
		PipedOutputStream out_ = new PipedOutputStream(in_);
		ObjectOutputStream out = new ObjectOutputStream(out_);
		ObjectInputStream in = new ObjectInputStream(in_);

		new Thread(() -> {
			try {
				out.writeObject(w1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();

		World w2 = (World) in.readObject();

		out.close();

		validateReflectedEquality(w1, w2, filename + " world", false, true, printResults);
	}

	/**
	 * This method uses reflection to determine actual equality of two objects
	 * without the need to implement an equals() method for the objects' class.
	 * 
	 * @param objectA
	 *            The first object to compare.
	 * @param objectB
	 *            The second object to compare.
	 * @param rootName
	 *            This is the name of the item, for help in debugging. It will
	 *            constitute the first item in a fully qualified field name.
	 * @param testTransiants
	 *            Whether to include transient fields in the test. For
	 *            serialization testing, this should be false, because one would
	 *            expect that transients aren't going to be equal before and
	 *            after serialization anyway.
	 * @param testFinals
	 *            Whether to include final members in the test. There may be
	 *            points where this is useless to test.
	 * @throws Exception
	 *             An exception will be thrown if it can be shown that the two
	 *             objects are not deep-field equal.
	 */
	public static void validateReflectedEquality(Object objectA, Object objectB, String rootName,
			boolean testTransients, boolean testFinals, boolean printResults) throws Exception {

		ArrayList<String> matched = new ArrayList<String>(), unmatched = new ArrayList<String>(),
				undetermined = new ArrayList<String>();
		validateReflectedEqualityRecursive(objectA, objectB, matched, unmatched, undetermined, new HashSet<Object>(),
				rootName, testTransients, testFinals);

		String result = rootName + "   Matched fields: " + matched.size() + "   Unmatched fields: " + unmatched.size()
				+ "   Undetermined fields: " + undetermined.size();
		if (printResults) {
			System.out.println(result);
			for (String str : matched)
				System.out.println(str);
		}
		if (unmatched.size() > 0 || undetermined.size() > 0)
			throw new Exception(result);

	}

	private static void validateReflectedEqualityRecursive(Object objectA, Object objectB, ArrayList<String> matched,
			ArrayList<String> unmatched, ArrayList<String> undetermined, HashSet<Object> checked, String rootName,
			boolean testTransients, boolean testFinals) {
		if (!checked.add(objectA))
			return;

		if (objectA == null) {
			if (objectB == null)
				matched.add(rootName);
			else
				unmatched.add(rootName);
			return;
		} else if (objectB == null) {
			unmatched.add(rootName);
			return;
		}

		// From here, neither are null.
		Class<?> classA = objectA.getClass(), classB = objectB.getClass();
		if (!classA.equals(classB)) {
			unmatched.add(rootName);
			return;
		}
		if (objectA.equals(objectB)) {
			matched.add(rootName);
			return;
		}

		// From here, the classes match but they are not equal.
		if (classA.isArray()) {
			int lengthA = Array.getLength(objectA), lengthB = Array.getLength(objectB);
			if (lengthA != lengthB)
				unmatched.add(rootName + ".length");
			else {
				for (int i = 0; i < lengthA; i++) {
					validateReflectedEqualityRecursive(Array.get(objectA, i), Array.get(objectB, i), matched, unmatched,
							undetermined, checked, rootName + "[" + i + "]", testTransients, testFinals);
				}
			}
			return;
		}
		if (classA == boolean.class) {
			if (((boolean) objectA) == ((boolean) objectB))
				matched.add(rootName);
			else
				unmatched.add(rootName);
			return;
		}
		if (classA.isPrimitive()) {
			if (((Number) objectA).equals((Number) objectB))
				matched.add(rootName);
			else
				unmatched.add(rootName);
			return;
		}

		// Iterables require special handling.
		if (isIterableClass(classA)) {
			@SuppressWarnings("rawtypes")
			Iterator iterA = ((Iterable) objectA).iterator(), iterB = ((Iterable) objectB).iterator();
			int i = 0;
			ArrayList<String> tempMatched = new ArrayList<String>(), tempUnmatched = new ArrayList<String>(),
					tempUndetermined = new ArrayList<String>();
			while (iterA.hasNext() && iterB.hasNext()) {
				Object childA = iterA.next(), childB = iterB.next();
				validateReflectedEqualityRecursive(childA, childB, tempMatched, tempUnmatched, tempUndetermined,
						checked, rootName + "[" + i++ + "]", testTransients, testFinals);
			}
			if (iterA.hasNext() || iterB.hasNext())
				unmatched.add(rootName);
			else
				matched.add(rootName);
			matched.addAll(tempMatched);
			unmatched.addAll(tempUnmatched);
			undetermined.addAll(tempUndetermined);
			while (iterA.hasNext())
				validateReflectedEqualityRecursive(iterA.next(), new Object(), tempMatched, tempUnmatched,
						tempUndetermined, checked, rootName + "[" + i++ + "]", testTransients, testFinals);
			while (iterB.hasNext())
				validateReflectedEqualityRecursive(iterB.next(), new Object(), tempMatched, tempUnmatched,
						tempUndetermined, checked, rootName + "[" + i++ + "]", testTransients, testFinals);
			return;
		}

		// From here the objects are guaranteed to be objects passed by
		// reference, and they might have fields.
		Field[] fields = classA.getDeclaredFields();
		matched.add(rootName); // Added to make tree-like structure.
		for (Field field : fields) {
			int mods = field.getModifiers();

			// Skip conditions that are supposed to be ignored.
			if (!testTransients && Modifier.isTransient(mods))
				continue;
			if (!testFinals && Modifier.isFinal(mods))
				continue;

			String fieldName = rootName + "." + field.getName();

			// If the values couldn't be read for whatever reason, then
			// throw them on the "undetermined" pile.
			Object valA = null, valB = null;
			boolean originalAccessible = field.isAccessible();
			field.setAccessible(true);
			try {
				valA = field.get(objectA);
				valB = field.get(objectB);
			} catch (IllegalArgumentException e) {
				System.out.println(fieldName + ": " + e.getMessage());
				undetermined.add(fieldName + getStringModifiers(field.getModifiers()));
				continue;
			} catch (IllegalAccessException e) {
				System.out.println(rootName + "." + field.getName() + ": " + e.getMessage());
				undetermined.add(fieldName + getStringModifiers(field.getModifiers()));
				continue;
			}
			field.setAccessible(originalAccessible);

			// Finally, recursively test equality of the values of the fields.
			validateReflectedEqualityRecursive(valA, valB, matched, unmatched, undetermined, checked, fieldName,
					testTransients, testFinals);
		}

	}

	/**
	 * Checks whether the specified class parameter is an instance of a
	 * collection class.
	 *
	 * @param clazz
	 *            <code>Class</code> to check.
	 *
	 * @return <code>true</code> is <code>clazz</code> is instance of a
	 *         collection class, <code>false</code> otherwise.
	 */
	private static boolean isIterableClass(Class<?> clazz) {
		// This from "JBoss" at
		// http://www.java2s.com/Code/Java/Reflection/Returnstrueiftypeisaiterabletype.htm,
		// posted Apr, 2009, last downloaded 2/22/18
		List<Class<?>> classes = new ArrayList<Class<?>>();
		computeClassHierarchy(clazz, classes);
		return classes.contains(Iterable.class);
	}

	/**
	 * Get all superclasses and interfaces recursively.
	 *
	 * @param clazz
	 *            The class to start the search with.
	 * @param classes
	 *            List of classes to which to add all found super classes and
	 *            interfaces.
	 */
	@SuppressWarnings("rawtypes")
	private static void computeClassHierarchy(Class<?> clazz, List<Class<?>> classes) {
		// This from "JBoss" at
		// http://www.java2s.com/Code/Java/Reflection/Returnstrueiftypeisaiterabletype.htm,
		// posted Apr. 2009, last downloaded 2/22/18
		for (Class current = clazz; current != null; current = current.getSuperclass()) {
			if (classes.contains(current)) {
				return;
			}
			classes.add(current);
			for (Class currentInterface : current.getInterfaces()) {
				computeClassHierarchy(currentInterface, classes);
			}
		}
	}

	private static String getStringModifiers(int mod) {
		String str = "";
		if (Modifier.isAbstract(mod))
			str += " | abstract" + Modifier.ABSTRACT;
		if (Modifier.isFinal(mod))
			str += " | final" + Modifier.FINAL;
		if (Modifier.isInterface(mod))
			str += " | interface" + Modifier.INTERFACE;
		if (Modifier.isNative(mod))
			str += " | native" + Modifier.NATIVE;
		if (Modifier.isPrivate(mod))
			str += " | private" + Modifier.PRIVATE;
		if (Modifier.isProtected(mod))
			str += " | protected" + Modifier.PROTECTED;
		if (Modifier.isPublic(mod))
			str += " | public" + Modifier.PUBLIC;
		if (Modifier.isStatic(mod))
			str += " | static" + Modifier.STATIC;
		if (Modifier.isStrict(mod))
			str += " | strict" + Modifier.STRICT;
		if (Modifier.isSynchronized(mod))
			str += " | synchronized" + Modifier.SYNCHRONIZED;
		if (Modifier.isTransient(mod))
			str += " | transient" + Modifier.TRANSIENT;
		if (Modifier.isVolatile(mod))
			str += " | volatile" + Modifier.VOLATILE;

		if (str.equals(""))
			return "( unknown" + mod + " )";
		return "( " + str + " )";
	}
}
