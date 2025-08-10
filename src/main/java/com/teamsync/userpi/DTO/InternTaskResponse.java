package com.teamsync.userpi.DTO;

import com.teamsync.userpi.entity.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class InternTaskResponse {
    private String id;
    private String internId;
    private LocalDate taskDate;
    private String description;
    private TaskStatus status;
}
