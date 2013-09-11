package org.wicketstuff.rest.resource.gson.hateous;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class HateousSerializer implements JsonSerializer<HateousEntity> {

	@Override
	public JsonElement serialize(HateousEntity entity, Type type, JsonSerializationContext ctx) {
		JsonObject jsonObject = new JsonObject();
		
		ctx.serialize(entity.getTargetEntity());
		
		return null;
	}
}
