/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wicketstuff.rest.resource.gson.hateous;

import java.lang.reflect.Type;

import org.wicketstuff.rest.heteaos.HateousResource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class HateousSerializer implements JsonSerializer<HateousResource> {

	@Override
	public JsonElement serialize(HateousResource entity, Type type, JsonSerializationContext ctx) {
		JsonElement jsonElement = ctx.serialize(entity.getTargetEntity());
		
		if (jsonElement instanceof JsonObject)
		{
			JsonObject jsonObject = (JsonObject)jsonElement;
			jsonObject.add("links", ctx.serialize(entity.getLinks()));
			
			jsonElement = jsonObject;
		}
		
		return jsonElement;
	}
}
