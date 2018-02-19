package com.undead_pixels.dungeon_bots.file;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.undead_pixels.dungeon_bots.math.IntegerSet;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.annotations.UserScript;

/**
 * An object which can function to either serialize an object into a JSON
 * string, or to create an object from a JSON string.
 */
public class Serializer {

	private Gson _GsonSerialize;
	private Gson _GsonDeserialize;

	public Serializer() {
		GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();
		builder.registerTypeAdapter(World.class, worldSerializer);
		_GsonSerialize = builder.create();

		builder = new GsonBuilder();
		builder.serializeNulls();
		builder.registerTypeAdapter(World.class, worldDeserializer);
		_GsonDeserialize = builder.create();
	}

	/** Serialize the given world. */
	public String Serialize(World world) {
		UserScript u = new UserScript("script's name", "script's code", SecurityLevel.AUTHOR);
		u.locks.add(new IntegerSet.Interval(10,15));
		u.locks.add(new IntegerSet.Interval(20, 25));
		String test = _GsonSerialize.toJson(u);
		System.out.println("Done.\n" + test);
		
		UserScript v = _GsonDeserialize.fromJson(test, UserScript.class);
		System.out.println("Deserialized.");
		return _GsonSerialize.toJson(world);
	}

	/** Deserialize the given string into a World. */
	public World Deserialize(String jsonString) {
		return _GsonDeserialize.fromJson(jsonString, World.class);
	}

	JsonSerializer<World> worldSerializer = new JsonSerializer<World>() {

		@Override
		public JsonElement serialize(World world, Type typeOfSrc, JsonSerializationContext context) {
			//

			JsonObject obj = new JsonObject();

			return null;
		}

	};

	JsonDeserializer<World> worldDeserializer = new JsonDeserializer<World>() {

		@Override
		public World deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();

			// TODO Auto-generated method stub
			return null;
		}

	};

}
