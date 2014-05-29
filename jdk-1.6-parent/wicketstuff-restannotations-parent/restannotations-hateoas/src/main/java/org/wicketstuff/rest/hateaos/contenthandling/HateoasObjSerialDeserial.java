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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.util.string.Strings;
import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.contenthandling.IObjectSerialDeserial;
import org.wicketstuff.rest.hateaos.annotations.HypermediaEntityLink;
import org.wicketstuff.rest.hateaos.annotations.HypermediaParameter;
import org.wicketstuff.rest.hateoas.HypermediaLink;
import org.wicketstuff.rest.hateoas.IEntityContextProducer;
import org.wicketstuff.rest.hateoas.MappedHypermediaLink;
import org.wicketstuff.rest.resource.MethodMappingInfo;
import org.wicketstuff.rest.resource.urlsegments.AbstractURLSegment;
import org.wicketstuff.rest.resource.urlsegments.FixedURLSegment;
import org.wicketstuff.rest.resource.urlsegments.MultiParamSegment;
import org.wicketstuff.rest.resource.urlsegments.ParamSegment;

public abstract class HateoasObjSerialDeserial implements IObjectSerialDeserial<String>
{
    public static final String ENTITY_PREFIX = "entity";
    private final Map<Class<?>, List<MappedHypermediaLink>> hypermediaLinks = new ConcurrentHashMap<Class<?>, List<MappedHypermediaLink>>();
    private final Map<Class<?>, List<IEntityContextProducer<?>>> enitityContextProducers = new ConcurrentHashMap<Class<?>, List<IEntityContextProducer<?>>>();
    
    public HateoasObjSerialDeserial(Class<?>... targetClasses)
    {
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
	Map<String, ?> enitityContext = getEnitityContext(target);
	
	if (entityLinks != null)
	{
	    for (MappedHypermediaLink hypermediaLink : entityLinks)
	    {		
		StringBuffer linkUrl = generateHypermediaLink(hypermediaLink, enitityContext, target);
		
		links.add(new HypermediaLink(hypermediaLink.getRel(),
			hypermediaLink.getType(), linkUrl.toString()));
	    }
	}
	
	return serializeObject(target, links, mimeType);
    }
    
    protected abstract String serializeObject(Object target, List<HypermediaLink> links,
	    String mimeType);

    @SuppressWarnings("unchecked")
    private <T> Map<String, ?> getEnitityContext(Object target)
    {
	List<IEntityContextProducer<?>> contextProducer = enitityContextProducers.get(target.getClass());
	
	if(contextProducer == null)
	{
	    return Collections.emptyMap();
	}
	
	Map<String, ?> entityContext = new HashMap<String, Object>();
	
	for (IEntityContextProducer<?> iEntityContextProducer : contextProducer)
	{
	    IEntityContextProducer<T> enityContextProducer = (IEntityContextProducer<T>) iEntityContextProducer;
	    enityContextProducer.createContext((T)target, entityContext);
	}
	
	return entityContext;
    }

    private StringBuffer generateHypermediaLink(MappedHypermediaLink hypermediaLink, 
	    					Map<String, ?> entityContext, Object target)
    {
	List<AbstractURLSegment> segments = hypermediaLink
		.getMappingInfo().getSegments();
	StringBuffer linkUrl = new StringBuffer('/');
	Iterator<String> parametersExp = hypermediaLink.getPropertiesIterator();
	int maxSegmIndex = segments.size() - 1;
	
	Iterator<String> paramValues = resolveParametersExp(parametersExp, entityContext, target);
	
	for (AbstractURLSegment abstractURLSegment : segments)
	{
	    linkUrl.append(populateSegmentVariables(abstractURLSegment, paramValues));
	    
	    if(segments.indexOf(abstractURLSegment) < maxSegmIndex )
	    {
		linkUrl.append('/');
	    }		   
	}

	return linkUrl;
    }

    private StringBuffer populateSegmentVariables(
	    AbstractURLSegment abstractURLSegment, Iterator<String> paramValues)
    {
	StringBuffer buffer = new StringBuffer();
	
	if(abstractURLSegment instanceof FixedURLSegment)
	{
	    buffer.append(abstractURLSegment.toString());
	}
	
	if(abstractURLSegment instanceof ParamSegment)
	{
	    buffer.append(paramValues.next());
	}

	if(abstractURLSegment instanceof MultiParamSegment)
	{
	    MultiParamSegment segment = (MultiParamSegment) abstractURLSegment;
	    
	    for (AbstractURLSegment urlSegment : segment.getSubSegments())
	    {
		buffer.append(populateSegmentVariables(urlSegment, paramValues));
	    }
	}
	
	return buffer;
    }

    private Iterator<String> resolveParametersExp(Iterator<String> parametersExp, Map<String, ?> entityContext, Object target)
    {
	List<String> values = new ArrayList<String>();
	
	while (parametersExp.hasNext())
	{
	    String expression = parametersExp.next();
	    String[] splittedExp = expression.split("\\.");
	    Object targetInstance;
	    String value;
	    
	    
	    if (splittedExp.length < 2)
	    {
		throw new IllegalArgumentException("The expression '" + expression + "' is not a valid property expression");
	    }
	    
	    if (splittedExp[0].equals(ENTITY_PREFIX))
	    {
		targetInstance = target;
	    }
	    else 
	    {
		targetInstance = entityContext.get(splittedExp[0]);
	    }
	    
	    String subExpression = Strings.join(".", Arrays.copyOfRange(splittedExp, 1, splittedExp.length));
	    
	    value = PropertyResolver.getValue(subExpression, targetInstance)
		    .toString();
	    
	    values.add(value);
	}
	
	return values.iterator();
    }    
}
