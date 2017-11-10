package com.b2international.snowowl.atc.api.rest;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.b2international.commons.http.AcceptHeader;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.atc.core.domain.AtcConcept;
import com.b2international.snowowl.atc.core.domain.AtcConcepts;
import com.b2international.snowowl.atc.core.request.AtcRequests;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.atc.api.rest.domain.ChangeRequest;
import com.b2international.snowowl.atc.api.rest.domain.AtcBadRequestException;
import com.b2international.snowowl.atc.api.rest.domain.AtcConceptRestInput;
import com.b2international.snowowl.atc.api.rest.domain.AtcConceptRestUpdate;


@RestController
public class AtcConceptRestService extends AbstractAtcRestService{

	//todo: get user_id from principal.getName()
	
	private static final String USER_ID = "system";

	@GetMapping("/{path:**}/concepts")
	public ResponseEntity<AtcConcepts> search(
			
			@PathVariable(value="path")
			final String branch,
			
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

			//todo: sortingField
			return ResponseEntity.ok(
					AtcRequests.prepareSearchConcept()
					.setLimit(limit)
					.setOffset(offset)
//					.filterByIds(ids)  //throwing error, if not there, and not working too
					.filterByDescription(descriptionFilter)
					.filterByParents(parents)
					.setExpand(expand)
					.setLocales(extendedLocales)
					.build(repositoryId, branch)
					.execute(bus)
					.getSync()
					);		
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@GetMapping("/{path:**}/concepts/{conceptId}")
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
			return ResponseEntity.ok(
					AtcRequests.prepareGetConcept(conceptId)
					.setExpand(expand)
					.setLocales(extendedLocales)
					.build(repositoryId, branch)
					.execute(bus)
					.getSync());
									
		} catch (Exception e) {
			throw new AtcBadRequestException(e.getMessage());
		}
	}
	
	@PostMapping("/{path:**}/concepts")
	public ResponseEntity<Void> create(
			
			@PathVariable(value="path")
			final String branch,
			
			@RequestBody 
			final ChangeRequest<AtcConceptRestInput> body) {
		
		final AtcConceptRestInput change = body.getChange();
		final String commitComment = body.getCommitComment();
		
		
		try {
			
		final String createdConceptId = change
				.toRequestBuilder()
			.build(repositoryId, branch, USER_ID, commitComment)
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS)
			.getResultAs(String.class);
			
		return ResponseEntity
				.created(linkTo(AtcConceptRestService.class).slash("concepts").slash(createdConceptId).toUri())
				.build();	
		
		} catch (Exception e) {
			throw new AtcBadRequestException(e.getMessage());
		}
	}

	@PostMapping("/{path:**}/concepts/{conceptId}")
	public  ResponseEntity<Void> update(
			
			@PathVariable(value="path")
			final String branch,
			
			@PathVariable(value="conceptId")
			final String conceptId,
			@RequestBody 
			final ChangeRequest<AtcConceptRestUpdate> body) {
			
		try {
			
	   body.getChange().toRequestBuilder(conceptId)
			.build(repositoryId, branch, USER_ID, body.getCommitComment())
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS);
			
	   return ResponseEntity.status(204).build();
				
		} catch (Exception e) {
			throw new AtcBadRequestException(e.getMessage());
		}
	}
	
	@DeleteMapping("/{path:**}/concepts/{conceptId}")
	public ResponseEntity<Void> delete(
			
			@PathVariable(value="path")
			final String branch,
			
			@PathVariable(value="conceptId")
			final String conceptId,
			@RequestParam(defaultValue="false", required=false)
			final Boolean force) {
			
		try {
			
			AtcRequests.prepareDeleteConcept(conceptId)
			.force(force)
			.build(repositoryId, branch, USER_ID,String.format("Deleted Concept '%s' from store.", conceptId))
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS);
			
			 return ResponseEntity.status(204).build();
				
		} catch (Exception e) {
			throw new AtcBadRequestException(e.getMessage());
		}
	}
	
}
