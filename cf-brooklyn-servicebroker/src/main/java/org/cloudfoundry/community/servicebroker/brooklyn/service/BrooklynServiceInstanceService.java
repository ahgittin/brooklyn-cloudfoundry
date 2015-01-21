package org.cloudfoundry.community.servicebroker.brooklyn.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cloudfoundry.community.servicebroker.brooklyn.config.CatalogConfig;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
		// TODO init service
		
		String json = restTemplate.getForObject(CatalogConfig.BROOKLYN_BASE_URL + "/v1/catalog/applications/" + service.getId(), String.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request= new HttpEntity<String>(json, headers);
        restTemplate.postForObject(CatalogConfig.BROOKLYN_BASE_URL + "/v1/applications", request, String.class);
        
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
		}
		return instance;
	}

}
