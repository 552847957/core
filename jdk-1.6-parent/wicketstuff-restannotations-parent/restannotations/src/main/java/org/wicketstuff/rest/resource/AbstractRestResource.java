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
package org.wicketstuff.rest.resource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Localizer;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.util.collections.MultiMap;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.rest.annotations.AuthorizeInvocation;
import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.annotations.parameters.CookieParam;
import org.wicketstuff.rest.annotations.parameters.HeaderParam;
import org.wicketstuff.rest.annotations.parameters.MatrixParam;
import org.wicketstuff.rest.annotations.parameters.PathParam;
import org.wicketstuff.rest.annotations.parameters.RequestBody;
import org.wicketstuff.rest.annotations.parameters.RequestParam;
import org.wicketstuff.rest.contenthandling.IWebSerialDeserial;
import org.wicketstuff.rest.contenthandling.RestMimeTypes;
import org.wicketstuff.rest.resource.urlsegments.AbstractURLSegment;
import org.wicketstuff.rest.utils.http.HttpMethod;
import org.wicketstuff.rest.utils.http.HttpUtils;
import org.wicketstuff.rest.utils.reflection.MethodParameter;
import org.wicketstuff.rest.utils.reflection.ReflectionUtils;

/**
 * Base class to build a resource that serves REST requests.
 * 
 * @author andrea del bene
 * 
 */
public abstract class AbstractRestResource<T extends IWebSerialDeserial> implements IResource, IErrorMessageSource
{
	private static final Logger log = LoggerFactory.getLogger(AbstractRestResource.class);

	/**
	 * HashMap that stores every mapped method of the class. Mapped method are stored concatenating
	 * the number of the segments of their URL and their HTTP method (see annotation MethodMapping)
	 */
	private final Map<String, List<MethodMappingInfo>> mappedMethods;
	
	/**
	 * 
	 */
	private final Map<String, IValidator> declaredValidators = new HashMap<String, IValidator>();
	
	/**
	 * The implementation of {@link IWebSerialDeserial} that is used to serialize/desiarilze objects
	 * to/from string (for example to/from JSON)
	 */
	private final T objSerialDeserial;

	/** Role-checking strategy. */
	private final IRoleCheckingStrategy roleCheckingStrategy;

	/**
	 * Constructor with no role-checker (i.e we don't use annotation {@link AuthorizeInvocation}).
	 * 
	 * @param serialDeserial
	 *            General class that is used to serialize/desiarilze objects to string.
	 */
	public AbstractRestResource(T serialDeserial)
	{
		this(serialDeserial, null);
	}

	/**
	 * Main constructor that takes in input the object serializer/deserializer and the role-checking
	 * strategy to use.
	 * 
	 * @param serialDeserial
	 *            General class that is used to serialize/desiarilze objects to string
	 * @param roleCheckingStrategy
	 *            the role-checking strategy.
	 */
	public AbstractRestResource(T serialDeserial, IRoleCheckingStrategy roleCheckingStrategy)
	{
		Args.notNull(serialDeserial, "serialDeserial");

		this.objSerialDeserial = serialDeserial;
		this.roleCheckingStrategy = roleCheckingStrategy;
		this.mappedMethods = loadAnnotatedMethods(new MultiMap<String, MethodMappingInfo>());

		configureObjSerialDeserial(serialDeserial);
	}

