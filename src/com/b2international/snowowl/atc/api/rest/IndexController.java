package com.b2international.snowowl.atc.api.rest;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.b2international.commons.http.AcceptHeader;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.atc.core.AtcCoreActivator;
import com.b2international.snowowl.atc.core.domain.AtcConcept;
import com.b2international.snowowl.atc.core.domain.AtcConcepts;
import com.b2international.snowowl.atc.core.request.AtcRequests;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.atc.api.rest.domain.ChangeRequest;
import com.b2international.snowowl.atc.api.rest.domain.AtcConceptRestInput;
import com.b2international.snowowl.atc.api.rest.domain.AtcConceptRestUpdate;


@RestController
public class IndexController {

	//todo: get user_id from principal.getName()
	//todo: get branchPath from path
	
	private static final String USER_ID = "system";
	private static final String REPOSITORY_ID = AtcCoreActivator.REPOSITORY_UUID;
	private static final long COMMIT_TIMEOUT = 120L * 1000L;
	private IEventBus bus;

	public IndexController() {
		bus = ApplicationContext.getInstance().getService(IEventBus.class);
	}

	@GetMapping("/concepts")
	public AtcConcepts search(
			
			@RequestParam(value="id", defaultValue="",required=false)
			final String idFilter,
			
			@RequestParam(value="description",required=false)
			final String descriptionFilter,
			
			@RequestParam(value="parent", defaultValue="",required=false)
			final String parentFilter,
			
			@RequestParam(value="offset", defaultValue="0", required=false)
			final int offset,
			
			@RequestParam(value="limit", defaultValue="50", required=false)
			final int limit,
			
			@RequestParam(value="expand", required=false)
			final String expand,
			
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage) {
		
		final List<ExtendedLocale> extendedLocales;
		 List<String> ids;
		 List<String> parents;
		
		
		try {
			ids=  Arrays.asList(idFilter.split(","));
			parents=  Arrays.asList(parentFilter.split(","));
			extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(acceptLanguage));
		} catch (IOException e) {
			throw new BadRequestException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}
			
			//todo: sortingField
			return AtcRequests.prepareSearchConcept()
					.setLimit(limit)
					.setOffset(offset)
//					.filterByIds(ids)  //throwing error, if not there, and not working too
					.filterByDescription(descriptionFilter)
					.filterByParents(parents)
					.setExpand(expand)
					.setLocales(extendedLocales)
					.build(REPOSITORY_ID, IBranchPath.MAIN_BRANCH)
					.execute(bus)
					.getSync();
		
	
	}
	
	@GetMapping("/concepts/{conceptId}")
	public AtcConcept read(
			@PathVariable(value="conceptId")
			final String conceptId,
			
			@RequestParam(value="expand", required=false)
			final String expand,
			
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage
	) {

		final List<ExtendedLocale> extendedLocales;
		
		try {
			extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(acceptLanguage));
		} catch (IOException e) {
			throw new BadRequestException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}
		
			return AtcRequests.prepareGetConcept(conceptId)
					.setExpand(expand)
					.setLocales(extendedLocales)
					.build(REPOSITORY_ID, IBranchPath.MAIN_BRANCH)
					.execute(bus)
					.getSync();
	
	}
	
	@PostMapping("/concepts")
	public HttpStatus create(
			
			@RequestBody 
			final ChangeRequest<AtcConceptRestInput> body) {
		
		final AtcConceptRestInput change = body.getChange();
		final String commitComment = body.getCommitComment();
		
		
		try {
			
		final String createdConceptId = change
				.toRequestBuilder()
			.build(REPOSITORY_ID, IBranchPath.MAIN_BRANCH, USER_ID, commitComment)
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS)
			.getResultAs(String.class);
			
		return HttpStatus.CREATED;
				
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@PostMapping("/concepts/{conceptId}")
	public HttpStatus update(
			@PathVariable(value="conceptId")
			final String conceptId,
			@RequestBody 
			final ChangeRequest<AtcConceptRestUpdate> body) {
			
		try {
			
	   body.getChange().toRequestBuilder(conceptId)
			.build(REPOSITORY_ID, IBranchPath.MAIN_BRANCH, USER_ID, body.getCommitComment())
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS);
			
		return HttpStatus.OK;
				
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@DeleteMapping("/concepts/{conceptId}")
	public HttpStatus delete(
			@PathVariable(value="conceptId")
			final String conceptId,
			@RequestParam(defaultValue="false", required=false)
			final Boolean force) {
			
		try {
			
			AtcRequests.prepareDeleteConcept(conceptId)
			.force(force)
			.build(REPOSITORY_ID, IBranchPath.MAIN_BRANCH, USER_ID,String.format("Deleted Concept '%s' from store.", conceptId))
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS);
			
		return HttpStatus.OK;
				
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
}
