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
	public ServiceInstanceBinding createServiceInstanceBinding(
			String bindingId, ServiceInstance serviceInstance,
			String serviceId, String planId, String appGuid)
			throws ServiceInstanceBindingExistsException,
			ServiceBrokerException {

		ServiceInstanceBinding serviceInstanceBinding = getServiceInstanceBinding(bindingId);
		if (serviceInstanceBinding != null) {
			throw new ServiceInstanceBindingExistsException(
					serviceInstanceBinding);
		}
		// do create stuff
		Map<String, Object> credentials = new HashMap<String, Object>();
//		credentials.put("uri", "mysql://mysqluser:pass@mysqlhost:3306/dbname");
//		credentials.put("username", "mysqluser");
//		credentials.put("password", "pass");
//		credentials.put("host", "mysqlhost");
//		credentials.put("port", 3306);
//		credentials.put("database", "dbname");
		admin.getEntitySensors(serviceId);
		serviceInstanceBinding = new ServiceInstanceBinding(bindingId,
				serviceInstance.getId(), credentials, null, appGuid);
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

	private ServiceInstanceBinding getServiceInstanceBinding(String id) {
		return repository.get(id);
	}

}
