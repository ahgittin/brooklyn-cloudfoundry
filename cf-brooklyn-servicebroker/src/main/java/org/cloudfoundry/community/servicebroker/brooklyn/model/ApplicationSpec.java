package org.cloudfoundry.community.servicebroker.brooklyn.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationSpec {
	
	public static class Service {
		private String type;
		
		public String getType() {
			return type;
		}
	}
	
	private List<Service> services;
	private String location;
	
	
	public List<Service> getServices() {
		return services;
	}
	
	public void setServices(List<String> services) {
		this.services = new ArrayList<Service>();
		for(String s : services){
			Service service = new Service();
			service.type = s;
			this.services.add(service);
		}
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}

}
