package com.project.coalba.domain.substituteReq.service;

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
         * ?????? ?????? ??????
         * ?????? ????????? ?????? ???????????? ?????? ?????? ??? ??????(?)
         * ?????? ????????? ???????????? ?????? ?????????
         */
        SubstituteReq substituteReq = this.getSubstituteReqById(substituteReqId);
        substituteReq.accept();
    }

    @Transactional
    public void rejectSubstituteReq(Long substituteReqId) {
        SubstituteReq substituteReq = this.getSubstituteReqById(substituteReqId);
        substituteReq.refuse();
    }
}
