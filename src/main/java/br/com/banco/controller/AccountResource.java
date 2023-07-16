package br.com.banco.controller;

import br.com.banco.dto.AccountDto;
import br.com.banco.dto.AccountView;
import br.com.banco.entity.Account;
import br.com.banco.service.impl.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping(AccountResource.ACCOUNTS_BASE_PATH)
public class AccountResource {
    public static final String ACCOUNTS_BASE_PATH = "/api/accounts";
    private final AccountService accountService;

    public AccountResource(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountView> save(@RequestBody @Valid AccountDto accountDto) {
        Account savedAccount = accountService.save(accountDto.toEntity());
        return ResponseEntity.created(URI.create(ACCOUNTS_BASE_PATH + "/" +  savedAccount.getId()))
                .body(new AccountView(savedAccount));
    }

    @GetMapping("{id}")
    public ResponseEntity<AccountView> findById(@PathVariable Integer id) {
        Account account = accountService.findById(id);
        return ResponseEntity.ok(new AccountView(account));
    }
}
