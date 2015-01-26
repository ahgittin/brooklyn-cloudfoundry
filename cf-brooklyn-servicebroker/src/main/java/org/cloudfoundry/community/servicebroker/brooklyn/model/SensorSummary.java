package org.cloudfoundry.community.servicebroker.brooklyn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorSummary {
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Links {
		String self;
		
		public String getSelf() {
			return self;
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
