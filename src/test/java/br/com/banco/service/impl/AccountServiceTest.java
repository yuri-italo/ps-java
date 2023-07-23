package br.com.banco.service.impl;

import br.com.banco.entity.Account;
import br.com.banco.exception.BusinessException;
import br.com.banco.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    private final static Integer ACCOUNT_ID = 1;
    private static final Integer ACCOUNT_ID_TWO = 2;
    private final static String ACCOUNT_NAME = "Elias Santos";
    private static final String ACCOUNT_NAME_TWO = "Carlos Maia";
    private static final String UPDATED_ACCOUNT_NAME = "Antônio Nunes";
    private static final String NON_EXISTING_ID_ERROR_MESSAGE_KEY = "non-existing.id.error.message";
    private static final String SAME_ACCOUNT_ID_ERROR_MESSAGE_KEY = "same.account.id.message.error";
    private static final String EXPECTED_MESSAGE_ERROR_FOR_INVALID_ID = "Invalid ID. The specified ID does not exist " +
            "in our records. Please check and try again.";
    private static final String EXPECTED_MESSAGE_ERROR_FOR_SAME_ACCOUNT_ID = "Source and destination account IDs " +
            "cannot be the same.";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private AccountService accountService;

    @Test
    void save_ValidAccount_ShouldSaveAccountSuccessfully() {
        // given
        var account = buildAccount();

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // when
        var savedAccount = accountService.save(account);

        // then
        verify(accountRepository, times(1)).save(account);
        assertNotNull(savedAccount);
        assertEquals(account.getId(), savedAccount.getId());
        assertEquals(account.getOwnerName(), savedAccount.getOwnerName());
    }

    @Test
    void findById_ExistingAccountId_ShouldReturnAccount() {
        // given
        var account = buildAccount();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        // when
        var foundAccount = accountService.findById(ACCOUNT_ID);

        // then
        verify(accountRepository, times(1)).findById(ACCOUNT_ID);
        assertNotNull(foundAccount);
        assertEquals(ACCOUNT_ID, foundAccount.getId());
        assertEquals(ACCOUNT_NAME, foundAccount.getOwnerName());
    }

    @Test
    void findById_NonExistingAccountId_ShouldThrowBusinessException() {
        // given
        int randomId = generateRandomId();

        when(accountRepository.findById(randomId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(NON_EXISTING_ID_ERROR_MESSAGE_KEY), isNull(), any(Locale.class)))
                .thenReturn(EXPECTED_MESSAGE_ERROR_FOR_INVALID_ID);

        // when
        // then
        var exception = assertThrows(BusinessException.class, () -> accountService.findById(randomId));
        verify(accountRepository, times(1)).findById(randomId);
        assertNotNull(exception);
        assertEquals(EXPECTED_MESSAGE_ERROR_FOR_INVALID_ID, exception.getMessage());
    }

    @Test
    void findAll_ExistingAccountsPersisted_ShouldReturnAllAccounts() {
        // given
        List<Account> accountsList = generateListOfAccounts();

        when(accountRepository.findAll()).thenReturn(accountsList);

        // when
        List<Account> foundAccounts = accountService.findAll();

        // then
        verify(accountRepository, times(1)).findAll();
        assertNotNull(foundAccounts);
        assertEquals(accountsList.size(), foundAccounts.size());
        assertTrue(foundAccounts.contains(accountsList.get(0)));
        assertTrue(foundAccounts.contains(accountsList.get(1)));
    }

    @Test
    void findAll_NonExistingAccountsPersisted_ShouldReturnEmpty() {
        // given
        List<Account> accountsList = Collections.emptyList();

        when(accountRepository.findAll()).thenReturn(accountsList);

        // when
        List<Account> foundAccounts = accountService.findAll();

        // then
        verify(accountRepository, times(1)).findAll();
        assertNotNull(foundAccounts);
        assertTrue(foundAccounts.isEmpty());
    }


    @Test
    public void update_ValidAccount_ShouldUpdateAccountSuccessfully() {
        // given
        var account = buildAccount();
        var updatedAccount = buildUpdatedAccount();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(updatedAccount);

        // when
        var resultAccount = accountService.update(updatedAccount);

        // then
        verify(accountRepository, times(1)).findById(ACCOUNT_ID);
        verify(accountRepository, times(1)).save(account);

        assertNotNull(resultAccount);
        assertEquals(ACCOUNT_ID, resultAccount.getId());
        assertEquals(UPDATED_ACCOUNT_NAME, resultAccount.getOwnerName());
    }

    @Test
    public void update_InvalidAccountId_ShouldThrowBusinessException() {
        // given
        var updatedAccount = buildUpdatedAccount();
        var id = updatedAccount.getId();

        when(accountRepository.findById(id)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(NON_EXISTING_ID_ERROR_MESSAGE_KEY), isNull(), any(Locale.class)))
                .thenReturn(EXPECTED_MESSAGE_ERROR_FOR_INVALID_ID);

        // when
        // then
        var exception = assertThrows(BusinessException.class, () -> accountService.findById(id));

        verify(accountRepository, times(1)).findById(id);
        verify(accountRepository, times(0)).save(any());

        assertNotNull(exception);
        assertEquals(EXPECTED_MESSAGE_ERROR_FOR_INVALID_ID, exception.getMessage());
    }

    @Test
    public void delete_ExistingAccountId_ShouldDeleteAccountSuccessfully() {
        //given
        var account = buildAccount();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        // when
        accountService.delete(ACCOUNT_ID);

        // then
        verify(accountRepository, times(1)).findById(ACCOUNT_ID);
        verify(accountRepository, times(1)).delete(account);
    }

    @Test
    public void delete_NonExistingAccountId_ShouldThrowBusinessException() {
        // given
        var id = generateRandomId();

        when(accountRepository.findById(id)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(NON_EXISTING_ID_ERROR_MESSAGE_KEY), isNull(), any(Locale.class)))
                .thenReturn(EXPECTED_MESSAGE_ERROR_FOR_INVALID_ID);

        // when
        // then
        var exception = assertThrows(BusinessException.class, () -> accountService.delete(id));

        verify(accountRepository, times(1)).findById(id);
        verify(accountRepository, times(0)).delete(any());

        assertNotNull(exception);
        assertEquals(EXPECTED_MESSAGE_ERROR_FOR_INVALID_ID, exception.getMessage());
    }

    @Test
    public void checkEquals_DifferentAccounts_ShouldNotThrowException() {
        // given
        var account = buildAccount();
        var account2 = buildSecondAccount();

        // when
        // then
        assertDoesNotThrow(() -> accountService.checkEquals(account, account2));
    }

    @Test
    public void checkEquals_SameAccounts_ShouldThrowBusinessException() {
        // given
        var account = buildAccount();

        when(messageSource.getMessage(eq(SAME_ACCOUNT_ID_ERROR_MESSAGE_KEY), isNull(), any(Locale.class)))
                .thenReturn(EXPECTED_MESSAGE_ERROR_FOR_SAME_ACCOUNT_ID);
        // when
        // then
        var exception = assertThrows(BusinessException.class, () -> accountService.checkEquals(account, account));

        assertNotNull(exception);
        assertEquals(EXPECTED_MESSAGE_ERROR_FOR_SAME_ACCOUNT_ID, exception.getMessage());
    }

    private int generateRandomId() {
        return new Random().nextInt();
    }

    private List<Account> generateListOfAccounts() {
        return List.of(buildAccount(), buildSecondAccount());
    }

    private Account buildAccount() {
        var account = new Account(ACCOUNT_NAME);
        account.setId(ACCOUNT_ID);

        return account;
    }

    private Account buildSecondAccount() {
        var account = new Account(ACCOUNT_NAME_TWO);
        account.setId(ACCOUNT_ID_TWO);

        return account;
    }

    private Account buildUpdatedAccount() {
        var account = new Account(UPDATED_ACCOUNT_NAME);
        account.setId(ACCOUNT_ID);

        return account;
    }
}
