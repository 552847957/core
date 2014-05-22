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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.contenthandling.IObjectSerialDeserial;
import org.wicketstuff.rest.hateaos.annotations.HypermediaEntityLink;
import org.wicketstuff.rest.hateaos.annotations.HypermediaParameter;
import org.wicketstuff.rest.hateoas.HypermediaLink;
import org.wicketstuff.rest.hateoas.MappedHypermediaLink;
import org.wicketstuff.rest.resource.MethodMappingInfo;
import org.wicketstuff.rest.resource.urlsegments.AbstractURLSegment;

public class HateoasObjSerialDeserial implements IObjectSerialDeserial<String>
{
    public static final String ENTITY_PREFIX = "entity.";
    private final Map<Class<?>, List<MappedHypermediaLink>> hypermediaLinks = new ConcurrentHashMap<Class<?>, List<MappedHypermediaLink>>();
    private final IObjectSerialDeserial<String> delegateSerialDeserial;

    public HateoasObjSerialDeserial(
	    IObjectSerialDeserial<String> delegateSerialDeserial,
	    Class<?>... targetClasses)
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
	    MethodMapping methodMapped = method
		    .getAnnotation(MethodMapping.class);
	    HypermediaEntityLink resourceLink = method
		    .getAnnotation(HypermediaEntityLink.class);

	    if (methodMapped != null && resourceLink != null)
	    {
		addHypermediaLink(method, methodMapped, resourceLink);
	    }
	}
    }

    private void addHypermediaLink(Method method, MethodMapping methodMapped,
	    HypermediaEntityLink resourceLink)
    {
	Class<?> entityClass = resourceLink.entityClass();
	List<MappedHypermediaLink> linkslist = hypermediaLinks.get(entityClass);
	MethodMappingInfo mappingInfo = new MethodMappingInfo(methodMapped,
		method);
	String rel = resourceLink.linkRel();
	String type = resourceLink.linkType();
	HypermediaParameter[] linkParams = resourceLink.linkParams();
	
	if (linkslist == null)
	{
	    linkslist = new ArrayList<MappedHypermediaLink>();
	    hypermediaLinks.put(entityClass, linkslist);
	}

	linkslist.add(new MappedHypermediaLink(mappingInfo, rel, type, linkParams,
		entityClass));
    }

    @Override
    public String serializeObject(Object target, String mimeType)
    {
	List<MappedHypermediaLink> entityLinks = hypermediaLinks.get(target
		.getClass());
	List<HypermediaLink> links = new ArrayList<HypermediaLink>();

	if (entityLinks != null)
	{
	    for (MappedHypermediaLink hypermediaLink : entityLinks)
	    {
		List<AbstractURLSegment> segments = hypermediaLink
			.getMappingInfo().getSegments();
		StringBuffer linkUrl = generateHypermediaLink(segments, 
			hypermediaLink.getPropertiesIterator(), target);
		
		links.add(new HypermediaLink(hypermediaLink.getRel(),
			hypermediaLink.getType(), linkUrl.toString()));
	    }
	}
	
	try
	{
	    return new JSONObject(target).put("links", links).toString();
	} catch (Exception e)
	{
	    throw new WicketRuntimeException("An error occurred during hateaos links serialization", e);
	}
    }

    private StringBuffer generateHypermediaLink(
	    List<AbstractURLSegment> segments, Iterator<String> parametersExp, Object target)
    {
	StringBuffer linkUrl = new StringBuffer('/');
	int maxSegmIndex = segments.size() - 1;
	Iterator<String> paramValues = resolveParametersExp(parametersExp, target);
	
	for (AbstractURLSegment abstractURLSegment : segments)
	{
	    linkUrl.append(
		     abstractURLSegment.populateVariableFromEntity(paramValues));
	    
	    if(segments.indexOf(abstractURLSegment) < maxSegmIndex )
	    {
		linkUrl.append('/');
	    }		   
	}

	return linkUrl;
    }

    private Iterator<String> resolveParametersExp(Iterator<String> parametersExp, Object target)
    {
	List<String> values = new ArrayList<String>();
	
	while (parametersExp.hasNext())
	{
	    String expression = parametersExp.next();
	    String value;
	    
	    if(expression.startsWith(ENTITY_PREFIX))
	    {
		value = PropertyResolver.getValue(
			expression.replace(ENTITY_PREFIX, ""), 
			target).toString();
	    }
	    else
	    {
		value = PropertyResolver.getValue(expression, this)
			.toString();
	    }
	    
	    values.add(value);
	}
	
	return values.iterator();
    }

    @Override
    public <E> E deserializeObject(String source, Class<E> targetClass,
	    String mimeType)
    {
	return delegateSerialDeserial.deserializeObject(source, targetClass,
		mimeType);
    }
}
