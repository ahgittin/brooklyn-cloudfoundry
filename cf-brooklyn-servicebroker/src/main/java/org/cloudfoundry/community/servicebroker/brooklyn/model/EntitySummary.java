package org.cloudfoundry.community.servicebroker.brooklyn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntitySummary {
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Links {
		String sensors;
		
		public String getSensors() {
			return sensors;
		}
	}
	
	private String name;
	private Links links;

	public String getName() {
		return name;
	}
	
	public Links getLinks() {
		return links;
	}

}
