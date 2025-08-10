package com.teamsync.userpi.controller;

import com.teamsync.userpi.entity.CertificateRequest;
import com.teamsync.userpi.entity.CertificateRequestPayload;
import com.teamsync.userpi.service.interfaces.CertificateRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class CertificateRequestController {

    private final CertificateRequestService service;

    @PostMapping("/request/{internId}")
    public ResponseEntity<CertificateRequest> requestCertificate(
            @PathVariable String internId,
            @RequestBody CertificateRequestPayload payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.requestCertificate(internId, payload));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CertificateRequest>> getAllRequests() {
        return ResponseEntity.ok(service.getAllRequests());
    }

    @GetMapping("/intern/{internId}")
    public ResponseEntity<List<CertificateRequest>> getByIntern(@PathVariable String internId) {
        return ResponseEntity.ok(service.getRequestsByInternId(internId));
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<CertificateRequest> approveRequest(@PathVariable String id) {
        return ResponseEntity.ok(service.approveRequest(id));
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<CertificateRequest> rejectRequest(@PathVariable String id) {
        return ResponseEntity.ok(service.rejectRequest(id));
    }

    @PutMapping("/update-status/{id}")
    public ResponseEntity<CertificateRequest> updateStatus(@PathVariable String id, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(service.updateStatus(id, payload.get("status")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable String id) {
        service.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
}
