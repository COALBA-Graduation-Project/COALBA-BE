package com.project.coalba.domain.message.service;

import com.project.coalba.domain.message.dto.response.MessageResponse;
import com.project.coalba.domain.message.entity.Message;
import com.project.coalba.domain.message.entity.enums.Criteria;
import com.project.coalba.domain.message.repository.MessageRepository;
import com.project.coalba.domain.profile.entity.Staff;
import com.project.coalba.domain.profile.repository.StaffProfileRepository;
import com.project.coalba.domain.workspace.entity.Workspace;
import com.project.coalba.domain.workspace.repository.WorkspaceRepository;
import com.project.coalba.domain.workspace.service.BossWorkspaceService;
import com.project.coalba.global.utils.ProfileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RequiredArgsConstructor
@Service
public class BossMessageService {
    private final BossWorkspaceService bossWorkspaceService;
    private final MessageRepository messageRepository;
    private final ProfileUtil profileUtil;

    @Transactional
    public void sendMessageToStaff(Long workspaceId, Long staffId, String content) {
        Workspace workspace = bossWorkspaceService.getWorkspace(workspaceId);
        Staff staff = profileUtil.getStaffById(staffId);

        Message message = Message.builder()
                .content(content)
                .criteria(Criteria.WORKSPACE_TO_STAFF)
                .staff(staff)
                .workspace(workspace)
                .build();

        messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public MessageResponse.BossMessageResponse getDetailMessages(Long staffId, Long workspaceId){
        Workspace workspace = bossWorkspaceService.getWorkspace(workspaceId);
        Staff staff = profileUtil.getStaffById(staffId);

        List<Message> messages = messageRepository.getMessages(staffId, workspaceId);
        return new MessageResponse.BossMessageResponse(workspace, staff, messages);
    }


}
