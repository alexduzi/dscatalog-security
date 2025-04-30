package com.devsuperior.dscatalog.services.exceptions;

import java.io.Serial;

public class EmailException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;

	public EmailException(String msg) {
		super(msg);
	}
}
