# Spring Boot 2-Hour Practical Lab

## Lab: Multi-Table Database Relationships

**Duration:** 2 hours\
**Backend:** Spring Boot REST API\
**Database:** PostgreSQL\
**Persistence:** Spring Data JPA\
**Testing:** Postman\
**Difficulty:** Intermediate

**Scenario:** A college needs an API to manage departments and students.
Each department can have multiple students, but each student belongs to
one department.

This lab extends the existing Spring Boot project. Do not create a new
Spring Boot project or introduce a new project structure.

## 1. Learning objectives

By the end of this practical, learners should be able to:

1.  Create multiple related PostgreSQL tables using JPA entities.
2.  Understand `@OneToMany` and `@ManyToOne`.
3.  Use DTOs instead of exposing database entities directly.
4.  Implement repositories using `JpaRepository`.
5.  Separate business logic into service classes.
6.  Implement REST controllers using `ResponseEntity`.
7.  Handle exceptions using custom exceptions.
8.  Understand how Spring Boot manages relationships between database
    tables.

## 2. Application architecture

Maintain the existing package structure:

``` text
com.murendeni.crtd
│
├── CrtdApplication.java
│
├── controller
│   ├── DepartmentController.java
│   └── StudentController.java
│
├── dto
│   ├── DepartmentRequest.java
│   ├── DepartmentResponse.java
│   ├── StudentRequest.java
│   └── StudentResponse.java
│
├── entity
│   ├── Department.java
│   └── Student.java
│
├── repository
│   ├── DepartmentRepository.java
│   └── StudentRepository.java
│
├── service
│   ├── DepartmentService.java
│   ├── StudentService.java
│   └── impl
│       ├── DepartmentServiceImpl.java
│       └── StudentServiceImpl.java
│
└── exceptions
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java
```

**Important:** Continue working inside the existing project.

## 3. Database relationship

We will create two tables.

### DEPARTMENT

  Column          Description
  --------------- ------------------------
  `id`            Primary key (PK)
  `name`          Department name
  `description`   Department description

### STUDENT

  Column            Description
  ----------------- -----------------------------------------------
  `id`              Primary key (PK)
  `first_name`      Student's first name
  `last_name`       Student's last name
  `email`           Student's unique email address
  `department_id`   Foreign key (FK) referencing `departments.id`

One department can have many students. Each student references one
department through `department_id`.

### Example data

**Department**

    ID Name
  ---- ------------------------
     1 Information Technology
     2 Finance

**Student**

    ID First name     Department ID
  ---- ------------ ---------------
     1 John                       1
     2 Mary                       1
     3 Peter                      2

## 4. Two-hour schedule

  Time           Activity
  -------------- --------------------------------------------
  00--10 min     Understand requirements and relationships
  10--25 min     Create entities and database relationships
  25--40 min     Create DTOs
  40--55 min     Create repositories
  55--75 min     Implement services
  75--90 min     Implement controllers
  90--105 min    Implement exception handling
  105--115 min   Test using Postman
  115--120 min   Review and Git commit

## 5. Implementation

### Step 1: Configure PostgreSQL (10 minutes)

Open `src/main/resources/application.properties`.

Add or update the following configuration:

``` properties
spring.application.name=crtd

spring.datasource.url=jdbc:postgresql://localhost:5432/crtd_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD

spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8080
```

Replace `YOUR_PASSWORD` with your PostgreSQL password.

Create the database in PostgreSQL:

``` sql
CREATE DATABASE crtd_db;
```

Hibernate will create the tables based on your entity mappings when the
application starts.

### Step 2: Create the entities (15 minutes)

Entities represent database tables.

#### A. Department.java

**Location:** `entity/Department.java`

``` java
package com.murendeni.crtd.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @OneToMany(mappedBy = "department")
    private List<Student> students = new ArrayList<>();

    public Department() {}

    public Department(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
```

#### B. Student.java

**Location:** `entity/Student.java`

``` java
package com.murendeni.crtd.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    public Student() {}

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Department getDepartment() {
        return department;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
```

#### Important concepts

