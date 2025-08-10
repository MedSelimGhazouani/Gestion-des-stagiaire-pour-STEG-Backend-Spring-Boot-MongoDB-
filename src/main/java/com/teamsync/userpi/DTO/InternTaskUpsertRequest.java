package com.teamsync.userpi.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InternTaskUpsertRequest {
    private String description;   // may be blank/cleared
    private String status;        // PENDING | IN_PROGRESS | COMPLETED (case-insensitive)
}
