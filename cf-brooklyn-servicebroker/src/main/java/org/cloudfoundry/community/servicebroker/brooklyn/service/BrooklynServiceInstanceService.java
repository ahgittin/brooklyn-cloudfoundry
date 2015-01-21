package org.cloudfoundry.community.servicebroker.brooklyn.service;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cloudfoundry.community.servicebroker.brooklyn.config.CatalogConfig;
import org.cloudfoundry.community.servicebroker.brooklyn.model.ApplicationSpec;
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
		instance = new ServiceInstance(serviceInstanceId, service.getId(), planId, organizationGuid, spaceGuid, null);
		repository.put(serviceInstanceId, instance);

		ApplicationSpec applicationSpec = new ApplicationSpec();
		applicationSpec.setLocation("localhost");
		applicationSpec.setServices(Arrays.asList(service.getId()));
		
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        restTemplate.postForObject(CatalogConfig.BROOKLYN_BASE_URL + "/v1/applications", applicationSpec, String.class);
        
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
		ServiceInstance instance = getServiceInstance(instanceId);
		return instance;
	}

	@Override
	public ServiceInstance deleteServiceInstance(String id, String serviceId,
			String planId) throws ServiceBrokerException {
		ServiceInstance instance = getServiceInstance(id);
		if (instance != null) {
			System.out.println("Deleting service");
			repository.remove(id);
			// TODO find out the brooklyn identifiers to put into the following POST request.
			restTemplate.postForObject(CatalogConfig.BROOKLYN_BASE_URL + "/v1/applications/{application}/entities/{entity}/effectors/{effector}", 
					null, String.class);
		}
		return instance;
	}

}
