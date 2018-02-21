package com.undead_pixels.dungeon_bots.scene;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.HashSet;

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
	public void testSerializeWorld() throws IOException, ClassNotFoundException {
		testWorldMadeFromScript("level1.lua");
		testWorldMadeFromScript("maze1.lua");
		testWorldMadeFromScript("maze2.lua");
	}

	private static void testWorldMadeFromScript(String filename) throws IOException, ClassNotFoundException {

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

		validateReflectedEquality(w1, w2, filename + " world", false, true);
	}

	/**
	 * This method uses reflection to determine actual equality of two objects
	 * without the need to implement an equals() method for the objects' class.
	 * 
	 * @param a
	 *            The first object to compare.
	 * @param b
	 *            The second object to compare.
	 * @param itemName
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
	 * @throws RuntimeException
	 *             An exception will be thrown if it can be shown that the two
	 *             objects are not deep-field equal.
	 */
	public static void validateReflectedEquality(Object a, Object b, String itemName, boolean testTransients,
			boolean testFinals) {
		HashSet<String> matched = new HashSet<String>();
		HashSet<String> unmatched = new HashSet<String>();
		HashSet<String> undetermined = new HashSet<String>();
		Class<?> classA = a.getClass(), classB = b.getClass();

		if (!classA.equals(classB))
			throw new RuntimeException("Unequal classes for two objects.");

		Field[] fields = classA.getDeclaredFields();
		for (Field field : fields) {
			int mods = field.getModifiers();

			// Skip for uninteresting conditions.
			if (!testTransients && Modifier.isTransient(mods))
				continue;
			if (!testFinals && Modifier.isFinal(mods))
				continue;
			String fieldName = itemName + "." + field.getName();

			// If the values couldn't be read for whatever reason, then throw
			// them on the "undetermined" pile.
			Object valA = null, valB = null;
			boolean originalAccessible = field.isAccessible();
			field.setAccessible(true);
			try {
				valA = field.get(a);
				valB = field.get(b);
			} catch (IllegalArgumentException e) {
				System.out.println(fieldName + ": " + e.getMessage());
				undetermined.add(fieldName + getStringModifiers(field.getModifiers()));
				continue;
			} catch (IllegalAccessException e) {
				System.out.println(itemName + "." + field.getName() + ": " + e.getMessage());
				undetermined.add(fieldName + getStringModifiers(field.getModifiers()));
				continue;
			}
			field.setAccessible(originalAccessible);

			// If the field specifies a boolean or a value-type, then just
			// determine their equality and move on.
			Class<?> fieldType = field.getType();
			if (fieldType == boolean.class) {
				if (((boolean) valA) == ((boolean) valB))
					matched.add(fieldName);
				else
					unmatched.add(fieldName + getStringModifiers(field.getModifiers()) + " " + valA.toString() + ", "
							+ valB.toString());
				continue;
			} else if (fieldType.isPrimitive()) {
				if (((Number) valA).equals((Number) valB))
					matched.add(fieldName);
				else
					unmatched.add(fieldName + getStringModifiers(field.getModifiers()) + " " + valA.toString() + ", "
							+ valB.toString());
				continue;
			}

			// Otherwise, the field specifies a reference type, so handle
			// possible nulls.
			else if (valA == null && valB == null) {
				matched.add(fieldName);
				continue;
			} else if (valA == null || valB == null) {
				unmatched.add(fieldName + getStringModifiers(field.getModifiers()) + " " + valA.toString() + ", "
						+ valB.toString());
				continue;
			}

			// If two objects are designed to be equal(), return that.
			else if (valA.equals(valB)) {
				matched.add(fieldName);
				continue;
			}

			// Finally, we're dealing with a reference type, so recursively test
			// equality.
			else
				validateReflectedEquality(valA, valB, fieldName, testTransients, testFinals);
		}

		// Having tested all the fields, if anything is on the "undetermined" or
		// "unmatched" pile, throw an exception.
		if (undetermined.size() > 0) {
			System.out.println("Undetermined:\n" + undetermined.toString());
			throw new RuntimeException("Items exist whose equality could not be determined.");
		}

		if (unmatched.size() > 0) {
			System.out.println("Unmatched:\n" + unmatched.toString());
			throw new RuntimeException("Unmatched items exist.");
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
