package com.medisphere.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorRequestDTO {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String specialty;
    private String licenseUrl;
}
