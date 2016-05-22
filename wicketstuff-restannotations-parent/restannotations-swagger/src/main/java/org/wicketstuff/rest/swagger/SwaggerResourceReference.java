package org.wicketstuff.rest.swagger;

import java.util.List;

import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.models.Swagger;

public class SwaggerResourceReference extends ResourceReference
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 119988330726975225L;
	private final SwaggerResource swaggerResource;

	public SwaggerResourceReference(String name, List<IResource> restResources)
	{
		super(name);
		this.swaggerResource = new SwaggerResource(restResources);
	}

	@Override
	public IResource getResource()
	{
		return swaggerResource;
	}

	public class SwaggerResource implements IResource 
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3947099959365814199L;

		private final Swagger swaggerData;
		
		private final ObjectMapper mapper = new ObjectMapper();
		
		public SwaggerResource(List<IResource> restResources)
		{
			this.swaggerData = buildSwagger(restResources);
		}

		private Swagger buildSwagger(final List<IResource> restResources2)
		{
			return new Swagger();
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
}
