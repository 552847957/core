package org.wicketstuff.rest.resource.gson.hateous;

import org.apache.wicket.request.Url;
import org.wicketstuff.rest.heteaos.HateousResource;
import org.wicketstuff.rest.resource.gson.GsonSerialDeserial;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class GsonHateousSerialDeserialBuilder {
	public static GsonSerialDeserial newSerialDeserial()
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(HateousResource.class, new HateousSerializer());
		builder.registerTypeAdapter(Url.class, new HateousUrlSerializer());
		builder.setPrettyPrinting();

		Gson gson = builder.create();
		
		return new GsonSerialDeserial(gson);
	}
}
