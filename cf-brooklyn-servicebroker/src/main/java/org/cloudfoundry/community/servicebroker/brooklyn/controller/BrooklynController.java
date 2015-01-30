package org.cloudfoundry.community.servicebroker.brooklyn.controller;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.cloudfoundry.community.servicebroker.brooklyn.service.BrooklynRestAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import brooklyn.util.stream.Streams;

@RestController
public class BrooklynController {
	
	private BrooklynRestAdmin admin;

	@Autowired
	public BrooklynController(BrooklynRestAdmin admin) {
		this.admin = admin;
	}
	
	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED)
	public void create(InputStream uploadedInputStream){
		admin.postBlueprint(Streams.readFullyString(uploadedInputStream));
	}

}
