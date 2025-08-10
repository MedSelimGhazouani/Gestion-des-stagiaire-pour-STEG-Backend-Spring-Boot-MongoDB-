package com.teamsync.userpi.controller;


import com.teamsync.userpi.DTO.ConversationSummary;
import com.teamsync.userpi.DTO.MessageResponse;
import com.teamsync.userpi.DTO.MessageSendRequest;
import com.teamsync.userpi.service.interfaces.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class MessageController {

    private final MessageService messageService;

    /**
     * Send a message (intern → admin, admin → intern, etc.).
     */
    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody MessageSendRequest req) {
        MessageResponse created = messageService.sendMessage(
                req.getSenderId(),
                req.getReceiverId(),
                req.getContent()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get full conversation (sorted oldest→newest).
     */
    @GetMapping("/conversation/{userA}/{userB}")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @PathVariable String userA,
            @PathVariable String userB) {
        return ResponseEntity.ok(messageService.getConversation(userA, userB));
    }

    /**
     * Inbox for a given user: one summary per conversation partner.
     */
    @GetMapping("/inbox/{userId}")
    public ResponseEntity<List<ConversationSummary>> getInbox(@PathVariable String userId) {
        return ResponseEntity.ok(messageService.getInbox(userId));
    }

    /**
     * Mark all messages FROM {partnerId} TO {currentUserId} as read.
     */
    @PutMapping("/read/{currentUserId}/{partnerId}")
    public ResponseEntity<Void> markConversationRead(
            @PathVariable String currentUserId,
            @PathVariable String partnerId) {
        messageService.markConversationRead(currentUserId, partnerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a single message (admin tool, or future feature).
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/conversations/{userId}")   // <-- alias for inbox
    public ResponseEntity<List<ConversationSummary>> getConversationsAlias(@PathVariable String userId) {
        return ResponseEntity.ok(messageService.getInbox(userId));
    }

    @GetMapping("/thread")
    public ResponseEntity<List<MessageResponse>> getThreadAlias(
            @RequestParam String user1,
            @RequestParam String user2) {
        return ResponseEntity.ok(messageService.getConversation(user1, user2));
    }

}
