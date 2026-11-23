package org.example.student_management.service;

import org.example.student_management.dto.*;
import org.example.student_management.entity.Department;
import org.example.student_management.exception.ResourceNotFoundException;
import org.example.student_management.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService{

    private final DepartmentRepository repository;

    public DepartmentServiceImpl(
            DepartmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public DepartmentResponse create(
            DepartmentRequest request) {

        Department department = new Department();

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
