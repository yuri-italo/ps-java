package br.com.banco.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class DepositDto {
    @NotNull
    @Min(value = 10, message = "{min.deposit.value.exceeded.message.error}")
    private Double value;
}
