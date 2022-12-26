package com.project.coalba.domain.schedule.controller;

import com.project.coalba.domain.schedule.service.dto.ScheduleCreateServiceDto;
import com.project.coalba.domain.schedule.dto.request.ScheduleRequest;
import com.project.coalba.domain.schedule.dto.response.BossHomeScheduleResponse;
import com.project.coalba.domain.schedule.dto.response.BossWorkspaceScheduleResponse;
import com.project.coalba.domain.schedule.entity.Schedule;
import com.project.coalba.domain.schedule.mapper.ScheduleMapper;
import com.project.coalba.domain.schedule.service.BossScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/boss/schedules")
@RequiredArgsConstructor
@RestController
public class BossScheduleController {

    private final BossScheduleService bossScheduleService;
    private final ScheduleMapper mapper;

    @GetMapping("/home/selected")
    public BossHomeScheduleResponse getHomeScheduleList(@RequestParam Long workspaceId,
                                                        @RequestParam int year, @RequestParam int month, @RequestParam int day) {
        LocalDate selectedDate = LocalDate.of(year, month, day);
        List<Schedule> homeScheduleList = bossScheduleService.getHomeScheduleList(workspaceId, selectedDate);
        return mapper.toDto(selectedDate, workspaceId, () -> homeScheduleList);
    }

    @GetMapping("/workspaces/{workspaceId}/selected")
    public BossWorkspaceScheduleResponse getWorkspaceScheduleList(@PathVariable Long workspaceId,
                                                                  @RequestParam int year, @RequestParam int month, @RequestParam int day) {
        LocalDate selectedDate = LocalDate.of(year, month, day);
        List<Schedule> workspaceScheduleList = bossScheduleService.getWorkspaceScheduleList(workspaceId, selectedDate);
        return mapper.toDto(day, () -> workspaceScheduleList);
    }

    @PostMapping
    public ResponseEntity<Void> saveSchedule(@Validated @RequestBody ScheduleRequest scheduleRequest) {
        ScheduleCreateServiceDto serviceDto = mapper.toServiceDto(scheduleRequest);
        bossScheduleService.save(serviceDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("{scheduleId}")
    public ResponseEntity<Void> cancelSchedule(@PathVariable Long scheduleId) {
        bossScheduleService.cancel(scheduleId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
