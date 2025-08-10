package com.teamsync.userpi.service.impl;

import com.teamsync.userpi.DTO.ConversationSummary;
import com.teamsync.userpi.DTO.MessageResponse;
import com.teamsync.userpi.entity.Message;
import com.teamsync.userpi.entity.User;
import com.teamsync.userpi.repository.MessageRepository;
import com.teamsync.userpi.repository.UserRepository;
import com.teamsync.userpi.service.interfaces.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepo;
    private final UserRepository userRepo;

    @Override
    public MessageResponse sendMessage(String senderId, String receiverId, String content) {
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("Message content cannot be empty.");
        }

        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found: " + senderId));
        User receiver = userRepo.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found: " + receiverId));

        Message saved = messageRepo.save(Message.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content.trim())
                .timestamp(Instant.now())
                .read(false)
                .build());

        return toResponse(saved, sender, receiver);
    }

    @Override
    public List<MessageResponse> getConversation(String userA, String userB) {
        List<Message> messages = messageRepo
                .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
                        userA, userB,
                        userB, userA);

        // Preload both ends (null allowed)
        User a = userRepo.findById(userA).orElse(null);
        User b = userRepo.findById(userB).orElse(null);

        return messages.stream()
                .map(m -> toResponse(m, a, b))
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationSummary> getInbox(String userId) {
        List<Message> all = messageRepo.findBySenderIdOrReceiverIdOrderByTimestampDesc(userId, userId);

        Map<String, List<Message>> grouped = all.stream().collect(Collectors.groupingBy(m ->
                userId.equals(m.getSenderId()) ? m.getReceiverId() : m.getSenderId()
        ));

        List<ConversationSummary> summaries = new ArrayList<>();

        grouped.forEach((partnerId, msgs) -> {
            msgs.sort(Comparator.comparing(Message::getTimestamp).reversed());
            Message latest = msgs.get(0);
            boolean latestFromPartner = partnerId.equals(latest.getSenderId());

            long unread = msgs.stream()
                    .filter(m -> m.getSenderId().equals(partnerId) && !m.isRead())
                    .count();

            User partner = userRepo.findById(partnerId).orElse(null);

            summaries.add(ConversationSummary.builder()
                    .partnerId(partnerId)
                    .partnerName(partner != null ? safe(partner.getFullName()) : "Unknown")
                    .partnerEmail(partner != null ? safe(partner.getEmail()) : "")
                    .partnerPhotoUrl(null) // add if you later add field
                    .lastMessagePreview(buildPreview(latest.getContent()))
                    .lastMessageTime(latest.getTimestamp())
                    .lastMessageFromPartner(latestFromPartner)
                    .unreadCount(unread)
                    .build());
        });

        summaries.sort(Comparator.comparing(ConversationSummary::getLastMessageTime,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return summaries;
    }

    @Override
    public void markConversationRead(String currentUserId, String partnerId) {
        List<Message> unread = messageRepo.findBySenderIdAndReceiverIdAndReadIsFalse(partnerId, currentUserId);
        if (unread.isEmpty()) return;
        unread.forEach(m -> m.setRead(true));
        messageRepo.saveAll(unread);
    }

    @Override
    public void deleteMessage(String messageId) {
        messageRepo.deleteById(messageId);
    }

    // ----------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------
    private MessageResponse toResponse(Message m, User a, User b) {
        String senderName = null;
        String receiverName = null;

        if (a != null && m.getSenderId().equals(a.getId())) senderName = a.getFullName();
        if (b != null && m.getSenderId().equals(b.getId())) senderName = b.getFullName();

        if (a != null && m.getReceiverId().equals(a.getId())) receiverName = a.getFullName();
        if (b != null && m.getReceiverId().equals(b.getId())) receiverName = b.getFullName();

        if (senderName == null) {
            senderName = userRepo.findById(m.getSenderId()).map(User::getFullName).orElse("Unknown");
        }
        if (receiverName == null) {
            receiverName = userRepo.findById(m.getReceiverId()).map(User::getFullName).orElse("Unknown");
        }

        return MessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSenderId())
                .senderName(senderName)
                .receiverId(m.getReceiverId())
                .receiverName(receiverName)
                .content(m.getContent())
                .timestamp(m.getTimestamp())
                .read(m.isRead())
                .build();
    }

    private String buildPreview(String content) {
        if (content == null) return "";
        return content.length() <= 40 ? content : content.substring(0, 40) + "...";
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
