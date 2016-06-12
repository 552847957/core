package org.wicketstuff.rest.swagger;

import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.annotations.ResourcePath;
import org.wicketstuff.rest.annotations.parameters.RequestBody;
import org.wicketstuff.rest.contenthandling.IObjectSerialDeserial;
import org.wicketstuff.rest.contenthandling.json.webserialdeserial.JsonWebSerialDeserial;
import org.wicketstuff.rest.resource.AbstractRestResource;
import org.wicketstuff.rest.swagger.TestMountedResource.TestSerialDeserial;
import org.wicketstuff.rest.utils.http.HttpMethod;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

@ResourcePath("/api/test")
public class TestMountedResource extends AbstractRestResource<TestSerialDeserial>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8320914980960191681L;

	public TestMountedResource()
	{
		super(new TestSerialDeserial());
	}
	
	static class TestSerialDeserial extends JsonWebSerialDeserial
	{

		public TestSerialDeserial()
		{
			super(new IObjectSerialDeserial<String>()
			{

				@Override
				public String serializeObject(Object target, String mimeType)
				{
					return null;
				}

				@Override
				public <E> E deserializeObject(String source, Class<E> targetClass, String mimeType)
				{
					return null;
				}
			});
		}
		
	}
	
	@MethodMapping(value = "/person/{id}")
	@ApiOperation(value = "Find pet by ID", 
	    notes = "Returns a pet when ID <= 10.  ID > 10 or nonintegers will simulate API error conditions",
	    response = Person.class,
	    authorizations = @Authorization(value = "api_key")
	  )
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
    @ApiResponse(code = 404, message = "Pet not found") })
	public Person getString(@ApiParam(value = "Person id") int id)
	{
		return  new Person();
	}
	
	@MethodMapping(value = "/person", httpMethod = HttpMethod.POST)
	public void postString(@RequestBody Person person)
	{
	}

}
