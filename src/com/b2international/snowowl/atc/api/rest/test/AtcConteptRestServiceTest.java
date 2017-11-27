package com.b2international.snowowl.atc.api.rest.test;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class AtcConteptRestServiceTest {

	String baseURL = "http://localhost:8080/snowowl/atc/v1/MAIN/concepts/";

	@Test()

	public void testSearch() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL)
				 .basicAuth("snowowl", "snowowl")
				 .queryString("sort", "?sort=-id,parent,score")
				.asJson();
		assertThat(jsonResponse.getStatus(), equalTo(200));
	}
	
	public void testSearchInvalidFilter() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL)
				 .basicAuth("snowowl", "snowowl")
				 .queryString("apple", "true")
				.asJson();
		assertThat(jsonResponse.getStatus(), equalTo(400));
	}
	

	@Test()
	public void testRead() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL + "{conceptId}")
				.routeParam("conceptId", "D01A")
				.queryString("expand","descendants()")
				 .basicAuth("snowowl", "snowowl")
				.asJson();
		assertThat(jsonResponse.getStatus(), equalTo(200));
	}
	
	@Test()
	public void testReadNotFound() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL + "{conceptId}")
				.routeParam("conceptId", "000")
				 .basicAuth("snowowl", "snowowl")
				.asJson();
		assertThat(jsonResponse.getStatus(), equalTo(404));
	}

	@Test()
	public void testCreate() throws UnirestException {
		 JsonNode newConcept = new JsonNode("{\"id\":\"D01A5\",\"description\":\"Example concept\",\"parent\":\"D01A\",\"commitComment\":\"This is a commit comment\"}");
		 
		HttpResponse<JsonNode> jsonResponse = Unirest.post(baseURL)
				  .basicAuth("snowowl", "snowowl")
	                .header("Content-Type", "application/json")
	                .body(newConcept)
	                .asJson();
		assertThat(jsonResponse.getStatus(), equalTo(201));
	}
	
	@Test()
	public void testCreateConflict() throws UnirestException {
		 JsonNode newConcept = new JsonNode("{\"id\":\"D01A5\",\"description\":\"Example concept\",\"parent\":\"D01A\",\"commitComment\":\"This is a commit comment\"}");
		 
		HttpResponse<JsonNode> jsonResponse = Unirest.post(baseURL)
				  .basicAuth("snowowl", "snowowl")
	                .header("Content-Type", "application/json")
	                .body(newConcept)
	                .asJson();
		assertThat(jsonResponse.getStatus(), equalTo(409));
	}
	
	@Test()
	public void testUpdate() throws UnirestException {
		 JsonNode updateConcept = new JsonNode("{\"description\":\"Updated concept\",\"commitComment\":\"Update description\"}");
		 
		HttpResponse<JsonNode> jsonResponse = Unirest.post(baseURL + "{conceptId}")
				  .basicAuth("snowowl", "snowowl")
					.routeParam("conceptId", "D01A5")
	                .body(updateConcept)
	                .asJson();
		assertThat(jsonResponse.getStatus(), equalTo(204));
	}
	
	@Test()
	public void testUpdateNotFound() throws UnirestException {
		 JsonNode updateConcept = new JsonNode("{\"description\":\"Updated concept\",\"commitComment\":\"Update description\"}");
		 
		HttpResponse<JsonNode> jsonResponse = Unirest.post(baseURL + "{conceptId}")
				  .basicAuth("snowowl", "snowowl")
					.routeParam("conceptId", "000")
	                .body(updateConcept)
	                .asJson();
		assertThat(jsonResponse.getStatus(), equalTo(404));
	}
	
	@Test()
	public void testDelete() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.delete(baseURL + "{conceptId}")
				.routeParam("conceptId", "D01A5")
				 .basicAuth("snowowl", "snowowl")
				.asJson();
		
		assertThat(jsonResponse.getStatus(), equalTo(204));
	}
	
	@Test()
	public void testDeleteNotFound() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.delete(baseURL + "{conceptId}")
				.routeParam("conceptId", "D01A5")
				 .basicAuth("snowowl", "snowowl")
				.asJson();
		
		assertThat(jsonResponse.getStatus(), equalTo(404));
	}
	
}