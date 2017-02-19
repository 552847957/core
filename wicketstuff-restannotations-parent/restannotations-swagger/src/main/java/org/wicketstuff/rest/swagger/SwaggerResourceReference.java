package org.wicketstuff.rest.swagger;

import java.util.List;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

import io.swagger.models.Swagger;

public class SwaggerResourceReference extends ResourceReference
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 119988330726975225L;
	private final SwaggerResource swaggerResource;

	public SwaggerResourceReference(final String name, final List<IResource> restResources,
									final Swagger swaggerData)
	{
		super(name);
		this.swaggerResource = new SwaggerResource(restResources, swaggerData);
	}

	@Override
	public IResource getResource()
	{
		return swaggerResource;
	}
}
