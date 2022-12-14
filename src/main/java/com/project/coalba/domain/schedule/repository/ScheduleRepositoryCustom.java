package com.project.coalba.domain.schedule.repository;

import com.project.coalba.domain.schedule.entity.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepositoryCustom {

    List<Schedule> findAllByStaffIdAndDateTimeRangeAndEndStatus(Long staffId, LocalDateTime fromDateTime, LocalDateTime toDateTime);
    List<Schedule> findAllByWorkspaceIdAndDateTimeRangeAndEndStatus(Long workspaceId, LocalDateTime fromDateTime, LocalDateTime toDateTime);
    List<Schedule> findAllByStaffIdAndDateFetch(Long staffId, LocalDate date);
    List<Schedule> findAllByWorkspaceIdAndDateFetch(Long workspaceId, LocalDate date);
    List<Schedule> findAllByStaffIdAndDateRange(Long staffId, LocalDate fromDate, LocalDate toDate);
    List<Schedule> findAllByWorkspaceIdAndDateRange(Long workspaceId, LocalDate fromDate, LocalDate toDate);
    List<Schedule> findAllByWorkspaceIdsAndDateRange(List<Long> workspaceIds, LocalDate fromDate, LocalDate toDate);
}
