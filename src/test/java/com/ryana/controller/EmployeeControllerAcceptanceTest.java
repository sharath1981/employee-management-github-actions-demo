package com.ryana.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;

import com.ryana.domain.Employee;
import com.ryana.domain.Gender;
import com.ryana.dto.EmployeeDTO;
import com.ryana.mapper.EmployeeMapper;

/* This is also an Integration Testing and End to End testing with Test Container DB 
 * Whole spring context will be loaded including web,service and repository layer*/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeControllerAcceptanceTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	@DisplayName("Should get all the Employees")
	void shouldGetAllEmployees() {
		final var responseEntity = testRestTemplate.getForEntity("/employees", EmployeeDTO[].class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).hasSize(100);
	}

	@Test
	@DisplayName("Should get an Employee for valid Id")
	void shouldGetAnEmployeeById() {
		final var responseEntity = testRestTemplate.getForEntity("/employees/3", EmployeeDTO.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isNotNull();
		assertThat(responseEntity.getBody()).extracting(EmployeeDTO::id).isEqualTo(3l);
	}

	@Test
	@DisplayName("Should get HttpStatus.NOT_FOUND for invalid Id")
	void shouldGetNotFoundStatusForInvalidId() {
		final var responseEntity = testRestTemplate.getForEntity("/employees/104", EmployeeDTO.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(responseEntity.getBody()).extracting(EmployeeDTO::id).isNull();
	}

	@Test
	@DisplayName("Should save an Employee and return")
	void shouldSaveAnEmployeeAndReturn() {
		final var sharath = Employee.builder().name("sharath").gender(Gender.MALE)
				.doj(LocalDate.of(2020, Month.JANUARY, 5)).salary(10000.00).build();
		final var sharathDTO = EmployeeMapper.INSTANCE.toEmployeeDTO(sharath);
		final var responseEntity = testRestTemplate.postForEntity(URI.create("/employees"), sharathDTO,
				EmployeeDTO.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(responseEntity.getBody()).extracting(EmployeeDTO::id).isNotNull();
		assertThat(responseEntity.getHeaders().get("Location"))
				.anyMatch(location -> location.endsWith("/employees/" + responseEntity.getBody().id()));
	}

	@Test
	@DisplayName("Should not save an Employee when fields are missing")
	void shouldNotSaveAnEmployee() {
		final var responseEntity = testRestTemplate.postForEntity(URI.create("/employees"), new EmployeeDTO(),
				EmployeeDTO.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(responseEntity.getBody()).extracting(EmployeeDTO::id).isNull();
	}

	@Test
	@DisplayName("Should update an Employee and return for Valid Id")
	void shouldUpdateAnEmployeeAndReturnForValidId() {
		final var sharath = Employee.builder().id(2l).name("sharath").gender(Gender.MALE)
				.doj(LocalDate.of(2020, Month.JANUARY, 5)).salary(10000.00).build();
		final var sharathDTO = EmployeeMapper.INSTANCE.toEmployeeDTO(sharath);
		final var requestEntity = new RequestEntity<>(sharathDTO, HttpMethod.PUT, URI.create("/employees/2"));
		final var responseEntity = testRestTemplate.exchange(requestEntity, EmployeeDTO.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isNotNull();
		assertThat(responseEntity.getBody()).extracting(EmployeeDTO::id).isEqualTo(2l);
		assertThat(responseEntity.getBody()).isEqualTo(sharathDTO);
	}

	@Test
	@DisplayName("Update Should get HttpStatus.NOT_FOUND for invalid Id")
	void updateShouldReturnANullForInvalidId() {
		final var sharath = Employee.builder().id(112l).name("sharath").gender(Gender.MALE)
				.doj(LocalDate.of(2020, Month.JANUARY, 5)).salary(10000.00).build();
		final var sharathDTO = EmployeeMapper.INSTANCE.toEmployeeDTO(sharath);
		final var requestEntity = new RequestEntity<>(sharathDTO, HttpMethod.PUT, URI.create("/employees/112"));
		final var responseEntity = testRestTemplate.exchange(requestEntity, EmployeeDTO.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(responseEntity.getBody()).extracting(EmployeeDTO::id).isNull();
	}

	@Test
	@DisplayName("Should delete an Employee for valid Id")
	void shouldDeleteAnEmployeeById() {
		final var requestEntity = new RequestEntity<>(HttpMethod.DELETE, URI.create("/employees/50"));
		final var deleteResponse = testRestTemplate.exchange(requestEntity, EmployeeDTO.class);
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		final var responseEntity = testRestTemplate.getForEntity("/employees/50", EmployeeDTO.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(responseEntity.getBody()).extracting(EmployeeDTO::id).isNull();
	}

	@Order(1)
	@Test
	@DisplayName("Should get an Average salary of Male Employees")
	void shouldGetAnAverageSalaryOfMaleEmployees() {
		final var responseEntity = testRestTemplate.getForEntity(URI.create("/employees/averageSalary/" + Gender.MALE),
				Double.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isNotNull();
		assertThat(responseEntity.getBody()).extracting(Double::doubleValue).isEqualTo(54963.450877192976);
	}

	@Order(2)
	@Test
	@DisplayName("Should get an Average salary of Female Employees")
	void shouldGetAnAverageSalaryOfFemaleEmployees() {
		final var responseEntity = testRestTemplate
				.getForEntity(URI.create("/employees/averageSalary/" + Gender.FEMALE), Double.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isNotNull();
		assertThat(responseEntity.getBody()).extracting(Double::doubleValue).isEqualTo(62144.49302325581);
	}

	@Test
	@DisplayName("Should get Employees by Year Of Joining")
	void shouldGetEmployeesByYear() {
		final var responseEntity = testRestTemplate.getForEntity(URI.create("/employees/year/2020"),
				EmployeeDTO[].class);
		System.out.println(responseEntity.getBody().length);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isNotEmpty();
		assertThat(responseEntity.getBody()).hasSize(16);
	}

	@Test
	@DisplayName("Should get Empty List of Employees for invalid Year Of Joining")
	void shouldGetEmptyListOfEmployeesByYear() {
		final var responseEntity = testRestTemplate.getForEntity(URI.create("/employees/year/2010"),
				EmployeeDTO[].class);
		System.out.println(responseEntity.getBody());
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isEmpty();
	}
}