	/***
	 * Handles a REST request invoking one of the methods annotated with {@link MethodMapping}. If
	 * the annotated method returns a value, this latter is automatically serialized to a given
	 * string format (like JSON, XML, etc...) and written to the web response.<br/>
	 * If no method is found to serve the current request, a 400 HTTP code is returned to the
	 * client. Similarly, a 401 HTTP code is return if the user doesn't own one of the roles
	 * required to execute an annotated method (See {@link AuthorizeInvocation}).
	 */
	@Override
	public final void respond(Attributes attributes)
	{
		AttributesWrapper attributesWrapper = new AttributesWrapper(attributes);

		PageParameters pageParameters = attributesWrapper.getPageParameters();
		WebResponse response = attributesWrapper.getWebResponse();
		HttpMethod httpMethod = attributesWrapper.getHttpMethod();

		// 1-select the best "candidate" method to serve the request
		MethodMappingInfo mappedMethod = selectMostSuitedMethod(attributesWrapper);

		if (mappedMethod != null)
		{
			// 2-check if user is authorized to invoke the method
			if (!isUserAuthorized(mappedMethod))
			{
				response.sendError(401, "User is not allowed to use this resource.");
				return;
			}

			// 3-extract method parameters
			List parametersValues = extractMethodParameters(mappedMethod, attributesWrapper);

			if (parametersValues == null)
			{
				noSuitableMethodFound(response, httpMethod);
				return;
			}

			// 4-validate method parameters
			List<IValidationError> validationErrors = validateMethodParameters(mappedMethod, parametersValues);

			if(validationErrors.size() > 0)
			{
				return;
			}
			
			// 5-invoke method triggering the before-after hooks
			onBeforeMethodInvoked(mappedMethod, attributes);
			Object result = invokeMappedMethod(mappedMethod.getMethod(), parametersValues, response);
			onAfterMethodInvoked(mappedMethod, attributes, result);

			// 6-if the invoked method returns a value, it is written to response
			if (result != null)
			{
				serializeObjectToResponse(response, result, mappedMethod.getMimeOutputFormat());
			}
		}
		else
		{
			noSuitableMethodFound(response, httpMethod);
		}
	}

	private boolean isUserAuthorized(MethodMappingInfo mappedMethod)
	{
		if (!hasAny(mappedMethod.getRoles()))
		{
			return false;
		}
		
		return true;
	}
	
	protected void noSuitableMethodFound(WebResponse response, HttpMethod httpMethod)
	{
		response.sendError(400, "No suitable method found for URL '" + extractUrlFromRequest() +
			"' and HTTP method " + httpMethod);
	}

	private List<IValidationError> validateMethodParameters(MethodMappingInfo mappedMethod, List parametersValues)
	{
		List<MethodParameter> methodParameters = mappedMethod.getMethodParameters();
		List<IValidationError> errors = new ArrayList<IValidationError>();
		
		for (MethodParameter methodParameter : methodParameters) 
		{
			int i = methodParameters.indexOf(methodParameter);
			
			String validatorKey = methodParameter.getValdatorKey();			
			IValidator validator = getValidator(validatorKey);
			Validatable validatable = new Validatable(parametersValues.get(i));
			
			if(validator != null)
			{
				validator.validate(validatable);
				errors.addAll(validatable.getErrors());
			}
			else if(Strings.isEmpty(validatorKey))
			{
				log.debug("No validator found for key '" + validatorKey + "'");
			}
		}
		
		return errors;
	}
	
	private String resolveErrorMessage(List<IValidationError> errors)
	{
		Localizer localizer = Application.get().getResourceSettings().getLocalizer();
		return null;
	}
	
	@Override
	public String getMessage(String key, Map<String, Object> vars) 
	{
		return null;
	}
	
	/**
	 * Invoked just before a mapped method is invoked to serve the current request.
	 * 
	 * @param mappedMethod
	 *            the mapped method.
	 * @param attributes
	 *            the current Attributes object.
	 */
	protected void onBeforeMethodInvoked(MethodMappingInfo mappedMethod, Attributes attributes)
	{
	}

	/**
	 * Invoked just after a mapped method has been invoked to serve the current request.
	 * 
	 * @param mappedMethod
	 *            the mapped method.
	 * @param attributes
	 *            the current Attributes object.
	 * @param result
	 *            the value returned by the invoked method.
	 */
	protected void onAfterMethodInvoked(MethodMappingInfo mappedMethod, Attributes attributes,
		Object result)
	{
	}

	/**
	 * Method invoked to serialize the result of the invoked method and write this value to the
	 * response.
	 * 
	 * @param response
	 *            The current response object.
	 * @param result
	 *            The object to write to response.
	 * @param restMimeFormats
	 */
	private void serializeObjectToResponse(WebResponse response, Object result, String mimeType)
	{
		try
		{
			response.setContentType(mimeType);
			objSerialDeserial.objectToResponse(result, response, mimeType);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error writing object to response.", e);
		}
	}

