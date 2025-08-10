package com.teamsync.userpi.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Simple point-to-point message between two users (intern/admin/etc.).
 * Extend later for attachments, threads, soft delete, etc.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    @Indexed
    private String senderId;

    @Indexed
    private String receiverId;

    private String content;

    @Builder.Default
    private Instant timestamp = Instant.now();

    @Builder.Default
    private boolean read = false;
}
