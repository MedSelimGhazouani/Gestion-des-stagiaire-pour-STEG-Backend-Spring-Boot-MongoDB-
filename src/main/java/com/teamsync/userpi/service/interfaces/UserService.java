package com.teamsync.userpi.service.interfaces;

import com.teamsync.userpi.DTO.InternRegisterDto;
import com.teamsync.userpi.DTO.LoginDto;
import com.teamsync.userpi.entity.ChatMessage;
import com.teamsync.userpi.entity.User;
import com.teamsync.userpi.exception.UserCollectionException;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UserService {
//    public User createUser(User user) throws UserCollectionException ;
//    public List<User> getAllUsers() ;
//    public User getSingleUser(String id) throws UserCollectionException;
//    public void updateUser(String id,User user) throws UserCollectionException;
//    public void deleteUserById(String id) throws UserCollectionException;
//    User loginUser(String email, String password) throws UserCollectionException;
//    void blockUserById(String id) throws UserCollectionException;
//    void unblockUserById(String id) throws UserCollectionException;
//    void initiatePasswordReset(String email) throws UserCollectionException;
//
//    void resetPassword(String token, String newPassword) throws UserCollectionException;
//    void saveChatMessage(ChatMessage message);
//    public List<ChatMessage> getChatHistory(String userId);
void registerIntern(InternRegisterDto internDto) throws IOException;

    List<User> getPendingInterns();

    ResponseEntity<Map<String, String>> verifyIntern(String internId);
    User getUserById(String id);

    ResponseEntity<?> login(LoginDto dto);
    List<User> getAllInterns();
    ResponseEntity<User> getLoggedInInternProfile(User sessionUser);
    public void resetPassword(String token, String newPassword) throws UserCollectionException ;
    public void initiatePasswordReset(String email) throws UserCollectionException ;
    List<User> getCertificateRequests();
    User updateIntern(String id, User updatedUser, MultipartFile profileImage) throws IOException;





}
