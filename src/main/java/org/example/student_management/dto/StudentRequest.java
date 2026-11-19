package org.example.student_management.dto;

public record StudentRequest(
        String firstName,
        String lastName,
        String email,
        Long departmentId
) {
}
