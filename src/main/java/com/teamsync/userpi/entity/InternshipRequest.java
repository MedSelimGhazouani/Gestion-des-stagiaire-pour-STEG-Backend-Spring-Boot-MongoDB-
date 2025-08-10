package com.teamsync.userpi.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "internship_requests")
public class InternshipRequest {
    @Id
    private String id;

    private String internId;

    private String department;
    private String specialty;
    private String level;
    private String internshipType;

    private Date startDate;
    private Date endDate;

    private RequestStatus status; // PENDING, APPROVED, REJECTED
    private String feedback; // from admin if rejected
}
