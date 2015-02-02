package org.cloudfoundry.community.servicebroker.brooklyn.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.brooklyn.config.BrooklynConfig;
import org.cloudfoundry.community.servicebroker.brooklyn.model.ApplicationSpec;
import org.cloudfoundry.community.servicebroker.brooklyn.model.CatalogApplication;
import org.cloudfoundry.community.servicebroker.brooklyn.model.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import brooklyn.rest.client.BrooklynApi;
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
	
	public Map<String, Object> getApplicationSensors(String application){
		Map<String, Object> result = new HashMap<String, Object>();
		for (brooklyn.rest.domain.EntitySummary s : restApi.getEntityApi().list(application)) {
			String entity = s.getId();
			Map<String, Object> sensors = new HashMap<String, Object>();
			for (brooklyn.rest.domain.SensorSummary sensorSummary : restApi.getSensorApi().list(application, entity)) {
				String sensor = sensorSummary.getName();
				sensors.put(sensorSummary.getName(), restApi.getSensorApi().get(application, entity, sensor));
			}
			result.put(s.getName(), sensors);
		}
		return result;
	}

	public void postBlueprint(String file) {
		restApi.getCatalogApi().create(file);
	}
}
