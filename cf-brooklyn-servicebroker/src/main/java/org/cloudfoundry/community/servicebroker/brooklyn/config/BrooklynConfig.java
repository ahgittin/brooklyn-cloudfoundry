package org.cloudfoundry.community.servicebroker.brooklyn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix="brooklyn")
public class BrooklynConfig {

	private String uri;
	private String port;
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getPort() {
		return port;
	}
	
	public void setPort(String port) {
		this.port = port;
	}
	
	public String toFullUrl(String... resource){
		StringBuilder sb = new StringBuilder();
		sb.append(getUri()).append(":").append(getPort());
		for(String s : resource){
			sb.append("/");
			sb.append(s);
		}
		return sb.toString();
	}
}
