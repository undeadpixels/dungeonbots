package com.undead_pixels.dungeon_bots.file;

import java.io.FileWriter;
import java.io.IOException;
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

}
