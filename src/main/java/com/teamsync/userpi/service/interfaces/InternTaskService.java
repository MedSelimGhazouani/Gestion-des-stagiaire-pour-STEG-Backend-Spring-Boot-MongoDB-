package com.teamsync.userpi.service.interfaces;

import com.teamsync.userpi.DTO.InternTaskResponse;
import com.teamsync.userpi.DTO.InternTaskUpsertRequest;
import com.teamsync.userpi.entity.InternTask;

import java.time.LocalDate;
import java.util.List;

public interface InternTaskService {

    /**
     * Create or update the intern's task for the given date.
     */
    InternTaskResponse upsertTask(String internId, LocalDate date, InternTaskUpsertRequest req);

    /**
     * Get all tasks for an intern in [start,end] (inclusive).
     */
    List<InternTaskResponse> getTasksInRange(String internId, LocalDate start, LocalDate end);
    List<InternTask> getTasksForIntern(String internId, LocalDate from, LocalDate to);

    /**
     * Delete a task by id (optional admin tool / intern clearing).
     */
    void deleteTask(String taskId);
    List<InternTask> getTasksByIntern(String internId);
    List<InternTask> getTasksByMonth(String internId, int year, int month);
}
