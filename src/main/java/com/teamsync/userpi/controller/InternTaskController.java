package com.teamsync.userpi.controller;

import com.teamsync.userpi.DTO.InternTaskResponse;
import com.teamsync.userpi.DTO.InternTaskUpsertRequest;
import com.teamsync.userpi.entity.InternTask;
import com.teamsync.userpi.service.interfaces.InternTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class InternTaskController {

    private final InternTaskService internTaskService;

    /**
     * Upsert a task for a given intern+date.
     * Example: POST /api/tasks/intern/123/2025-07-25
     */
    @PostMapping("/intern/{internId}/{date}")
    public ResponseEntity<InternTaskResponse> upsertTask(
            @PathVariable String internId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody InternTaskUpsertRequest req) {

        InternTaskResponse saved = internTaskService.upsertTask(internId, date, req);
        return ResponseEntity.ok(saved);
    }

    /**
     * Get tasks for an intern in a date range (inclusive).
     * Example: GET /api/tasks/intern/123/range?from=2025-07-01&to=2025-07-31
     */
    @GetMapping("/intern/{internId}/range")
    public ResponseEntity<List<InternTaskResponse>> getTasksInRange(
            @PathVariable String internId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(internTaskService.getTasksInRange(internId, from, to));
    }

    /**
     * Get all tasks for a specific intern (no date filter).
     * Example: GET /api/tasks/intern/123/all
     */
    @GetMapping("/intern/{internId}/all")
    public ResponseEntity<List<InternTask>> getAllTasksForIntern(@PathVariable String internId) {
        return ResponseEntity.ok(internTaskService.getTasksByIntern(internId));
    }

    /**
     * Get tasks by month.
     * Example: GET /api/tasks/intern/123/month/2025/07
     */
    @GetMapping("/intern/{internId}/month/{year}/{month}")
    public ResponseEntity<List<InternTask>> getTasksByMonth(
            @PathVariable String internId,
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(internTaskService.getTasksByMonth(internId, year, month));
    }

    /**
     * Delete a task by id.
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskId) {
        internTaskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
