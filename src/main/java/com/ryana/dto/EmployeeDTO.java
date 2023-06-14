package com.ryana.dto;

import java.time.LocalDate;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ryana.domain.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Validated
public record EmployeeDTO(
		Long id,
		@NotBlank String name,
		@NotNull Gender gender,
		@JsonFormat(pattern = "yyyy-MM-dd") @NotNull LocalDate doj,
		@NotNull Double salary) {
	public EmployeeDTO() {
		this(null, null, null, null, null);
	}
}
