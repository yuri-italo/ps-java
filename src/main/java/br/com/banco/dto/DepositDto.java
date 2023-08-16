package br.com.banco.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class DepositDto {
    @NotNull
    @Min(value = 10, message = "{min.deposit.value.exceeded.message.error}")
    private final Double value;
}
