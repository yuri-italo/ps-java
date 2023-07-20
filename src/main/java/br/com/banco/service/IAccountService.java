package br.com.banco.service;

import br.com.banco.entity.Account;

import java.util.List;

public interface IAccountService {
    Account save(Account account);
    Account findById(Integer id);
    List<Account> findAll();
    Account update(Account uppdatedAccount);
    void delete(Integer id);
    void checkEquals(Account account, Account destinationAccount);
}
