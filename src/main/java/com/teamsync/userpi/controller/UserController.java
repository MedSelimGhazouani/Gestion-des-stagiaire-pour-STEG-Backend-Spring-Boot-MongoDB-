package com.teamsync.userpi.controller;

import com.teamsync.userpi.DTO.InternRegisterDto;
import com.teamsync.userpi.DTO.LoginDto;
import com.teamsync.userpi.entity.CertificateRequest;
import com.teamsync.userpi.entity.Role;
import com.teamsync.userpi.entity.User;
import com.teamsync.userpi.exception.UserCollectionException;
import com.teamsync.userpi.repository.ChatMessageRepository;
import com.teamsync.userpi.repository.UserRepository;
import com.teamsync.userpi.service.CloudinaryService;
import com.teamsync.userpi.service.EmailService;
import com.teamsync.userpi.service.FileStorageService;
import com.teamsync.userpi.service.JwtService;
import com.teamsync.userpi.service.interfaces.CertificateRequestService;
import com.teamsync.userpi.service.interfaces.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final JwtService jwtService;
    private final EmailService emailService;
    private final CloudinaryService cloudinaryService;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final ChatClient chatClient;

    // Injected to expose optional certificate request convenience endpoints:
    private final CertificateRequestService certificateRequestService;

    // ----------------------------------------------------------------
    // Intern Registration / Verification / Login
    // ----------------------------------------------------------------

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Backend is up and running");
    }


    @PostMapping("/register-intern")
    public ResponseEntity<?> registerIntern(
            @ModelAttribute InternRegisterDto internDto) {
        try {
            userService.registerIntern(internDto);
            return ResponseEntity.ok("Intern registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }


    /** Admin: fetch interns awaiting verification (status = INACTIVE). */
    @GetMapping("/pending-interns")
    public List<User> getPendingInterns() {
        return userService.getPendingInterns();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto, HttpSession session) {
        ResponseEntity<?> response = userService.login(dto);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof User user) {
            session.setAttribute("user", user);
            log.debug("User {} stored in session.", user.getId());
        }
        return response;
    }

    /** Admin: all interns (regardless of status). */
    @GetMapping("/all-interns")
    public ResponseEntity<List<User>> getAllInterns() {
        return ResponseEntity.ok(userService.getAllInterns());
    }

    /** Admin verifies an intern account (activates). */
    @PostMapping("/verify-intern/{id}")
    public ResponseEntity<Map<String, String>> verifyIntern(@PathVariable String id) {
        return userService.verifyIntern(id);
    }

    /** Return the currently logged‑in user from session (intern/admin). */
    @GetMapping("/profile")
    public ResponseEntity<User> getLoggedInUser(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(sessionUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }


    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return ResponseEntity.ok(Map.of("message", "Logout successful."));
    }

    // ----------------------------------------------------------------
    // Password Reset
    // ----------------------------------------------------------------

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        try {
            userService.initiatePasswordReset(email);
            return ResponseEntity.ok(Map.of("message", "Reset email sent successfully."));
        } catch (UserCollectionException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password reset successful."));
        } catch (UserCollectionException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    // Certificate Convenience Endpoints (Optional)
    // ----------------------------------------------------------------
    // These delegate to CertificateRequestService so Angular can stay
    // within /api/users if you prefer. Remove if not needed.

    /**
     * Admin: get users who have submitted certificate requests.
     * (Your original userService.getCertificateRequests() returned List<User>;
     *  keep it if you want a user‑centric view.)
     */
    @GetMapping("/certificate-requests/users")
    public ResponseEntity<List<User>> getUsersWithCertificateRequests() {
        List<User> requests = userService.getCertificateRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * Admin: all certificate requests (delegates to certificate service).
     */
    @GetMapping("/certificate-requests/all")
    public ResponseEntity<List<CertificateRequest>> allCertificateRequests() {
        return ResponseEntity.ok(certificateRequestService.getAllRequests());
    }

    /**
     * Intern/Admin: get certificate requests by internId (delegates).
     */
    @GetMapping("/certificate-requests/intern/{internId}")
    public ResponseEntity<List<CertificateRequest>> getCertificateRequestsByIntern(@PathVariable String internId) {
        return ResponseEntity.ok(certificateRequestService.getRequestsByInternId(internId));
    }

    // ----------------------------------------------------------------
    // (Optionally: endpoints to approve/reject via users path)
    // ----------------------------------------------------------------

    @PutMapping("/certificate-requests/approve/{id}")
    public ResponseEntity<CertificateRequest> approveCertificateViaUsers(@PathVariable String id) {
        return ResponseEntity.ok(certificateRequestService.approveRequest(id));
    }

    @PutMapping("/certificate-requests/reject/{id}")
    public ResponseEntity<CertificateRequest> rejectCertificateViaUsers(@PathVariable String id) {
        return ResponseEntity.ok(certificateRequestService.rejectRequest(id));
    }
    @GetMapping("/admin")
    public ResponseEntity<User> getFirstAdmin() {
        return ResponseEntity.ok(
                userRepository.findFirstByRole("ADMIN")
                        .orElseThrow(() -> new RuntimeException("No admin account found"))
        );
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateInternProfile(
            @PathVariable String id,
            @RequestPart("user") User updatedUser,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        try {
            Optional<User> existingUserOptional = userRepository.findById(id);
            if (existingUserOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Intern not found.");
            }

            User user = existingUserOptional.get();

            // Update text fields
            user.setFullName(updatedUser.getFullName());
            user.setPhone(updatedUser.getPhone());
            user.setUniversity(updatedUser.getUniversity());
            user.setSpecialty(updatedUser.getSpecialty());
            user.setDepartment(updatedUser.getDepartment());
            user.setLevel(updatedUser.getLevel());
            user.setInternshipType(updatedUser.getInternshipType());
            user.setStartDate(updatedUser.getStartDate());
            user.setEndDate(updatedUser.getEndDate());

            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(imageFile); // can throw IOException
                user.setProfileImageUrl(imageUrl);
            }

            userRepository.save(user);

            return ResponseEntity.ok(user);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload image: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating intern profile: " + e.getMessage());
        }
    }



}
