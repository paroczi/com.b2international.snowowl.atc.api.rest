package com.b2international.snowowl.atc.api.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b2international.snowowl.atc.core.AtcCoreActivator;
import com.b2international.snowowl.atc.core.domain.AtcConcepts;
import com.b2international.snowowl.atc.core.request.AtcRequests;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.eventbus.IEventBus;

@RestController
public class IndexController {
	
	private static final String REPOSITORY_ID = AtcCoreActivator.REPOSITORY_UUID;
	private IEventBus bus;
	
	
	
	
	public IndexController() {
		bus = ApplicationContext.getInstance().getService(IEventBus.class);

	}


	@GetMapping("/")
	public AtcConcepts index() {
		try {
			final AtcConcepts concepts = AtcRequests
					.prepareSearchConcept()
					.all()
					.build(REPOSITORY_ID, IBranchPath.MAIN_BRANCH)
					.execute(bus)
					.getSync();
			
			return concepts;
			
		} catch(Exception e) {
			throw new BadRequestException(e.getMessage());
		}
		
		
	
	}
}
