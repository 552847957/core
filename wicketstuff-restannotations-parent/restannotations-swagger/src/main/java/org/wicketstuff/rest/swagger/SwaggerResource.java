package org.wicketstuff.rest.swagger;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.IResource;
import org.wicketstuff.rest.resource.AbstractRestResource;
import org.wicketstuff.rest.swagger.utils.SwaggerUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.swagger.models.Swagger;

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
		
		if (Application.get().usesDevelopmentConfig())
		{			
			this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
		}
	}

	private Swagger buildSwagger(final List<IResource> restResources, Swagger swaggerData)
	{
		WebApplication application = WebApplication.get();
		WicketFilter wicketFilter = application.getWicketFilter();
		
		for (IResource iResource : restResources)
		{
			if (iResource instanceof AbstractRestResource)
			{
				SwaggerUtils.addTagAndPathInformations(swaggerData, (AbstractRestResource<?>)iResource);
			}
		}
		
		swaggerData.setBasePath(wicketFilter.getFilterPath());
		
		return swaggerData;
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