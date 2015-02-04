package org.cloudfoundry.community.servicebroker.brooklyn.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.cloudfoundry.community.servicebroker.brooklyn.config.BrooklynConfig;
import org.cloudfoundry.community.servicebroker.brooklyn.model.ApplicationSpec;
import org.cloudfoundry.community.servicebroker.brooklyn.model.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import brooklyn.rest.client.BrooklynApi;
import brooklyn.rest.domain.CatalogItemSummary;
import brooklyn.rest.domain.LocationSummary;
import brooklyn.rest.domain.TaskSummary;

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

	
	public List<CatalogItemSummary> getCatalogApplicaitons(){
		return restApi.getCatalogApi().listApplications("", "");
	}

	public List<LocationSummary> getLocations() {
		return restApi.getLocationApi().list();
	}
	
	public TaskSummary createApplication(String applicationSpec){
		Response response = restApi.getApplicationApi().createFromForm(applicationSpec);
		return BrooklynApi.getEntity(response, TaskSummary.class);
	}
	
	public TaskSummary deleteApplication(String id) {
		System.out.println("deleting id " + id);
		Response response = restApi.getApplicationApi().delete(id);
		return BrooklynApi.getEntity(response, TaskSummary.class);	
	}
	
	public Map<String, Object> getApplicationSensors(String application){
		Map<String, Object> result = new HashMap<String, Object>();
		for (brooklyn.rest.domain.EntitySummary s : restApi.getEntityApi().list(application)) {
			String entity = s.getId();
			Map<String, Object> sensors = new HashMap<String, Object>();
			for (brooklyn.rest.domain.SensorSummary sensorSummary : restApi.getSensorApi().list(application, entity)) {
				String sensor = sensorSummary.getName();
				sensors.put(sensorSummary.getName(), restApi.getSensorApi().get(application, entity, sensor, false));
			}
			result.put(s.getName(), sensors);
		}
		return result;
	}

	public String postBlueprint(String file) {
		Response response = restApi.getCatalogApi().create(file);
		return BrooklynApi.getEntity(response, String.class);
	}
}
