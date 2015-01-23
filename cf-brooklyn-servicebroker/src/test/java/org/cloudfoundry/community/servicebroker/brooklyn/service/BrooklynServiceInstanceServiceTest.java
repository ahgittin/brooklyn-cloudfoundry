package org.cloudfoundry.community.servicebroker.brooklyn.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.cloudfoundry.community.servicebroker.brooklyn.BrooklynConfiguration;
import org.cloudfoundry.community.servicebroker.brooklyn.config.BrooklynConfig;
import org.cloudfoundry.community.servicebroker.brooklyn.model.ApplicationSpec;
import org.cloudfoundry.community.servicebroker.brooklyn.model.Entity;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BrooklynConfiguration.class})
public class BrooklynServiceInstanceServiceTest {
	
	private final static String SVC_INST_ID = "serviceInstanceId";
	
	@Mock
	private BrooklynRestAdmin admin;
	@Mock
	private ServiceDefinition serviceDefinition;
	@Mock 
	private Entity entity;
	
	private BrooklynServiceInstanceService service;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		service = new BrooklynServiceInstanceService(admin);
	}
	
	@Test
	public void newServiceInstanceCreatedSuccessfully() 
			throws ServiceInstanceExistsException, ServiceBrokerException {

		when(admin.createApplication(any(ApplicationSpec.class))).thenReturn(entity);
		
		ServiceInstance instance = service.createServiceInstance(serviceDefinition, SVC_INST_ID, "planId", "organizationGuid", "spaceGuid");
		
		assertNotNull(instance);
		assertEquals(SVC_INST_ID, instance.getId());
	}
	
	@Test(expected=ServiceInstanceExistsException.class)
	public void serviceInstanceCreationFailsWithExistingInstance()  
			throws ServiceInstanceExistsException, ServiceBrokerException {
		when(admin.createApplication(any(ApplicationSpec.class))).thenReturn(entity);		
		service.createServiceInstance(serviceDefinition, SVC_INST_ID, "planId", "organizationGuid", "spaceGuid");
		service.createServiceInstance(serviceDefinition, SVC_INST_ID, "planId", "organizationGuid", "spaceGuid");
	}
	
	@Test
	public void serviceInstanceRetrievedSuccessfully() 
			throws ServiceInstanceExistsException, ServiceBrokerException{
		
		when(admin.createApplication(any(ApplicationSpec.class))).thenReturn(entity);
		
		assertNull(service.getServiceInstance(SVC_INST_ID));
		service.createServiceInstance(serviceDefinition, SVC_INST_ID, "planId", "organizationGuid", "spaceGuid");
		assertNotNull(service.getServiceInstance(SVC_INST_ID));
	}
	
	@Test
	public void serviceInstanceDeletedSuccessfully() 
			throws ServiceInstanceExistsException, ServiceBrokerException {

		when(admin.createApplication(any(ApplicationSpec.class))).thenReturn(entity);
		
		ServiceInstance instance = service.createServiceInstance(serviceDefinition, SVC_INST_ID, "planId", "organizationGuid", "spaceGuid");
		assertNotNull(service.getServiceInstance(SVC_INST_ID));
		
		service.deleteServiceInstance(SVC_INST_ID, instance.getId(), "planId");
		assertNull(service.getServiceInstance(SVC_INST_ID));
		
	}
}
