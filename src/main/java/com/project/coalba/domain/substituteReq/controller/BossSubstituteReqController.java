package com.project.coalba.domain.substituteReq.controller;

import com.project.coalba.domain.substituteReq.dto.response.BothDetailSubstituteReq;
import com.project.coalba.domain.substituteReq.dto.response.BothSubstituteReq;
import com.project.coalba.domain.substituteReq.dto.response.SubstituteReqsResponse;
import com.project.coalba.domain.substituteReq.repository.dto.BothSubstituteReqDto;
import com.project.coalba.domain.substituteReq.service.BossSubstituteReqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/boss/substituteReqs")
@RequiredArgsConstructor
@RestController
public class BossSubstituteReqController {
    private final BossSubstituteReqService bossSubstituteReqService;

    @GetMapping("/{substituteReqId}")
    public BothDetailSubstituteReq getDetailSubstituteReqs(@PathVariable Long substituteReqId){
        BothSubstituteReqDto detailSubstituteReqDto = bossSubstituteReqService.getDetailSubstituteReq(substituteReqId);
        return new BothDetailSubstituteReq(detailSubstituteReqDto);
    }

    @GetMapping
    public SubstituteReqsResponse getSubstituteReqs() {
        List<BothSubstituteReq> bothSubstituteReqs = bossSubstituteReqService.getSubstituteReqs();
        return new SubstituteReqsResponse(bothSubstituteReqs);
    }

    @PutMapping("/{substituteReqId}/accept")
    public ResponseEntity approveSubstituteReq(@PathVariable Long substituteReqId) {
        bossSubstituteReqService.approveSubstituteReq(substituteReqId);
        return ResponseEntity.ok("대타근무 요청을 최종승인하였습니다. 스케줄이 교체 됩니다.");
    }

    @PutMapping("/{substituteReqId}/reject")
    public ResponseEntity disapproveSubstituteReq(@PathVariable Long substituteReqId) {
        bossSubstituteReqService.disapproveSubstituteReq(substituteReqId);
        return ResponseEntity.ok("대타근무 요청을 최종거절하였습니다.");
    }
}
