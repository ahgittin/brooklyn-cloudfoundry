package org.cloudfoundry.community.servicebroker.brooklyn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogApplication {

	private String id;
	private String name;
	private String registeredType;
	private String javaType;
	private String type;
	private String planYaml;
	private String description;
	private String iconUrl;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getRegisteredType() {
		return registeredType;
	}

	public String getJavaType() {
		return javaType;
	}

	public String getType() {
		return type;
	}

	public String getPlanYaml() {
		return planYaml;
	}

	public String getDescription() {
		return description;
	}

	public String getIconUrl() {
		return iconUrl;
	}

}
