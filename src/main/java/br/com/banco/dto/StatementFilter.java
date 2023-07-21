package br.com.banco.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class StatementFilter {
    private final String transactionOperator;
    private final LocalDateTime initDate;
    private final LocalDateTime endDate;
}
