package com.project.coalba.domain.workspace.service;

import com.project.coalba.domain.profile.entity.*;
import com.project.coalba.domain.profile.service.StaffProfileService;
import com.project.coalba.domain.workspace.entity.*;
import com.project.coalba.domain.workspace.repository.*;
import com.project.coalba.domain.workspace.service.dto.*;
import com.project.coalba.global.exception.*;
import com.project.coalba.global.utils.ProfileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BossWorkspaceService {
    private final StaffProfileService staffProfileService;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProfileUtil profileUtil;

    @Transactional(readOnly = true)
    public List<Workspace> getMyWorkspaceList() {
        Long bossId = profileUtil.getCurrentBoss().getId();
        return workspaceRepository.findAllByBossId(bossId);
    }

    @Transactional(readOnly = true)
    public Workspace getWorkspace(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_NOT_FOUND));
    }

    @Transactional
    public List<Workspace> saveWorkspace(WorkspaceCreateServiceDto serviceDto) {
        Optional<Workspace> workspaceOptional = workspaceRepository.findByBusinessNumber(serviceDto.getBusinessNumber());
        if (workspaceOptional.isPresent()) throw new BusinessException(ErrorCode.ALREADY_EXIST_WORKSPACE);

        Boss boss = profileUtil.getCurrentBoss();
        workspaceRepository.save(serviceDto.toEntity(boss));
        return workspaceRepository.findAllByBossId(boss.getId());
    }

    @Transactional
    public void updateWorkspace(Long workspaceId, WorkspaceUpdateServiceDto serviceDto) {
        Workspace workspace = getWorkspace(workspaceId);
        workspace.update(serviceDto.getName(), serviceDto.getPhoneNumber(), serviceDto.getAddress(), serviceDto.getImageUrl());
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMember> getWorkspaceMemberInfoList(Long workspaceId) {
        return workspaceMemberRepository.findAllByWorkspaceIdFetch(workspaceId);
    }

    @Transactional(readOnly = true)
    public Workspace validateInvitation(Long workspaceId, String receiverEmail) {
        Workspace workspace = getWorkspace(workspaceId);
        Long staffId = staffProfileService.getStaffWithEmail(receiverEmail).getId();
        Optional<WorkspaceMember> workspaceMemberOptional = workspaceMemberRepository.findByWorkspaceIdAndStaffId(workspaceId, staffId);
        if (workspaceMemberOptional.isPresent()) throw new BusinessException(ErrorCode.ALREADY_EXIST_STAFF_IN_WORKSPACE);
        return workspace;
    }

    @Transactional
    public void saveWorkspaceMemberForStaff(Long workspaceId, String email) {
        Workspace workspace = getWorkspace(workspaceId);
        Staff staff = staffProfileService.getStaffWithEmail(email);
        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .workspace(workspace)
                .staff(staff)
                .build();
        workspaceMemberRepository.save(workspaceMember);
    }
}
