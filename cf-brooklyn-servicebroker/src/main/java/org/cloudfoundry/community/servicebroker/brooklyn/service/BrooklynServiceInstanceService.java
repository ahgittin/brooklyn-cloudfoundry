package org.cloudfoundry.community.servicebroker.brooklyn.service;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cloudfoundry.community.servicebroker.brooklyn.model.ApplicationSpec;
import org.cloudfoundry.community.servicebroker.brooklyn.model.Entity;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import brooklyn.rest.domain.EntitySpec;
import brooklyn.rest.domain.TaskSummary;

import com.google.common.collect.ImmutableSet;

@Service
public class BrooklynServiceInstanceService implements ServiceInstanceService {

	private Map<String, ServiceInstance> repository = new ConcurrentHashMap<String, ServiceInstance>();

	private BrooklynRestAdmin admin;

	@Autowired
	public BrooklynServiceInstanceService(BrooklynRestAdmin admin) {
		this.admin = admin;
	}

	@Override
	public ServiceInstance createServiceInstance(ServiceDefinition service,
			String serviceInstanceId, String planId, String organizationGuid,
			String spaceGuid) throws ServiceInstanceExistsException,
			ServiceBrokerException {

		// check if exists already
		ServiceInstance instance = getServiceInstance(serviceInstanceId);
		if (instance != null) {
			throw new ServiceInstanceExistsException(instance);
		}
		

		//ApplicationSpec applicationSpec = new ApplicationSpec();
		String location = "localhost"; // default
		for(Plan p : service.getPlans()){
			if(p.getId().equals(planId)){
				location = p.getName();
			}
		}
//		applicationSpec.setLocation(location);
//		applicationSpec.setServices(Arrays.asList(service.getId()));
		
//		brooklyn.rest.domain.ApplicationSpec spec = brooklyn.rest.domain.ApplicationSpec.builder()
//			.entities(ImmutableSet.of(new EntitySpec()))
//			.locations(ImmutableSet.of(location)).build();
		
		TaskSummary taskSummary = admin.createApplication("{\"services\":[\"type\": \"" + service.getId() + "\"], \"locations\": [ \"" + location +"\"]}");

		//Entity response = admin.createApplication(applicationSpec);

		instance = new ServiceInstance(serviceInstanceId,
				taskSummary.getEntityId(),//response.getEntityId(), 
				planId, organizationGuid, spaceGuid,
				null);
		repository.put(serviceInstanceId, instance);
		return instance;
	}

	@Override
	public ServiceInstance getServiceInstance(String id) {
		return repository.get(id);
	}

	@Override
	public ServiceInstance updateServiceInstance(String instanceId,
			String planId) throws ServiceInstanceUpdateNotSupportedException,
			ServiceBrokerException, ServiceInstanceDoesNotExistException {
		throw new ServiceInstanceUpdateNotSupportedException("");
	}

	@Override
	public ServiceInstance deleteServiceInstance(String id, String serviceId,
			String planId) throws ServiceBrokerException {
		ServiceInstance instance = getServiceInstance(id);
		if (instance != null) {
			repository.remove(id);
			admin.deleteApplication(instance.getServiceDefinitionId());
		}
		return instance;
	}

}
