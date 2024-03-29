package com.project.coalba.domain.substituteReq.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class YearMonth {
    private int year;
    private int month;

    public YearMonth(LocalDateTime localDateTime) {
        year = localDateTime.getYear();
        month = localDateTime.getMonthValue();
    }
}
