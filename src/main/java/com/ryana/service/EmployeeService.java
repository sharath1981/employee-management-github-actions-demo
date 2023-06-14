package com.ryana.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.ryana.domain.Employee;
import com.ryana.domain.Gender;
import com.ryana.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EmployeeService {

	private final EmployeeRepository employeeRepository;

	public List<Employee> findAll() {
		return employeeRepository.findAll();
	}

	public Optional<Employee> findById(Long id) {
		return employeeRepository.findById(id);
	}

	public Employee save(Employee employee) {
		return employeeRepository.save(employee);
	}

	public Optional<Employee> update(Long id, Employee employee) {

		return employeeRepository.findById(id).map(old -> {
			BeanUtils.copyProperties(employee, old, Employee.class);
			old.setId(id);
			return old;
		}).map(employeeRepository::save);
	}

	public void deleteById(Long id) {
		employeeRepository.deleteById(id);
	}

	public Double findAverageSalaryByGender(Gender gender) {
		return employeeRepository.findAverageSalaryByGender(gender);
	}

	public List<Employee> findByYear(Integer doj) {
		return Optional.ofNullable(doj)
				.map(employeeRepository::findByYear).orElseGet(List::of);
	}

}
