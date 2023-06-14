package com.ryana.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryana.domain.Employee;
import com.ryana.domain.Gender;
import com.ryana.dto.EmployeeDTO;
import com.ryana.mapper.EmployeeMapper;
import com.ryana.service.EmployeeService;

/* This is not a pure unit test as it involves MockMVC
 * Using MockMVC Standalone Mode
 * */
@ExtendWith(MockitoExtension.class)
class EmployeeControllerUnitTest {

	private MockMvc mockMvc;

	@Mock
	private EmployeeService employeeService;
	@Mock
	private EmployeeMapper employeeMapper;

	@InjectMocks
	private EmployeeController employeeController;

	private JacksonTester<EmployeeDTO> jsonEmployeeDTO;
	private JacksonTester<List<EmployeeDTO>> jsonEmployeeDTOs;

	@BeforeEach
	public void setup() {
		final var objectMapper = new ObjectMapper();
		objectMapper.findAndRegisterModules();
		JacksonTester.initFields(this, objectMapper);
		mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
	}

	@Test
	@DisplayName("Should get all the Employees")
	void shouldGetAllEmployees() throws Exception {
		final var sharath = Employee.builder().id(1l).name("sharath").gender(Gender.MALE)
				.doj(LocalDate.of(2020, Month.JANUARY, 5)).salary(10000.00).build();
		final var kumar = Employee.builder().id(2l).name("kumar").gender(Gender.MALE)
				.doj(LocalDate.of(2021, Month.DECEMBER, 15)).salary(20000.00).build();
		final var employees = List.of(sharath, kumar);
		final var employeeDTOs = EmployeeMapper.INSTANCE.toEmployeeDTOs(employees);
		given(employeeService.findAll()).willReturn(employees);
		given(employeeMapper.toEmployeeDTOs(anyList())).willReturn(employeeDTOs);

		final var response = mockMvc.perform(get("/employees").accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andReturn().getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).isEqualTo(jsonEmployeeDTOs.write(employeeDTOs).getJson());
	}

	@Test
	@DisplayName("Should get an Employee for valid Id")
	void shouldGetAnEmployeeById() throws Exception {
		final var kumar = Employee.builder().id(3l).name("kumar").gender(Gender.MALE)
				.doj(LocalDate.of(2021, Month.DECEMBER, 15)).salary(20000.00).build();
		final var employeeDTO = EmployeeMapper.INSTANCE.toEmployeeDTO(kumar);
		given(employeeService.findById(anyLong())).willReturn(Optional.of(kumar));
		given(employeeMapper.toEmployeeDTO(any())).willReturn(employeeDTO);

		final var response = mockMvc.perform(get("/employees/3").accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andReturn().getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).isEqualTo(jsonEmployeeDTO.write(employeeDTO).getJson());
	}

