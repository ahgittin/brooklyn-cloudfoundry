package org.cloudfoundry.community.servicebroker.brooklyn.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;

public enum BrooklynServiceInstanceBindingRepository {
	
	INSTANCE;

	private Map<String, ServiceInstanceBinding> repository = new ConcurrentHashMap<String, ServiceInstanceBinding>();

	public void put(String bindingId,
			ServiceInstanceBinding serviceInstanceBinding) {
		repository.put(bindingId, serviceInstanceBinding);
	}

	public void remove(String bindingId) {
		repository.remove(bindingId);
	}

	public ServiceInstanceBinding get(String id) {
		return repository.get(id);
	}
}
