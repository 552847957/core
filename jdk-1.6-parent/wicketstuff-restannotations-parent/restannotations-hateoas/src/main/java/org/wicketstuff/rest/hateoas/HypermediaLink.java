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
package org.wicketstuff.rest.hateoas;

import org.wicketstuff.rest.resource.MethodMappingInfo;

public class HypermediaLink {
	private final MethodMappingInfo mappingInfo;
	private final String rel;
	private final String type;
	private final Class<?> entityClass;
	
	public HypermediaLink(MethodMappingInfo mappingInfo, String rel, String type, 
		          Class<?> entityClass) {
		this.mappingInfo = mappingInfo;
		this.rel = rel;
		this.type = type;
		this.entityClass = entityClass;
	}

	public String getRel() {
		return rel;
	}

	public String getType() {
		return type;
	}

	public Class<?> getResourceClass()
	{
	    return entityClass;
	}

	public MethodMappingInfo getMappingInfo()
	{
	    return mappingInfo;
	}
}
