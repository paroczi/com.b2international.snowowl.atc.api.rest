/*
 * Copyright 2011-2017 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.b2international.snowowl.atc.api.rest.domain;

import com.b2international.snowowl.atc.core.request.AtcConceptCreateRequestBuilder;
import com.b2international.snowowl.atc.core.request.AtcRequests;

/**
 * @since 1.0
 */
public class AtcConceptRestInput{

	private String id;
	private String description;
	private String parent;
	
	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	protected AtcConceptCreateRequestBuilder createRequestBuilder() {
		return AtcRequests.prepareNewConcept();
	}
	
	public AtcConceptCreateRequestBuilder toRequestBuilder() {
		
		final AtcConceptCreateRequestBuilder req = createRequestBuilder();
		req.setId(getId());  //todo: handle idnot found excaption
		req.setDescription(getDescription());
		req.setParent(getParent());
		return req;
	}	
	
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("AtcConceptRestInput [getId()=");
		builder.append(getId());
		builder.append(", getDescription()=");
		builder.append(getDescription());
		builder.append(", getParent()=");
		builder.append(getParent());
		builder.append("]");
		return builder.toString();
	}
}
