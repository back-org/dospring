package com.java.dospring.model;

import lombok.Value;

@Value
public class ApiResponse {
	private Boolean success;
	private String message;
}
