package com.teamsync.userpi.service.impl;

import com.teamsync.userpi.DTO.InternTaskResponse;
import com.teamsync.userpi.DTO.InternTaskUpsertRequest;
import com.teamsync.userpi.entity.InternTask;
import com.teamsync.userpi.entity.TaskStatus;
import com.teamsync.userpi.entity.User;
import com.teamsync.userpi.repository.InternTaskRepository;
import com.teamsync.userpi.repository.UserRepository;
import com.teamsync.userpi.service.interfaces.InternTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InternTaskServiceImpl implements InternTaskService {

    private final InternTaskRepository taskRepo;
    private final UserRepository userRepo;

    @Override
    public InternTaskResponse upsertTask(String internId, LocalDate date, InternTaskUpsertRequest req) {
        // Validate intern
        User intern = userRepo.findById(internId)
                .orElseThrow(() -> new RuntimeException("Intern not found: " + internId));

        // Normalize status
        TaskStatus status = TaskStatus.PENDING;
        if (StringUtils.hasText(req.getStatus())) {
            status = TaskStatus.valueOf(req.getStatus().trim().toUpperCase(Locale.ROOT));
        }

        // Find existing
        InternTask task = taskRepo.findByInternIdAndTaskDate(internId, date)
                .orElse(InternTask.builder()
                        .internId(internId)
                        .taskDate(date)
                        .build());

        task.setDescription(req.getDescription());
        task.setStatus(status);
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(Instant.now());
        }
        task.setUpdatedAt(Instant.now());

        InternTask saved = taskRepo.save(task);
        return toResponse(saved);
    }
    @Override
    public List<InternTask> getTasksForIntern(String internId, LocalDate from, LocalDate to) {
        return taskRepo.findByInternIdAndTaskDateBetween(internId, from, to);
    }
    @Override
    public List<InternTaskResponse> getTasksInRange(String internId, LocalDate start, LocalDate end) {
        return taskRepo.findByInternIdAndTaskDateBetween(internId, start, end)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTask(String taskId) {
        taskRepo.deleteById(taskId);
    }

    private InternTaskResponse toResponse(InternTask t) {
        return InternTaskResponse.builder()
                .id(t.getId())
                .internId(t.getInternId())
                .taskDate(t.getTaskDate())
                .description(t.getDescription())
                .status(t.getStatus())
                .build();
    }

    @Override
    public List<InternTask> getTasksByIntern(String internId) {
        return taskRepo.findByInternId(internId);
    }
    @Override
    public List<InternTask> getTasksByMonth(String internId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return taskRepo.findByInternId(internId).stream()
                .filter(task -> !task.getTaskDate().isBefore(start) && !task.getTaskDate().isAfter(end))
                .toList();
    }
}
