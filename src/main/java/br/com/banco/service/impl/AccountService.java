package br.com.banco.service.impl;

import br.com.banco.entity.Account;
import br.com.banco.repository.AccountRepository;
import br.com.banco.service.IAccountService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AccountService implements IAccountService {
    private final AccountRepository accountRepository;
    private final MessageSource messageSource;

    public AccountService(AccountRepository accountRepository, MessageSource messageSource) {
        this.accountRepository = accountRepository;
        this.messageSource = messageSource;
    }

    @Override
    public Account save(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Account findById(Integer id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("non-existing.id.error.message",null, Locale.getDefault())
                ));
    }

    @Override
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public Account update(Account uppdatedAccount) {
        Account exitingAccount = this.findById(uppdatedAccount.getId());
        updateFields(uppdatedAccount, exitingAccount);
        return accountRepository.save(exitingAccount);
    }

    private void updateFields(Account uppdatedAccount, Account exitingAccount) {
        exitingAccount.setOwnerName(uppdatedAccount.getOwnerName());
    }

    @Override
    public void delete(Integer id) {
        Account account = this.findById(id);
        accountRepository.delete(account);
    }
}
