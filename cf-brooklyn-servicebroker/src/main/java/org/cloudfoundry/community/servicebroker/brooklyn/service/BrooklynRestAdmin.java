package org.cloudfoundry.community.servicebroker.brooklyn.service;

import org.cloudfoundry.community.servicebroker.brooklyn.config.BrooklynConfig;
import org.cloudfoundry.community.servicebroker.brooklyn.model.ApplicationSpec;
import org.cloudfoundry.community.servicebroker.brooklyn.model.CatalogApplication;
import org.cloudfoundry.community.servicebroker.brooklyn.model.Entity;
import org.cloudfoundry.community.servicebroker.brooklyn.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class BrooklynRestAdmin {
	
	@Autowired
	private BrooklynConfig config;
	@Autowired
	private RestTemplate restTemplate;
	
	public CatalogApplication[] getCatalogApplications() {
		CatalogApplication[] page;
		try{
			page = restTemplate.getForObject(
				config.toFullUrl("v1/catalog/applications"),
				CatalogApplication[].class);
		}catch(RestClientException e){
			page = new CatalogApplication[0];
		}
		return page;
	}

	public Location[] getLocations() {
		Location[] locations;
		try{ 
			locations = restTemplate.getForObject(
				config.toFullUrl("v1/locations"), Location[].class);
		} catch (RestClientException e){
			locations = new Location[0];
		}
		return locations;
	}
	
	public Entity createApplication(ApplicationSpec applicationSpec) {
		Entity response;
		try {
			restTemplate.getMessageConverters().add(
					new MappingJackson2HttpMessageConverter());
			restTemplate.getMessageConverters().add(
					new StringHttpMessageConverter());
			response = restTemplate.postForObject(
					config.toFullUrl("v1/applications"), applicationSpec,
					Entity.class);
		} catch (RestClientException e) {
			response = new Entity();
		}
		return response;
	}
	
	public void deleteApplication(String id) {
		try {
			restTemplate.delete(
					config.toFullUrl("v1/applications/{application}"), id);
		} catch (RestClientException e) {
			// TODO log error
		}
	}
}
