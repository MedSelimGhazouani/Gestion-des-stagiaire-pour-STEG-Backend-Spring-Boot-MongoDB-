package com.teamsync.userpi.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Data
public class InternRegisterDto {
    private String fullName;
    private String email;
    private String password;

    private String phone;
    private String specialty;
    private String university;

    private Date startDate;
    private Date endDate;
    private MultipartFile photo;  // ✅ New

}



