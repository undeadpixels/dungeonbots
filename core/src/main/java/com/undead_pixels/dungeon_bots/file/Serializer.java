package com.undead_pixels.dungeon_bots.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;

/**
 * An object which can function to either serialize an object into a JSON
 * string, or to create an object from a JSON string.
 */
public class Serializer {

	private Gson gson;

	public Serializer() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();

		builder.serializeNulls();
		builder.registerTypeAdapter(LevelPack.class, levelPackSerializer);
		builder = new GsonBuilder();
		builder.serializeNulls();
		builder.registerTypeAdapter(LevelPack.class, levelPackDeserializer);

		gson = builder.create();
	}

	public void writeToFile(String fileName, String json) {

		FileWriter writer = null;
		try {
			writer = new FileWriter(fileName);
			writer.write(json);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static byte[] worldToBytes(World world) {
		byte[] worldAsBytes = null;
		ByteArrayOutputStream byte_out = null;
		try {
			byte_out = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byte_out);
			out.writeObject(world);
			out.flush();
			worldAsBytes = byte_out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				byte_out.close();
			} catch (Exception e) {
				// Ignore close exceptions.
			}
		}
		if (worldAsBytes == null)
			worldAsBytes = new byte[0];
		return worldAsBytes;
	}

	/** Converts the given bytes to a World. */
	private static World bytesToWorld(byte[] bytes) {
		ByteArrayInputStream byte_in = null;
		try {
			byte_in = new ByteArrayInputStream(bytes);
			ObjectInputStream in = new ObjectInputStream(byte_in);
			return (World) in.readObject();
		} catch (IOException ioe) {
		} catch (ClassNotFoundException cnfe) {
		} finally {
			if (byte_in != null)
				try {
					byte_in.close();
				} catch (IOException e) {
					// Do nothing.
				}
		}
		return null;
	}

	JsonSerializer<LevelPack> levelPackSerializer = new JsonSerializer<LevelPack>() {

		@Override
		public JsonElement serialize(LevelPack src, Type typeOfSrc, JsonSerializationContext context) {

			// TODO Auto-generated method stub
			return null;
		}

	};
	JsonDeserializer<LevelPack> levelPackDeserializer = new JsonDeserializer<LevelPack>() {

		@Override
		public LevelPack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {

			// TODO Auto-generated method stub
			return null;
		}
	};

	public byte[] toBytes(World world) {
		return worldToBytes(world);
	}

	/** Uses Java serialization to make a deep copy of the given object. */
	@SuppressWarnings("unchecked")
	public static <T> T deepCopy(T original) {
		T result = null;
		PipedInputStream in_ = new PipedInputStream();
		PipedOutputStream out_ = null;
		ObjectOutputStream out = null;
		try {
			out_ = new PipedOutputStream(in_);
			out = new ObjectOutputStream(out_);
			ObjectInputStream in = new ObjectInputStream(in_);
			out.writeObject(original);
			result = (T) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in_ != null)
					in_.close();
				if (out_ != null)
					out_.close();
				// if (in != null) in.close();
				if (out != null)
					out.close();
			} catch (Exception e) {
				// Ignore close errors.
			}
		}
		return result;
	}

	public World toWorld(byte[] bs) {
		// TODO Auto-generated method stub
		return null;
	}

}
