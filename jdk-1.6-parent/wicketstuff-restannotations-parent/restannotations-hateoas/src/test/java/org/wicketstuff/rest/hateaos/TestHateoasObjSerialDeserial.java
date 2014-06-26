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
package org.wicketstuff.rest.hateaos;

import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.wicketstuff.rest.domain.PersonPojo;
import org.wicketstuff.rest.hateaos.contenthandling.HateoasObjSerialDeserial;
import org.wicketstuff.rest.hateoas.HypermediaLink;

public class TestHateoasObjSerialDeserial extends Assert
{
    private HateoasObjSerialDeserial objSerialDeserial = new HateoasObjSerialDeserial()
    {

	@Override
	public <E> E deserializeObject(String source, Class<E> targetClass,
		String mimeType)
	{
	    return null;
	}

	@Override
	protected String serializeObject(Object target,
		List<HypermediaLink> links, String mimeType)
	{
	    try
	    {
		return new JSONObject(target).put("links", links).toString();
	    } catch (Exception e)
	    {
		throw new WicketRuntimeException(
			"An error occurred during hateaos links serialization",
			e);
	    }
	}

    };

    @Test
    public void test()
    {
	PersonPojo person = new PersonPojo(1, "Freddie Mercury",
		"fmercury@queen.com", "Eeehooo!");
	System.out.println(objSerialDeserial.serializeObject(person, ""));
    }

}
