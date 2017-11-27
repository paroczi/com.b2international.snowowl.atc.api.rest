package com.b2international.snowowl.atc.api.rest;

import org.springframework.context.annotation.Bean;

import com.google.common.base.Predicates;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spi.DocumentationType;

@EnableSwagger2
public class SwaggerConfiguration {

	 @Bean
	  public Docket swaggerSpringMvcPlugin() {
	    return new Docket(DocumentationType.SWAGGER_2)
	            .groupName("ATC")
	            .select() 
	            .paths(Predicates.alwaysTrue()) // and by paths
	            .build();
//	            .apiInfo(apiInfo())
//	            .securitySchemes(securitySchemes())
//	            .securityContext(securityContext());
	  }


}
