package br.com.banco.dto;

import br.com.banco.entity.Transference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class WithdrawResponse {
    private final String name;
    private final double withdrawalAmount;

    public WithdrawResponse(Transference transference) {
        this.name = transference.getOwnerName();
        this.withdrawalAmount = transference.getValue();
    }
}
