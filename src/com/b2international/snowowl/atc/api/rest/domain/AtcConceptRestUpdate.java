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

import com.b2international.snowowl.atc.core.request.AtcConceptUpdateRequestBuilder;
import com.b2international.snowowl.atc.core.request.AtcRequests;

/**
 * @since 1.0
 */
public class AtcConceptRestUpdate{

	private String description;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	public AtcConceptUpdateRequestBuilder toRequestBuilder(String id) {
		
		return AtcRequests.prepareUpdateConcept()
		.setId(id)
		.setDescription(getDescription());
		
	}	
	
}
