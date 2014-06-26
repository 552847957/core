/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wicketstuff.rest.hateoas.resources;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.wicket.Application;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceReference.Key;
import org.wicketstuff.rest.resource.AbstractRestResource;
import org.wicketstuff.rest.resource.MethodMappingInfo;
import org.wicketstuff.rest.resource.urlsegments.AbstractURLSegment;
import org.wicketstuff.rest.utils.reflection.MethodParameter;

public class ResourceUrlBuilder 
{
	
	@SuppressWarnings("unchecked")
	public static <T extends AbstractRestResource<?>> T forResourceClass(Class<T> resourceClass) 
	{
		T proxyResource = (T) Enhancer.create(resourceClass, new MappedMethodInterceptor());
		return proxyResource;
	}	
}


class MappedMethodInterceptor implements MethodInterceptor
{
	
	@Override
	public Object intercept(Object obj, Method method, Object[] args,
			MethodProxy proxy) throws Throwable 
	{
		Key key = null;
		
		ResourceReference ref = Application.get()
				.getResourceReferenceRegistry()
				.getResourceReference(key, false, false);
		
		if(ref == null || !(ref instanceof HateoasResourceReference))
		{
			return "";
		}

		HateoasResourceReference hateoasReference = (HateoasResourceReference) ref;
		AbstractRestResource<?> resource = hateoasReference.getResource();
		MethodMappingInfo methodInfo = resource.getMethodInfo(method);
		
		if (methodInfo == null) 
		{
			return "";
		}

		return buildMethodUrl(methodInfo, args);
	}

	private Object buildMethodUrl(MethodMappingInfo methodInfo, Object[] args) 
	{
		List<AbstractURLSegment> segments = methodInfo.getSegments();
		List<MethodParameter> parameters = methodInfo.getMethodParameters();
		
		
		return null;
	}
}
