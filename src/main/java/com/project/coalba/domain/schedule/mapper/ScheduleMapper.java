package com.project.coalba.domain.schedule.mapper;

import com.project.coalba.domain.profile.entity.Staff;
import com.project.coalba.domain.schedule.service.dto.*;
import com.project.coalba.domain.schedule.dto.request.ScheduleCreateRequest;
import com.project.coalba.domain.schedule.dto.response.*;
import com.project.coalba.domain.schedule.entity.Schedule;
import com.project.coalba.domain.workspace.entity.Workspace;
import org.mapstruct.*;

import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    @Mappings({
            @Mapping(source = "scheduleDateTime.start", target = "scheduleStartDateTime"),
            @Mapping(source = "scheduleDateTime.end", target = "scheduleEndDateTime")
    })
    ScheduleCreateServiceDto toServiceDto(ScheduleCreateRequest scheduleCreateRequest);

    @Mappings({
            @Mapping(source = "id", target = "scheduleId"),
            @Mapping(source = "scheduleStartDateTime", target = "scheduleDate", qualifiedByName = "toLocalDate"),
            @Mapping(source = "scheduleStartDateTime", target = "scheduleStartTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "scheduleEndDateTime", target = "scheduleEndTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "workspace.name", target = "workspaceName")
    })
    ScheduleBriefResponse toDto(Schedule schedule);

    default HomeDateResponse toDto(HomeDateServiceDto serviceDto) {
        return new HomeDateResponse(serviceDto.getDate(), serviceDto.getTotalScheduleStatus());
    }

    default BossHomePageResponse toDto(BossHomePageServiceDto serviceDto) {
        List<HomeDateResponse> dateList = serviceDto.getDateList().stream().map(this::toDto).collect(Collectors.toList());
        BossHomeSelectedSubPageResponse selectedSubPage = toDto(serviceDto.getSelectedDate(), serviceDto.getScheduleListOfWorkspace());
        return new BossHomePageResponse(dateList, selectedSubPage);
    }

    default BossHomeSelectedSubPageResponse toDto(LocalDate selectedDate, Map<Workspace, List<Schedule>> scheduleListOfWorkspaces) {
        List<BossHomeSelectedSubPageResponse.WorkspaceResponse> workspaceList = new ArrayList<>();
        for (Workspace workspace : scheduleListOfWorkspaces.keySet()) {
            workspaceList.add(new BossHomeSelectedSubPageResponse.WorkspaceResponse(
                    workspace.getId(), workspace.getName(), workspace.getImageUrl(),
                    scheduleListOfWorkspaces.get(workspace)
                            .stream().map(this::toScheduleDtoOfBossHome).collect(Collectors.toList())
            ));
        }
        return new BossHomeSelectedSubPageResponse(selectedDate, workspaceList);
    }

    @Mappings({
            @Mapping(source = "id", target = "scheduleId"),
            @Mapping(source = "scheduleStartDateTime", target = "scheduleStartTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "scheduleEndDateTime", target = "scheduleEndTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "logicalStartDateTime", target = "logicalStartTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "logicalEndDateTime", target = "logicalEndTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "status", target = "status"),
            @Mapping(source = "staff.id", target = "worker.workerId"),
            @Mapping(source = "staff.realName", target = "worker.name"),
            @Mapping(source = "staff.imageUrl", target = "worker.imageUrl")
    })
    BossHomeSelectedSubPageResponse.ScheduleResponse toScheduleDtoOfBossHome(Schedule homeSchedule);

    default BossWorkspacePageResponse toDto(BossWorkspacePageServiceDto serviceDto) {
        BossWorkspacePageResponse.WorkspaceResponse selectedWorkspace = toWorkspaceDtoOfBossWorkspace(serviceDto.getWorkspace());
        LocalDate selectedDate = serviceDto.getSelectedDate();
        List<BossWorkspaceDateResponse> dateList = serviceDto.getDateList().stream().map(this::toDto).collect(Collectors.toList());
        BossWorkspaceSelectedSubPageResponse selectedSubPage = toDto(selectedDate.getDayOfMonth(), serviceDto::getSelectedScheduleList);
        return BossWorkspacePageResponse.builder()
                .selectedWorkspace(selectedWorkspace)
                .year(selectedDate.getYear())
                .month(selectedDate.getMonthValue())
                .dateList(dateList)
                .selectedSubPage(selectedSubPage)
                .build();
    }

    @Mappings({
            @Mapping(source = "id", target = "workspaceId"),
            @Mapping(source = "name", target = "name"),
            @Mapping(source = "imageUrl", target = "imageUrl")
    })
    BossWorkspacePageResponse.WorkspaceResponse toWorkspaceDtoOfBossWorkspace(Workspace workspace);

    default BossWorkspaceDateResponse toDto(BossWorkspaceDateServiceDto serviceDto) {
        return new BossWorkspaceDateResponse(serviceDto.getDate(), serviceDto.getTotalScheduleStatus());
    }

    interface BossWorkspaceScheduleListRef extends Supplier<List<Schedule>> {}
    default BossWorkspaceSelectedSubPageResponse toDto(int selectedDay, BossWorkspaceScheduleListRef ref) {
        List<BossWorkspaceSelectedSubPageResponse.ScheduleResponse> selectedScheduleList = ref.get().stream()
                .map(this::toScheduleDtoOfBossWorkspace)
                .collect(Collectors.toList());
        return new BossWorkspaceSelectedSubPageResponse(selectedDay, selectedScheduleList);
    }

    @Mappings({
            @Mapping(source = "id", target = "scheduleId"),
            @Mapping(source = "scheduleStartDateTime", target = "scheduleStartTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "scheduleEndDateTime", target = "scheduleEndTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "status", target = "status"),
            @Mapping(source = "staff.id", target = "worker.workerId"),
            @Mapping(source = "staff.realName", target = "worker.name")
    })
    BossWorkspaceSelectedSubPageResponse.ScheduleResponse toScheduleDtoOfBossWorkspace(Schedule homeSchedule);

    interface BossWorkReportMapRef extends Supplier<Map<Staff, WorkReportServiceDto>> {}
    default BossWorkReportListResponse toDto(Long selectedWorkspaceId, int selectedYear, int selectedMonth, BossWorkReportMapRef ref) {
        List<BossWorkReportListResponse.WorkReportResponse> workReportList = ref.get().entrySet().stream()
                .map(entry -> toWorkReportDtoOfBoss(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return new BossWorkReportListResponse(selectedWorkspaceId, selectedYear, selectedMonth, workReportList);
    }

    @Mappings({
            @Mapping(source = "staff.id", target = "worker.workerId"),
            @Mapping(source = "staff.realName", target = "worker.name"),
            @Mapping(source = "staff.imageUrl", target = "worker.imageUrl"),
            @Mapping(source = "workReport.totalWorkTimeHour", target="totalWorkTimeHour"),
            @Mapping(source = "workReport.totalWorkTimeMin", target="totalWorkTimeMin"),
            @Mapping(source = "workReport.totalWorkPay", target="totalWorkPay")
    })
    BossWorkReportListResponse.WorkReportResponse toWorkReportDtoOfBoss(Staff staff, WorkReportServiceDto workReport);

    interface StaffListRef extends Supplier<List<Staff>> {}
    default PossibleStaffListResponse toDto(StaffListRef ref) {
        List<PossibleStaffListResponse.StaffResponse> staffList = ref.get().stream()
                .map(this::toSubDto).collect(Collectors.toList());
        return new PossibleStaffListResponse(staffList);
    }

    @Mappings({
            @Mapping(source = "id", target = "staffId"),
            @Mapping(source = "realName", target = "name"),
            @Mapping(source = "imageUrl", target = "imageUrl")
    })
    PossibleStaffListResponse.StaffResponse toSubDto(Staff staff);

    default StaffHomePageResponse toDto(StaffHomePageServiceDto serviceDto) {
        List<HomeDateResponse> dateList = serviceDto.getDateList().stream().map(this::toDto).collect(Collectors.toList());
        StaffHomeSelectedSubPageResponse selectedSubPage = toDto(serviceDto.getSelectedDate(), serviceDto::getSelectedScheduleList);
        return new StaffHomePageResponse(dateList, selectedSubPage);
    }

    interface StaffHomeScheduleListRef extends Supplier<List<Schedule>> {}
    default StaffHomeSelectedSubPageResponse toDto(LocalDate selectedDate, StaffHomeScheduleListRef ref) {
        List<StaffHomeSelectedSubPageResponse.ScheduleResponse> selectedScheduleList = ref.get().stream()
                .map(this::toScheduleDtoOfStaffHome)
                .collect(Collectors.toList());
        return new StaffHomeSelectedSubPageResponse(selectedDate, selectedScheduleList);
    }

    @Mappings({
            @Mapping(source = "id", target = "scheduleId"),
            @Mapping(source = "scheduleStartDateTime", target = "scheduleStartTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "scheduleEndDateTime", target = "scheduleEndTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "logicalStartDateTime", target = "logicalStartTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "logicalEndDateTime", target = "logicalEndTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "status", target = "status"),
            @Mapping(source = "workspace.id", target = "workspace.workspaceId"),
            @Mapping(source = "workspace.name", target = "workspace.name")
    })
    StaffHomeSelectedSubPageResponse.ScheduleResponse toScheduleDtoOfStaffHome(Schedule homeSchedule);

    default StaffWorkspacePageResponse toDto(StaffWorkspacePageServiceDto serviceDto) {
        StaffWorkspacePageResponse.WorkspaceResponse selectedWorkspace = toWorkspaceDtoOfStaffWorkspace(serviceDto.getWorkspace());
        LocalDate selectedDate = serviceDto.getSelectedDate();
        List<StaffWorkspaceDateResponse> dateList = serviceDto.getDateList().stream().map(this::toDto).collect(Collectors.toList());
        StaffWorkspaceSelectedSubPageResponse selectedSubPage = toDto(selectedDate.getDayOfMonth(), serviceDto::getSelectedScheduleList);
        return StaffWorkspacePageResponse.builder()
                .selectedWorkspace(selectedWorkspace)
                .year(selectedDate.getYear())
                .month(selectedDate.getMonthValue())
                .dateList(dateList)
                .selectedSubPage(selectedSubPage)
                .build();
    }

    @Mappings({
            @Mapping(source = "id", target = "workspaceId"),
            @Mapping(source = "name", target = "name"),
            @Mapping(source = "imageUrl", target = "imageUrl")
    })
    StaffWorkspacePageResponse.WorkspaceResponse toWorkspaceDtoOfStaffWorkspace(Workspace workspace);

    default StaffWorkspaceDateResponse toDto(StaffWorkspaceDateServiceDto serviceDto) {
        return new StaffWorkspaceDateResponse(serviceDto.getDate(), serviceDto.getIsMySchedule());
    }

    interface StaffWorkspaceScheduleListRef extends Supplier<List<ScheduleServiceDto>> {}
    default StaffWorkspaceSelectedSubPageResponse toDto(int selectedDay, StaffWorkspaceScheduleListRef ref) {
        List<StaffWorkspaceSelectedSubPageResponse.ScheduleResponse> selectedScheduleList = ref.get().stream()
                .map(this::toScheduleDtoOfStaffWorkspace)
                .collect(Collectors.toList());
        return new StaffWorkspaceSelectedSubPageResponse(selectedDay, selectedScheduleList);
    }

    @Mappings({
            @Mapping(source = "schedule.id", target = "scheduleId"),
            @Mapping(source = "schedule.scheduleStartDateTime", target = "scheduleStartTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "schedule.scheduleEndDateTime", target = "scheduleEndTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "schedule.status", target = "status"),
            @Mapping(source = "schedule.staff.id", target = "worker.workerId"),
            @Mapping(source = "schedule.staff.realName", target = "worker.name"),
            @Mapping(source = "isMySchedule", target = "isMySchedule")
    })
    StaffWorkspaceSelectedSubPageResponse.ScheduleResponse toScheduleDtoOfStaffWorkspace(ScheduleServiceDto workspaceScheduleDto);

    interface StaffWorkReportMapRef extends Supplier<Map<Integer, WorkReportServiceDto>> {}
    default StaffWorkReportListResponse toDto(int selectedYear, StaffWorkReportMapRef ref) {
        List<StaffWorkReportListResponse.WorkReportResponse> workReportList = ref.get().entrySet().stream()
                .map(entry -> toWorkReportDtoOfStaff(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return new StaffWorkReportListResponse(selectedYear, workReportList);
    }

    @Mappings({
            @Mapping(source = "month", target = "month"),
            @Mapping(source = "workReport.totalWorkTimeHour", target = "totalWorkTimeHour"),
            @Mapping(source = "workReport.totalWorkTimeMin", target = "totalWorkTimeMin"),
            @Mapping(source = "workReport.totalWorkPay", target = "totalWorkPay"),
    })
    StaffWorkReportListResponse.WorkReportResponse toWorkReportDtoOfStaff(int month, WorkReportServiceDto workReport);

    @Mappings({
            @Mapping(source = "id", target = "scheduleId"),
            @Mapping(source = "logicalStartDateTime", target = "logicalStartTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "status", target = "status")
    })
    ScheduleStartResponse toStartDto(Schedule schedule);

    @Mappings({
            @Mapping(source = "id", target = "scheduleId"),
            @Mapping(source = "logicalEndDateTime", target = "logicalEndTime", qualifiedByName = "toLocalTime"),
            @Mapping(source = "status", target = "status")
    })
    ScheduleEndResponse toEndDto(Schedule schedule);

    @Named("toLocalDate")
    default LocalDate localDateTimeToLocalDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.toLocalDate();
    }

    @Named("toLocalTime")
    default LocalTime localDateTimeToLocalTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.toLocalTime();
    }
}
