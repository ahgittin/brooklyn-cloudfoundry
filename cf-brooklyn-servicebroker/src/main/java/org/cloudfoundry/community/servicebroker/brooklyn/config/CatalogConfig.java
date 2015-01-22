package org.cloudfoundry.community.servicebroker.brooklyn.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.brooklyn.model.CatalogApplication;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.DashboardClient;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CatalogConfig {

	// TODO get this from properties
	//public static String BROOKLYN_BASE_URL = "http://127.0.0.1:8081";
	@Autowired
	private BrooklynConfig config;
	
	@Autowired
	private RestTemplate restTemplate;

	@Bean
	public Catalog catalog() {
		// get catalog info from REST call
		CatalogApplication[] page = restTemplate.getForObject(config.toFullUrl("v1/catalog/applications"), CatalogApplication[].class);
		List<ServiceDefinition> definitions = new ArrayList<ServiceDefinition>();
		for (CatalogApplication app : page) {
			String id = app.getId();
			String name = app.getName();
			String description = app.getDescription();
			boolean bindable = true;
			boolean planUpdatable = false;
			List<Plan> plans = getPlans();
			List<String> tags = getTags();
			Map<String, Object> metadata = getServiceDefinitionMetadata();
			List<String> requires = getTags();
			DashboardClient dashboardClient = null;

			definitions.add(new ServiceDefinition(id,
					name, description, bindable, planUpdatable, plans, tags,
					metadata, requires, dashboardClient));
		}

		return new Catalog(definitions);
	}

	private List<String> getTags() {
		return Arrays.asList();
	}

	private List<Plan> getPlans() {
		String id = "brooklyn-plan";
		String name = "Default Brooklyn Plan";
		String description = "This is the default brooklyn plan";
		Map<String, Object> metadata = new HashMap<String, Object>();
		Plan plan = new Plan(id, name, description, metadata);
		return Arrays.asList(plan);
	}

	private Map<String, Object> getServiceDefinitionMetadata() {
		Map<String, Object> metadata = new HashMap<String, Object>();
		return metadata;
	}

}
