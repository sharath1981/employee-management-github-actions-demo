package com.ryana.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ryana.domain.Employee;
import com.ryana.domain.Gender;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	@Query("select AVG(salary) from Employee where gender=:gender")
	Double findAverageSalaryByGender(@Param("gender") Gender gender);

	@Query("from Employee where YEAR(doj)=:doj")
	List<Employee> findByYear(@Param("doj") Integer doj);

}
