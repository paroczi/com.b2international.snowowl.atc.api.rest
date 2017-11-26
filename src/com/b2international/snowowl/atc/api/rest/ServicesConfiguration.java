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
package com.b2international.snowowl.atc.api.rest;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import com.b2international.commons.platform.PlatformUtil;
import com.b2international.snowowl.atc.api.rest.domain.BranchMixin;
import com.b2international.snowowl.core.branch.Branch;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.alternates.AlternateTypeRule;
import com.mangofactory.swagger.paths.RelativeSwaggerPathProvider;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import com.mangofactory.swagger.models.dto.ApiInfo;


/**
 * The Spring configuration class for Snow Owl's internal REST services module.
 *
 * @since 1.0
 */
@Configuration
@EnableSwagger
@EnableWebMvc
public class ServicesConfiguration extends WebMvcConfigurerAdapter {

	private SpringSwaggerConfig springSwaggerConfig;
	private ServletContext servletContext;

	private String apiVersion;

	private String apiTitle;
	private String apiTermsOfServiceUrl;
	private String apiContact;
	private String apiLicense;
	private String apiLicenseUrl;
	
	@Autowired
	public void setServletContext(final ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Autowired
	public void setSpringSwaggerConfig(final SpringSwaggerConfig springSwaggerConfig) {
		this.springSwaggerConfig = springSwaggerConfig;
	}

	@Autowired
	@Value("${api.version}")
	public void setApiVersion(final String apiVersion) {
		this.apiVersion = apiVersion;
	}

	@Autowired
	@Value("${api.title}")
	public void setApiTitle(final String apiTitle) {
		this.apiTitle = apiTitle;
	}

	@Autowired
	@Value("${api.termsOfServiceUrl}")
	public void setApiTermsOfServiceUrl(final String apiTermsOfServiceUrl) {
		this.apiTermsOfServiceUrl = apiTermsOfServiceUrl;
	}

	@Autowired
	@Value("${api.contact}")
	public void setApiContact(final String apiContact) {
		this.apiContact = apiContact;
	}

	@Autowired
	@Value("${api.license}")
	public void setApiLicense(final String apiLicense) {
		this.apiLicense = apiLicense;
	}

	@Autowired
	@Value("${api.licenseUrl}")
	public void setApiLicenseUrl(final String apiLicenseUrl) {
		this.apiLicenseUrl = apiLicenseUrl;
	}

	@Bean
	public SwaggerSpringMvcPlugin swaggerSpringMvcPlugin() {
		final SwaggerSpringMvcPlugin swaggerSpringMvcPlugin = new SwaggerSpringMvcPlugin(springSwaggerConfig);
		swaggerSpringMvcPlugin.apiInfo(new ApiInfo(apiTitle, readApiDescription(), apiTermsOfServiceUrl, apiContact, apiLicense, apiLicenseUrl));
		swaggerSpringMvcPlugin.apiVersion(apiVersion);
		swaggerSpringMvcPlugin.pathProvider(new RelativeSwaggerPathProvider(servletContext));
		swaggerSpringMvcPlugin.useDefaultResponseMessages(false);
		swaggerSpringMvcPlugin.ignoredParameterTypes(Principal.class, Void.class);
		final TypeResolver resolver = new TypeResolver();
		swaggerSpringMvcPlugin.genericModelSubstitutes(ResponseEntity.class);
		swaggerSpringMvcPlugin.genericModelSubstitutes(DeferredResult.class);
		swaggerSpringMvcPlugin.alternateTypeRules(new AlternateTypeRule(resolver.resolve(UUID.class), resolver.resolve(String.class)));
		swaggerSpringMvcPlugin.directModelSubstitute(Branch.class, BranchMixin.class);

		return swaggerSpringMvcPlugin;
	}

	private String readApiDescription() {
		try {
			final File apiDesc = new File(PlatformUtil.toAbsolutePath(ServicesConfiguration.class, "api-description.html"));
			return Joiner.on("\n").join(Files.readLines(apiDesc, Charsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException("Failed to read api-description.html file", e);
		}
	}

	
}
