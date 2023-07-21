package br.com.banco.dto;

import br.com.banco.entity.Transference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DepositResponse {
    private final String name;
    private final double depositAmount;

    public DepositResponse(Transference transference) {
        this.name = transference.getOwnerName();
        this.depositAmount = transference.getValue();
    }
}
