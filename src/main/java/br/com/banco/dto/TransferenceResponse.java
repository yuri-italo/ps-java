package br.com.banco.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TransferenceResponse {
    private final String sender;
    private final String addressee;
    private final Double value;
}
