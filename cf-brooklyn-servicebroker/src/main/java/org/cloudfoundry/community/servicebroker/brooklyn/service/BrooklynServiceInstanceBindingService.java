package org.cloudfoundry.community.servicebroker.brooklyn.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BrooklynServiceInstanceBindingService implements
		ServiceInstanceBindingService {

	private Map<String, ServiceInstanceBinding> repository = new ConcurrentHashMap<String, ServiceInstanceBinding>();

	private BrooklynRestAdmin admin;

	@Autowired
	public BrooklynServiceInstanceBindingService(BrooklynRestAdmin admin) {
		this.admin = admin;

	}

	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
			String serviceId, String planId, String appGuid) throws ServiceInstanceBindingExistsException,
			ServiceBrokerException {

		ServiceInstanceBinding serviceInstanceBinding = getServiceInstanceBinding(bindingId);
		if (serviceInstanceBinding != null) {
			throw new ServiceInstanceBindingExistsException(
					serviceInstanceBinding);
		}
		Map<String, Object> credentials = admin.getApplicationSensors(serviceInstance.getServiceDefinitionId());
		serviceInstanceBinding = new ServiceInstanceBinding(bindingId, serviceInstance.getId(), credentials, null, appGuid);
		repository.put(bindingId, serviceInstanceBinding);
		return serviceInstanceBinding;
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(
			String bindingId, ServiceInstance instance, String serviceId,
			String planId) throws ServiceBrokerException {
		
		ServiceInstanceBinding serviceInstanceBinding = getServiceInstanceBinding(bindingId);
		if (serviceInstanceBinding != null) {
			// do delete stuff
			repository.remove(bindingId);
		}
		return serviceInstanceBinding;
	}

	protected ServiceInstanceBinding getServiceInstanceBinding(String id) {
		return repository.get(id);
	}

}
