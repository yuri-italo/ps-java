package br.com.banco.exception.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ExceptionDto {
    private final String title;
    private final LocalDateTime timeStamp;
    private final Integer status;
    private final String exception;
    private final Map<String, String> details;
}
