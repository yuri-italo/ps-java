package br.com.banco.service.impl;

import br.com.banco.entity.Account;
import br.com.banco.entity.Transference;
import br.com.banco.entity.Type;
import br.com.banco.repository.TransferenceRepository;
import br.com.banco.service.ITransferenceService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class TransferenceService implements ITransferenceService {
    private final TransferenceRepository transferenceRepository;
    private final MessageSource messageSource;

    public TransferenceService(TransferenceRepository transferenceRepository, MessageSource messageSource) {
        this.transferenceRepository = transferenceRepository;
        this.messageSource = messageSource;
    }

    @Override
    public Transference save(Transference transference) {
        return transferenceRepository.save(transference);
    }

    @Override
    public Transference findById(Integer id) {
        return transferenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("non-existing.id.error.message",null, Locale.getDefault())
                ));
    }

    @Override
    public List<Transference> findAll() {
        return transferenceRepository.findAll();
    }

    @Override
    public void delete(Integer id) {
        Transference transference = this.findById(id);
        transferenceRepository.delete(transference);
    }

    @Override
    public Transference realizeTransfer(Account account, Account destinationAccount, Double value) {
        Transference transference = getOwnerTransference(account, destinationAccount, value);
        Transference destinationTransference = getDestinationTransference(account, destinationAccount, value);

        Transference savedTransference = this.save(transference);
        this.save(destinationTransference);

        return savedTransference;
    }

    @Override
    public Transference withdraw(Account account, Double value) {
        var withdraw = getWithdraw(account, value);
        return this.save(withdraw);
    }

    @Override
    public Transference deposit(Account account, Double value) {
        Transference deposit = getDeposit(account, value);
        return this.save(deposit);
    }

    private Transference getDeposit(Account account, Double value) {
        return new Transference(
                value,
                Type.DEPOSIT,
                account,
                null
        );
    }

    private Transference getWithdraw(Account account, Double value) {
        return new Transference(
                -value,
                Type.WITHDRAW,
                account,
                null
        );
    }

    private Transference getOwnerTransference(Account account, Account destinationAccount, Double value) {
        return new Transference(
                -value,
                Type.TRANSFERENCE,
                account,
                destinationAccount.getOwnerName());
    }

    private Transference getDestinationTransference(Account account, Account destinationAccount, Double value) {
        return new Transference(
                value,
                Type.TRANSFERENCE,
                destinationAccount,
                account.getOwnerName());
    }
}
