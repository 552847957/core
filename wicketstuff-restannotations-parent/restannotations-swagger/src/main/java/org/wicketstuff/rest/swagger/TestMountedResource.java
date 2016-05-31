package org.wicketstuff.rest.swagger;

import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.annotations.ResourcePath;
import org.wicketstuff.rest.annotations.parameters.RequestBody;
import org.wicketstuff.rest.contenthandling.IObjectSerialDeserial;
import org.wicketstuff.rest.contenthandling.json.webserialdeserial.JsonWebSerialDeserial;
import org.wicketstuff.rest.resource.AbstractRestResource;
import org.wicketstuff.rest.swagger.TestMountedResource.TestSerialDeserial;
import org.wicketstuff.rest.utils.http.HttpMethod;

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
	public Person getString(int id)
	{
		return  new Person();
	}
	
	@MethodMapping(value = "/person", httpMethod = HttpMethod.POST)
	public void postString(@RequestBody Person person)
	{
	}

}