	/**
	 * Method invoked to select the most suited method to serve the current request.
	 * 
	 * @param mappedMethods
	 *            List of {@link MethodMappingInfo} containing the informations of mapped methods.
	 * @param pageParameters
	 *            The PageParameters of the current request.
	 * @return The "best" method found to serve the request.
	 */
	private MethodMappingInfo selectMostSuitedMethod(AttributesWrapper attributesWrapper)
	{
		int indexedParamCount = attributesWrapper.getPageParameters().getIndexedCount();
		PageParameters pageParameters = attributesWrapper.getPageParameters();
		List<MethodMappingInfo> mappedMethodsCandidates = mappedMethods.get(indexedParamCount +
			"_" + attributesWrapper.getHttpMethod());

		MultiMap<Integer, MethodMappingInfo> mappedMethodByScore = new MultiMap<Integer, MethodMappingInfo>();
		int highestScore = 0;

		// no method mapped
		if (mappedMethodsCandidates == null || mappedMethodsCandidates.size() == 0)
			return null;

		/**
		 * To select the "best" method, a score is assigned to every mapped method. To calculate the
		 * score method calculateScore is executed for every segment.
		 */
		for (MethodMappingInfo mappedMethod : mappedMethodsCandidates)
		{
			List<AbstractURLSegment> segments = mappedMethod.getSegments();
			int score = 0;

			for (AbstractURLSegment segment : segments)
			{
				int i = segments.indexOf(segment);
				String currentActualSegment = AbstractURLSegment.getActualSegment(pageParameters.get(
					i).toString());

				int partialScore = segment.calculateScore(currentActualSegment);

				if (partialScore == 0)
				{
					score = -1;
					break;
				}

				score += partialScore;
			}

			if (score >= highestScore)
			{
				highestScore = score;
				mappedMethodByScore.addValue(score, mappedMethod);
			}
		}
		// if we have more than one method with the highest score, throw
		// ambiguous exception.
		if (mappedMethodByScore.get(highestScore) != null &&
			mappedMethodByScore.get(highestScore).size() > 1)
			throwAmbiguousMethodsException(mappedMethodByScore.get(highestScore));

		return mappedMethodByScore.getFirstValue(highestScore);
	}

	/**
	 * Throw an exception if two o more methods have the same "score" for the current request. See
	 * method selectMostSuitedMethod.
	 * 
	 * @param list
	 *            the list of ambiguous methods.
	 */
	private void throwAmbiguousMethodsException(List<MethodMappingInfo> list)
	{
		WebRequest request = (WebRequest)RequestCycle.get().getRequest();
		String methodsNames = "";

		for (MethodMappingInfo urlMappingInfo : list)
		{
			if (!methodsNames.isEmpty())
				methodsNames += ", ";

			methodsNames += urlMappingInfo.getMethod().getName();
		}

		throw new WicketRuntimeException("Ambiguous methods mapped for the current request: URL '" +
			request.getClientUrl() + "', HTTP method " + HttpUtils.getHttpMethod(request) + ". " +
			"Mapped methods: " + methodsNames);
	}

	/**
	 * Method called to initialize and configure the object serializer/deserializer.
	 * 
	 * @param objSerialDeserial
	 *            the object serializer/deserializer
	 */
	protected void configureObjSerialDeserial(T objSerialDeserial)
	{
	};

	/***
	 * Internal method to load class methods annotated with {@link MethodMapping}
	 * 
	 * @param multiMap
	 * @return
	 */
	private Map<String, List<MethodMappingInfo>> loadAnnotatedMethods(
		MultiMap<String, MethodMappingInfo> mappedMethods)
	{
		Method[] methods = getClass().getDeclaredMethods();
		boolean isUsingAuthAnnot = false;

		for (int i = 0; i < methods.length; i++)
		{
			Method method = methods[i];
			MethodMapping methodMapped = method.getAnnotation(MethodMapping.class);
			AuthorizeInvocation authorizeInvocation = method.getAnnotation(AuthorizeInvocation.class);

			isUsingAuthAnnot = isUsingAuthAnnot || authorizeInvocation != null;

			if (methodMapped != null)
			{
				HttpMethod httpMethod = methodMapped.httpMethod();
				MethodMappingInfo methodMappingInfo = new MethodMappingInfo(methodMapped, method);

				if (!isMimeTypesSupported(methodMappingInfo.getMimeInputFormat()) ||
					!isMimeTypesSupported(methodMappingInfo.getMimeOutputFormat()))
					throw new WicketRuntimeException(
						"Mapped methods use a MIME type not supported by obj serializer/deserializer!");

				mappedMethods.addValue(
					methodMappingInfo.getSegmentsCount() + "_" + httpMethod.getMethod(),
					methodMappingInfo);
			}
		}
		// if AuthorizeInvocation has been found but no role-checker has been
		// configured, throw an exception
		if (isUsingAuthAnnot && roleCheckingStrategy == null)
			throw new WicketRuntimeException(
				"Annotation AuthorizeInvocation is used but no role-checking strategy has been set for the controller!");

		return makeListMapImmutable(mappedMethods);
	}

