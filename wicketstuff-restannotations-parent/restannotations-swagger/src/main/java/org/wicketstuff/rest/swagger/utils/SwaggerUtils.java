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
package org.wicketstuff.rest.swagger.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.string.Strings;
import org.wicketstuff.rest.annotations.ResourcePath;
import org.wicketstuff.rest.annotations.parameters.CookieParam;
import org.wicketstuff.rest.annotations.parameters.HeaderParam;
import org.wicketstuff.rest.annotations.parameters.RequestBody;
import org.wicketstuff.rest.annotations.parameters.RequestParam;
import org.wicketstuff.rest.resource.AbstractRestResource;
import org.wicketstuff.rest.resource.MethodMappingInfo;
import org.wicketstuff.rest.utils.collection.SegmentIterator;
import org.wicketstuff.rest.utils.http.HttpMethod;
import org.wicketstuff.rest.utils.reflection.MethodParameter;
import org.wicketstuff.rest.utils.reflection.ReflectionUtils;

import io.swagger.annotations.ApiParam;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;

public class SwaggerUtils
{

	public static void addTagAndPathInformations(final Swagger swaggerData, 
		final AbstractRestResource<?> resource)
	{
		Class<? extends IResource> resourceClass = resource.getClass();
		ResourcePath mountAnnotation = resourceClass.getAnnotation(ResourcePath.class);
		Map<Method, MethodMappingInfo> mappedMethodsInfos = resource.getMappedMethodsInfo();
		String basePath = mountAnnotation.value();
		
		for (MethodMappingInfo methodMappingInfo : mappedMethodsInfos.values())
		{
			Path path = new Path();
			HttpMethod httpMethod = methodMappingInfo.getHttpMethod();
			Operation operation = new Operation();
			
			operation.tag(resourceClass.getSimpleName());
			
			SwaggerUtils.loadOperationParameters(operation, methodMappingInfo);
			
			path.set(httpMethod.name().toLowerCase(), operation);
			
			swaggerData.path(Strings.join("/", basePath, methodMappingInfo.getMountPath()), path);
		}
		
		swaggerData.addTag(new Tag().name(resourceClass.getSimpleName()));
	}

	public static void loadOperationParameters(final Operation operation, 
		final MethodMappingInfo methodMappingInfo)
	{
		SegmentIterator segmentIterator = new SegmentIterator(
			methodMappingInfo.getSegments().iterator());
		
		List<MethodParameter<?>> methodParameters = methodMappingInfo.getMethodParameters();
		
		for (MethodParameter<?> methodParameter : methodParameters)
		{
			Annotation annotationParam = methodParameter.getAnnotationParam();
			
			if (annotationParam == null)
			{
				annotationParam = methodParameter.getParameterClass()
					.getAnnotation(ApiParam.class);
			}
			
			String parameterName = annotationParam != null ? 
				ReflectionUtils.getAnnotationField(annotationParam, "value", "") :
				segmentIterator.nextParamName();
			
			if (annotationParam instanceof RequestBody)
			{
				parameterName = "body";
			}	
			
			Parameter parameter = annotationToSwaggerParam(annotationParam, 
				methodMappingInfo);
			
			parameter.setName(parameterName);
			
			operation.addParameter(parameter);
		}
		
		operation.setConsumes(Arrays.asList(methodMappingInfo.getInputFormat()));
		operation.setProduces(Arrays.asList(methodMappingInfo.getOutputFormat()));
	}

	public static Parameter annotationToSwaggerParam(final Annotation annotationParam, 
		final MethodMappingInfo methodMappingInfo)
	{
		Parameter parameter = null;
		
		if (annotationParam instanceof RequestBody)
		{
			parameter = new BodyParameter();
		} 
		else if (annotationParam instanceof HeaderParam) 
		{
			parameter = new HeaderParameter();
		}
		else if (annotationParam instanceof RequestParam) 
		{
			HttpMethod httpMethod = methodMappingInfo.getHttpMethod();
			boolean isPost = httpMethod.equals(HttpMethod.POST);
			
			parameter = isPost ? new FormParameter() : new QueryParameter();
		}
		else if (annotationParam instanceof CookieParam) 
		{
			parameter = new CookieParameter();
		}
		
		return parameter == null ? new PathParameter() : parameter;
	}

}
