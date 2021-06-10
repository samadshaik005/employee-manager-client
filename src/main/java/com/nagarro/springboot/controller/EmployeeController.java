package com.nagarro.springboot.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import com.nagarro.springboot.model.Employee;
import com.nagarro.springboot.service.EmployeeService;

@Controller
public class EmployeeController {

	private static final String GET_EMPLOYEE_ENDPOINT_URL = "http://localhost:8080/employees/{id}";
	private static final String CREATE_EMPLOYEE_ENDPOINT_URL = "http://localhost:8080/employees/add";
	private static final String DELETE_EMPLOYEE_ENDPOINT_URL = "http://localhost:8080/delete/employees/{id}";

	@Autowired
	private EmployeeService employeeService;

	// display list of employees
	@GetMapping("/")
	public String viewHomePage(Model model) {
		return findPaginated(1, "employename", "asc", model);
	}

	@GetMapping("/showNewEmployeeForm")
	public String showNewEmployeeForm(Model model) {
		// create model attribute to bind form data
		Employee employee = new Employee();
		model.addAttribute("employee", employee);
		return "new_employee";
	}

	@PostMapping("/saveEmployee")
	public String saveEmployee(@ModelAttribute("employee") Employee employee) {

		// employeeService.saveEmployee(employee);
		RestTemplate restTemplate = new RestTemplate();
		Employee result = restTemplate.postForObject(CREATE_EMPLOYEE_ENDPOINT_URL, employee, Employee.class);

		System.out.println(result);
		return "redirect:/";
	}

	@GetMapping("/deleteEmployee/{id}")
	public String deleteEmployee(@PathVariable(value = "id") long id) {
		// this.employeeService.deleteEmployeeById(id);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.delete(DELETE_EMPLOYEE_ENDPOINT_URL, id);
		return "redirect:/";
	}
	@GetMapping("/employeebyid/{id}")
	public void getEmployeeById(@PathVariable(value = "id") long id) {

		Map<String, String> params = new HashMap<String, String>();
	   params.put("id", "1");

		RestTemplate restTemplate = new RestTemplate();
		Employee result = restTemplate.getForObject(GET_EMPLOYEE_ENDPOINT_URL, Employee.class, params);

		System.out.println(result);
	}

	@GetMapping("/showFormForUpdate/{id}")
	public String showFormForUpdate(@PathVariable(value = "id") long id, Model model) {

		Employee employee = employeeService.getEmployeeById(id);
		model.addAttribute("employee", employee);
		return "update_employee";
	}

	@GetMapping("/page/{pageNo}")
	public String findPaginated(@PathVariable(value = "pageNo") int pageNo, @RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir, Model model) {
		int pageSize = 5;

		Page<Employee> page = employeeService.findPaginated(pageNo, pageSize, sortField, sortDir);
		List<Employee> listEmployees = page.getContent();

		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

		model.addAttribute("listEmployees", listEmployees);
		return "index";
	}

	// download as csv
	@GetMapping("/download")
	public void downloadCSV(HttpServletResponse response) throws IOException {
		String csvFileName = "Employees.csv";
		response.setContentType("text/csv");

		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);

		response.setHeader(headerKey, headerValue);

		List<Employee> employees = employeeService.getEmployees();

		ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);

		String[] header = { "id", "employename", "location", "email", "dateofbirth" };

		csvWriter.writeHeader(header);

		for (Employee employee1 : employees) {
			csvWriter.write(employee1, header);
		}

		csvWriter.close();
	}


}
