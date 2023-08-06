package br.com.banco.service.impl;

import br.com.banco.dto.StatementFilter;
import br.com.banco.entity.Account;
import br.com.banco.entity.Transference;
import br.com.banco.entity.Type;
import br.com.banco.repository.TransferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferenceServiceTest {
    private final String NON_EXISTING_ID_ERROR_MESSAGE_KEY = "non-existing.id.error.message";
    private final String EXPECTED_MESSAGE_ERROR_FOR_INVALID_ID = "Invalid ID. The specified ID does not exist in our " +
            "records. Please check and try again.";
    private final String NULL_POINTER_EXCEPTION_VALUE_MESSAGE_ERROR = "Cannot invoke " +
            "\"java.lang.Double.doubleValue()\" because \"value\" is null";
    private final String NULL_POINTER_EXCEPTION_TRANSFERENCE_VALUE_MESSAGE_ERROR = "Value is mandatory.";
    private final String NULL_POINTER_EXCEPTION_ACCOUNT_MESSAGE_ERROR = "Account is mandatory.";
    private final String NULL_POINTER_EXCEPTION_DESTINATION_ACCOUNT_MESSAGE_ERROR = "Cannot invoke " +
            "\"br.com.banco.entity.Account.getOwnerName()\" because \"destinationAccount\" is null";
    private final int TRANSFERENCE_ID = 1;
    private final int ANOTHER_TRANSFERENCE_ID = 2;
    private final String TRANSACTION_OPERATOR_NAME = "Alex de Souza";
    private static final String ANOTHER_TRANSACTION_OPERATOR_NAME = "Chico Maia";
    private final Double TRANSFERENCE_VALUE = 100.00;

    @Mock
    private TransferenceRepository transferenceRepository;
    @Mock
    private MessageSource messageSource;
    @InjectMocks
    private TransferenceService transferenceService;
    @Test
    void save_ValidTransference_ShouldSaveTransferenceSuccessfully() {
        // given
        var transference = buildTransference();

        when(transferenceRepository.save(any(Transference.class))).thenReturn(transference);

        // when
        var savedTransference = transferenceService.save(transference);

        // then
        verify(transferenceRepository, times(1)).save(transference);

        assertNotNull(savedTransference);
        assertEquals(TRANSFERENCE_ID, savedTransference.getId());
        assertEquals(transference.getTransferenceDate(), savedTransference.getTransferenceDate());
        assertEquals(transference.getValue(), savedTransference.getValue());
        assertEquals(transference.getType(), savedTransference.getType());
        assertEquals(TRANSACTION_OPERATOR_NAME, savedTransference.getTransactionOperatorName());
        assertEquals(transference.getAccount(), savedTransference.getAccount());
    }

    @Test
    void findById_ExistingTransferenceId_ShouldReturnTransference() {
        // given
        var transference = buildTransference();

        when(transferenceRepository.findById(TRANSFERENCE_ID)).thenReturn(Optional.of(transference));

        // when
        var foundTransference = transferenceService.findById(TRANSFERENCE_ID);

        // then
        verify(transferenceRepository, times(1)).findById(TRANSFERENCE_ID);

        assertNotNull(foundTransference);
        assertEquals(TRANSFERENCE_ID, foundTransference.getId());
        assertEquals(transference.getTransferenceDate(), foundTransference.getTransferenceDate());
        assertEquals(transference.getValue(), foundTransference.getValue());
        assertEquals(transference.getType(), foundTransference.getType());
        assertEquals(TRANSACTION_OPERATOR_NAME, foundTransference.getTransactionOperatorName());
        assertEquals(transference.getAccount(), foundTransference.getAccount());
    }

    @Test
    void findById_NonExistingTransferenceId_ShouldThrowIllegalArgumentException() {
        // given
        var id = generateRandomId();

        when(transferenceRepository.findById(id)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(NON_EXISTING_ID_ERROR_MESSAGE_KEY), isNull(), any(Locale.class)))
                .thenReturn(EXPECTED_MESSAGE_ERROR_FOR_INVALID_ID);

        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> transferenceService.findById(id));

        // then
        verify(transferenceRepository, times(1)).findById(id);

        assertNotNull(exception);
        assertEquals(EXPECTED_MESSAGE_ERROR_FOR_INVALID_ID, exception.getMessage());
    }

    @Test
    void findAll_ExistingTransference_ShouldReturnAllTransference() {
        // given
        var transferenceList = generateListOfTransference();

        when(transferenceRepository.findAll()).thenReturn(transferenceList);

        // when
        var foundTransferenceList = transferenceService.findAll();

        // then
        verify(transferenceRepository, times(1)).findAll();

        assertNotNull(foundTransferenceList);
        assertEquals(transferenceList.size(), foundTransferenceList.size());
        assertTrue(foundTransferenceList.contains(transferenceList.get(0)));
        assertTrue(foundTransferenceList.contains(transferenceList.get(1)));
    }

    @Test
    void findAll_NonExistingTransference_ShouldReturnEmpty() {
        // given
        List<Transference> transferenceList = Collections.emptyList();

        when(transferenceRepository.findAll()).thenReturn(transferenceList);

        // when
        var foundTransferenceList = transferenceService.findAll();

        // then
        verify(transferenceRepository, times(1)).findAll();

        assertNotNull(foundTransferenceList);
        assertEquals(0, foundTransferenceList.size());
    }


    @Test
    void delete_ExistingTransferenceId_ShouldDeleteTransferenceSuccessfully() {
        // given
        var transference = buildTransference();

        when(transferenceRepository.findById(TRANSFERENCE_ID)).thenReturn(Optional.of(transference));

        // when
        transferenceService.delete(TRANSFERENCE_ID);

        // then
        verify(transferenceRepository, times(1)).findById(TRANSFERENCE_ID);
        verify(transferenceRepository, times(1)).delete(transference);
    }

    @Test
    void delete_NonExistingTransferenceId_ShouldThrowIllegalArgumentException() {
        // given
        var id = generateRandomId();

        when(transferenceRepository.findById(id)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(NON_EXISTING_ID_ERROR_MESSAGE_KEY), isNull(), any(Locale.class)))
                .thenReturn(EXPECTED_MESSAGE_ERROR_FOR_INVALID_ID);

        // when
        // then
        var exception = assertThrows(IllegalArgumentException.class, () -> transferenceService.delete(id));

        verify(transferenceRepository, times(1)).findById(id);
        verify(transferenceRepository, times(0)).delete(any());

        assertNotNull(exception);
        assertEquals(EXPECTED_MESSAGE_ERROR_FOR_INVALID_ID, exception.getMessage());
    }

    @Test
    void transfer_ValidTransference_ShouldTransferSuccessfully() {
        // given
        var account = AccountServiceTest.buildAccount();
        var destinationAccount = AccountServiceTest.buildSecondAccount();
        var transference = buildOwnerTransference(account, destinationAccount, TRANSFERENCE_VALUE);
        var destindationTransference = buildDestinationTransference(account, destinationAccount, TRANSFERENCE_VALUE);

        when(transferenceRepository.save(any())).thenReturn(transference).thenReturn(destindationTransference);

        // when
        var transferred = transferenceService.transfer(account, destinationAccount, TRANSFERENCE_VALUE);

        // then
        verify(transferenceRepository, times(2)).save(any());

        assertNotNull(transferred);
        assertEquals(transference.getId(), transferred.getId());
        assertEquals(transference.getOwnerName(), transferred.getOwnerName());
        assertEquals(transference.getTransactionOperatorName(), transferred.getTransactionOperatorName());
        assertEquals(transference.getValue(), transferred.getValue());
        assertEquals(transference.getType(), transferred.getType());
        assertEquals(transference.getAccount(), transferred.getAccount());
        assertEquals(transference.getTransferenceDate(), transferred.getTransferenceDate());
    }

    @Test
    void transfer_TransferenceMissingValue_ShouldThrowNullPointerException() {
        // given
        var account = AccountServiceTest.buildAccount();
        var destinationAccount = AccountServiceTest.buildSecondAccount();

        // when
        // then
        var exception = assertThrows(NullPointerException.class, () -> transferenceService.transfer(
                account,
                destinationAccount,
                null));

        verify(transferenceRepository, times(0)).save(any());

        assertNotNull(exception);
        assertEquals(NULL_POINTER_EXCEPTION_VALUE_MESSAGE_ERROR, exception.getMessage());
    }

    @Test
    void transfer_TransferenceMissingAccount_ShouldThrowNullPointerException() {
        // given
        var account = AccountServiceTest.buildAccount();

        // when
        // then
        var exception = assertThrows(NullPointerException.class, () -> transferenceService.transfer(
                account,
                null,
                TRANSFERENCE_VALUE));

        verify(transferenceRepository, times(0)).save(any());

        assertNotNull(exception);
        assertEquals(NULL_POINTER_EXCEPTION_DESTINATION_ACCOUNT_MESSAGE_ERROR, exception.getMessage());
    }

    @Test
    void withdraw_ValidTransference_ShouldWithdrawSuccessfully() {
        // given
        var account = AccountServiceTest.buildAccount();
        var withdraw = buildWithdraw(account);

        when(transferenceRepository.save(any())).thenReturn(withdraw);

        // when
        var savedWithdraw = transferenceService.withdraw(account, TRANSFERENCE_VALUE);

        // then
        verify(transferenceRepository, times(1)).save(any());

        assertNotNull(savedWithdraw);
        assertEquals(TRANSFERENCE_ID, savedWithdraw.getId());
        assertEquals(-TRANSFERENCE_VALUE, savedWithdraw.getValue());
        assertEquals(Type.WITHDRAW, savedWithdraw.getType());
        assertNotNull(savedWithdraw.getTransferenceDate());
        assertEquals(account, savedWithdraw.getAccount());
        assertEquals(account.getOwnerName(), savedWithdraw.getOwnerName());
        assertNull(savedWithdraw.getTransactionOperatorName());
    }

    @Test
    void withdraw_TransferenceMissingValue_ShouldThrowNullPointerException() {
        // given
        var account = AccountServiceTest.buildAccount();

        // when
        // then
        var exception = assertThrows(NullPointerException.class, () -> transferenceService.withdraw(
                account,
                null));

        verify(transferenceRepository, times(0)).save(any());

        assertNotNull(exception);
        assertEquals(NULL_POINTER_EXCEPTION_VALUE_MESSAGE_ERROR, exception.getMessage());
    }

    @Test
    void withdraw_TransferenceMissingAccount_ShouldThrowNullPointerException() {
        // given
        // when
        // then
        var exception = assertThrows(NullPointerException.class, () -> transferenceService.withdraw(
                null,
                TRANSFERENCE_VALUE));

        verify(transferenceRepository, times(0)).save(any());

        assertNotNull(exception);
        assertEquals(NULL_POINTER_EXCEPTION_ACCOUNT_MESSAGE_ERROR, exception.getMessage());
    }

    @Test
    void deposit_ValidTransference_ShouldDepositSuccessfully() {
        // given
        var account = AccountServiceTest.buildAccount();
        var deposit = buildDeposit(account);

        when(transferenceRepository.save(any())).thenReturn(deposit);

        // when
        var savedDeposit = transferenceService.deposit(account, TRANSFERENCE_VALUE);

        // then
        verify(transferenceRepository, times(1)).save(any());

        assertNotNull(savedDeposit);
        assertEquals(TRANSFERENCE_ID, savedDeposit.getId());
        assertEquals(TRANSFERENCE_VALUE, savedDeposit.getValue());
        assertEquals(Type.DEPOSIT, savedDeposit.getType());
        assertNotNull(savedDeposit.getTransferenceDate());
        assertEquals(account, savedDeposit.getAccount());
        assertEquals(account.getOwnerName(), savedDeposit.getOwnerName());
        assertNull(savedDeposit.getTransactionOperatorName());
    }

    @Test
    void deposit_TransferenceMissingValue_ShouldThrowNullPointerException() {
        // given
        var account = AccountServiceTest.buildAccount();

        // when
        // then
        var exception = assertThrows(NullPointerException.class, () -> transferenceService.deposit(
                account,
                null));

        verify(transferenceRepository, times(0)).save(any());

        assertNotNull(exception);
        assertEquals(NULL_POINTER_EXCEPTION_TRANSFERENCE_VALUE_MESSAGE_ERROR, exception.getMessage());
    }

    @Test
    void deposit_TransferenceMissingAccount_ShouldThrowNullPointerException() {
        // given
        // when
        // then
        var exception = assertThrows(NullPointerException.class, () -> transferenceService.deposit(
                null,
                TRANSFERENCE_VALUE));

        verify(transferenceRepository, times(0)).save(any());

        assertNotNull(exception);
        assertEquals(NULL_POINTER_EXCEPTION_ACCOUNT_MESSAGE_ERROR, exception.getMessage());
    }

    @Test
    void getBankStatements_AllFiltersPresent_ShouldReturnAllMatchingTransference() {
        // given
        var account = AccountServiceTest.buildAccount();
        var filter = buildFilter();
        var transferenceList = generateListOfTransferenceForAccount(account);

        when(transferenceRepository.findAll(any(Specification.class))).thenReturn(transferenceList);

        // when
        var foundBankStatements = transferenceService.getBankStatements(account, filter);

        // then
        verify(transferenceRepository, times(1)).findAll(any(Specification.class));

        assertNotNull(foundBankStatements);
        assertEquals(1, foundBankStatements.size());
        assertTrue(foundBankStatements.contains(transferenceList.get(0)));
        assertTrue(foundBankStatements.get(0).getTransferenceDate().isAfter(filter.getInitDate()));
        assertTrue(foundBankStatements.get(0).getTransferenceDate().isBefore(filter.getEndDate()));
        assertEquals(ANOTHER_TRANSACTION_OPERATOR_NAME, foundBankStatements.get(0).getTransactionOperatorName());
    }

    @Test
    void getBankStatements_TransactionOperatorNameFilterPresent_ShouldReturnAllMatchingTransference() {
        // given
        var account = AccountServiceTest.buildAccount();
        var filter = buildFilterMissingDates();
        var transferenceList = generateListOfTransferenceForAccount(account);

        when(transferenceRepository.findAll(any(Specification.class))).thenReturn(transferenceList);

        // when
        var foundBankStatements = transferenceService.getBankStatements(account, filter);

        // then
        verify(transferenceRepository, times(1)).findAll(any(Specification.class));

        assertNotNull(foundBankStatements);
        assertEquals(1, foundBankStatements.size());
        assertTrue(foundBankStatements.contains(transferenceList.get(0)));
        assertEquals(ANOTHER_TRANSACTION_OPERATOR_NAME, foundBankStatements.get(0).getTransactionOperatorName());
    }

    @Test
    void getBankStatements_DatesFilterPresent_ShouldReturnAllMatchingTransference() {
        // given
        var account = AccountServiceTest.buildAccount();
        var filter = buildFilterMissingTransactionOperatorName();
        var transferenceList = generateListOfTransferenceForAccount(account);

        when(transferenceRepository.findAll(any(Specification.class))).thenReturn(transferenceList);

        // when
        var foundBankStatements = transferenceService.getBankStatements(account, filter);

        // then
        verify(transferenceRepository, times(1)).findAll(any(Specification.class));

        assertNotNull(foundBankStatements);
        assertEquals(1, foundBankStatements.size());
        assertTrue(foundBankStatements.contains(transferenceList.get(0)));
        assertTrue(foundBankStatements.get(0).getTransferenceDate().isAfter(filter.getInitDate()));
        assertTrue(foundBankStatements.get(0).getTransferenceDate().isBefore(filter.getEndDate()));
    }

    @Test
    void getBankStatements_NoFilterPresent_ShouldReturnAllAccountTransference() {
        // given
        var account = AccountServiceTest.buildAccount();
        var transferenceList = generateManyTransferenceForAccount(account);

        when(transferenceRepository.findAll(any(Specification.class))).thenReturn(transferenceList);

        // when
        var foundBankStatements = transferenceService.getBankStatements(account, null);

        // then
        verify(transferenceRepository, times(1)).findAll(any(Specification.class));

        assertNotNull(foundBankStatements);
        assertEquals(2, foundBankStatements.size());
        assertTrue(foundBankStatements.contains(transferenceList.get(0)));
        assertTrue(foundBankStatements.contains(transferenceList.get(1)));
    }


    private Transference buildTransference() {
        Transference transference = new Transference(
                100.00,
                Type.TRANSFERENCE,
                AccountServiceTest.buildAccount(),
                TRANSACTION_OPERATOR_NAME);
        transference.setId(TRANSFERENCE_ID);
        transference.setTransferenceDate(LocalDateTime.of(2023, 7, 25, 0, 11, 0));

        return transference;
    }

    private Transference buildSecondTransference() {
        Transference transference = new Transference(
                50.00,
                Type.TRANSFERENCE,
                AccountServiceTest.buildSecondAccount(),
                ANOTHER_TRANSACTION_OPERATOR_NAME);
        transference.setId(ANOTHER_TRANSFERENCE_ID);
        transference.setTransferenceDate(LocalDateTime.of(2023, 7, 25, 0, 2, 0));

        return transference;
    }

    private Transference buildThirdTransference() {
        Transference transference = new Transference(
                25.00,
                Type.WITHDRAW,
                AccountServiceTest.buildAccount(),
                null);
        transference.setId(ANOTHER_TRANSFERENCE_ID);
        transference.setTransferenceDate(LocalDateTime.now());

        return transference;
    }

    private Transference buildOwnerTransference(Account account, Account destinationAccount, Double value) {
        var transference = new Transference(
                -value,
                Type.TRANSFERENCE,
                account,
                destinationAccount.getOwnerName());
        transference.setId(TRANSFERENCE_ID);
        transference.setTransferenceDate(LocalDateTime.now());

        return transference;
    }

    private Transference buildDestinationTransference(Account account, Account destinationAccount, Double value) {
        var transference = new Transference(
                value,
                Type.TRANSFERENCE,
                destinationAccount,
                account.getOwnerName());
        transference.setId(ANOTHER_TRANSFERENCE_ID);
        transference.setTransferenceDate(LocalDateTime.now());

        return transference;
    }

    private Transference buildWithdraw(Account account) {
        var withdrawValue = -TRANSFERENCE_VALUE;
        var withdraw = new Transference(withdrawValue, Type.WITHDRAW, account, null);
        withdraw.setId(TRANSFERENCE_ID);
        withdraw.setTransferenceDate(LocalDateTime.now());

        return withdraw;
    }

    private Transference buildDeposit(Account account) {
        var deposit = new Transference(TRANSFERENCE_VALUE, Type.DEPOSIT, account, null);
        deposit.setId(TRANSFERENCE_ID);
        deposit.setTransferenceDate(LocalDateTime.now());

        return deposit;
    }

    private List<Transference> generateListOfTransference() {
        return List.of(buildTransference(), buildSecondTransference());
    }

    private List<Transference> generateListOfTransferenceForAccount(Account account) {
        var transference = buildSecondTransference();
        transference.setAccount(account);

        return List.of(transference);
    }

    private List<Transference> generateManyTransferenceForAccount(Account account) {
        var transference = buildSecondTransference();
        transference.setAccount(account);

        var transference2 = buildThirdTransference();
        transference2.setAccount(account);

        return List.of(transference, transference2);
    }

    public static StatementFilter buildFilter() {
        return new StatementFilter(
                ANOTHER_TRANSACTION_OPERATOR_NAME,
                LocalDateTime.of(2023, 7, 25, 0, 1, 0),
                LocalDateTime.of(2023, 7, 25, 0, 10, 0));
    }

    private StatementFilter buildFilterMissingDates() {
        return new StatementFilter(ANOTHER_TRANSACTION_OPERATOR_NAME, null, null);
    }

    private StatementFilter buildFilterMissingTransactionOperatorName() {
        return new StatementFilter(
                null,
                LocalDateTime.of(2023, 7, 25, 0, 1, 0),
                LocalDateTime.of(2023, 7, 25, 0, 10, 0));
    }

    private int generateRandomId() {
        return new Random().nextInt();
    }
}
