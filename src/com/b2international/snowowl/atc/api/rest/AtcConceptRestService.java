package com.b2international.snowowl.atc.api.rest;

import java.io.IOException;
import java.io.StringReader;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import com.b2international.snowowl.core.domain.PageableCollectionResource;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.core.request.SearchResourceRequest;
import com.b2international.snowowl.eventbus.IEventBus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.b2international.snowowl.atc.api.rest.domain.ChangeRequest;
import com.b2international.snowowl.atc.api.rest.domain.RestApiError;
import com.b2international.snowowl.atc.api.rest.domain.AtcConceptRestInput;
import com.b2international.snowowl.atc.api.rest.domain.AtcConceptRestUpdate;

@Api("ATC Concepts")
@RestController
public class AtcConceptRestService{

	
	private static final String REPOSITORY_ID = AtcCoreActivator.REPOSITORY_UUID;
	private static final long COMMIT_TIMEOUT = 120L * 1000L;
	private IEventBus bus = ApplicationContext.getInstance().getService(IEventBus.class);

	@ApiOperation(
			value="Retrieve Concepts from a branch", 
			notes="Returns a list with all/filtered Concepts from a branch."
					+ "<p>The following properties can be expanded:"
					+ "<p>"
					+ "&bull; descendants() &ndash; the list of descendants of the concept<br>")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
		@ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
		@ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
	})
	@GetMapping("/{path}/concepts")
	public ResponseEntity<AtcConcepts> search(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branch,
			
			@ApiParam(value="The list of identifiers to match")
			@RequestParam(value="id",required=false)
			final String idFilter,
			
			@ApiParam(value="The description to match")
			@RequestParam(value="description",required=false)
			final String descriptionFilter,
			
			@ApiParam(value="The list of parent identifiers to match")
			@RequestParam(value="parent",required=false)
			final String parentFilter,
			
			@ApiParam(value="The starting offset in the list")
			@RequestParam(value="offset", defaultValue="0", required=false)
			final int offset,
			
			@ApiParam(value="The maximum number of items to return")
			@RequestParam(value="limit", defaultValue="50", required=false)
			final int limit,
			
			@ApiParam(value="The list of sorting fields in order and start with (+/-) as orientation")
			@RequestParam(value="sort", required=false)
			final String sortFilter,
			
			@ApiParam(value="Expansion parameters")
			@RequestParam(value="expand", required=false)
			final String expand,
			
			@ApiParam(value="Accepted language tags, in order of preference")
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
			 request = request.filterByParents(parents);
		 }
		 
		 if(!StringUtils.isEmpty(sortFilter)) {
			 
			  String[] sortStringArray = sortFilter.split(",");
		       Map<String, Boolean> sortMap = new TreeMap<>();

		        for (String item : sortStringArray) {
		            if (item.startsWith("-"))
		                sortMap.put(item.substring(1), false);
		            else if (item.startsWith("+"))
		                sortMap.put(item.substring(1), true);
		            else
		                sortMap.put(item, true);
		        }
		        
		        List<SearchResourceRequest.SortField> sortList = new ArrayList<>();
		        
		        sortMap.forEach((key, desc) -> {
		        	sortList.add( new SearchResourceRequest.SortField(key,desc));
		        });
		        	
		       
			 request = request.sortBy(sortList);
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
				.build(REPOSITORY_ID, branch)
				.execute(bus)
				.getSync());		
		
	}
	
	@ApiOperation(
			value="Retrieve Concept properties",
			notes="Returns all properties of the specified Concept, including a summary of inactivation indicator and association members."
					+ "<p>The following properties can be expanded:"
					+ "<p>"
					+ "&bull; descendants() &ndash; the list of descendants of the concept<br>")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK", response = Void.class),
		@ApiResponse(code = 404, message = "Branch or Concept not found", response = RestApiError.class)
	})
	@GetMapping("/{path}/concepts/{conceptId}")
	public ResponseEntity<AtcConcept> read(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branch,
			
			@ApiParam(value="The Concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,
			
			@ApiParam(value="Expansion parameters")
			@RequestParam(value="expand", required=false)
			final String expand,
			
			@ApiParam(value="Accepted language tags, in order of preference")
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
	
	@ApiOperation(
			value="Create Concept", 
			notes="Creates a new Concept directly on a branch.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "Concept created on task"),
		@ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
	})
	@PostMapping("/{path}/concepts")
	public ResponseEntity<Void> create(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branch,
			
			@ApiParam(value="Concept parameters")
			@RequestBody 
			final ChangeRequest<AtcConceptRestInput> body,
			final Principal principal) {
		
		final String createdConceptId =  body.getChange()
				.toRequestBuilder()
			.build(REPOSITORY_ID, branch, principal.getName(), body.getCommitComment())
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS)
			.getResultAs(String.class);
			
		
		return ResponseEntity
				.created(linkTo(AtcConceptRestService.class).slash(branch).slash("concepts").slash(createdConceptId).toUri())
				.build();	
		
	}


	@ApiOperation(
			value="Update Concept",
			notes="Updates properties of the specified Concept, also managing inactivation indicator and association reference set "
					+ "membership in case of inactivation."
					+ "<p>The following properties are allowed to change:"
					+ "<p>"
					+ "&bull; definition status<br>")
	@ApiResponses({
		@ApiResponse(code = 204, message = "Update successful"),
		@ApiResponse(code = 404, message = "Branch or Concept not found", response = RestApiError.class)
	})
	@PostMapping("/{path}/concepts/{conceptId}")
	public  ResponseEntity<Void> update(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branch,
			
			@ApiParam(value="The Concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,

			@ApiParam(value="Updated Concept parameters")
			@RequestBody 
			final ChangeRequest<AtcConceptRestUpdate> body,
			final Principal principal) {
	
		
	   body.getChange().toRequestBuilder(conceptId)
			.build(REPOSITORY_ID,branch, principal.getName(), body.getCommitComment())
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS);
			
	   return ResponseEntity.status(204).build();
				
	
	}

	@ApiOperation(
			value="Delete Concept",
			notes="Permanently removes the specified unreleased Concept and related components.<p>If any participating "
					+ "component has already been released the Concept can not be removed and a <code>409</code> "
					+ "status will be returned."
					+ "<p>The force flag enables the deletion of a released Concept. "
					+ "Deleting published components is against the RF2 history policy so"
					+ " this should only be used to remove a new component from a release before the release is published.</p>")
	@ApiResponses({
		@ApiResponse(code = 204, message = "Deletion successful"),
		@ApiResponse(code = 404, message = "Branch or Concept not found", response = RestApiError.class),
		@ApiResponse(code = 409, message = "Cannot be deleted if released", response = RestApiError.class)
	})
	@DeleteMapping("/{path}/concepts/{conceptId}")
	public ResponseEntity<Void> delete(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branch,
			
			@ApiParam(value="The Concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,

			@ApiParam(value="Force deletion flag")
			@RequestParam(defaultValue="false", required=false)
			final Boolean force,
			final Principal principal) {
		
		
			AtcRequests.prepareDeleteConcept(conceptId)
			.force(force)
			.build(REPOSITORY_ID,branch,principal.getName(),String.format("Deleted Concept '%s' from store.", conceptId))
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS);
			
			 return ResponseEntity.status(204).build();
	
	}
	
}