	@Test
	@DisplayName("Should get HttpStatus.NOT_FOUND for invalid Id")
	void shouldGetNotFoundStatusForInvalidId() throws Exception {
		given(employeeService.findById(anyLong())).willReturn(Optional.empty());

		final var response = mockMvc.perform(get("/employees/104").accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andReturn().getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
		assertThat(response.getContentAsString()).isBlank();
	}

	@Test
	@DisplayName("Should save an Employee and return")
	void shouldSaveAnEmployeeAndReturn() throws Exception {
		final var kumar = Employee.builder().name("kumar").gender(Gender.MALE)
				.doj(LocalDate.of(2021, Month.DECEMBER, 15)).salary(20000.00).build();
		final var employeeDTO = EmployeeMapper.INSTANCE.toEmployeeDTO(kumar);
		given(employeeService.save(any())).willReturn(kumar);
		given(employeeMapper.toEmployeeDTO(any())).willReturn(employeeDTO);
		final var response = mockMvc
				.perform(post("/employees").contentType(MediaType.APPLICATION_JSON)
						.content(jsonEmployeeDTO.write(employeeDTO).getJson()))
				.andDo(print()).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
		assertThat(response.getContentAsString()).isEqualTo(jsonEmployeeDTO.write(employeeDTO).getJson());
	}

	@Test
	@DisplayName("Should not save an Employee when fields are missing")
	void shouldNotSaveAnEmployee() throws Exception {
		final var kumar = Employee.builder().gender(Gender.MALE).doj(LocalDate.of(2021, Month.DECEMBER, 15))
				.salary(20000.00).build();
		final var employeeDTO = EmployeeMapper.INSTANCE.toEmployeeDTO(kumar);
		mockMvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON)
				.content(jsonEmployeeDTO.write(employeeDTO).getJson())).andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Should update an Employee and return for Valid Id")
	void shouldUpdateAnEmployeeAndReturnForValidId() throws Exception {
		final var kumar = Employee.builder().id(2l).name("kumar").gender(Gender.MALE)
				.doj(LocalDate.of(2021, Month.DECEMBER, 15)).salary(20000.00).build();
		final var employeeDTO = EmployeeMapper.INSTANCE.toEmployeeDTO(kumar);
		given(employeeService.update(anyLong(), any())).willReturn(Optional.of(kumar));
		given(employeeMapper.toEmployeeDTO(any())).willReturn(employeeDTO);
		final var response = mockMvc
				.perform(put("/employees/2").contentType(MediaType.APPLICATION_JSON)
						.content(jsonEmployeeDTO.write(employeeDTO).getJson()))
				.andDo(print()).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).isEqualTo(jsonEmployeeDTO.write(employeeDTO).getJson());
	}

	@Test
	@DisplayName("Update Should get HttpStatus.NOT_FOUND for invalid Id")
	void updateShouldReturnANullForInvalidId() throws Exception {
		final var kumar = Employee.builder().id(2l).name("kumar").gender(Gender.MALE)
				.doj(LocalDate.of(2021, Month.DECEMBER, 15)).salary(20000.00).build();
		final var employeeDTO = EmployeeMapper.INSTANCE.toEmployeeDTO(kumar);
		given(employeeService.update(anyLong(), any())).willReturn(Optional.empty());
		final var response = mockMvc
				.perform(put("/employees/104").contentType(MediaType.APPLICATION_JSON)
						.content(jsonEmployeeDTO.write(employeeDTO).getJson()))
				.andDo(print()).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
		assertThat(response.getContentAsString()).isBlank();
	}

	@Test
	@DisplayName("Should delete an Employee for valid Id")
	void shouldDeleteAnEmployeeById() throws Exception {
		willDoNothing().given(employeeService).deleteById(anyLong());
		mockMvc.perform(delete("/employees/50").contentType(MediaType.APPLICATION_JSON).content("{}")).andDo(print())
				.andExpect(status().isNoContent());
		verify(employeeService, times(1)).deleteById(anyLong());
	}

	@Test
	@DisplayName("Should get an Average salary of Male Employees")
	void shouldGetAnAverageSalaryOfMaleEmployees() throws Exception {
		given(employeeService.findAverageSalaryByGender(any())).willReturn(10000.0);
		mockMvc.perform(get("/employees/averageSalary/" + Gender.MALE).contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk()).andExpect(content().string("10000.0"));

	}

	@Test
	@DisplayName("Should get an Average salary of Female Employees")
	void shouldGetAnAverageSalaryOfFemaleEmployees() throws Exception {
		given(employeeService.findAverageSalaryByGender(any())).willReturn(10000.0);
		mockMvc.perform(get("/employees/averageSalary/" + Gender.FEMALE).contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk()).andExpect(content().string("10000.0"));
	}

	@Test
	@DisplayName("Should get Employees by Year Of Joining")
	void shouldGetEmployeesByYear() throws Exception {
		final var sharath = Employee.builder().id(1l).name("sharath").gender(Gender.MALE)
				.doj(LocalDate.of(2020, Month.JANUARY, 5)).salary(10000.00).build();
		final var kumar = Employee.builder().id(2l).name("kumar").gender(Gender.MALE)
				.doj(LocalDate.of(2020, Month.DECEMBER, 15)).salary(20000.00).build();
		final var employees = List.of(sharath, kumar);
		final var employeeDTOs = EmployeeMapper.INSTANCE.toEmployeeDTOs(employees);
		given(employeeService.findByYear(anyInt())).willReturn(employees);
		given(employeeMapper.toEmployeeDTOs(anyList())).willReturn(employeeDTOs);

		mockMvc.perform(get("/employees/year/2020").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andDo(print()).andExpect(content().json(jsonEmployeeDTOs.write(employeeDTOs).getJson()));
	}

	@Test
	@DisplayName("Should get Empty List of Employees for invalid Year Of Joining")
	void shouldGetEmptyListOfEmployeesByYear() throws Exception {
		given(employeeService.findByYear(anyInt())).willReturn(List.of());
		given(employeeMapper.toEmployeeDTOs(anyList())).willReturn(List.of());
		final var response = mockMvc.perform(get("/employees/year/2010").accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andReturn().getResponse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).isEqualTo("[]");
	}

}
