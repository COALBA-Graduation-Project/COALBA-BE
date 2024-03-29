package com.project.coalba.domain.schedule.service;

import com.project.coalba.domain.schedule.entity.enums.TotalScheduleStatus;
import com.project.coalba.domain.schedule.service.dto.*;
import com.project.coalba.domain.schedule.entity.Schedule;
import com.project.coalba.domain.schedule.entity.enums.ScheduleStatus;
import com.project.coalba.domain.schedule.repository.ScheduleRepository;
import com.project.coalba.domain.workspace.entity.Workspace;
import com.project.coalba.domain.workspace.service.BossWorkspaceService;
import com.project.coalba.global.exception.*;
import com.project.coalba.global.utils.ProfileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.*;
import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Service
public class StaffScheduleService {
    private final BossWorkspaceService bossWorkspaceService;
    private final ScheduleRepository scheduleRepository;
    private final ProfileUtil profileUtil;

    @Transactional(readOnly = true)
    public StaffHomePageServiceDto getHomePage() {
        final int offset = 3;
        Long staffId = profileUtil.getCurrentStaff().getId();
        LocalDate now = LocalDate.now(), fromDate = now.minusDays(offset), toDate = now.plusDays(offset);

        List<Schedule> homeScheduleList = scheduleRepository.findAllByStaffIdAndDateRange(staffId, fromDate, toDate);
        Map<LocalDate, List<Schedule>> homeScheduleMap = homeScheduleList.stream().collect(groupingBy(schedule -> schedule.getScheduleStartDateTime().toLocalDate()));

        List<HomeDateServiceDto> dateList = getHomeDateList(fromDate, toDate, homeScheduleMap);
        List<Schedule> selectedScheduleList = getHomeScheduleList(now);
        return new StaffHomePageServiceDto(dateList, now, selectedScheduleList);
    }

