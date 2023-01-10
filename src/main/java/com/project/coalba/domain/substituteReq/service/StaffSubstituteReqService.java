package com.project.coalba.domain.substituteReq.service;

import com.project.coalba.domain.notification.FirebaseCloudMessageService;
import com.project.coalba.domain.profile.entity.Boss;
import com.project.coalba.domain.profile.entity.Staff;
import com.project.coalba.domain.profile.service.BossProfileService;
import com.project.coalba.domain.schedule.entity.Schedule;
import com.project.coalba.domain.schedule.service.ScheduleService;
import com.project.coalba.domain.substituteReq.dto.response.*;
import com.project.coalba.domain.substituteReq.entity.SubstituteReq;
import com.project.coalba.domain.substituteReq.entity.enums.SubstituteReqStatus;
import com.project.coalba.domain.substituteReq.repository.dto.BothSubstituteReqDto;
import com.project.coalba.domain.substituteReq.repository.SubstituteRepository;
import com.project.coalba.domain.substituteReq.repository.dto.SubstituteReqDto;
import com.project.coalba.global.exception.BusinessException;
import com.project.coalba.global.exception.ErrorCode;
import com.project.coalba.global.utils.ProfileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

@RequiredArgsConstructor
@Service
public class StaffSubstituteReqService {
    private final ProfileUtil profileUtil;
    private final BossProfileService bossProfileService;
    private final ScheduleService scheduleService;
    private final SubstituteRepository substituteRepository;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    @Transactional
    public void createSubstituteReq(Long scheduleId, Long receiverId, String reqMessage) {
        Schedule schedule = scheduleService.getSchedule(scheduleId);
        Staff sender = profileUtil.getCurrentStaff();
        Staff receiver = profileUtil.getStaffById(receiverId);
        Boss boss = bossProfileService.getBossByScheduleId(schedule.getId());

        SubstituteReq substituteReq = SubstituteReq.builder()
                .schedule(schedule)
                .receiver(receiver)
                .sender(sender)
                .boss(boss)
                .reqMessage(reqMessage)
                .status(SubstituteReqStatus.WAITING)
                .build();

        substituteRepository.save(substituteReq);

        sendSubstituteRequestNotice(substituteReq);
    }

    private void sendSubstituteRequestNotice(SubstituteReq substituteReq) {
        String senderName = substituteReq.getSender().getRealName();
        String deviceToken = substituteReq.getReceiver().getDeviceToken();

        firebaseCloudMessageService.sendMessageTo(deviceToken, "대타근무 요청",senderName + "님이 대타를 요청하였어요");
    }

    @Transactional
    public void cancelSubstituteReq(Long substituteReqId) {
        SubstituteReq substituteReq = this.getSubstituteReqById(substituteReqId);
        if (substituteReq.isWaiting()) {
            substituteReq.cancel();
        }else {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_REQ);
        }
    }

    @Transactional(readOnly = true)
    public SubstituteReq getSubstituteReqById(Long substituteReqId) {
        return substituteRepository.findById(substituteReqId)
                .orElseThrow(()-> new BusinessException(ErrorCode.SUBSTITUTEREQ_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public BothSubstituteReqDto getDetailSubstituteReq(Long substituteReqId) {
        BothSubstituteReqDto substituteReq = substituteRepository.getSubstituteReq(substituteReqId);
        if (substituteReq != null) {
            return substituteReq;
        } else throw new BusinessException(ErrorCode.SUBSTITUTEREQ_NOT_FOUND);
    }

    @Transactional(readOnly = true)
    public List<SentSubstituteReqResponse> getSentSubstituteReqs() {
        Staff currentStaff = profileUtil.getCurrentStaff();
        List<SubstituteReqDto> substituteReqDtos = substituteRepository.getSentSubstituteReqs(currentStaff);

        Map<YearMonth, List<SubstituteReqDto>> substituteReqMap = substituteReqDtos.stream()
                .collect(groupingBy(SubstituteReqDto -> new YearMonth(SubstituteReqDto.getSubstituteReq().getCreatedDate())));

        List<SentSubstituteReqResponse> sentSubstituteReqs = new ArrayList<>();
        for (YearMonth yearMonth : substituteReqMap.keySet()) {
            sentSubstituteReqs.add(new SentSubstituteReqResponse(yearMonth, substituteReqMap.get(yearMonth)));
        }
        Collections.sort(sentSubstituteReqs);

        return sentSubstituteReqs;
    }

    @Transactional(readOnly = true)
    public List<ReceivedSubstituteReqResponse> getReceivedSubstituteReqs() {
        Staff currentStaff = profileUtil.getCurrentStaff();
        List<SubstituteReqDto> substituteReqDtos = substituteRepository.getReceivedSubstituteReqs(currentStaff);

        Map<YearMonth, List<SubstituteReqDto>> substituteReqMap = substituteReqDtos.stream()
                .collect(groupingBy(SubstituteReqDto -> new YearMonth(SubstituteReqDto.getSubstituteReq().getCreatedDate())));

        List<ReceivedSubstituteReqResponse> receivedSubstituteReqs = new ArrayList<>();
        for (YearMonth yearMonth : substituteReqMap.keySet()) {
            receivedSubstituteReqs.add(new ReceivedSubstituteReqResponse(yearMonth, substituteReqMap.get(yearMonth)));
        }
        Collections.sort(receivedSubstituteReqs);

        return receivedSubstituteReqs;
    }

    @Transactional
    public void acceptSubstituteReq(Long substituteReqId) {
        /**
         * 추후 기능 보완
         * 요청 성사시 다른 사람한테 보낸 요청 다 취소(?)
         */
        SubstituteReq substituteReq = this.getSubstituteReqById(substituteReqId);
        substituteReq.accept();

        sendAcceptanceNotice(substituteReq);
    }

    private void sendAcceptanceNotice(SubstituteReq substituteReq) {
        String bossDeviceToken = substituteReq.getBoss().getDeviceToken();
        String senderDeviceToken = substituteReq.getSender().getDeviceToken();
        String senderName = substituteReq.getSender().getRealName();

        firebaseCloudMessageService.sendMessageTo(bossDeviceToken, "대타 승인 요청","대타 승인 요청이 도착하였습니다.");
        firebaseCloudMessageService.sendMessageTo(senderDeviceToken, "대타 요청 수락",senderName + "님이 대타요청을 수락하였습니다. 사장님께 승인요청이 갑니다.");
    }

    @Transactional
    public void rejectSubstituteReq(Long substituteReqId) {
        SubstituteReq substituteReq = this.getSubstituteReqById(substituteReqId);
        substituteReq.refuse();
    }
}
