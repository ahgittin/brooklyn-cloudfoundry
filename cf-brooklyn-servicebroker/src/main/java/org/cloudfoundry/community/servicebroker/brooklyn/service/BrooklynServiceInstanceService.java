package org.cloudfoundry.community.servicebroker.brooklyn.service;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cloudfoundry.community.servicebroker.brooklyn.config.BrooklynConfig;
import org.cloudfoundry.community.servicebroker.brooklyn.model.ApplicationSpec;
import org.cloudfoundry.community.servicebroker.brooklyn.model.Entity;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BrooklynServiceInstanceService implements ServiceInstanceService {
	
	@Autowired
	private BrooklynConfig config;

	private Map<String, ServiceInstance> repository = new ConcurrentHashMap<String, ServiceInstance>();
	private RestTemplate restTemplate;

	@Autowired
	public BrooklynServiceInstanceService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
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

		ApplicationSpec applicationSpec = new ApplicationSpec();
		applicationSpec.setLocation("localhost");
		applicationSpec.setServices(Arrays.asList(service.getId()));

		restTemplate.getMessageConverters().add(
				new MappingJackson2HttpMessageConverter());
		restTemplate.getMessageConverters().add(
				new StringHttpMessageConverter());
		Entity response = restTemplate.postForObject(
				config.toFullUrl("v1/applications"),
				applicationSpec, Entity.class);

		instance = new ServiceInstance(serviceInstanceId,
				response.getEntityId(), planId, organizationGuid, spaceGuid,
				config.toFullUrl());
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
			restTemplate.delete(config.toFullUrl("v1/applications/{application}"),
					instance.getServiceDefinitionId());
		}
		return instance;
	}

}
