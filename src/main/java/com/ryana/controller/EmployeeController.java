package com.ryana.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ryana.domain.Gender;
import com.ryana.dto.EmployeeDTO;
import com.ryana.exception.EmployeeNotFoundException;
import com.ryana.mapper.EmployeeMapper;
import com.ryana.service.EmployeeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Validated
@RequiredArgsConstructor
@Log4j2
@RestController
@RequestMapping("employees")
public class EmployeeController {

	private final EmployeeService employeeService;
	private final EmployeeMapper employeeMapper;

	@GetMapping
	public ResponseEntity<List<EmployeeDTO>> findAll() {
		final var employees = employeeService.findAll();
		final var employeeDTOs = employeeMapper.toEmployeeDTOs(employees);
		log.trace("findAll:employeeDTOs=>{}", employeeDTOs);
		return ResponseEntity.ok().body(employeeDTOs);
	}

	@GetMapping("{id}")
	public ResponseEntity<EmployeeDTO> findById(@PathVariable Long id) {
		final var employeeDTO = employeeService.findById(id).map(employeeMapper::toEmployeeDTO)
				.orElseThrow(EmployeeNotFoundException::new);
		return ResponseEntity.ok().body(employeeDTO);
	}

	@PostMapping
	public ResponseEntity<EmployeeDTO> save(@RequestBody @Valid EmployeeDTO employeeDTO) {
		return saveEmployee(employeeDTO);
	}

	private ResponseEntity<EmployeeDTO> saveEmployee(EmployeeDTO employeeDTO) {
		final var employee = employeeService.save(employeeMapper.toEmployee(employeeDTO));
		final var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(employee.getId()).toUri();
		return ResponseEntity.created(location).body(employeeMapper.toEmployeeDTO(employee));
	}

	@PutMapping("{id}")
	public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @RequestBody @Valid EmployeeDTO employeeDTO) {
		return employeeService.update(id, employeeMapper.toEmployee(employeeDTO))
				.map(employeeMapper::toEmployeeDTO)
				.map(ResponseEntity.ok()::body)
				.orElseThrow(EmployeeNotFoundException::new);
	}

	@DeleteMapping("{id}")
	public ResponseEntity<EmployeeDTO> deleteById(@PathVariable Long id) {
		employeeService.deleteById(id);
		return ResponseEntity.noContent()
				.build();
	}

	@GetMapping("averageSalary/{gender}")
	public ResponseEntity<Double> findAverageSalaryByGender(@PathVariable Gender gender) {
		final var averageSalary = employeeService.findAverageSalaryByGender(gender);
		return ResponseEntity.ok().body(averageSalary);
	}

	@GetMapping("year/{doj}")
	public ResponseEntity<List<EmployeeDTO>> findByYear(@PathVariable Integer doj) {
		final var employee = employeeService.findByYear(doj);
		final var employeeDTOs = employeeMapper.toEmployeeDTOs(employee);
		return ResponseEntity.ok().body(employeeDTOs);
	}

}
