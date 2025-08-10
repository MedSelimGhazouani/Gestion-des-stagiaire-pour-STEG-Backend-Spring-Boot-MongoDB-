package com.teamsync.userpi.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "certificate_requests")
public class CertificateRequest {

    @Id
    private String id;

    private String internId;  // Reference to User
    private String internName; // For quick display
    private String type;       // e.g., Internship Completion
    private String reason;

    @Builder.Default
    private CertificateStatus status = CertificateStatus.REQUESTED;
    private Instant requestedAt = Instant.now();
    private Instant updatedAt;
}
