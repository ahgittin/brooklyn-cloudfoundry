package org.cloudfoundry.community.servicebroker.brooklyn.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cloudfoundry.community.servicebroker.model.ServiceInstance;

public enum BrooklynServiceInstanceRepository {

	INSTANCE;

	private Map<String, ServiceInstance> repository = new ConcurrentHashMap<String, ServiceInstance>();

	public void put(String serviceInstanceId, ServiceInstance instance) {
		repository.put(serviceInstanceId, instance);
	}

	public ServiceInstance get(String id) {
		return repository.get(id);
	}

	public void remove(String id) {
		repository.remove(id);
	}
	
	@Override
	public String toString() {
		return repository.toString();
	}
}
