package com.medisphere.admin.controller;

import com.medisphere.admin.dto.ApprovalRequestDTO;
import com.medisphere.admin.dto.DoctorRequestDTO;
import com.medisphere.admin.dto.ResponseDTO;
import com.medisphere.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping(
            value = "/doctors/pending",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ResponseDTO> registerDoctor(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phone") String phone,
            @RequestParam("specialty") String specialty,
            @RequestParam(value = "licenceImage", required = false) MultipartFile licenceImage
    ) {
        DoctorRequestDTO doctorDTO = DoctorRequestDTO.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .specialty(specialty)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Doctor registration request received")
                        .data(adminService.registerDoctor(doctorDTO, licenceImage))
                        .build());
    }

    @GetMapping("/doctors/pending")
    public ResponseEntity<ResponseDTO> getPendingDoctors() {
        return ResponseEntity.ok(ResponseDTO.builder()
                .status("SUCCESS")
                .message("Pending doctors retrieved successfully")
                .data(adminService.getPendingDoctors())
                .build());
    }

    @PutMapping("/doctors/{id}/approve")
    public ResponseEntity<ResponseDTO> approveDoctor(@PathVariable Integer id) {
        return ResponseEntity.ok(ResponseDTO.builder()
                .status("SUCCESS")
                .message("Doctor approved successfully")
                .data(adminService.approveDoctor(id))
                .build());
    }

    @PutMapping("/doctors/{id}/reject")
    public ResponseEntity<ResponseDTO> rejectDoctor(@PathVariable Integer id, @RequestBody ApprovalRequestDTO rejectionRequest) {
        return ResponseEntity.ok(ResponseDTO.builder()
                .status("SUCCESS")
                .message("Doctor rejected successfully")
                .data(adminService.rejectDoctor(id, rejectionRequest))
                .build());
    }

    @GetMapping("/reports/doctors")
    public ResponseEntity<ResponseDTO> getDoctorReport() {
        return ResponseEntity.ok(ResponseDTO.builder()
                .status("SUCCESS")
                .message("Doctor report generated successfully")
                .data(adminService.getDoctorReport())
                .build());
    }
}
