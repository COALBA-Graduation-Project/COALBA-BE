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
public class ReceivedSubstituteReqResponse implements Comparable<ReceivedSubstituteReqResponse> {
    private int year;
    private int month;
    private List<ReceivedDetailSubstituteReqResponse> substituteReqList = new ArrayList<>();

    public ReceivedSubstituteReqResponse(YearMonth yearMonth, List<SubstituteReqDto> substituteReqDtos) {
        year = yearMonth.getYear();
        month = yearMonth.getMonth();
        substituteReqList = substituteReqDtos.stream().map(ReceivedDetailSubstituteReqResponse::new).collect(Collectors.toList());
    }

    @Override
    public int compareTo(@NotNull ReceivedSubstituteReqResponse o) {
        if ((year == o.year && month < o.getMonth()) || year < o.getYear()) return 1;
        else return -1;
    }
}