	/**
	 * Make a list map immutable.
	 * 
	 * @param listMap
	 *            the list map in input.
	 * @return the immutable list map.
	 */
	private <T, E> Map<T, List<E>> makeListMapImmutable(Map<T, List<E>> listMap)
	{
		for (T key : listMap.keySet())
		{
			listMap.put(key, Collections.unmodifiableList(listMap.get(key)));
		}

		return Collections.unmodifiableMap(listMap);
	}

	/**
	 * Checks if the given MIME type is supported by the current obj serial/deserial.
	 * 
	 * @param mimeType
	 *            the MIME type we want to check.
	 * @return true if the MIME type is supported, false otherwise.
	 */
	private boolean isMimeTypesSupported(String mimeType)
	{
		if (RestMimeTypes.TEXT_PLAIN.equals(mimeType))
			return true;

		return objSerialDeserial.isMimeTypeSupported(mimeType);
	}

	/***
	 * Invokes one of the resource methods annotated with {@link MethodMapping}.
	 * 
	 * @param mappedMethod
	 *            mapping info of the method.
	 * @param attributes
	 *            Attributes object for the current request.
	 * @return the value returned by the invoked method
	 */
	private List extractMethodParameters(MethodMappingInfo mappedMethod,
		AttributesWrapper attributesWrapper)
	{
		Method method = mappedMethod.getMethod();
		List parametersValues = new ArrayList();
		PageParameters pageParameters = attributesWrapper.getPageParameters();
		LinkedHashMap<String, String> pathParameters = mappedMethod.populatePathParameters(pageParameters);
		Iterator<String> pathParamsIterator = pathParameters.values().iterator();
		List<MethodParameter> methodParameters = mappedMethod.getMethodParameters();
		
		for (MethodParameter methodParameter : methodParameters)
		{
			Object paramValue = null;
			Annotation annotation = ReflectionUtils.getAnnotationParam(methodParameters.indexOf(methodParameter), method);

			// retrieve parameter value
			if (annotation != null)
				paramValue = extractParameterValue(methodParameter, pathParameters, annotation,
					pageParameters);
			else
				paramValue = extractParameterFromUrl(methodParameter, pathParamsIterator);

			// try to use the default value
			if (paramValue == null && !methodParameter.getDeaultValue().isEmpty())
				paramValue = toObject(methodParameter.getParameterClass(),
					methodParameter.getDeaultValue());

			// if parameter is null and is required, abort extraction.
			if (paramValue == null && methodParameter.isRequired())
			{
				return null;
			}

			parametersValues.add(paramValue);
		}

		return parametersValues;
	}

	/**
	 * Execute a method implemented in the current resource class
	 * 
	 * @param method
	 *            the method that must be executed.
	 * @param parametersValues
	 *            method parameters
	 * @param response
	 *            the current WebResponse object.
	 * @return the value (if any) returned by the method.
	 */
	private Object invokeMappedMethod(Method method, List parametersValues, WebResponse response)
	{
		try
		{
			return method.invoke(this, parametersValues.toArray());
		}
		catch (Exception e)
		{
			response.sendError(500, "General server error.");
			log.debug("Error invoking method '" + method.getName() + "'");
		}

		return null;
	}

	/**
	 * Utility method to extract the client URL from the current request.
	 * 
	 * @return the URL for the current request.
	 */
	static public Url extractUrlFromRequest()
	{
		return RequestCycle.get().getRequest().getClientUrl();
	}

