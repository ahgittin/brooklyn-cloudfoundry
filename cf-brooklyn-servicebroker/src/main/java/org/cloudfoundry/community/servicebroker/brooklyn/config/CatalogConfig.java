package org.cloudfoundry.community.servicebroker.brooklyn.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.brooklyn.model.CatalogApplication;
import org.cloudfoundry.community.servicebroker.brooklyn.model.Location;
import org.cloudfoundry.community.servicebroker.brooklyn.service.BrooklynRestAdmin;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.DashboardClient;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogConfig {

	@Autowired
	private BrooklynRestAdmin admin;

	@Bean
	public Catalog catalog() {
		CatalogApplication[] page = admin.getCatalogApplications();
		
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

			definitions.add(new ServiceDefinition(id, name, description,
					bindable, planUpdatable, plans, tags, metadata, requires,
					dashboardClient));
		}

		return new Catalog(definitions);
	}

	private List<String> getTags() {
		return Arrays.asList();
	}

	private List<Plan> getPlans() {
		Location[] locations = admin.getLocations();
		List<Plan> plans = new ArrayList<Plan>();
		for (Location l : locations) {
			String id = l.getName();
			String name = l.getSpec();
			String description = "The location on which to deploy this service";
			Map<String, Object> metadata = new HashMap<String, Object>();
			plans.add(new Plan(id, name, description, metadata));
		}
		return plans;
	}

	private Map<String, Object> getServiceDefinitionMetadata() {
		Map<String, Object> metadata = new HashMap<String, Object>();
		return metadata;
	}

}
