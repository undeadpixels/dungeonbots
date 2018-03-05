package com.undead_pixels.dungeon_bots.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
//import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.scene.level.WorldList;

/**
 * We have opted to serialize collections of Worlds held within a Level Pack
 * using Java serialization (to bytes), but the Level Packs themselves are
 * serialized using JSON.
 * 
 * This class includes a number of static methods for serialization convenience.
 * The added value of this class is that the serializer used is built with
 * support specifically for our classes.
 */
public class Serializer {

	/** Uses Java serialization to make a deep copy of the given object. */
	public static <T> T deepCopy(T original) {
		return Serializer.<T>deserializeFromBytes(serializeToBytes(original));
	}

	/** Serializes the given Serializable object into bytes. */
	public static byte[] serializeToBytes(Object obj) {
		byte[] result = null;
		ByteArrayOutputStream byte_out = null;
		try {
			byte_out = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byte_out);
			out.writeObject(obj);
			out.flush();
			result = byte_out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				byte_out.close();
			} catch (Exception e) {
				// Ignore close exceptions.
			}
		}
		if (result == null)
			result = new byte[0];
		return result;
	}

	/** Deserializes the given bytes into an object of type T. */
	@SuppressWarnings("unchecked")
	public static <T> T deserializeFromBytes(byte[] bytes) {
		ByteArrayInputStream byte_in = null;
		T result = null;
		try {
			byte_in = new ByteArrayInputStream(bytes);
			ObjectInputStream in = new ObjectInputStream(byte_in);
			result = (T) in.readObject();
		} catch (IOException iex) {
			iex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (byte_in != null)
				try {
					byte_in.close();
				} catch (IOException e) {
					// Do nothing.
				}
		}
		return result;
	}

	/**
	 * Serializes the given object into JSON, using the Gson object defined
	 * here.
	 */
	public static String serializeToJSON(Object obj) {
		if (_Gson == null)
			_Gson = setupGson();
		return _Gson.toJson(obj);
	}

	/**
	 * Deserializes the given object into JSON, using the Gson object defined
	 * here.
	 */
	public static <T> T deserializeFromJSON(String json, Class<T> classOfT) {
		if (_Gson == null)
			_Gson = setupGson();
		return (T) _Gson.fromJson(json, classOfT);
	}

	// ============================================================
	// ========= Serializer FILE CONVENIENCE METHODS ==============
	// ============================================================

	/**
	 * A convenience method to write bytes to a file.
	 * 
	 * @param filename
	 *            The name of the file to write.
	 * @param bytes
	 *            The bytes to write to the file.
	 * @return Returns whether the write was successful.
	 */
	public static boolean writeToFile(String filename, byte[] bytes) {
		try {
			Path path = Paths.get(filename);
			path.toFile().getParentFile().mkdirs();
			Files.write(path, bytes);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * A convenience method to write a String to a file.
	 * 
	 * @param filename
	 *            The name of the file to write.
	 * @param string
	 *            The String to write to the file.
	 * @return Returns whether the write was successful.
	 */
	public static boolean writeToFile(String filename, String string) {
		return writeToFile(filename, string.getBytes());
	}

	/**
	 * A convenience method to read bytes from a file. On an exception, null is
	 * returned.
	 * 
	 * @param filename
	 *            The name of the file to read.
	 */
	public static byte[] readBytesFromFile(String filename) {
		try {
			return Files.readAllBytes(Paths.get(filename));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * A convenience method to read a String from a file. On an exception, null
	 * is returned.
	 * 
	 * @param filename
	 *            The name of the file to read.
	 */
	public static String readStringFromFile(String filename) {
		byte[] bytes = readBytesFromFile(filename);
		if (bytes == null)
			return null;
		return new String(bytes);
	}

	// ============================================================
	// ========= Serializer WORLD SERIALIZATION (bytes)============
	// ============================================================

	/** Converts the given World object to serialized bytes. */
	public static byte[] serializeWorld(World world) {
		return serializeToBytes(world);
	}

	/** Converts the given bytes to a World. */
	public static World deserializeWorld(byte[] bytes) {
		return Serializer.<World>deserializeFromBytes(bytes);
	}

	/** Converts the given list of Worlds to bytes. */
	public static byte[] serializeWorlds(WorldList worlds) {
		byte[] result = serializeToBytes(worlds);
		return result;
	}

	/** Converts the given bytes to an ordered list of Worlds. */
	public static WorldList deserializeWorlds(byte[] bytes) {
		return Serializer.<WorldList>deserializeFromBytes(bytes);
	}

	// ============================================================
	// ====== Serializer LEVELPACK SERIALIZATION (json) ===========
	// ============================================================

	private static Gson _Gson = null;

	private static Gson setupGson() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.serializeNulls();
		builder.registerTypeAdapter((new WorldList()).getClass(), worldsSerializer);
		builder.registerTypeAdapter((new WorldList()).getClass(), worldsDeserializer);
		return builder.create();
	}

	private static JsonSerializer<WorldList> worldsSerializer = new JsonSerializer<WorldList>() {

		@Override
		public JsonElement serialize(WorldList src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			// To properly save the bytes, must use Base64 encoding.
			byte[] bytes = serializeWorlds(src);
			String str = Base64.getEncoder().encodeToString(bytes);
			return _Gson.toJsonTree(str);
		}
	};

	private static JsonDeserializer<WorldList> worldsDeserializer = new JsonDeserializer<WorldList>() {

		@Override
		public WorldList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			// To get the original bytes, must use Base64 decoding.
			String str = json.getAsJsonPrimitive().getAsString();
			byte[] bytes = Base64.getDecoder().decode(str.getBytes());
			WorldList wl = deserializeWorlds(bytes);
			return wl;
		}

	};

	public static String serializeLevelPack(LevelPack levelPack) {
		Stream.of(levelPack.getAllWorlds()).forEach(world -> world.serialized = true);
		return serializeToJSON(levelPack);
	}

	public static LevelPack deserializeLevelPack(String json) {
		return deserializeFromJSON(json, LevelPack.class);
	}

	// ============================================================
	// ====== Serializer VALIDATION STUFF =========================
	// ============================================================

	public enum PrintOptions {
		NONE(0), MATCHED(1), UNMATCHED(2), UNDETERMINED(4), ALL_NON_MATCHED(2 | 4);

		public int value;

		PrintOptions(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}

	/**
	 * Uses reflection to determine actual equality of two objects without the
	 * need to implement an equals() method for the objects' class.
	 * <p>
	 * (However, if an equals method IS implemented and returns true, that
	 * method will be used as a shortcut.)
	 * 
	 * @param objectA
	 *            The first object to compare.
	 * @param objectB
	 *            The second object to compare.
	 * @param rootName
	 *            This is the name of the item, for help in debugging. It will
	 *            constitute the first item in a fully qualified field name.
	 * @param testTransients
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
	public static void validate(Object objectA, Object objectB, String rootName, boolean testTransients,
			boolean testFinals, PrintOptions options) throws Exception {

		ArrayList<String> matched = new ArrayList<String>(), unmatched = new ArrayList<String>(),
				undetermined = new ArrayList<String>();
		validateReflectedEqualityRecursive(objectA, objectB, matched, unmatched, undetermined, new HashSet<Object>(),
				rootName, testTransients, testFinals);

		String result = rootName + "   Matched fields: " + matched.size() + "   Unmatched fields: " + unmatched.size()
				+ "   Undetermined fields: " + undetermined.size();
		if (options.value > PrintOptions.NONE.value)
			System.out.println(result);
		if ((options.value & PrintOptions.MATCHED.value) == PrintOptions.MATCHED.value) {
			System.out.println("Matched: " + matched.size());
			for (String str : matched) {
				System.out.println(str);
			}
		}
		if ((options.value & PrintOptions.UNMATCHED.value) == PrintOptions.UNMATCHED.value) {
			System.out.println("Unmatched: " + unmatched.size());
			for (String str : unmatched) {
				System.out.println(str);
			}
		}
		if ((options.value & PrintOptions.UNDETERMINED.value) == PrintOptions.UNDETERMINED.value) {
			System.out.println("Undetermined: " + undetermined.size());
			for (String str : undetermined) {
				System.out.println(str);
			}
		}

		if (unmatched.size() > 0 || undetermined.size() > 0)
			throw new Exception(result);

	}

	/** The recursive guts of validation. */
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
			unmatched.add(rootName+" [class " + classA.getCanonicalName() + " != " + classB.getCanonicalName() + "]");
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
				unmatched.add(rootName+" ["+objectA +"!="+ objectB+"]");
			return;
		}
		if (classA.isPrimitive()) {
			if (((Number) objectA).equals((Number) objectB))
				matched.add(rootName);
			else
				unmatched.add(rootName+" ["+objectA +"!="+ objectB+"]");
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
				unmatched.add(rootName +" [iterators not of same length]");
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

			// Return because Iterables tend to have internals which don't
			// match, even when they contain exactly the same items.
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
				undetermined.add(fieldName + getStringModifiers(field.getModifiers()) +" [IllegalArgumentException]");
				continue;
			} catch (IllegalAccessException e) {
				System.out.println(rootName + "." + field.getName() + ": " + e.getMessage());
				undetermined.add(fieldName + getStringModifiers(field.getModifiers()) +" [IllegalAccessException]");
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
	 * @param c
	 *            <code>Class</code> to check.
	 *
	 * @return <code>true</code> is <code>c</code> is instance of a collection
	 *         class, <code>false</code> otherwise.
	 */
	private static boolean isIterableClass(Class<?> c) {
		// This from "JBoss" at
		// http://www.java2s.com/Code/Java/Reflection/Returnstrueiftypeisaiterabletype.htm,
		// posted Apr, 2009, last downloaded 2/22/18
		List<Class<?>> classes = new ArrayList<Class<?>>();
		computeClassHierarchy(c, classes);
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

	/**
	 * Gets a string expressing all the modifiers from a given field modifier
	 * mask.
	 */
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
