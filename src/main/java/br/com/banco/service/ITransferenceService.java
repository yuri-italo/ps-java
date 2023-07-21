package br.com.banco.service;

import br.com.banco.dto.StatementFilter;
import br.com.banco.entity.Account;
import br.com.banco.entity.Transference;

import java.util.List;

public interface ITransferenceService {
    Transference save(Transference transference);
    Transference findById(Integer id);
    List<Transference> findAll();
    void delete(Integer id);
    Transference transfer(Account account, Account destinationAccountId, Double value);
    Transference withdraw(Account account, Double value);
    Transference deposit(Account account, Double value);
    List<Transference> getBankStatements(Account account, StatementFilter statementFilter);
}
