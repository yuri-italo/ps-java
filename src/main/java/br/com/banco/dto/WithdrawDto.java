package br.com.banco.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class WithdrawDto {
    @NotNull
    @Min(value = 10, message = "{min.withdraw.value.exceeded.message.error}")
    private Double value;
}
