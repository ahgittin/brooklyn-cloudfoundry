package org.cloudfoundry.community.servicebroker.brooklyn.model;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonProperty;

import brooklyn.rest.domain.LinkWithMetadata;
import brooklyn.rest.domain.TaskSummary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Entity extends TaskSummary{
	
	public Entity(@JsonProperty("id") String id, 
			@JsonProperty("message") String message, 
			@JsonProperty("details") String details,
            @JsonProperty("displayName") String displayName, 
            @JsonProperty("description") String description, 
            @JsonProperty("entityId") String entityId, 
            @JsonProperty("entityDisplayName") String entityDisplayName, 
            @JsonProperty("tags") Set<Object> tags,
            @JsonProperty("submitTimeUtc") Long submitTimeUtc, 
            @JsonProperty("startTimeUtc") Long startTimeUtc, 
            @JsonProperty("endTimeUtc") Long endTimeUtc, 
            @JsonProperty("currentStatus") String currentStatus, 
            @JsonProperty("result") Object result, 
            @JsonProperty("isError") boolean isError, 
            @JsonProperty("isCancelled") boolean isCancelled, 
            @JsonProperty("children") List<LinkWithMetadata> children,
            @JsonProperty("submittedByTask") LinkWithMetadata submittedByTask,
            @JsonProperty("blockingTask") LinkWithMetadata blockingTask,
            @JsonProperty("blockingDetails") String blockingDetails,
            @JsonProperty("detailedStatus") String detailedStatus,
            @JsonProperty("streams") Map<String, LinkWithMetadata> streams,
            @JsonProperty("links") Map<String, URI> links) {
		super(id, displayName, description, entityId, entityDisplayName, tags,
				submitTimeUtc, startTimeUtc, endTimeUtc, currentStatus, result,
				isError, isCancelled, children, submittedByTask, blockingTask,
				blockingDetails, detailedStatus, streams, links);
		this.message = message;
	}

	private String message;

}
