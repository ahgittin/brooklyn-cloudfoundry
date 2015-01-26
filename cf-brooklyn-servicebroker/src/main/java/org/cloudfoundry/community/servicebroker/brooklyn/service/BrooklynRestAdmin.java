package org.cloudfoundry.community.servicebroker.brooklyn.service;

import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.community.servicebroker.brooklyn.config.BrooklynConfig;
import org.cloudfoundry.community.servicebroker.brooklyn.model.ApplicationSpec;
import org.cloudfoundry.community.servicebroker.brooklyn.model.CatalogApplication;
import org.cloudfoundry.community.servicebroker.brooklyn.model.Entity;
import org.cloudfoundry.community.servicebroker.brooklyn.model.EntitySensor;
import org.cloudfoundry.community.servicebroker.brooklyn.model.EntitySummary;
import org.cloudfoundry.community.servicebroker.brooklyn.model.SensorSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import brooklyn.rest.client.BrooklynApi;
import brooklyn.rest.domain.CatalogItemSummary;
import brooklyn.rest.domain.LocationSummary;

@Service
public class BrooklynRestAdmin {
	
	// TODO: This class would be simpler if it used the Brooklyn client to access the
	//       the REST api.
	
	@Autowired
	private BrooklynConfig config;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private BrooklynApi restApi;
	
	public CatalogApplication[] getCatalogApplications() {
		CatalogApplication[] page;
		try{
			page = restTemplate.getForObject(
				config.toFullUrl("v1/catalog/applications"), CatalogApplication[].class);
		}catch(RestClientException e){
			page = new CatalogApplication[0];
		}
		return page;
	}
	
	public List<CatalogItemSummary> getCatalogApplications2(){
		return restApi.getCatalogApi().listApplications("", "");
	}

	public List<LocationSummary> getLocations() {
		return restApi.getLocationApi().list();
	}
	
	public Entity createApplication(ApplicationSpec applicationSpec) {
		Entity response;
		try {
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
			response = restTemplate.postForObject(
					config.toFullUrl("v1/applications"), applicationSpec, Entity.class);
		} catch (RestClientException e) {
			response = new Entity();
		}
		return response;
	}
	
	public void deleteApplication(String id) {
		System.out.println("deleting id " + id);
		restApi.getApplicationApi().delete(id);
	}
	
	public String getSensorValue(String link){
		return restTemplate.getForObject(config.toFullUrl(link), String.class);
	}

	public List<EntitySensor> getEntitySensors(String serviceId) {
		
		EntitySummary[] summary = restTemplate.getForObject(
				config.toFullUrl("v1/applications/{application}/entities"), EntitySummary[].class, serviceId);
		
		List<EntitySensor> entitySensors = new ArrayList<EntitySensor>();
		for(EntitySummary s : summary){
			String sensorLink = s.getLinks().getSensors();
			SensorSummary[] sensors = restTemplate.getForObject(config.toFullUrl(sensorLink), SensorSummary[].class);
			entitySensors.add(new EntitySensor(s.getName(), sensors));
		}
		
		return entitySensors;
	}
}
