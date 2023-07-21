package br.com.banco.controller;

import br.com.banco.dto.AccountDto;
import br.com.banco.dto.AccountResponse;
import br.com.banco.entity.Account;
import br.com.banco.service.impl.AccountService;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.banco.controller.AccountResource.ACCOUNTS_BASE_PATH;

@RestController
@RequestMapping(ACCOUNTS_BASE_PATH)
public class AccountResource {
    public static final String ACCOUNTS_BASE_PATH = "/api/accounts";

    private final AccountService accountService;

    public AccountResource(AccountService accountService) {
        this.accountService = accountService;
    }

    @Transactional
    @PostMapping()
    public ResponseEntity<AccountResponse> save(@RequestBody @Valid AccountDto accountDto) {
        var savedAccount = accountService.save(accountDto.toEntity());
        return ResponseEntity.created(URI.create(ACCOUNTS_BASE_PATH + "/" + savedAccount.getId()))
                .body(new AccountResponse(savedAccount));
    }

    @GetMapping("{id}")
    public ResponseEntity<AccountResponse> findById(@PathVariable Integer id) {
        var account = accountService.findById(id);
        return ResponseEntity.ok(new AccountResponse(account));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAll() {
        var allAccounts = accountService.findAll();
        var accountViewList = getAccountViews(allAccounts);
        return ResponseEntity.ok(accountViewList);
    }

    private List<AccountResponse> getAccountViews(List<Account> allAccounts) {
        return allAccounts.stream().map(AccountResponse::new).collect(Collectors.toList());
    }

    @Transactional
    @PutMapping("{id}")
    public ResponseEntity<AccountResponse> update(@PathVariable Integer id, @RequestBody @Valid AccountDto accountDto) {
        var account = accountService.findById(id);
        var updatedAccount = getUpdatedAccount(accountDto, account);
        return ResponseEntity.ok(new AccountResponse(updatedAccount));
    }

    private Account getUpdatedAccount(AccountDto source, Account target) {
        BeanUtils.copyProperties(source, target);
        return accountService.save(target);
    }

    @Transactional
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