    private List<HomeDateServiceDto> getHomeDateList(LocalDate fromDate, LocalDate toDate, Map<LocalDate, List<Schedule>> homeScheduleMap) {
        List<HomeDateServiceDto> dateList = new ArrayList<>();
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            List<Schedule> scheduleList = homeScheduleMap.get(date);
            dateList.add(new HomeDateServiceDto(date, getTotalScheduleStatus(date, scheduleList)));
        }
        return dateList;
    }

    private TotalScheduleStatus getTotalScheduleStatus(LocalDate date, List<Schedule> scheduleList) {
        if (scheduleList == null) return TotalScheduleStatus.NONE;
        if (date.isAfter(LocalDate.now())) return TotalScheduleStatus.BEFORE;
        if (isAllSuccess(scheduleList)) return TotalScheduleStatus.COMPLETE;
        return TotalScheduleStatus.INCOMPLETE;
    }

    private boolean isAllSuccess(List<Schedule> scheduleList) {
        return scheduleList.stream().allMatch(schedule -> schedule.getStatus() == ScheduleStatus.SUCCESS);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getHomeScheduleList(LocalDate selectedDate) {
        Long staffId = profileUtil.getCurrentStaff().getId();
        return scheduleRepository.findAllByStaffIdAndDateFetch(staffId, selectedDate);
    }

    @Transactional(readOnly = true)
    public StaffWorkspacePageServiceDto getWorkspacePage(Long workspaceId) {
        LocalDate now = LocalDate.now(), fromDate = now.with(firstDayOfMonth()), toDate = now.with(lastDayOfMonth());
        List<Schedule> workspaceScheduleList = scheduleRepository.findAllByWorkspaceIdAndDateRange(workspaceId, fromDate, toDate);
        Map<LocalDate, List<Schedule>> workspaceScheduleMap = workspaceScheduleList.stream().collect(groupingBy(schedule -> schedule.getScheduleStartDateTime().toLocalDate()));

        Workspace workspace = bossWorkspaceService.getWorkspace(workspaceId);
        List<StaffWorkspaceDateServiceDto> dateList = getWorkspaceDateList(fromDate, toDate, workspaceScheduleMap);
        List<ScheduleServiceDto> selectedScheduleList = getWorkspaceScheduleList(workspaceId, now);
        return new StaffWorkspacePageServiceDto(workspace, dateList, now, selectedScheduleList);
    }

    private List<StaffWorkspaceDateServiceDto> getWorkspaceDateList(LocalDate fromDate, LocalDate toDate, Map<LocalDate, List<Schedule>> workspaceScheduleMap) {
        List<StaffWorkspaceDateServiceDto> dateList = new ArrayList<>();
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            List<Schedule> scheduleList = workspaceScheduleMap.get(date);
            boolean isMySchedule = isMySchedule(scheduleList);
            dateList.add(new StaffWorkspaceDateServiceDto(date, isMySchedule));
        }
        return dateList;
    }

    private boolean isMySchedule(List<Schedule> scheduleList) {
        if (scheduleList == null) return false;
        Long staffId = profileUtil.getCurrentStaff().getId();
        return scheduleList.stream().anyMatch(schedule -> Objects.equals(schedule.getStaff().getId(), staffId));
    }

    @Transactional(readOnly = true)
    public List<ScheduleServiceDto> getWorkspaceScheduleList(Long workspaceId, LocalDate selectedDate) {
        List<Schedule> workspaceScheduleList = scheduleRepository.findAllByWorkspaceIdAndDateFetch(workspaceId, selectedDate);
        Long staffId = profileUtil.getCurrentStaff().getId();
        return workspaceScheduleList.stream()
                .map(schedule -> {
                    boolean isMySchedule = Objects.equals(schedule.getStaff().getId(), staffId);
                    return new ScheduleServiceDto(schedule, isMySchedule);
                })
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public Schedule getScheduleBrief(Long scheduleId) {
        return scheduleRepository.findByIdFetch(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    @Transactional
    public Schedule start(Long scheduleId) {
        Schedule schedule = getSchedule(scheduleId);
        LocalDateTime currentDateTime = LocalDateTime.now();
        validateCurrentDateTime(currentDateTime, schedule.getScheduleStartDateTime().minusMinutes(10), schedule.getScheduleEndDateTime());
        start(currentDateTime, schedule);
        return schedule;
    }

    @Transactional
    public Schedule end(Long scheduleId) {
        Schedule schedule = getSchedule(scheduleId);
        LocalDateTime currentDateTime = LocalDateTime.now();
        validateScheduleStatus(schedule.getStatus());
        end(currentDateTime, schedule);
        return schedule;
    }

    private Schedule getSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    private void validateCurrentDateTime(LocalDateTime currentDateTime, LocalDateTime scheduleStartCriteriaDateTime, LocalDateTime scheduleEndDateTime) {
        if (currentDateTime.isBefore(scheduleStartCriteriaDateTime)) {
            throw new BusinessException(ErrorCode.EARLY_SCHEDULE_START);
        }
        if (currentDateTime.isAfter(scheduleEndDateTime)) {
            throw new BusinessException(ErrorCode.LATE_SCHEDULE_START);
        }
    }

    private void start(LocalDateTime currentDateTime, Schedule schedule) {
        LocalDateTime scheduleLateCriteriaDateTime = schedule.getScheduleStartDateTime().plusMinutes(10);
        if (currentDateTime.isAfter(scheduleLateCriteriaDateTime)) {
            schedule.late();
            schedule.stampLogicalStartDateTime(currentDateTime);
        }
        else {
            schedule.onDuty();
            schedule.stampScheduleStartDateTime(currentDateTime);
        }
    }

    private void validateScheduleStatus(ScheduleStatus status) {
        if (status != ScheduleStatus.ON_DUTY && status != ScheduleStatus.LATE) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE_END);
        }
    }

    private void end(LocalDateTime currentDateTime, Schedule schedule) {
        if (schedule.getStatus() == ScheduleStatus.LATE || currentDateTime.isBefore(schedule.getScheduleEndDateTime())) {
            schedule.fail();
        }
        else {
            schedule.success();
        }

        if (currentDateTime.isBefore(schedule.getScheduleEndDateTime())) {
            schedule.stampLogicalEndDateTime(currentDateTime);
        }
        else {
            schedule.stampScheduleEndDateTime(currentDateTime);
        }
    }
}
