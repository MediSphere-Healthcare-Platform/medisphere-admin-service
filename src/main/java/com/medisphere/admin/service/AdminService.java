package com.medisphere.admin.service;

import com.medisphere.admin.client.AuthClient;
import com.medisphere.admin.client.NotificationClient;
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
    private final AuthClient authClient;
    private final NotificationClient notificationClient;
    private final PasswordEncoder passwordEncoder;
    private final ExternalServiceClient externalServiceClient;

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

        // External Calls - Send pre-encoded password to Auth Service via Feign Client
        Map<String, String> authPayload = new HashMap<>();
        authPayload.put("email", doctor.getEmail());
        authPayload.put("password", doctor.getPassword());
        authPayload.put("role", "DOCTOR");
        
        // Pass extra details so Auth Service can synchronize with Doctor Service
        authPayload.put("firstName", doctor.getFirstName());
        authPayload.put("lastName", doctor.getLastName());
        authPayload.put("phone", doctor.getPhone() != null ? doctor.getPhone() : "N/A");
        authPayload.put("specialty", doctor.getSpecialty() != null ? doctor.getSpecialty() : "General");
        authPayload.put("licenseUrl", doctor.getLicenseUrl() != null ? doctor.getLicenseUrl() : "N/A");


        Map<String, Object> authResponse = authClient.createAuthUser(authPayload);
        
        // Check if user creation was successful
        String status = (String) authResponse.get("status");
        if ("FAILED".equalsIgnoreCase(status)) {
            String message = (String) authResponse.get("message");
            throw new RuntimeException("Failed to create Auth User: " + message);
        }
        
        String msUserId = (String) authResponse.get("data");

        // Send Approval Notification via Email
        try {
            Map<String, Object> doctorNotification = new HashMap<>();
            doctorNotification.put("userId", doctor.getEmail());
            doctorNotification.put("userRole", "DOCTOR");
            doctorNotification.put("title", "Medisphere Account Approved");
            String approvalMessage = String.format(
                "Dear Dr. %s,\n\n" +
                "We are pleased to inform you that your registration with Medisphere Healthcare Platform has been approved.\n\n" +
                "You can now access your account and start managing your profile and appointments.\n\n" +
                "Login Details:\n" +
                "- Username: %s\n" +
                "- Login URL: [Hospital Portal Link]\n\n" +
                "Please use the password you created during your registration. Welcome to our medical community!\n\n" +
                "Best regards,\n" +
                "Medisphere Team",
                doctor.getLastName(), doctor.getEmail()
            );
            doctorNotification.put("message", approvalMessage);
            doctorNotification.put("channel", "EMAIL");
            
            notificationClient.createNotification(doctorNotification);
        } catch (Exception notificationEx) {
            log.error("Failed to send approval notification to: {}", doctor.getEmail(), notificationEx);
        }

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

        // Revoke Access in Auth Service
        authClient.deleteAuthUser(doctor.getEmail());
        log.info("Access revoked in Auth Service for doctor: {}", doctor.getEmail());

        // Send Rejection Notification via Email
        try {
            Map<String, Object> doctorNotification = new HashMap<>();
            doctorNotification.put("userId", doctor.getEmail());
            doctorNotification.put("userRole", "DOCTOR");
            doctorNotification.put("title", "Update: Medisphere Registration Status");
            String rejectionMessage = String.format(
                "Dear Dr. %s,\n\n" +
                "Thank you for your interest in joining the Medisphere Healthcare Platform.\n\n" +
                "We have reviewed your application, and we regret to inform you that your registration has not been approved at this time.\n\n" +
                "Reason for Rejection: %s\n\n" +
                "If you believe this is an error or would like to provide additional information, please contact our support team.\n\n" +
                "Best regards,\n" +
                "Medisphere Team",
                doctor.getLastName(), rejectionRequest.getRejectionReason()
            );
            doctorNotification.put("message", rejectionMessage);
            doctorNotification.put("channel", "EMAIL");
            
            notificationClient.createNotification(doctorNotification);
        } catch (Exception notificationEx) {
            log.error("Failed to send rejection notification to: {}", doctor.getEmail(), notificationEx);
        }

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
