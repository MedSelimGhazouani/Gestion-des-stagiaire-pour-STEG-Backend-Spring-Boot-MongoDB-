package com.teamsync.userpi.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Document(collection = "intern_tasks")
@CompoundIndex(name = "intern_date_idx", def = "{'internId': 1, 'taskDate': 1}", unique = true)
public class InternTask {

    @Id
    private String id;

    private String internId;          // FK -> User

    private LocalDate taskDate;       // day represented on the calendar

    private String description;

    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    /** audit */
    @Builder.Default
    private Instant createdAt = Instant.now();
    private Instant updatedAt;
}
