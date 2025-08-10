package com.teamsync.userpi.repository;

import com.teamsync.userpi.entity.Message;
import com.teamsync.userpi.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
            String senderId1, String receiverId1,
            String senderId2, String receiverId2);

    // All messages involving a user (for inbox builds), newest first
    List<Message> findBySenderIdOrReceiverIdOrderByTimestampDesc(String userId, String userIdAgain);

    // Unread count from one user to another
    long countBySenderIdAndReceiverIdAndReadIsFalse(String senderId, String receiverId);

    // Bulk find unread from partner to current user
    List<Message> findBySenderIdAndReceiverIdAndReadIsFalse(String senderId, String receiverId);
}
