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
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.Property;
import io.swagger.servlet.ReaderContext;
import io.swagger.servlet.extensions.ServletReaderExtension;
import io.swagger.util.PrimitiveType;

public class SwaggerUtils
{

	public static void addTagAndPathInformations(final Swagger swaggerData, 
		final AbstractRestResource<?> resource)
	{
		final Class<? extends IResource> resourceClass = resource.getClass();
		final ReaderContext readerContext = new ReaderContext(swaggerData, resourceClass, null, null, false, null, null, null, null); 
		final ResourcePath mountAnnotation = resourceClass.getAnnotation(ResourcePath.class);
		final Map<Method, MethodMappingInfo> mappedMethodsInfos = resource.getMappedMethodsInfo();
		final String basePath = mountAnnotation.value();
		
		for (MethodMappingInfo methodMappingInfo : mappedMethodsInfos.values())
		{
			Path path = new Path();
			HttpMethod httpMethod = methodMappingInfo.getHttpMethod();
			Operation operation = new Operation();
			
			operation.tag(resourceClass.getSimpleName());
			
			loadOperationParameters(swaggerData, operation, methodMappingInfo);
			
			operation.setConsumes(Arrays.asList(methodMappingInfo.getInputFormat()));
			operation.setProduces(Arrays.asList(methodMappingInfo.getOutputFormat()));
			operation.setOperationId(methodMappingInfo.getMethod().getName());
			
			loadOperationOptsFromAnnotation(readerContext, operation, methodMappingInfo);
			
			path.set(httpMethod.name().toLowerCase(), operation);
			
			swaggerData.path(Strings.join("/", basePath, methodMappingInfo.getMountPath()), path);
		}
		
		swaggerData.addTag(new Tag().name(resourceClass.getSimpleName()));
	}

	public static void loadOperationOptsFromAnnotation(final ReaderContext readerContext, final Operation operation,
		final MethodMappingInfo methodMappingInfo)
	{
		final Method method = methodMappingInfo.getMethod();
		final ServletReaderExtension readerExtension = new ServletReaderExtension();
		
		readerExtension.applyDescription(operation, method);
		readerExtension.applySummary(operation, method);
		readerExtension.applyResponses(readerContext, operation, method);
		readerExtension.applySecurityRequirements(readerContext, operation, method);
	}

	public static void loadOperationParameters(final Swagger swaggerData, final Operation operation, 
		final MethodMappingInfo methodMappingInfo)
	{
		SegmentIterator segmentIterator = new SegmentIterator(
			methodMappingInfo.getSegments().iterator());
		
		List<MethodParameter<?>> methodParameters = methodMappingInfo.getMethodParameters();
		
		for (MethodParameter<?> methodParameter : methodParameters)
		{
			Annotation annotationParam = methodParameter.getAnnotationParam();
			Annotation swaggerAnnotationParam = ReflectionUtils.getAnnotationParam(
				methodParameter.getParamIndex(), methodMappingInfo.getMethod(), ApiParam.class);
			
			String parameterName = "";
			String parameterDescr = "";
			
			if (swaggerAnnotationParam != null)
			{
				parameterName = ReflectionUtils.getAnnotationField(swaggerAnnotationParam, "name", "");
				parameterDescr = ReflectionUtils.getAnnotationField(swaggerAnnotationParam, "value", "");
			}

			if (annotationParam != null && parameterName.isEmpty())
			{
				parameterName = ReflectionUtils.getAnnotationField(annotationParam, "value", "");
			}
			
			if (parameterName.isEmpty())
			{
				parameterName = segmentIterator.nextParamName();
			}
			
			if (annotationParam instanceof RequestBody)
			{
				parameterName = "body";
			}	
			
			Parameter parameter = annotationToSwaggerParam(swaggerData, annotationParam, 
				methodParameter);
			
			parameter.setName(parameterName);
			parameter.setDescription(parameterDescr);
			
			operation.addParameter(parameter);
		}
	}

	public static Parameter annotationToSwaggerParam(final Swagger swaggerData, final Annotation annotationParam, 
		final MethodParameter<?> methodParameter)
	{
		Parameter parameter = new PathParameter();
		MethodMappingInfo ownerMethod = methodParameter.getOwnerMethod();
		Class<?> parameterClass = methodParameter.getParameterClass();
		
		if (annotationParam instanceof RequestBody)
		{
			parameter = new BodyParameter().
				schema(new RefModel().asDefault(parameterClass.getSimpleName()));
		} 
		else if (annotationParam instanceof HeaderParam) 
		{
			parameter = new HeaderParameter();
		}
		else if (annotationParam instanceof RequestParam) 
		{
			HttpMethod httpMethod = ownerMethod.getHttpMethod();
			boolean isPost = httpMethod.equals(HttpMethod.POST);
			
			parameter = isPost ? new FormParameter() : new QueryParameter();
		}
		else if (annotationParam instanceof CookieParam) 
		{
			parameter = new CookieParameter();
		}
		
		if (parameter instanceof AbstractSerializableParameter)
		{
			AbstractSerializableParameter<?> serializableParameter = 
				(AbstractSerializableParameter<?>)parameter;
			
			Property typedProperty = PrimitiveType.createProperty(parameterClass);
			
			serializableParameter.setType(typedProperty.getType());
			serializableParameter.setFormat(typedProperty.getFormat());			
		}
		
		return parameter;
	}

}
