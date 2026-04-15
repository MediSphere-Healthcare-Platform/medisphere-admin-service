package com.medisphere.admin.controller;

import com.medisphere.admin.dto.ApprovalRequestDTO;
import com.medisphere.admin.dto.DoctorRequestDTO;
import com.medisphere.admin.dto.ResponseDTO;
import com.medisphere.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/doctors/pending")
    public ResponseEntity<ResponseDTO> registerDoctor(@RequestBody DoctorRequestDTO doctorDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Doctor registration request received")
                        .data(adminService.registerDoctor(doctorDTO))
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
