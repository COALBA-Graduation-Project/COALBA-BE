package com.project.coalba.domain.substituteReq.dto.response;

import com.project.coalba.domain.substituteReq.repository.dto.SubstituteReqDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class SentSubstituteReq implements Comparable<SentSubstituteReq> {
    private Integer year;
    private Integer month;
    private List<SentDetailSubstituteReq> substituteReqList = new ArrayList<>();

    public SentSubstituteReq(YearMonth yearMonth, List<SubstituteReqDto> substituteReqDtos) {
        year = yearMonth.getYear();
        month = yearMonth.getMonth();
        substituteReqList = substituteReqDtos.stream().map(substituteReqDto -> new SentDetailSubstituteReq(substituteReqDto)).collect(Collectors.toList());
    }

    @Override
    public int compareTo(@NotNull SentSubstituteReq o) {
        if ((year == o.year && month < o.getMonth()) || year < o.getYear()) return 1;
        else return -1;
    }
}
