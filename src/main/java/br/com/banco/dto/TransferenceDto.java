package br.com.banco.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@RequiredArgsConstructor
public class TransferenceDto {
    @NotNull
    @Min(value = 0, message = "{min.value.exceeded.message.error}")
    private final Double value;

    @NotNull
    @Min(value = 1, message = "{min.id.value.exceeded.message.error}")
    private final Integer destinationAccountId;
}
