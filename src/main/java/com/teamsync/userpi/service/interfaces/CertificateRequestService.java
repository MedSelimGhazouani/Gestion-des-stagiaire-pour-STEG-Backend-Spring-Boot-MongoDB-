package com.teamsync.userpi.service.interfaces;

import com.teamsync.userpi.entity.CertificateRequest;
import com.teamsync.userpi.entity.CertificateRequestPayload;

import java.util.List;

public interface CertificateRequestService {
    CertificateRequest requestCertificate(String internId, CertificateRequestPayload payload);
    List<CertificateRequest> getAllRequests();
    List<CertificateRequest> getRequestsByInternId(String internId);
    CertificateRequest approveRequest(String requestId);
    CertificateRequest rejectRequest(String requestId);
    void deleteRequest(String requestId);
    public CertificateRequest updateStatus(String id, String status);

    }
