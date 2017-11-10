package com.b2international.snowowl.atc.api.rest.domain;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import com.b2international.snowowl.core.exceptions.BadRequestException;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AtcBadRequestException extends BadRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AtcBadRequestException(String message) {
		super(message);
	}

}
