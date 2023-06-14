package com.ryana.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ryana.domain.Employee;
import com.ryana.domain.Gender;
import com.ryana.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

	@Mock
	private EmployeeRepository employeeRepository;

	@InjectMocks
	private EmployeeService employeeService;

	@Test
	@DisplayName("Should update an Employee and return for Valid Id")
	void shouldUpdateAnEmployeeAndReturnForValidId() {
		final var kumar = Employee.builder().name("kumar").gender(Gender.MALE)
				.doj(LocalDate.of(2021, Month.DECEMBER, 15)).salary(20000.00).build();
		given(employeeRepository.findById(anyLong())).willReturn(Optional.of(kumar));
		given(employeeRepository.save(any())).willReturn(kumar);
		final var employee = employeeService.update(2l, kumar).orElse(null);
		assertThat(employee).isEqualTo(kumar);
	}

	@Test
	@DisplayName("Update Should return a null for invalid Id")
	void updateShouldReturnANullForInvalidId() {
		final var kumar = Employee.builder().id(2l).name("kumar").gender(Gender.MALE)
				.doj(LocalDate.of(2021, Month.DECEMBER, 15)).salary(20000.00).build();
		given(employeeRepository.findById(anyLong())).willReturn(Optional.empty());
		final var employee = employeeService.update(4l, kumar).orElse(null);
		assertThat(employee).isNull();
	}

	@Test
	@DisplayName("Should return an Average salary of Male Employees")
	void shouldReturnAnAverageSalaryOfMaleEmployees() {
		given(employeeRepository.findAverageSalaryByGender(any())).willReturn(10000.00);
		final var avgSalary = employeeService.findAverageSalaryByGender(Gender.MALE);
		assertThat(avgSalary).isEqualTo(10000.00);
	}

	@Test
	@DisplayName("Should return an Average salary of Female Employees")
	void shouldReturnAnAverageSalaryOfFemaleEmployees() {
		given(employeeRepository.findAverageSalaryByGender(any())).willReturn(20000.00);
		final var avgSalary = employeeService.findAverageSalaryByGender(Gender.FEMALE);
		assertThat(avgSalary).isEqualTo(20000.00);
	}

	@Test
	@DisplayName("Should return Employees by Year Of Joining")
	void shouldReturnEmployeesByYear() {
		final var sharath = Employee.builder().id(1l).name("sharath").gender(Gender.MALE)
				.doj(LocalDate.of(2020, Month.JANUARY, 5)).salary(10000.00).build();
		final var vivek = Employee.builder().id(3l).name("vivek").gender(Gender.MALE)
				.doj(LocalDate.of(2020, Month.JANUARY, 5)).salary(10000.00).build();
		given(employeeRepository.findByYear(anyInt())).willReturn(List.of(sharath, vivek));
		final var employees = employeeService.findByYear(2020);
		assertThat(employees).hasSize(2);
	}

	@Test
	@DisplayName("Should return Empty List of Employees for invalid Year Of Joining")
	void shouldReturnEmptyListOfEmployeesByYear() {
		given(employeeRepository.findByYear(anyInt())).willReturn(List.of());
		final var employees = employeeService.findByYear(2010);
		assertThat(employees).isEmpty();
	}

}
