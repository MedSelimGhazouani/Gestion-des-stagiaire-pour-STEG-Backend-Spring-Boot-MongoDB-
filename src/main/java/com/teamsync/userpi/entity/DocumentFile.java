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
@Document(collection = "documents")
public class DocumentFile {
    @Id
    private String id;

    private String uploadedByUserId;
    private String fileName;
    private String fileUrl;
    private String type; // e.g., "CV", "Demande", "Rapport"
    private Date uploadedAt;
}
