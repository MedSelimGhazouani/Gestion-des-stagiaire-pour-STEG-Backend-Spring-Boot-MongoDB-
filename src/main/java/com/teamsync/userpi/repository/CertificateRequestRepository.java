package com.teamsync.userpi.repository;

import com.teamsync.userpi.entity.CertificateRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CertificateRequestRepository extends MongoRepository<CertificateRequest, String> {
    List<CertificateRequest> findByInternId(String internId);
}
