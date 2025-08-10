package com.teamsync.userpi.service.interfaces;

import com.teamsync.userpi.DTO.ConversationSummary;
import com.teamsync.userpi.DTO.MessageResponse;
import com.teamsync.userpi.entity.Message;

import java.util.List;

public interface MessageService {

    MessageResponse sendMessage(String senderId, String receiverId, String content);

    List<MessageResponse> getConversation(String userA, String userB);

    List<ConversationSummary> getInbox(String userId);

    void markConversationRead(String currentUserId, String partnerId);

    void deleteMessage(String messageId); // optional basic delete
}
