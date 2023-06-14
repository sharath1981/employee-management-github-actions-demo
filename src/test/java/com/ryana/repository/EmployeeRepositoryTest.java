package com.ryana.repository;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.ryana.domain.Employee;
import com.ryana.domain.Gender;


/*
 * If we are using
 * @TestPropertySource(properties = "spring.datasource.url=jdbc:tc:postgresql:latest:///employee_db") 
 * then @Testcontainers, @Container and @DynamicPropertySource are not required
 */
//@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class EmployeeRepositoryTest {

//	@Container
//	private static PostgreSQLContainer postgresContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:latest"));
//
//	@DynamicPropertySource
//	private static void overrridePostgresDBProperties(DynamicPropertyRegistry registry) {
//		registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
//		registry.add("spring.datasource.username", postgresContainer::getUsername);
//		registry.add("spring.datasource.password", postgresContainer::getPassword);
//	}
//	
	@Autowired
	private EmployeeRepository employeeRepository;

	@Test
	@DisplayName("Should find Average Salary of FEMALE employees")
	void shouldFindAvgSalaryOfFemaleEmployees() {
		assertEquals(62144.49302325581, employeeRepository.findAverageSalaryByGender(Gender.FEMALE));
	}

	@Test
	@DisplayName("Should find Average Salary of MALE employees")
	void shouldFindAvgSalaryOfMaleEmployees() {
		final var findAveragSalaryByGender = employeeRepository.findAverageSalaryByGender(Gender.MALE);
		assertEquals(54963.450877192976, findAveragSalaryByGender);
	}

	@Test
	@DisplayName("Should find all Employees by Year of Joining")
	void shouldFindEmployeesByDoj() {
		assertTrue(employeeRepository.findByYear(2020).stream().map(Employee::getDoj).map(LocalDate::getYear)
				.allMatch(year -> year == 2020));
	}

}
