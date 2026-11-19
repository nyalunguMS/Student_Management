package org.example.student_management.service;

import org.example.student_management.dto.DepartmentRequest;
import org.example.student_management.dto.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse create(DepartmentRequest request);

    List<DepartmentResponse> findAll();

    DepartmentResponse findById(Long id);
}
