package com.b2international.snowowl.atc.api.rest;

import java.io.IOException;
import java.io.StringReader;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.b2international.commons.http.AcceptHeader;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.atc.core.AtcCoreActivator;
import com.b2international.snowowl.atc.core.domain.AtcConcept;
import com.b2international.snowowl.atc.core.domain.AtcConcepts;
import com.b2international.snowowl.atc.core.request.AtcConceptSearchRequestBuilder;
import com.b2international.snowowl.atc.core.request.AtcRequests;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.atc.api.rest.domain.ChangeRequest;
import com.b2international.snowowl.atc.api.rest.domain.AtcConceptRestInput;
import com.b2international.snowowl.atc.api.rest.domain.AtcConceptRestUpdate;


@RestController
public class AtcConceptRestService{

	//todo: get user_id from principal.getName(), after set principle
	//todo: do promise()
	//todo: sortingField
	
	private static final String USER_ID = "system";
	private static final String REPOSITORY_ID = AtcCoreActivator.REPOSITORY_UUID;
	private static final long COMMIT_TIMEOUT = 120L * 1000L;
	private IEventBus bus = ApplicationContext.getInstance().getService(IEventBus.class);

	@GetMapping("/{path}/concepts")
	public ResponseEntity<AtcConcepts> search(
			
			@PathVariable(value="path")
			final String branch,
			
			@RequestParam(value="id",required=false)
			final String idFilter,
			
			@RequestParam(value="description",required=false)
			final String descriptionFilter,
			
			@RequestParam(value="parent",required=false)
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
		 
		AtcConceptSearchRequestBuilder request = AtcRequests.prepareSearchConcept();
		 
		 if(!StringUtils.isEmpty(idFilter)) {
			 List<String> ids = Arrays.asList(idFilter.split(","));
			 request = request.filterByIds(ids);
		 }
		 
		 if(!StringUtils.isEmpty(parentFilter)) {
			 List<String> parents = Arrays.asList(parentFilter.split(","));
			 request = request.filterByIds(parents);
		 }
		
		
		try {
			extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(acceptLanguage));
		} catch (IOException e) {
			throw new BadRequestException(e.getMessage(),e);
		}
		
		
		return ResponseEntity.ok(
				request
				.setLimit(limit)
				.setOffset(offset)
				.filterByDescription(descriptionFilter)
				.setExpand(expand)
				.setLocales(extendedLocales)
//				.sortBy(new SortField("id", true))
				.build(REPOSITORY_ID, branch)
				.execute(bus)
				.getSync());		
		
	}
	
	@GetMapping("/{path}/concepts/{conceptId}")
	public ResponseEntity<AtcConcept> read(
			
			@PathVariable(value="path")
			final String branch,
			
			@PathVariable(value="conceptId")
			final String conceptId,
			
			@RequestParam(value="expand", required=false)
			final String expand,
			
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage) {

		final List<ExtendedLocale> extendedLocales;
		
		
			try {
				extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(acceptLanguage));
			} catch (IOException e) {
				throw new BadRequestException(e.getMessage(),e);
			}
			
			return ResponseEntity.ok(
					AtcRequests.prepareGetConcept(conceptId)
					.setExpand(expand)
					.setLocales(extendedLocales)
					.build(REPOSITORY_ID, branch)
					.execute(bus)
					.getSync());
	}
	
	
	@PostMapping("/{path}/concepts")
	public ResponseEntity<Void> create(
			
			@PathVariable(value="path")
			final String branch,
			
			@RequestBody 
			final ChangeRequest<AtcConceptRestInput> body,
			final Principal principal) {
		
			
		final String createdConceptId =  body.getChange()
				.toRequestBuilder()
			.build(REPOSITORY_ID, branch, USER_ID, body.getCommitComment())
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS)
			.getResultAs(String.class);
			
		
		return ResponseEntity
				.created(linkTo(AtcConceptRestService.class).slash(branch).slash("concepts").slash(createdConceptId).toUri())
				.build();	
		
	}

	@PostMapping("/{path}/concepts/{conceptId}")
	public  ResponseEntity<Void> update(
			
			@PathVariable(value="path")
			final String branch,
			
			@PathVariable(value="conceptId")
			final String conceptId,
			@RequestBody 
			final ChangeRequest<AtcConceptRestUpdate> body,
			final Principal principal) {
	
		
	   body.getChange().toRequestBuilder(conceptId)
			.build(REPOSITORY_ID,branch, USER_ID, body.getCommitComment())
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS);
			
	   return ResponseEntity.status(204).build();
				
	
	}
	
	@DeleteMapping("/{path}/concepts/{conceptId}")
	public ResponseEntity<Void> delete(
			
			@PathVariable(value="path")
			final String branch,
			
			@PathVariable(value="conceptId")
			final String conceptId,
			@RequestParam(defaultValue="false", required=false)
			final Boolean force,
			final Principal principal) {
		
			AtcRequests.prepareDeleteConcept(conceptId)
			.force(force)
			.build(REPOSITORY_ID,branch, USER_ID,String.format("Deleted Concept '%s' from store.", conceptId))
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS);
			
			 return ResponseEntity.status(204).build();
	
	}
	
}