package org.cloudfoundry.community.servicebroker.brooklyn.model;

public class EntitySensor {

	private String name;
	private SensorSummary[] sensors;

	public EntitySensor(String name, SensorSummary[] sensors) {
		this.name = name;
		this.sensors = sensors;
	}
	
	public String getName() {
		return name;
	}
	
	public SensorSummary[] getSensors() {
		return sensors;
	}

}
