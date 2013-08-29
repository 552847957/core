package org.wicketstuff.rest.heteaos.resource;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.wicketstuff.rest.contenthandling.IWebSerialDeserial;
import org.wicketstuff.rest.heteaos.IHateaosUriResolver;
import org.wicketstuff.rest.heteaos.annotations.ClassHandler;
import org.wicketstuff.rest.resource.AbstractRestResource;
import org.wicketstuff.rest.utils.http.HttpMethod;

public abstract class AbstractHateoasResource<T extends IWebSerialDeserial> extends AbstractRestResource<T> {
	private final Map<HttpMethod, Integer> httpMethodAndSegNumber = new HashMap<HttpMethod, Integer>()
    {
		{
			put(HttpMethod.POST, 0);
			put(HttpMethod.PUT, 0);
			put(HttpMethod.GET, 1);
			put(HttpMethod.DELETE, 1);
		}
	};
	
	public AbstractHateoasResource(T serialDeserial, IHateaosUriResolver uriResolver) {
		super(serialDeserial);
		loadHateaosData(uriResolver);
	}

	public AbstractHateoasResource(T serialDeserial, IRoleCheckingStrategy roleCheckingStrategy, IHateaosUriResolver uriResolver) {
		super(serialDeserial, roleCheckingStrategy);
		loadHateaosData(uriResolver);
	}

	private void loadHateaosData(IHateaosUriResolver uriResolver) {
		ClassHandler classHandler = getClass().getAnnotation(ClassHandler.class);
		Class<?>[] supportedClasses = classHandler.value();
		
		for (int i = 0; i < supportedClasses.length; i++) {
			Class<?> clazz = supportedClasses[i];
			
			
		}
	}
}
