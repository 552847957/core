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

import org.apache.wicket.ajax.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.wicketstuff.rest.contenthandling.IObjectSerialDeserial;
import org.wicketstuff.rest.domain.PersonPojo;
import org.wicketstuff.rest.hateaos.contenthandling.HateoasObjSerialDeserial;
import org.wicketstuff.rest.resource.PersonsRestResource;

public class TestHateoasObjSerialDeserial extends Assert
{
//    private WicketTester tester;
    private HateoasObjSerialDeserial objSerialDeserial = new HateoasObjSerialDeserial(
	    new WicketObjSerialDeserial(), PersonsRestResource.class);

//    @Before
//    public void setUp()
//    {
//	tester = new WicketTester(new WicketApplication(objSerialDeserial));
//    }

    @Test
    public void test()
    {
	PersonPojo person = new PersonPojo(1, "Freddie Mercury", "fmercury@queen.com", "Eeehooo!");
	System.out.println(objSerialDeserial.serializeObject(person, ""));
    }

}

class WicketObjSerialDeserial implements IObjectSerialDeserial<String>
{

    @Override
    public String serializeObject(Object target, String mimeType)
    {
	return new JSONObject(target).toString();
    }

    @Override
    public <E> E deserializeObject(String source, Class<E> targetClass,
	    String mimeType)
    {
	// TODO Auto-generated method stub
	return null;
    }
}