-   `@OneToMany`: One department can have multiple students.
-   `@ManyToOne`: Multiple students can belong to one department.
-   `@JoinColumn`: Specifies the foreign key column in the students
    table.
-   `mappedBy`: Indicates that the `Student` entity owns the
    relationship.

The foreign key is stored in the `students` table, not the `departments`
table.

### Step 3: Create DTOs (15 minutes)

We do not want our controllers exposing database entities directly.
Instead, we use Data Transfer Objects (DTOs) to control what information
enters and leaves our API.

#### A. DepartmentRequest.java

``` java
package com.murendeni.crtd.dto;

public record DepartmentRequest(
        String name,
        String description
) {}
```

#### B. DepartmentResponse.java

``` java
package com.murendeni.crtd.dto;

public record DepartmentResponse(
        Long id,
        String name,
        String description
) {}
```

#### C. StudentRequest.java

Notice that we accept `departmentId` instead of the complete
`Department` object.

``` java
package com.murendeni.crtd.dto;

public record StudentRequest(
        String firstName,
        String lastName,
        String email,
        Long departmentId
) {}
```

#### D. StudentResponse.java

``` java
package com.murendeni.crtd.dto;

public record StudentResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Long departmentId,
        String departmentName
) {}
```

**Why is this important?**

When creating a student, the frontend only needs to send:

``` json
{
  "firstName": "John",
  "lastName": "Smith",
  "email": "john@example.com",
  "departmentId": 1
}
```

The backend will find the department with ID `1` and associate it with
the student.

### Step 4: Create repositories (10 minutes)

Repositories communicate with the database.

#### DepartmentRepository.java

``` java
package com.murendeni.crtd.repository;

import com.murendeni.crtd.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository
        extends JpaRepository<Department, Long> {
}
```

#### StudentRepository.java

``` java
package com.murendeni.crtd.repository;

import com.murendeni.crtd.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository
        extends JpaRepository<Student, Long> {

    List<Student> findByDepartmentId(Long departmentId);
}
```

Notice that we did not write SQL to retrieve students belonging to a
department. Spring Data JPA generates the query from the repository
method name.

`findByDepartmentId()` is a **derived query method**.

### Step 5: Create custom exceptions (10 minutes)

We need a standard way to handle situations where a requested resource
does not exist.

#### ResourceNotFoundException.java

**Location:** `exceptions/ResourceNotFoundException.java`

``` java
package com.murendeni.crtd.exceptions;

public class ResourceNotFoundException
        extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

#### GlobalExceptionHandler.java

**Location:** `exceptions/GlobalExceptionHandler.java`

``` java
package com.murendeni.crtd.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            ResourceNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", exception.getMessage()
                ));
    }
}
```

Whenever a service throws `ResourceNotFoundException`, Spring will
return HTTP `404 Not Found`.

### Step 6: Implement the service layer (20 minutes)

This is where the business logic belongs. The controller should not
communicate directly with repositories.

#### A. DepartmentService.java

``` java
package com.murendeni.crtd.service;

import com.murendeni.crtd.dto.DepartmentRequest;
import com.murendeni.crtd.dto.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse create(DepartmentRequest request);

    List<DepartmentResponse> findAll();

    DepartmentResponse findById(Long id);
}
```

#### B. DepartmentServiceImpl.java

``` java
package com.murendeni.crtd.service.impl;

import com.murendeni.crtd.dto.*;
import com.murendeni.crtd.entity.Department;
import com.murendeni.crtd.exceptions.ResourceNotFoundException;
import com.murendeni.crtd.repository.DepartmentRepository;
import com.murendeni.crtd.service.DepartmentService;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl
        implements DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentServiceImpl(
            DepartmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public DepartmentResponse create(
            DepartmentRequest request) {

        Department department = new Department(
                request.name(),
                request.description()
        );

        Department saved = repository.save(department);

        return mapToResponse(saved);
    }

    @Override
    public List<DepartmentResponse> findAll() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public DepartmentResponse findById(Long id) {

        Department department = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found: " + id
                        ));

        return mapToResponse(department);
    }

    private DepartmentResponse mapToResponse(
            Department department) {

        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription()
        );
    }
}
```

#### C. StudentService.java

``` java
package com.murendeni.crtd.service;