	/**
	 * Extract the value for an annotated-method parameter (see package
	 * {@link org.wicketstuff.rest.annotations.parameters}).
	 * 
	 * @param methodParameter
	 *            the current method parameter.
	 * @param pathParameters
	 *            the values of path parameters for the current request.
	 * @param annotation
	 *            the annotation for the current parameter that indicates how to retrieve the value
	 *            for the current parameter.
	 * @param pageParameters
	 *            PageParameters for the current request.
	 * @return the extracted value.
	 */
	private Object extractParameterValue(MethodParameter methodParameter,
		LinkedHashMap<String, String> pathParameters, Annotation annotation,
		PageParameters pageParameters)
	{
		Object paramValue = null;
		Class<?> argClass = methodParameter.getParameterClass();
		String mimeInputFormat = methodParameter.getOwnerMethod().getMimeInputFormat();

		if (annotation instanceof RequestBody)
			paramValue = deserializeObjectFromRequest(argClass, mimeInputFormat);
		else if (annotation instanceof PathParam)
			paramValue = toObject(argClass, pathParameters.get(((PathParam)annotation).value()));
		else if (annotation instanceof RequestParam)
			paramValue = extractParameterFromQuery(pageParameters, (RequestParam)annotation,
				argClass);
		else if (annotation instanceof HeaderParam)
			paramValue = extractParameterFromHeader((HeaderParam)annotation, argClass);
		else if (annotation instanceof CookieParam)
			paramValue = extractParameterFromCookies((CookieParam)annotation, argClass);
		else if (annotation instanceof MatrixParam)
			paramValue = extractParameterFromMatrixParams(pageParameters, (MatrixParam)annotation,
				argClass);

		return paramValue;
	}

	/**
	 * Extract method parameter value from matrix parameters.
	 * 
	 * @param pageParameters
	 *            PageParameters for the current request.
	 * @param matrixParam
	 *            the {@link MatrixParam} annotation used for the current parameter.
	 * @param argClass
	 *            the type of the current method parameter.
	 * @return the value obtained from query parameters and converted to argClass.
	 */
	private Object extractParameterFromMatrixParams(PageParameters pageParameters,
		MatrixParam matrixParam, Class<?> argClass)
	{
		int segmentIndex = matrixParam.segmentIndex();
		String variableName = matrixParam.parameterName();
		String rawsSegment = pageParameters.get(segmentIndex).toString();
		Map<String, String> matrixParameters = AbstractURLSegment.getSegmentMatrixParameters(rawsSegment);

		if (matrixParameters.get(variableName) == null)
			return null;

		return toObject(argClass, matrixParameters.get(variableName));
	}

	/**
	 * Extract method parameter value from request header.
	 * 
	 * @param headerParam
	 *            the {@link HeaderParam} annotation used for the current method parameter.
	 * @param argClass
	 *            the type of the current method parameter.
	 * @return the extracted value converted to argClass.
	 */
	private Object extractParameterFromHeader(HeaderParam headerParam, Class<?> argClass)
	{
		String value = headerParam.value();
		WebRequest webRequest = (WebRequest)RequestCycle.get().getRequest();

		return toObject(argClass, webRequest.getHeader(value));
	}

	/**
	 * Extract method parameter's value from query string parameters.
	 * 
	 * @param pageParameters
	 *            the PageParameters of the current request.
	 * @param requestParam
	 *            the {@link RequestParam} annotation used for the current method parameter.
	 * @param argClass
	 *            the type of the current method parameter.
	 * @return the extracted value converted to argClass.
	 */
	private Object extractParameterFromQuery(PageParameters pageParameters,
		RequestParam requestParam, Class<?> argClass)
	{

		String value = requestParam.value();

		if (pageParameters.get(value) == null)
			return null;

		return toObject(argClass, pageParameters.get(value).toString());
	}

	/**
	 * Extract method parameter's value from cookies.
	 * 
	 * @param annotation
	 *            the {@link CookieParam} annotation used for the current method parameter.
	 * @param argClass
	 *            the type of the current method parameter.
	 * @return the extracted value converted to argClass.
	 */
	private Object extractParameterFromCookies(CookieParam cookieParam, Class<?> argClass)
	{
		String value = cookieParam.value();
		WebRequest webRequest = (WebRequest)RequestCycle.get().getRequest();

		if (webRequest.getCookie(value) == null)
			return null;

		return toObject(argClass, webRequest.getCookie(value).getValue());
	}

