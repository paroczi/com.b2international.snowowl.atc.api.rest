package com.b2international.snowowl.atc.api.rest.test;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.runners.MethodSorters;
import org.junit.FixMethodOrder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AtcConteptRestServiceTest {

	String baseURL = "http://localhost:8080/snowowl/atc/v1/MAIN/concepts/";

	@Test()
	public void test11Search() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL)
				 .basicAuth("snowowl", "snowowl")
				 .queryString("sort", "-id,parent,score")
				.asJson();
		assertThat(jsonResponse.getStatus(), equalTo(200));
	}
	
	@Test()
	public void test12SearchInvalidFilter() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL)
				 .basicAuth("snowowl", "snowowl")
				 .queryString("limit","aaa")
				.asJson();
		assertThat(jsonResponse.getStatus(), equalTo(400));
	}
	

	@Test()
	public void test21Read() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL + "{conceptId}")
				.routeParam("conceptId", "D01A")
				.queryString("expand","descendants()")
				 .basicAuth("snowowl", "snowowl")
				.asJson();
		assertThat(jsonResponse.getStatus(), equalTo(200));
	}
	
	@Test()
	public void test22ReadNotFound() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL + "{conceptId}")
				.routeParam("conceptId", "000")
				 .basicAuth("snowowl", "snowowl")
				.asJson();
		assertThat(jsonResponse.getStatus(), equalTo(404));
	}

	@Test()
	public void test31Create() throws UnirestException {
		 JsonNode newConcept = new JsonNode("{\"id\":\"D01A5\",\"description\":\"Example concept\",\"parent\":\"D01A\",\"commitComment\":\"This is a commit comment\"}");
		 
		HttpResponse<JsonNode> jsonResponse = Unirest.post(baseURL)
				  .basicAuth("snowowl", "snowowl")
	                .header("Content-Type", "application/json")
	                .body(newConcept)
	                .asJson();
		assertThat(jsonResponse.getStatus(), equalTo(201));
	}
	
	@Test()
	public void test32CreateConflict() throws UnirestException {
		 JsonNode newConcept = new JsonNode("{\"id\":\"D01A5\",\"description\":\"Example concept\",\"parent\":\"D01A\",\"commitComment\":\"This is a commit comment\"}");
		 
		HttpResponse<JsonNode> jsonResponse = Unirest.post(baseURL)
				  .basicAuth("snowowl", "snowowl")
	                .header("Content-Type", "application/json")
	                .body(newConcept)
	                .asJson();
		assertThat(jsonResponse.getStatus(), equalTo(409));
	}
	
	@Test()
	public void test41Update() throws UnirestException {
		 JsonNode updateConcept = new JsonNode("{\"description\":\"Updated concept\",\"commitComment\":\"Update description\"}");
		 
		HttpResponse<JsonNode> jsonResponse = Unirest.post(baseURL + "{conceptId}")
				  .basicAuth("snowowl", "snowowl")
				  .header("Content-Type", "application/json")
					.routeParam("conceptId", "D01A5")
	                .body(updateConcept)
	                .asJson();
		assertThat(jsonResponse.getStatus(), equalTo(204));
	}
	
	@Test()
	public void test42UpdateNotFound() throws UnirestException {
		 JsonNode updateConcept = new JsonNode("{\"description\":\"Updated concept\",\"commitComment\":\"Update description\"}");
		 
		HttpResponse<JsonNode> jsonResponse = Unirest.post(baseURL + "{conceptId}")
				  .basicAuth("snowowl", "snowowl")
				  .header("Content-Type", "application/json")
					.routeParam("conceptId", "000")
	                .body(updateConcept)
	                .asJson();
		assertThat(jsonResponse.getStatus(), equalTo(404));
	}
	
	@Test()
	public void test51Delete() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.delete(baseURL + "{conceptId}")
				.routeParam("conceptId", "D01A5")
				 .basicAuth("snowowl", "snowowl")
				.asJson();
		
		assertThat(jsonResponse.getStatus(), equalTo(204));
	}
	
	@Test()
	public void test52DeleteNotFound() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.delete(baseURL + "{conceptId}")
				.routeParam("conceptId", "D01A5")
				 .basicAuth("snowowl", "snowowl")
				.asJson();
		
		assertThat(jsonResponse.getStatus(), equalTo(404));
	}
	
}