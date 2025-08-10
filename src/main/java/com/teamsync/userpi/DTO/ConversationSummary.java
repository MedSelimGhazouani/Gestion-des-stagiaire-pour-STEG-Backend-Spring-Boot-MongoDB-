package com.teamsync.userpi.DTO;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ConversationSummary {
    String partnerId;
    String partnerName;
    String partnerEmail;
    String partnerPhotoUrl;

    String lastMessagePreview;
    Instant lastMessageTime;
    boolean lastMessageFromPartner;
    long unreadCount; // messages from partner not read by current user
}
