package org.wicketstuff.rest.heteaos;

import org.wicketstuff.rest.resource.MethodMappingInfo;
import org.wicketstuff.rest.utils.http.HttpMethod;

public interface IHateaosUriResolver {
	public <T> String getUri(Class<T> clazz, HttpMethod httpMethod, T entity);
	
	public void registerUriMethod(Class<?> clazz, MethodMappingInfo mappingInfo);
	
	public void registerUriMethod(Class<?> clazz, MethodMappingInfo mappingInfo, String fieldName);
}
