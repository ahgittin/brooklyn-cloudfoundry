package org.cloudfoundry.community.servicebroker.brooklyn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {

	private String name;
	private String spec;

	public String getName() {
		return name;
	}

	public String getSpec() {
		return spec;
	}

}
