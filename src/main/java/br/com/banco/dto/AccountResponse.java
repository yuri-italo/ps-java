package br.com.banco.dto;

import br.com.banco.entity.Account;
import lombok.Getter;

@Getter
public class AccountResponse {
    private final Integer id;
    private final String ownerName;

    public AccountResponse(Account account) {
        this.id = account.getId();
        this.ownerName = account.getOwnerName();
    }
}
