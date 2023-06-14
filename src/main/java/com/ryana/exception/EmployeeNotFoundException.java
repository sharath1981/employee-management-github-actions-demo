package com.ryana.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmployeeNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = -3875546943966708822L;

	public EmployeeNotFoundException() {
		super("Employee is not found...");
	}
}
