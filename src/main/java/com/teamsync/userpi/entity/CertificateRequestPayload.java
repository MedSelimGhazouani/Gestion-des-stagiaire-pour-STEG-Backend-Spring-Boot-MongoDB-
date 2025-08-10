package com.teamsync.userpi.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificateRequestPayload {
    private String type;
    private String reason;
}
