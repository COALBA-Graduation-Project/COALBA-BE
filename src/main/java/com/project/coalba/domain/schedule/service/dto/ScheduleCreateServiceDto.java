package com.project.coalba.domain.schedule.service.dto;

import com.project.coalba.domain.profile.entity.Staff;
import com.project.coalba.domain.schedule.entity.Schedule;
import com.project.coalba.domain.workspace.entity.Workspace;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ScheduleCreateServiceDto {
    private Long workspaceId;
    private Long staffId;
    private LocalDateTime scheduleStartDateTime;
    private LocalDateTime scheduleEndDateTime;
    private Integer hourlyWage;

    public Schedule toEntity(Workspace workspace, Staff staff) {
        return Schedule.builder()
                .workspace(workspace)
                .staff(staff)
                .scheduleStartDateTime(scheduleStartDateTime)
                .scheduleEndDateTime(scheduleEndDateTime)
                .hourlyWage(hourlyWage)
                .build();
    }
}
