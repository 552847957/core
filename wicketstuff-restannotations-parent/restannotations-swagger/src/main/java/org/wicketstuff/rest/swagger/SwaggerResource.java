package org.wicketstuff.rest.swagger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.string.Strings;
import org.wicketstuff.rest.annotations.ResourcePath;
import org.wicketstuff.rest.resource.AbstractRestResource;
import org.wicketstuff.rest.resource.MethodMappingInfo;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;

public class SwaggerResource implements IResource 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3947099959365814199L;

	private final Swagger swaggerData;
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	public SwaggerResource(List<IResource> restResources, Swagger swaggerData)
	{
		this.swaggerData = buildSwagger(restResources, swaggerData);
		this.mapper.setSerializationInclusion(Include.NON_NULL);
	}

	private Swagger buildSwagger(final List<IResource> restResources, Swagger swaggerData)
	{
		WebApplication application = WebApplication.get();
		WicketFilter wicketFilter = application.getWicketFilter();
		
		for (IResource iResource : restResources)
		{
			if (iResource instanceof AbstractRestResource)
			{
				addTagAndPathInformations(swaggerData, 
					(AbstractRestResource<?>)iResource);
			}
		}
		
		swaggerData.setBasePath(wicketFilter.getFilterPath());
		
		return swaggerData;
	}
	
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
			path.set(methodMappingInfo.getHttpMethod().name().toLowerCase(), 
				new Operation());
			
			swaggerData.path(Strings.join("/", basePath, methodMappingInfo.getMountPath()), path);
		}
		
		swaggerData.addTag(new Tag().name(resourceClass.getSimpleName()));
	}

	@Override
	public void respond(Attributes attributes)
	{
		Response response = attributes.getResponse();
		
		try
		{
			response.write(mapper.writeValueAsString(swaggerData));
		}
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}
	}		
}