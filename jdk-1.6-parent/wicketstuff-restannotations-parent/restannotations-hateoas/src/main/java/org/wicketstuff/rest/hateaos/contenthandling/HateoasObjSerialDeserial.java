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
package org.wicketstuff.rest.hateaos.contenthandling;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.contenthandling.IObjectSerialDeserial;
import org.wicketstuff.rest.hateaos.annotations.HypermediaEntityLink;
import org.wicketstuff.rest.hateoas.HateoasResource;
import org.wicketstuff.rest.hateoas.HypermediaLink;
import org.wicketstuff.rest.resource.MethodMappingInfo;
import org.wicketstuff.rest.resource.urlsegments.AbstractURLSegment;

public class HateoasObjSerialDeserial implements IObjectSerialDeserial<String>
{
    private final Map<Class<?>, List<HypermediaLink>> hypermediaLinks = new ConcurrentHashMap<Class<?>, List<HypermediaLink>>();
    private final IObjectSerialDeserial<String> delegateSerialDeserial;
    
    public HateoasObjSerialDeserial(
	    IObjectSerialDeserial<String> delegateSerialDeserial, Class<?>... targetClasses)
    {
	this.delegateSerialDeserial = delegateSerialDeserial;
	
	for (Class<?> clazz : targetClasses)
	{
	    loadAnnotatedMethods(clazz);
	}
    }

    private void loadAnnotatedMethods(Class<?> wicketResource)
    {
	Method[] methods = wicketResource.getDeclaredMethods();
	
	for (Method method : methods)
	{
	    MethodMapping methodMapped = method.getAnnotation(MethodMapping.class);
	    HypermediaEntityLink resourceLink = method.getAnnotation(HypermediaEntityLink.class);
	    
	    if(methodMapped != null && resourceLink != null)
	    {
		addHypermediaLink(method, methodMapped, resourceLink);
	    }
	}
    }

    private void addHypermediaLink(Method method, MethodMapping methodMapped,
	    HypermediaEntityLink resourceLink)
    {
	Class<?> entityClass = resourceLink.entityClass();
	List<HypermediaLink> linkslist = hypermediaLinks.get(entityClass);
	MethodMappingInfo mappingInfo = new MethodMappingInfo(methodMapped, method);
	String rel = resourceLink.linkRel();
	String type = resourceLink.linkType();
	
	if(linkslist == null)
	{
	    linkslist = new ArrayList<HypermediaLink>();
	    hypermediaLinks.put(entityClass, linkslist);
	}
	
	linkslist.add(new HypermediaLink(mappingInfo, rel, type, entityClass));
    }

    @Override
    public String serializeObject(Object target, String mimeType)
    {
	List<HypermediaLink> entityLinks = hypermediaLinks.get(target.getClass());
	List<String> links = new ArrayList<String>();
	
	if(entityLinks != null)
	{    	
        	for (HypermediaLink hypermediaLink : entityLinks)
        	{
        	    List<AbstractURLSegment> segments = hypermediaLink.getMappingInfo().getSegments();
        	    StringBuffer linkUrl = generateHypermediaLink(segments, target);
        	    links.add(linkUrl.toString());
        	}
	}
	
	return delegateSerialDeserial.serializeObject(new HateoasResource(target, links), mimeType);
    }

    private StringBuffer generateHypermediaLink(List<AbstractURLSegment> segments, Object target)
    {
	StringBuffer linkUrl = new StringBuffer('/');
	
	for (AbstractURLSegment abstractURLSegment : segments)
	{
	    linkUrl.append(abstractURLSegment.populateVariableFromEntity(target))
	           .append('/');
	}
	
	return linkUrl;
    }

    @Override
    public <E> E deserializeObject(String source, Class<E> targetClass,
	    java.lang.String mimeType)
    {
	return delegateSerialDeserial.deserializeObject(source, targetClass, mimeType);
    }
}
