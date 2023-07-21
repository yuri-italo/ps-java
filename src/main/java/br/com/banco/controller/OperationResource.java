package br.com.banco.controller;

import br.com.banco.dto.*;
import br.com.banco.entity.Account;
import br.com.banco.service.impl.AccountService;
import br.com.banco.service.impl.TransferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static br.com.banco.controller.OperationResource.OPERATIONS_BASE_PATH;

@RestController
@RequestMapping(OPERATIONS_BASE_PATH)
public class OperationResource {
    public static final String OPERATIONS_BASE_PATH = "api/operations/";
    private static final String TRANSFERENCE_PATH = "transference/";
    private static final String WITHDRAW_PATH = "withdraw/";
    private static final String DEPOSIT_PATH = "deposit/";

    private final AccountService accountService;
    private final TransferenceService transferenceService;

    public OperationResource(AccountService accountService, TransferenceService transferenceService) {
        this.accountService = accountService;
        this.transferenceService = transferenceService;
    }

    @Transactional
    @PostMapping(TRANSFERENCE_PATH + "{accountId}")
    public ResponseEntity<TransferenceResponse> transfer(
            @PathVariable Integer accountId,
            @RequestBody @Valid TransferenceDto transferenceDto) {
        var account = accountService.findById(accountId);
        var destinationAccount = accountService.findById(transferenceDto.getDestinationAccountId());
        accountService.checkEquals(account,destinationAccount);
        Double transferenceValue = transferenceDto.getValue();

        var transference = transferenceService.realizeTransfer(account, destinationAccount, transferenceValue);

        return ResponseEntity.ok(new TransferenceResponse(
                account.getOwnerName(),
                destinationAccount.getOwnerName(),
                transferenceValue));
    }

    @Transactional
    @PostMapping(WITHDRAW_PATH + "{accountId}")
    public ResponseEntity<WithdrawResponse> witdraw(
            @PathVariable Integer accountId,
            @RequestBody @Valid WithdrawDto withdrawDto) {
        Account account = accountService.findById(accountId);
        var transference = transferenceService.withdraw(account, withdrawDto.getValue());

        return ResponseEntity.ok(new WithdrawResponse(transference));
    }

    @Transactional
    @PostMapping(DEPOSIT_PATH + "{accountId}")
    public ResponseEntity<DepositResponse> deposit(
            @PathVariable Integer accountId,
            @RequestBody @Valid DepositDto depositDto) {
        Account account = accountService.findById(accountId);
        var transference = transferenceService.deposit(account, depositDto.getValue());

        return ResponseEntity.ok(new DepositResponse(transference));
    }
}