	/**
	 * Internal method that tries to extract an instance of the given class from the request body.
	 * 
	 * @param argClass
	 *            the type we want to extract from request body.
	 * @return the extracted object.
	 */
	private Object deserializeObjectFromRequest(Class<?> argClass, String mimeType)
	{
		WebRequest servletRequest = (WebRequest)RequestCycle.get().getRequest();
		try
		{
			return objSerialDeserial.requestToObject(servletRequest, argClass, mimeType);
		}
		catch (Exception e)
		{
			log.debug("Error deserializing object from request");
			return null;
		}
	}

	/***
	 * Extract a parameter values from the rest URL.
	 * 
	 * @param methodParameter
	 *            the current method parameter.
	 * @param pathParamIterator
	 *            an iterator on the current values of path parameters.
	 * 
	 * @return the parameter value.
	 */
	private Object extractParameterFromUrl(MethodParameter methodParameter,
		Iterator<String> pathParamIterator)
	{

		if (!pathParamIterator.hasNext())
			return null;

		return toObject(methodParameter.getParameterClass(), pathParamIterator.next());
	}

	/**
	 * Utility method to convert string values to the corresponding objects.
	 * 
	 * @param clazz
	 *            the type of the object we want to obtain.
	 * @param value
	 *            the string value we want to convert.
	 * @return the object corresponding to the converted string value, or null if value parameter is
	 *         null
	 */
	public static Object toObject(Class clazz, String value) throws IllegalArgumentException
	{
		if (value == null)
			return null;
		// we use the standard Wicket conversion mechanism to obtain the
		// converted value.
		try
		{
			IConverter converter = Application.get().getConverterLocator().getConverter(clazz);

			return converter.convertToObject(value, Session.get().getLocale());
		}
		catch (Exception e)
		{
			WebResponse response = (WebResponse)RequestCycle.get().getResponse();

			response.setStatus(400);
			log.debug("Could not find a suitable constructor for value '" + value + "' of type '" +
				clazz + "'");

			return null;
		}
	}

	/**
	 * Utility method to check that the user owns one of the roles provided in input.
	 * 
	 * @param roles
	 *            the checked roles.
	 * @return true if the user owns one of roles in input, false otherwise.
	 */
	protected final boolean hasAny(Roles roles)
	{
		if (roles.isEmpty())
		{
			return true;
		}
		else
		{
			return roleCheckingStrategy.hasAnyRole(roles);
		}
	}

	/**
	 * Set the status code for the current response.
	 * 
	 * @param statusCode
	 *            the status code we want to set on the current response.
	 */
	protected final void setResponseStatusCode(int statusCode)
	{
		try
		{
			Response request = RequestCycle.get().getResponse();
			WebResponse webRequest = (WebResponse)request;

			webRequest.setStatus(statusCode);
		}
		catch (Exception e)
		{
			throw new IllegalStateException(
				"Could not find a suitable WebResponse object for the current ThreadContext.", e);
		}
	}

	/**
	 * Return mapped methods grouped by number of segments and HTTP method. So for example, to get
	 * all methods mapped on a path with three segments and with GET method, the key to use will be
	 * "3_GET" (underscore-separated)
	 * 
	 * @return the immutable map containing mapped methods.
	 */
	protected Map<String, List<MethodMappingInfo>> getMappedMethods()
	{
		return mappedMethods;
	}
	
	protected void registerValidator(String key, IValidator validator){
		declaredValidators.put(key, validator);
	}
	
	protected void unregisterValidator(String key){
		declaredValidators.remove(key);
	}
	
	protected IValidator getValidator(String key){
		return declaredValidators.get(key);
	}
}

/**
 * Utility class to extract and handle the information from class IResource.Attributes
 * 
 * @author andrea del bene
 *
 */
class AttributesWrapper
{
	private final WebResponse webResponse;

	private final WebRequest webRequest;

	private final PageParameters pageParameters;

	private final HttpMethod httpMethod;

	public AttributesWrapper(Attributes attributes)
	{
		this.webRequest = (WebRequest)attributes.getRequest();
		this.webResponse = (WebResponse)attributes.getResponse();
		this.pageParameters = attributes.getParameters();
		this.httpMethod = HttpUtils.getHttpMethod(attributes.getRequest());
	}

	public WebResponse getWebResponse()
	{
		return webResponse;
	}

	public WebRequest getWebRequest()
	{
		return webRequest;
	}

	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	public HttpMethod getHttpMethod()
	{
		return httpMethod;
	}
}
