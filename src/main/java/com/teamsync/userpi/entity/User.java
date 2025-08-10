package com.teamsync.userpi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @NotNull(message = "Full name is required")
    private String fullName;

    @Email
    @NotNull(message = "Email is required")
    @Indexed(unique = true)
    private String email;

    @NotNull(message = "Password is required")
    private String password;

    private String phone; // 8-digit expected

    private String profileImageUrl; // For uploaded image preview

    private Role role; // INTERN or ADMIN
    private Status status = Status.ACTIVE; // Default active

    private String department; // e.g., "IT", "HR"
    private String specialty;  // e.g., "Software Engineering"
    private String institution; // e.g., "ESPRIT"
    private String university;  // Needed for certificates

    private String level; // e.g., "3rd year"
    private String internshipType; // e.g., "PFE", "Summer"

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date endDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate hireDate; // Optional, if needed for HR flow

    private String assignedSupervisorId; // Admin ID

    private String resetToken; // For password reset flow

    private boolean certificateRequested = false;
    private boolean hasRequestedCertificate = false;
}
