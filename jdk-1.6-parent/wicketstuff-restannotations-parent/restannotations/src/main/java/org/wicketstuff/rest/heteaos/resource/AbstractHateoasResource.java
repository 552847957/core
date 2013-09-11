/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
