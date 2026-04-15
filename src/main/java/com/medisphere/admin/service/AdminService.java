package com.medisphere.admin.service;

import com.medisphere.admin.dto.ApprovalRequestDTO;
import com.medisphere.admin.dto.DoctorRequestDTO;
import com.medisphere.admin.entity.MedisphereAdmin;
import com.medisphere.admin.exception.ResourceNotFoundException;
import com.medisphere.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;
    private final ExternalServiceClient externalServiceClient;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MedisphereAdmin registerDoctor(DoctorRequestDTO doctorDTO) {
        log.info("Registering doctor: {}", doctorDTO.getEmail());
        MedisphereAdmin doctor = new MedisphereAdmin();
        doctor.setEmail(doctorDTO.getEmail());
        // Encode password before saving to DB
        doctor.setPassword(passwordEncoder.encode(doctorDTO.getPassword()));
        doctor.setFirstName(doctorDTO.getFirstName());
        doctor.setLastName(doctorDTO.getLastName());
        doctor.setPhone(doctorDTO.getPhone());
        doctor.setSpecialty(doctorDTO.getSpecialty());
        doctor.setLicenseUrl(doctorDTO.getLicenseUrl());
        doctor.setStatus("PENDING");
        return adminRepository.save(doctor);
    }

    public List<MedisphereAdmin> getPendingDoctors() {
        return adminRepository.findByStatus("PENDING");
    }

    @Transactional
    public MedisphereAdmin approveDoctor(Integer id) {
        MedisphereAdmin doctor = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        doctor.setStatus("APPROVED");
        doctor.setReviewedDate(Instant.now());
        doctor.setReviewedBy("ADMIN");
        MedisphereAdmin savedDoctor = adminRepository.save(doctor);

        // External Calls - Note: Sends the already encoded password to Auth Service
        externalServiceClient.createAuthUser(doctor.getEmail(), doctor.getPassword());
        externalServiceClient.sendNotification(doctor.getEmail(), "Your doctor registration has been approved!");

        return savedDoctor;
    }

    @Transactional
    public MedisphereAdmin rejectDoctor(Integer id, ApprovalRequestDTO rejectionRequest) {
        MedisphereAdmin doctor = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        doctor.setStatus("REJECTED");
        doctor.setRejectionReason(rejectionRequest.getRejectionReason());
        doctor.setReviewedDate(Instant.now());
        doctor.setReviewedBy("ADMIN");
        MedisphereAdmin savedDoctor = adminRepository.save(doctor);

        // External Call
        externalServiceClient.sendNotification(doctor.getEmail(), "Your doctor registration has been rejected. Reason: " + rejectionRequest.getRejectionReason());

        return savedDoctor;
    }

    public Map<String, Long> getDoctorReport() {
        Map<String, Long> report = new HashMap<>();
        report.put("total", adminRepository.count());
        report.put("pending", adminRepository.countByStatus("PENDING"));
        report.put("approved", adminRepository.countByStatus("APPROVED"));
        report.put("rejected", adminRepository.countByStatus("REJECTED"));
        return report;
    }
}
