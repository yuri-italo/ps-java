package br.com.banco.repository;

import br.com.banco.entity.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {
    private static final String ACCOUNT_OWNER_NAME = "Carlos Santos";
    private static final String ANOTHER_ACCOUNT_OWNER_NAME = "Maria Maia";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Account account;
    private Account anotherAccount;

    @BeforeEach
    public void setUp() {
        account = testEntityManager.persist(buildAccount());
        anotherAccount = testEntityManager.persist(buildAnotherAccount());
    }

    @Test
    void findById_ValidAccountId_ShouldReturnCorrectAccount() {
        // given
        var accountId = account.getId();

        // when
        Optional<Account> foundAccount = accountRepository.findById(accountId);

        // then
        assertFalse(foundAccount.isEmpty());
        assertEquals(account, foundAccount.get());
    }

    @Test
    void findById_NonExistingAccountId_ShouldReturnEmpty() {
        // given
        var nonExistingId = generateRandomId();

        // when
        Optional<Account> foundAccount = accountRepository.findById(nonExistingId);

        // then
        assertTrue(foundAccount.isEmpty());
    }

    @Test
    void findAll_ExistingAccounts_ShouldReturnAllPersistedAccounts() {
        // given
        // when
        var accountList = accountRepository.findAll();

        // then
        assertFalse(accountList.isEmpty());
        assertEquals(2, accountList.size());
        assertTrue(accountList.contains(account));
        assertTrue(accountList.contains(anotherAccount));
    }

    private int generateRandomId() {
        return new Random().nextInt();
    }


    protected static Account buildAccount() {
        return new Account(ACCOUNT_OWNER_NAME);
    }

    protected static Account buildAnotherAccount() {
        return new Account(ANOTHER_ACCOUNT_OWNER_NAME);
    }
}
