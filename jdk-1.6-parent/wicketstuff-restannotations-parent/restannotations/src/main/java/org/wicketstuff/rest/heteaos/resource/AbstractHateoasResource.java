package org.wicketstuff.rest.heteaos.resource;

import java.util.List;
import java.util.Map;

import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.wicketstuff.rest.contenthandling.IWebSerialDeserial;
import org.wicketstuff.rest.heteaos.IHateaosUriResolver;
import org.wicketstuff.rest.resource.AbstractRestResource;
import org.wicketstuff.rest.resource.MethodMappingInfo;
import org.wicketstuff.rest.utils.http.HttpMethod;

public abstract class AbstractHateoasResource<T extends IWebSerialDeserial> extends
		AbstractRestResource<T> {
	private final IHateaosUriResolver uriResolver;

	public AbstractHateoasResource(T serialDeserial, IHateaosUriResolver uriResolver) {
		this(serialDeserial, null, uriResolver);
	}

	public AbstractHateoasResource(T serialDeserial, IRoleCheckingStrategy roleCheckingStrategy,
			IHateaosUriResolver uriResolver) {
		super(serialDeserial, roleCheckingStrategy);
		this.uriResolver = uriResolver;

		loadHateaosLinks();
	}

	private void loadHateaosLinks() {
		Class<?> supportedClasses = getHandledEntityClass();
		Map<String, List<MethodMappingInfo>> mappedMethods = getMappedMethods();
		
		List<MethodMappingInfo> candidatesMethos = mappedMethods.get("1_" + HttpMethod.DELETE);
	}

	protected abstract Class<?> getHandledEntityClass();
}
