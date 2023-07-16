package br.com.banco.controller;

import br.com.banco.dto.AccountDto;
import br.com.banco.dto.AccountView;
import br.com.banco.entity.Account;
import br.com.banco.service.impl.AccountService;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

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
        var savedAccount = accountService.save(accountDto.toEntity());
        return ResponseEntity.created(URI.create(ACCOUNTS_BASE_PATH + "/" +  savedAccount.getId()))
                .body(new AccountView(savedAccount));
    }

    @GetMapping("{id}")
    public ResponseEntity<AccountView> findById(@PathVariable Integer id) {
        var account = accountService.findById(id);
        return ResponseEntity.ok(new AccountView(account));
    }

    @GetMapping
    public ResponseEntity<List<AccountView>> findAll() {
        var allAccounts = accountService.findAll();
        var accountViewList = getAccountViews(allAccounts);
        return ResponseEntity.ok(accountViewList);
    }

    private List<AccountView> getAccountViews(List<Account> allAccounts) {
        return allAccounts.stream().map(AccountView::new).collect(Collectors.toList());
    }

    @PutMapping("{id}")
    public ResponseEntity<AccountView> update(@PathVariable Integer id, @RequestBody @Valid AccountDto accountDto) {
        var account = accountService.findById(id);
        var updatedAccount = getUpdatedAccount(accountDto, account);
        return ResponseEntity.ok(new AccountView(updatedAccount));
    }

    private Account getUpdatedAccount(AccountDto source, Account target) {
        BeanUtils.copyProperties(source, target);
        return accountService.save(target);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
