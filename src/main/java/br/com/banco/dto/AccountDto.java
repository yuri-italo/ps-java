package br.com.banco.dto;

import br.com.banco.entity.Account;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;

import javax.validation.constraints.NotBlank;

@Getter
@RequiredArgsConstructor
public class AccountDto {
    private final MessageSource messageSource;

    @NotBlank(message = "{empty.owner.name.message.error}")
    private final String ownerName;

    public Account toEntity() {
        return new Account(this.ownerName);
    }
}
