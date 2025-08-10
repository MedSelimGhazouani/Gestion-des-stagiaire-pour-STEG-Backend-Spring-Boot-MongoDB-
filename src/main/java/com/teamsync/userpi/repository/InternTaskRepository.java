package com.teamsync.userpi.repository;

import com.teamsync.userpi.entity.InternTask;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InternTaskRepository extends MongoRepository<InternTask, String> {

    Optional<InternTask> findByInternIdAndTaskDate(String internId, LocalDate taskDate);

    List<InternTask> findByInternIdAndTaskDateBetween(String internId, LocalDate start, LocalDate end);
    List<InternTask> findByInternId(String internId);
}
