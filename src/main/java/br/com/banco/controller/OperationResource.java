package br.com.banco.controller;

import br.com.banco.dto.TransferenceDto;
import br.com.banco.dto.TransferenceResponse;
import br.com.banco.entity.Account;
import br.com.banco.service.impl.AccountService;
import br.com.banco.service.impl.TransferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static br.com.banco.controller.OperationResource.*;

@RestController
@RequestMapping(OPERATIONS_BASE_PATH)
public class OperationResource {
    public static final String OPERATIONS_BASE_PATH = "api/operations/";
    private static final String TRANSFERENCE_PATH = "transference/";

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

        transferenceService.realizeTransfer(account, destinationAccount, transferenceValue);
        var transferenceView = createTransferenceResponse(account, destinationAccount, transferenceValue);

        return ResponseEntity.ok(transferenceView);
    }

    private TransferenceResponse createTransferenceResponse(Account account, Account destinationAccount, Double value) {
        return new TransferenceResponse(account.getOwnerName(), destinationAccount.getOwnerName(), value);
    }
}
