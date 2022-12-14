package com.project.coalba.domain.substituteReq.service;

import com.project.coalba.domain.profile.entity.Boss;
import com.project.coalba.domain.schedule.entity.Schedule;
import com.project.coalba.domain.substituteReq.dto.response.BothSubstituteReqResponse;
import com.project.coalba.domain.substituteReq.dto.response.YearMonth;
import com.project.coalba.domain.substituteReq.entity.SubstituteReq;
import com.project.coalba.domain.substituteReq.repository.SubstituteRepository;
import com.project.coalba.domain.substituteReq.repository.dto.BothSubstituteReqDto;
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
public class BossSubstituteReqService {
    private final ProfileUtil profileUtil;
    private final SubstituteRepository substituteRepository;

    @Transactional(readOnly = true)
    public BothSubstituteReqDto getDetailSubstituteReq(Long substituteReqId) {
        return substituteRepository.getSubstituteReq(substituteReqId);
    }

    @Transactional(readOnly = true)
    public List<BothSubstituteReqResponse> getSubstituteReqs() {
        Boss currentBoss = profileUtil.getCurrentBoss();
        List<BothSubstituteReqDto> bothSubstituteReqDtos = substituteRepository.getSubstituteReqs(currentBoss);

        Map<YearMonth, List<BothSubstituteReqDto>> substituteReqMap = bothSubstituteReqDtos.stream()
                .collect(groupingBy(bothSubstituteReqDto -> new YearMonth(bothSubstituteReqDto.getSubstituteReq().getCreatedDate())));

        List<BothSubstituteReqResponse> bothSubstituteReqs = new ArrayList<>();
        for (YearMonth yearMonth : substituteReqMap.keySet()) {
            bothSubstituteReqs.add(new BothSubstituteReqResponse(yearMonth, substituteReqMap.get(yearMonth)));
        }
        Collections.sort(bothSubstituteReqs);

        return bothSubstituteReqs;
    }

    @Transactional
    public void approveSubstituteReq(Long substituteReqId) {
        /**
         * ?????? ?????? ??????
         * ????????? ??????????????? ???????????? ?????? ?????????
         */
        SubstituteReq substituteReq = this.getSubstituteReqById(substituteReqId);
        substituteReq.approve();

        Schedule schedule = substituteReq.getSchedule();
        schedule.changeStaff(substituteReq.getReceiver());
    }

    @Transactional
    public void disapproveSubstituteReq(Long substituteReqId) {
        SubstituteReq substituteReq = this.getSubstituteReqById(substituteReqId);
        substituteReq.disapprove();
    }

    @Transactional(readOnly = true)
    public SubstituteReq getSubstituteReqById(Long substituteReqId) {
        return substituteRepository.findById(substituteReqId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSTITUTEREQ_NOT_FOUND));
        }
}