import com.murendeni.crtd.dto.StudentRequest;
import com.murendeni.crtd.dto.StudentResponse;

import java.util.List;

public interface StudentService {

    StudentResponse create(StudentRequest request);

    List<StudentResponse> findAll();

    StudentResponse findById(Long id);

    List<StudentResponse> findByDepartment(Long departmentId);
}
```

#### D. StudentServiceImpl.java

This is the most important class in the practical because it
demonstrates how to work with multiple tables.

``` java
package com.murendeni.crtd.service.impl;

import com.murendeni.crtd.dto.*;
import com.murendeni.crtd.entity.*;
import com.murendeni.crtd.exceptions.ResourceNotFoundException;
import com.murendeni.crtd.repository.*;
import com.murendeni.crtd.service.StudentService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;

    public StudentServiceImpl(
            StudentRepository studentRepository,
            DepartmentRepository departmentRepository) {

        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public StudentResponse create(StudentRequest request) {

        Department department = departmentRepository
                .findById(request.departmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found: "
                                        + request.departmentId()
                        ));

        Student student = new Student();

        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setEmail(request.email());
        student.setDepartment(department);

        Student saved = studentRepository.save(student);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {

        return studentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Student not found: " + id
                        ));

        return mapToResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> findByDepartment(
            Long departmentId) {

        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException(
                    "Department not found: " + departmentId
            );
        }

        return studentRepository
                .findByDepartmentId(departmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private StudentResponse mapToResponse(Student student) {

        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getDepartment().getId(),
                student.getDepartment().getName()
        );
    }
}
```

#### Important teaching moment

Consider this section:

``` java
Department department = departmentRepository
        .findById(request.departmentId())
        .orElseThrow(() ->
                new ResourceNotFoundException(
                        "Department not found"
                ));
```

The student creation process requires two things:

1.  Retrieve the existing department.
2.  Associate that department with the new student.

This is how multiple related tables work together through the service
layer.

### Step 7: Create REST controllers (15 minutes)

#### A. DepartmentController.java

``` java
package com.murendeni.crtd.controller;

import com.murendeni.crtd.dto.*;
import com.murendeni.crtd.service.DepartmentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(
            @RequestBody DepartmentRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> findAll() {

        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.findById(id));
    }
}
```

#### B. StudentController.java

``` java
package com.murendeni.crtd.controller;

