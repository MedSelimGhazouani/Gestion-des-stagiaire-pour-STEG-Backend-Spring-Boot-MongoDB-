package com.teamsync.userpi.service.impl;

import com.teamsync.userpi.DTO.InternRegisterDto;
import com.teamsync.userpi.DTO.LoginDto;
import com.teamsync.userpi.entity.Role;
import com.teamsync.userpi.entity.Status;
import com.teamsync.userpi.entity.User;
import com.teamsync.userpi.exception.UserCollectionException;
import com.teamsync.userpi.repository.UserRepository;
import com.teamsync.userpi.service.CloudinaryService;
import com.teamsync.userpi.service.EmailService;
import com.teamsync.userpi.service.interfaces.UserService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.mail.MessagingException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceIMPL implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CloudinaryService cloudinaryService;

    // ✅ Register Intern
    @Override
    public void registerIntern(InternRegisterDto dto) throws IOException {
        String imageUrl = null;

        // Upload image if present
        if (dto.getPhoto() != null && !dto.getPhoto().isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(dto.getPhoto());
        }

        User intern = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .specialty(dto.getSpecialty())
                .university(dto.getUniversity())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .profileImageUrl(imageUrl)
                .role(Role.INTERN)
                .status(Status.INACTIVE)
                .build();

        userRepository.save(intern);
    }

    // ✅ Fetch All Pending Interns
    public List<User> getPendingInterns() {
        return userRepository.findByRoleAndStatus(Role.INTERN, Status.INACTIVE);
    }

    // ✅ Verify Intern and Send Email
    @Override
    public ResponseEntity<Map<String, String>> verifyIntern(String internId) {
        Optional<User> optIntern = userRepository.findById(internId);
        if (optIntern.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Intern not found."));
        }

        User intern = optIntern.get();
        if (intern.getRole() != Role.INTERN) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "User is not an intern."));
        }

        intern.setStatus(Status.ACTIVE);
        userRepository.save(intern);

        try {
            String subject = "✅ Your Internship Account Has Been Verified";
            String body = "Hello " + intern.getFullName() + ",\n\n" +
                    "🎉 Your internship account has been verified by the STEG administration.\n\n" +
                    "You can now log in to the platform using your credentials:\n" +
                    "🔗 http://localhost:4200/login\n\n" +
                    "Best regards,\nSTEG Internship Team";

            emailService.sendEmail(intern.getEmail(), subject, body);
        } catch (MessagingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Failed to send verification email."));
        }

        return ResponseEntity.ok(Collections.singletonMap("message", "Intern verified and notified via email."));
    }

    // ✅ Login (only for ACTIVE accounts)
    public ResponseEntity<?> login(LoginDto dto) {
        Optional<User> optUser = userRepository.findByEmail(dto.getEmail());
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
        }

        User user = optUser.get();
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
        }

        if (user.getStatus() != Status.ACTIVE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account not verified yet.");
        }

        return ResponseEntity.ok(user);
    }

    // ✅ Get all interns
    @Override
    public List<User> getAllInterns() {
        return userRepository.findByRole(Role.INTERN);
    }

    @Override
    public ResponseEntity<User> getLoggedInInternProfile(User sessionUser) {
        if (sessionUser == null || sessionUser.getRole() != com.teamsync.userpi.entity.Role.INTERN) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOptional = userRepository.findById(sessionUser.getId());
        return userOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }



    @Override
    public void initiatePasswordReset(String email) throws UserCollectionException {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new UserCollectionException("User not found with email: " + email);
        }

        User user = optionalUser.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepository.save(user);

        String resetUrl = "http://localhost:4200/reset-password?token=" + token;

        String emailBody = """
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="UTF-8">
      <title>Reset Your Password</title>
    </head>
    <body style="margin:0; padding:0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f6f8;">
      <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); overflow: hidden;">
        <div style="background-color: #ffecb3; padding: 20px; text-align: center;">
          <h2 style="margin: 0; color: #f57c00;">🌞 Password Reset Request</h2>
        </div>
        <div style="padding: 30px;">
          <p style="font-size: 16px; color: #333;">Hello <strong>%s</strong>,</p>
          <p style="font-size: 16px; color: #333;">We received a request to reset your password.</p>
          <p style="text-align: center;">
            <a href="%s" 
               style="display: inline-block; padding: 12px 24px; margin-top: 20px; font-size: 16px; color: #fff; background-color: #ff9800; text-decoration: none; border-radius: 50px; font-weight: bold;">
              🔒 Reset Password
            </a>
          </p>
          <p style="font-size: 14px; color: #888; margin-top: 30px;">If you didn’t request this, you can safely ignore this email.</p>
          <p style="font-size: 14px; color: #888;">Thanks, <br> The TeamSync Team</p>
        </div>
        <div style="background-color: #f0f0f0; text-align: center; padding: 15px;">
          <small style="color: #888;">© 2025 STEG. All rights reserved.</small>
        </div>
      </div>
    </body>
    </html>
    """.formatted(user.getFullName(), resetUrl);
        try {
            emailService.sendEmail(user.getEmail(), "Reset Your Password", emailBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reset email", e);
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) throws UserCollectionException {
        Optional<User> optionalUser = userRepository.findByResetToken(token);
        if (optionalUser.isEmpty()) {
            throw new UserCollectionException("Invalid reset token");
        }

        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
    }

    @Override
    public List<User> getCertificateRequests() {
        return userRepository.findByRoleAndHasRequestedCertificateTrue("INTERN");
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }



    @Override
    public User updateIntern(String id, User updatedUser, MultipartFile profileImage) throws IOException {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }

        User user = existingUserOpt.get();
        // Update fields
        user.setEmail(updatedUser.getEmail());
        user.setPhone(updatedUser.getPhone());

        // Optional: upload new profile image
        if (profileImage != null && !profileImage.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(profileImage);
            user.setProfileImageUrl(imageUrl);
        }

        return userRepository.save(user);
    }
}
