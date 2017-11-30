package com.b2international.snowowl.atc.api.rest.test;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class AtcConteptRestServiceTest {

	private static final String SNOWOWL_USER = "snowowl";
	private static final String baseURL = "http://localhost:8080/snowowl/atc/v1/MAIN/concepts/";

	@Before
	public void before() throws UnirestException {
		deleteContept("D01A999");
		createContept("D01A999");
	}

	@After
	public void after() throws UnirestException {
		deleteContept("D01A999");
	}

	/*
	 * Search tests
	 */

	@Test
	public void searchTest() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL).basicAuth(SNOWOWL_USER, SNOWOWL_USER)
				.queryString("sort", "-id,parent,scorek").asJson();
		assertThat(jsonResponse.getStatus(), equalTo(200));
	}

	@Test
	public void searchWithSortTest() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL).basicAuth(SNOWOWL_USER, SNOWOWL_USER)
				.queryString("sort", "-id,parent,wrong").asJson();
		assertThat(jsonResponse.getStatus(), equalTo(200));
	}

	@Test
	public void searchInvalidFilterTest() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL).basicAuth(SNOWOWL_USER, SNOWOWL_USER)
				.queryString("limit", "aaa").asJson();
		assertThat(jsonResponse.getStatus(), equalTo(400));
	}

	@Test
	public void searchwithLimitTest() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL).basicAuth(SNOWOWL_USER, SNOWOWL_USER)
				.queryString("limit", 100).asJson();
		assertThat(jsonResponse.getStatus(), equalTo(200));
	}

	@Test
	public void searchwithLimitAndOffsetTest() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL).basicAuth(SNOWOWL_USER, SNOWOWL_USER)
				.queryString("limit", 200).queryString("offset", 100).asJson();
		assertThat(jsonResponse.getStatus(), equalTo(200));
	}

	@Test
	public void searchWithDescriptionFilterTest() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL).basicAuth(SNOWOWL_USER, SNOWOWL_USER)
				.queryString("description", "Xanthines").asJson();
		assertThat(jsonResponse.getStatus(), equalTo(200));
	}

	@Test
	public void searchWithIdFiltersTest() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL).basicAuth(SNOWOWL_USER, SNOWOWL_USER)
				.queryString("parent", "L01XC,J01DB").asJson();
		assertThat(jsonResponse.getStatus(), equalTo(200));
	}

	@Test
	public void searchWithParentsFiltersTest() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL).basicAuth(SNOWOWL_USER, SNOWOWL_USER)
				.queryString("parent", "L01XC,J01DB").asJson();
		assertThat(jsonResponse.getStatus(), equalTo(200));
	}

	/*
	 * Read tests
	 */

	@Test
	public void readOKTest() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL + "{conceptId}").routeParam("conceptId", "D01A")
				.basicAuth(SNOWOWL_USER, SNOWOWL_USER).asJson();
		assertThat(jsonResponse.getStatus(), equalTo(200));
	}

	@Test
	public void readWithExpandTest() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL + "{conceptId}").routeParam("conceptId", "D01A")
				.queryString("expand", "descendants()").basicAuth(SNOWOWL_USER, SNOWOWL_USER).asJson();
		assertThat(jsonResponse.getStatus(), equalTo(200));
	}

	@Test
	public void readNotFoundTest() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(baseURL + "{conceptId}").routeParam("conceptId", "000")
				.basicAuth(SNOWOWL_USER, SNOWOWL_USER).asJson();
		assertThat(jsonResponse.getStatus(), equalTo(404));
	}

	@Test
	public void readUnauthorizedTest() throws UnirestException {
		HttpResponse<String> response = Unirest.get(baseURL + "{conceptId}").routeParam("conceptId", "D01A")
				.basicAuth("fake_user", "fake_user").asString();
		assertThat(response.getStatus(), equalTo(401));
	}

	/*
	 * Create tests
	 */

	@Test
	public void createOKTest() throws UnirestException {

		//before
		deleteContept("D01A999");
		
		HttpResponse<JsonNode> jsonResponse = createContept("D01A999");
		assertThat(jsonResponse.getStatus(), equalTo(201));
	}

	@Test
	public void createConflictTest() throws UnirestException {

		HttpResponse<JsonNode> jsonResponse = createContept("D01A999");
		assertThat(jsonResponse.getStatus(), equalTo(409));

	}

	/*
	 * Update tests
	 */

	@Test
	public void updateOKTest() throws UnirestException {

		JsonNode updateConcept = new JsonNode(
				"{\"description\":\"Updated concept\",\"commitComment\":\"Update description\"}");

		HttpResponse<JsonNode> jsonResponse = Unirest.post(baseURL + "{conceptId}")
				.basicAuth(SNOWOWL_USER, SNOWOWL_USER).header("Content-Type", "application/json")
				.routeParam("conceptId", "D01A999").body(updateConcept).asJson();
		assertThat(jsonResponse.getStatus(), equalTo(204));
	}

	@Test
	public void updateNotFoundTest() throws UnirestException {


		JsonNode updateConcept = new JsonNode(
				"{\"description\":\"Updated concept\",\"commitComment\":\"Update description\"}");

		HttpResponse<JsonNode> jsonResponse = Unirest.post(baseURL + "{conceptId}")
				.basicAuth(SNOWOWL_USER, SNOWOWL_USER).header("Content-Type", "application/json")
				.routeParam("conceptId", "000").body(updateConcept).asJson();
		assertThat(jsonResponse.getStatus(), equalTo(404));
	}

	/*
	 * Delete tests
	 */

	@Test
	public void deleteOKTest() throws UnirestException {
		
		HttpResponse<JsonNode> jsonResponse = Unirest.delete(baseURL + "{conceptId}").routeParam("conceptId", "D01A999")
				.basicAuth(SNOWOWL_USER, SNOWOWL_USER).asJson();

		assertThat(jsonResponse.getStatus(), equalTo(204));
	}

	@Test
	public void deleteNotFoundTest() throws UnirestException {
		
		//before
		deleteContept("D01A999");
		
		HttpResponse<JsonNode> jsonResponse = Unirest.delete(baseURL + "{conceptId}").routeParam("conceptId", "D01A999")
				.basicAuth(SNOWOWL_USER, SNOWOWL_USER).asJson();

		assertThat(jsonResponse.getStatus(), equalTo(404));
	}

	/*
	 * Helpers
	 */

	public HttpResponse<JsonNode> deleteContept(String id) throws UnirestException {
		return Unirest.delete(baseURL + id).basicAuth(SNOWOWL_USER, SNOWOWL_USER).asJson();
	}

	public HttpResponse<JsonNode> createContept(String id) throws UnirestException {
		JsonNode newConcept = new JsonNode("{\"id\":\"" + id
				+ "\",\"description\":\"Example concept\",\"parent\":\"D01A\",\"commitComment\":\"This is a commit comment\"}");
		return Unirest.post(baseURL).basicAuth(SNOWOWL_USER, SNOWOWL_USER).header("Content-Type", "application/json")
				.body(newConcept).asJson();

	}

}