package br.com.banco.dto;

import br.com.banco.entity.Transference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@RequiredArgsConstructor
public class BankStatementResponse {
    private final String type;
    private final Double value;
    private final LocalDateTime operationDate;
    public BankStatementResponse(Transference transference) {
        this.type = transference.getType().toString();
        this.value = transference.getValue();
        this.operationDate = transference.getTransferenceDate().truncatedTo(ChronoUnit.SECONDS);
    }
}
