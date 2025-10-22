package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.controllers;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.CertificateDTO;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.User;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.CertificateRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.UserRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services.CertificateService;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateRepository certificateRepository;
    private final CertificateService certificateService;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<CertificateDTO>> getUserCertificates() {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(certificateService.getUserCertificates(user));
    }

    // get certificate by a specific streak like for 2 days or 3 days
    @GetMapping("/streak/{streak}")
    public ResponseEntity<List<CertificateDTO>> getCertificatesByStreak(@PathVariable int streak) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(certificateService.getCertificatesByUserAndStreak(user, streak));
    }

    // force to generate or manually generate certificate for specific streak
    @PostMapping("/generate/{streak}")
    public ResponseEntity<String> generateCertificates(@PathVariable int streak) {
        certificateService.generateCertificatesForStreakMilestones(streak);
        return ResponseEntity.ok("Certificate generation started for " + streak + "-day streaks");
    }

    // force to migrate(make change in existing certificate) existing ceritificates
    @PostMapping("/migrate")
    public ResponseEntity<String> migrateCertificates() {
        certificateService.migrateExistingCertificates();
        return ResponseEntity.ok("Certificate migration completed");
    }

    @DeleteMapping("/all")
    public String deleteAllCertificates() {
        certificateRepository.deleteAll();
        return "All certificates deleted";
    }
}