package org.cloudfoundry.community.servicebroker.brooklyn.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.brooklyn.model.CatalogApplication;
import org.cloudfoundry.community.servicebroker.brooklyn.model.Location;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.DashboardClient;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import brooklyn.rest.domain.LocationSummary;

@Service
public class BrooklynCatalogService implements CatalogService{

	@Autowired
	private BrooklynRestAdmin admin;

	@Override
	public Catalog getCatalog() {
		CatalogApplication[] page = admin.getCatalogApplications();
		
		List<ServiceDefinition> definitions = new ArrayList<ServiceDefinition>();
		for (CatalogApplication app : page) {
			String id = app.getId();
			String name = app.getName();
			String description = app.getDescription();
			boolean bindable = true;
			boolean planUpdatable = false;
			List<Plan> plans = getPlans(id);
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

	private List<Plan> getPlans(String serviceId) {
		List<LocationSummary> locations = admin.getLocations();
		List<Plan> plans = new ArrayList<Plan>();
		for (LocationSummary l : locations) {
			String id = serviceId + "." + l.getName();
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

	@Override
	public ServiceDefinition getServiceDefinition(String serviceId) {
		for(ServiceDefinition def : getCatalog().getServiceDefinitions()){
			if(def.getId().equals(serviceId)){
				return def;
			}
		}
		return null;
	}

}
