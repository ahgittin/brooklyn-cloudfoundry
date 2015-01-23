package org.cloudfoundry.community.servicebroker.brooklyn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntitySummary {
	
	private String[] links;

	public String[] getLinks() {
		return links;
	}

}
