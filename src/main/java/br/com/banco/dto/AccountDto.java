package br.com.banco.dto;

import br.com.banco.entity.Account;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class AccountDto {

    @NotBlank(message = "{empty.owner.name.message.error}")
    @Size(max = 50, message = "{max.name.size.exceeded.message.error}")
    private final String ownerName;

    public Account toEntity() {
        return new Account(this.ownerName);
    }
}
