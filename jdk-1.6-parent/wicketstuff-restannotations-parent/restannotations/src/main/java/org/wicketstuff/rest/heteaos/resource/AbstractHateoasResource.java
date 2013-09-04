package org.wicketstuff.rest.heteaos.resource;

import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.wicketstuff.rest.contenthandling.IWebSerialDeserial;
import org.wicketstuff.rest.heteaos.IHateaosUriResolver;
import org.wicketstuff.rest.heteaos.annotations.ClassHandler;
import org.wicketstuff.rest.resource.AbstractRestResource;

public abstract class AbstractHateoasResource<T extends IWebSerialDeserial> extends AbstractRestResource<T> {
	private final IHateaosUriResolver uriResolver;
	
	public AbstractHateoasResource(T serialDeserial, IHateaosUriResolver uriResolver) {
		this(serialDeserial, null, uriResolver);
	}

	public AbstractHateoasResource(T serialDeserial, IRoleCheckingStrategy roleCheckingStrategy, IHateaosUriResolver uriResolver) {
		super(serialDeserial, roleCheckingStrategy);
		this.uriResolver = uriResolver;
		
		loadHateaosLinks();
	}

	private void loadHateaosLinks() {
		ClassHandler classHandler = getClass().getAnnotation(ClassHandler.class);
		Class<?> supportedClasses = classHandler.value();
		
		
	}
}
