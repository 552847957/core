package org.wicketstuff.rest.resource.gson.hateous;

import java.util.List;

public class HateousEntity {
	private final Object targetEntity;
	private final List links;
	
	public HateousEntity(Object targetEntity, List links) {
		this.targetEntity = targetEntity;
		this.links = links;
	}

	public Object getTargetEntity() {
		return targetEntity;
	}

	public List getLinks() {
		return links;
	}
}
