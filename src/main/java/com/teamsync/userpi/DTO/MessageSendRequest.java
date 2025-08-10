package com.teamsync.userpi.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MessageSendRequest {
    private String senderId;
    private String receiverId;
    private String content;
}
