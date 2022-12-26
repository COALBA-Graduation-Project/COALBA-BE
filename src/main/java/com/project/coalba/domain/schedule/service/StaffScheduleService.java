package com.project.coalba.domain.schedule.service;

import com.project.coalba.domain.schedule.service.dto.HomePageServiceDto;
import com.project.coalba.domain.schedule.service.dto.HomeDateServiceDto;
import com.project.coalba.domain.schedule.service.dto.ScheduleServiceDto;
import com.project.coalba.domain.schedule.entity.Schedule;
import com.project.coalba.domain.schedule.entity.enums.ScheduleStatus;
import com.project.coalba.domain.schedule.repository.ScheduleRepository;
import com.project.coalba.global.utils.ProfileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class StaffScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ProfileUtil profileUtil;

    public HomePageServiceDto getHomePage() {
        Long staffId = profileUtil.getCurrentStaff().getId();
        LocalDate now = LocalDate.now();
        final int offset = 3;

        List<Schedule> homeScheduleList = scheduleRepository.findAllByStaffIdAndDateRange(staffId, now.minusDays(offset), now.plusDays(offset));
        Map<LocalDate, List<Schedule>> homeScheduleMap = homeScheduleList.stream()
                .collect(groupingBy(schedule -> schedule.getScheduleStartDateTime().toLocalDate()));

        List<HomeDateServiceDto> dateList = getHomeDateList(homeScheduleMap);
        List<Schedule> selectedScheduleList = getHomeScheduleList(now);
        return new HomePageServiceDto(dateList, now, selectedScheduleList);
    }

    private List<HomeDateServiceDto> getHomeDateList(Map<LocalDate, List<Schedule>> homeScheduleMap) {
        List<HomeDateServiceDto> dateList = new ArrayList<>();
        for (LocalDate date : homeScheduleMap.keySet()) {
            List<Schedule> scheduleList = homeScheduleMap.get(date);
            dateList.add(getHomeDate(date, scheduleList));
        }
        return dateList;
    }

    private HomeDateServiceDto getHomeDate(LocalDate date, List<Schedule> scheduleList) {
        if (scheduleList == null) return new HomeDateServiceDto(date, false, false, false);
        if (date.isAfter(LocalDate.now())) return new HomeDateServiceDto(date, true, true, false);
        if (isAllSuccess(scheduleList)) return new HomeDateServiceDto(date, true, false, true);
        return new HomeDateServiceDto(date, true, false, false);
    }

    private boolean isAllSuccess(List<Schedule> scheduleList) {
        return scheduleList.stream().allMatch(schedule -> schedule.getStatus() == ScheduleStatus.SUCCESS);
    }

    public List<Schedule> getHomeScheduleList(LocalDate selectedDate) {
        Long staffId = profileUtil.getCurrentStaff().getId();
        return scheduleRepository.findAllByStaffIdAndDateFetch(staffId, selectedDate);
    }

    public List<ScheduleServiceDto> getWorkspaceScheduleDtoList(Long workspaceId, LocalDate selectedDate) {
        List<Schedule> workspaceScheduleList = scheduleRepository.findAllByWorkspaceIdAndDateFetch(workspaceId, selectedDate);
        Long staffId = profileUtil.getCurrentStaff().getId();
        return workspaceScheduleList.stream()
                .map(schedule -> {
                    boolean isMySchedule = Objects.equals(schedule.getStaff().getId(), staffId);
                    return new ScheduleServiceDto(schedule, isMySchedule);
                })
                .collect(toList());
    }

    public Schedule getScheduleFetch(Long scheduleId) {
        return scheduleRepository.findByIdFetch(scheduleId)
                .orElseThrow(() -> new RuntimeException("해당 스케줄이 존재하지 않습니다."));
    }

    @Transactional
    public void startSchedule(Long scheduleId) {
        Schedule schedule = getSchedule(scheduleId);
        LocalDateTime currentDateTime = LocalDateTime.now();
        validateCurrentDateTime(currentDateTime, schedule.getScheduleStartDateTime().minusMinutes(10), schedule.getScheduleEndDateTime());
        start(currentDateTime, schedule);
    }

    @Transactional
    public void endSchedule(Long scheduleId) {
        Schedule schedule = getSchedule(scheduleId);
        LocalDateTime currentDateTime = LocalDateTime.now();
        validateScheduleStatus(schedule.getStatus());
        end(currentDateTime, schedule);
    }

    private Schedule getSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("해당 스케줄이 존재하지 않습니다."));
    }

    private void validateCurrentDateTime(LocalDateTime currentDateTime, LocalDateTime scheduleStartCriteriaDateTime, LocalDateTime scheduleEndDateTime) {
        if (currentDateTime.isBefore(scheduleStartCriteriaDateTime)) {
            throw new RuntimeException("해당 스케줄 시작 10분 전부터 출근 가능합니다.");
        }
        if (currentDateTime.isAfter(scheduleEndDateTime)) {
            throw new RuntimeException("해당 스케줄이 종료되어 출근 불가합니다.");
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
            throw new RuntimeException("출근 상태일 때에만 퇴근 가능합니다.");
        }
    }

    private void end(LocalDateTime currentDateTime, Schedule schedule) {
        if (schedule.getStatus() == ScheduleStatus.LATE ||
                currentDateTime.isBefore(schedule.getScheduleEndDateTime())) {
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
