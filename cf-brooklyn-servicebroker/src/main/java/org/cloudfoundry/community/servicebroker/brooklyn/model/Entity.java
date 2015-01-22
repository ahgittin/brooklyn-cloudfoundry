package org.cloudfoundry.community.servicebroker.brooklyn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Entity {
	
	private String entityId;

	public String getEntityId() {
		return entityId;
	}
}
