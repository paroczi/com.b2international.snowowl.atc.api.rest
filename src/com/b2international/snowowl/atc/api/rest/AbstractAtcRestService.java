package com.b2international.snowowl.atc.api.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.b2international.snowowl.eventbus.IEventBus;

public abstract class AbstractAtcRestService  {
	/**
	 * Two minutes timeout value for commit requests in milliseconds.
	 */
	protected static final long COMMIT_TIMEOUT = 120L * 1000L;

	/**
	 * The currently supported versioned media type of the snowowl RESTful API.
	 */
	public static final String SO_MEDIA_TYPE = "application/vnd.com.b2international.snowowl+json";

	@Autowired
	@Value("${repositoryId}")
	protected String repositoryId;
	
	@Autowired
	protected IEventBus bus;

}
