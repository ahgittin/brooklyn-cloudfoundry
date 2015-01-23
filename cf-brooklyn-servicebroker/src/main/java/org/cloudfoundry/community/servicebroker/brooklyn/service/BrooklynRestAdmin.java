package org.cloudfoundry.community.servicebroker.brooklyn.service;

import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.community.servicebroker.brooklyn.config.BrooklynConfig;
import org.cloudfoundry.community.servicebroker.brooklyn.model.ApplicationSpec;
import org.cloudfoundry.community.servicebroker.brooklyn.model.CatalogApplication;
import org.cloudfoundry.community.servicebroker.brooklyn.model.Entity;
import org.cloudfoundry.community.servicebroker.brooklyn.model.EntitySummary;
import org.cloudfoundry.community.servicebroker.brooklyn.model.Location;
import org.cloudfoundry.community.servicebroker.brooklyn.model.SensorSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class BrooklynRestAdmin {
	
	// TODO: This class would be simpler if it used the Brooklyn client to access the
	//       the REST api.
	
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

	public List<SensorSummary[]> getEntitySensors(String serviceId) {
		EntitySummary summary = restTemplate.getForObject(config.toFullUrl("v1/applications/{application}/entities"), EntitySummary.class);
		List<SensorSummary[]> sensors = new ArrayList<SensorSummary[]>();
		for(String link : summary.getLinks()){
			sensors.add(restTemplate.getForObject(config.toFullUrl(link), SensorSummary[].class));
		}
		return sensors;
	}
}
