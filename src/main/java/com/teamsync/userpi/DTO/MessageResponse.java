package com.teamsync.userpi.DTO;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class MessageResponse {
    String id;
    String senderId;
    String senderName;
    String receiverId;
    String receiverName;
    String content;
    Instant timestamp;
    boolean read;
}
