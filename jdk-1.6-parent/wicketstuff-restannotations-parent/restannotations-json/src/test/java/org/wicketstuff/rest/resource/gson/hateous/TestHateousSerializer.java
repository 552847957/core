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

import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.wicket.request.Url;
import org.junit.Test;
import org.wicketstuff.rest.Person;
import org.wicketstuff.rest.heteaos.HateousResource;
import org.wicketstuff.rest.heteaos.HateousLink;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestHateousSerializer {
	private final Gson gson;

	public TestHateousSerializer() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(HateousResource.class, new HateousSerializer());
		builder.registerTypeAdapter(Url.class, new HateousUrlSerializer());
		builder.setPrettyPrinting();

		this.gson = builder.create();
	}

	@Test
	public void testHateousSerialization() {
		Person person = new Person("Tim", "Jolly", "hero@gmail.com");
		HateousLink hateousLink = new HateousLink(new Url(Arrays.asList("one", "two", "three"),
				Charset.forName("UTF-8")), "add", "text/html");
		HateousResource hateousEntity = new HateousResource(person, Arrays.asList(hateousLink));

		System.out.println(gson.toJson(hateousEntity));
	}

}