import com.murendeni.crtd.dto.*;
import com.murendeni.crtd.service.StudentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> create(
            @RequestBody StudentRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> findAll() {

        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<StudentResponse>>
    findByDepartment(@PathVariable Long departmentId) {

        return ResponseEntity.ok(
                service.findByDepartment(departmentId)
        );
    }
}
```

## 6. Test using Postman (15 minutes)

Start the application:

``` bash
mvn spring-boot:run
```

**Base URL:** `http://localhost:8080`

### Test 1: Create a department

**Method:** `POST`\
**Endpoint:** `/api/v1/departments`\
**Expected status:** `201 Created`

Request body:

``` json
{
  "name": "Information Technology",
  "description": "IT and Software Development"
}
```

Expected response:

``` json
{
  "id": 1,
  "name": "Information Technology",
  "description": "IT and Software Development"
}
```

### Test 2: Create another department

**Method:** `POST`\
**Endpoint:** `/api/v1/departments`

Request body:

``` json
{
  "name": "Finance",
  "description": "Financial Management"
}
```

### Test 3: Register a student

**Method:** `POST`\
**Endpoint:** `/api/v1/students`\
**Expected status:** `201 Created`

Request body:

``` json
{
  "firstName": "John",
  "lastName": "Smith",
  "email": "john@example.com",
  "departmentId": 1
}
```

Expected response:

``` json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Smith",
  "email": "john@example.com",
  "departmentId": 1,
  "departmentName": "Information Technology"
}
```

### Test 4: Retrieve all students

**Method:** `GET`\
**Endpoint:** `/api/v1/students`

### Test 5: Retrieve students belonging to a department

**Method:** `GET`\
**Endpoint:** `/api/v1/students/department/1`

Expected result: Only students belonging to Information Technology.

### Test 6: Retrieve a department

**Method:** `GET`\
**Endpoint:** `/api/v1/departments/1`

### Test 7: Test exception handling

Try registering a student with a department ID that does not exist.

**Method:** `POST`\
**Endpoint:** `/api/v1/students`\
**Expected status:** `404 Not Found`

Request body:

``` json
{
  "firstName": "Peter",
  "lastName": "Jones",
  "email": "peter@example.com",
  "departmentId": 999
}
```

Expected response:

``` json
{
  "error": "Department not found: 999"
}
```

## 7. Verify the database relationship

Open PostgreSQL and execute:

``` sql
SELECT
    s.id,
    s.first_name,
    s.last_name,
    s.email,
    d.name AS department_name
FROM students s
INNER JOIN departments d
    ON s.department_id = d.id;
```

This query demonstrates how relational databases combine information
from multiple tables.

Learners should be able to identify:

-   Primary keys.
-   Foreign keys.
-   One-to-many relationships.
-   `INNER JOIN` operations.

## 8. Additional learner challenge

**Optional extension:** If learners finish before the two hours are
over, give them this additional assignment.

Implement the following features without changing the existing
architecture:

  Feature             Requirement
  ------------------- --------------------------------------------------
  Update student      `PUT /api/v1/students/{id}`
  Delete student      `DELETE /api/v1/students/{id}`
  Update department   `PUT /api/v1/departments/{id}`
  Department count    Return the number of students in a department
  Email validation    Prevent invalid email addresses
  Duplicate email     Prevent registering students with the same email

**Additional question:** What should happen if someone attempts to
delete a department that still has students?

**Expected discussion:** The API should prevent deleting a department
that is still referenced by students, unless the application has an
explicitly designed reassignment or deletion policy.

## 9. Assessment checklist

Learners must demonstrate the following 15 items:

-   [ ] Created Department and Student entities.
-   [ ] Implemented `@OneToMany` and `@ManyToOne` relationships.
-   [ ] Configured PostgreSQL successfully.
-   [ ] Created request and response DTOs.
-   [ ] Implemented both JPA repositories.
-   [ ] Implemented service interfaces and implementations.
-   [ ] Used constructor-based dependency injection.
-   [ ] Created REST controllers.
-   [ ] Implemented custom `ResourceNotFoundException`.
-   [ ] Implemented `GlobalExceptionHandler`.
-   [ ] Successfully registered a department.
-   [ ] Successfully registered a student linked to a department.
-   [ ] Retrieved students by department ID.
-   [ ] Demonstrated correct HTTP 404 handling.
-   [ ] Verified the relationship using an SQL `JOIN`.

**Practical progress:** 0/15

## 10. Git commit requirement

Learners must commit their completed implementation.

Use the following commands:

``` bash
git add .
git commit -m "feat: implement student and department management using JPA relationships"
```

## 11. Questions for discussion

Use these questions during the final five minutes:

1.  Why should we use DTOs instead of returning entities directly?
2.  What is the difference between `@OneToMany` and `@ManyToOne`?
3.  Why do we put business logic inside the service layer?
4.  Why does `StudentServiceImpl` need both repositories?
5.  What happens if we register a student with a department ID that does
    not exist?
6.  Why do we use `@RestControllerAdvice`?
7.  What is the difference between a primary key and a foreign key?
8.  Why should controllers not communicate directly with repositories?

## Final expected outcome

By the end of this practical, learners should have extended their
existing Spring Boot application with a working Student and Department
Management REST API backed by PostgreSQL and Spring Data JPA. They
should be able to create and retrieve departments and students,
associate each student with an existing department, retrieve students by
department, handle missing resources with an HTTP 404 response, and
verify the relationship using an SQL join.

Learners should also be able to explain how the entity relationship,
DTOs, repositories, services, controllers, and exception handling work
together in a layered Spring Boot application.
