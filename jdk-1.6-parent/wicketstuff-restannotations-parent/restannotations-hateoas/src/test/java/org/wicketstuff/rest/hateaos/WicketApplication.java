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

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.wicketstuff.rest.contenthandling.mimetypes.RestMimeTypes;
import org.wicketstuff.rest.contenthandling.serialdeserial.TextualWebSerialDeserial;
import org.wicketstuff.rest.hateaos.contenthandling.HateoasObjSerialDeserial;
import org.wicketstuff.rest.resource.PersonsRestResource;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 * 
 * @see org.wicketstuff.rest.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
    private final HateoasObjSerialDeserial objSerialDeserial;

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends WebPage> getHomePage()
    {
	return WebPage.class;
    }

    public WicketApplication(HateoasObjSerialDeserial objSerialDeserial)
    {

	this.objSerialDeserial = objSerialDeserial;
    }

    @Override
    public void init()
    {
	super.init();

	mountResource("/personsmanager", new ResourceReference("restReference")
	{

	    TextualWebSerialDeserial webSerialDeserial = new TextualWebSerialDeserial(
		    "UTF-8", RestMimeTypes.APPLICATION_JSON, objSerialDeserial);

	    PersonsRestResource resource = new PersonsRestResource(
		    webSerialDeserial);

	    @Override
	    public IResource getResource()
	    {
		return resource;
	    }

	});
    }
}
