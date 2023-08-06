package br.com.banco.repository;

import br.com.banco.dto.StatementFilter;
import br.com.banco.entity.Account;
import br.com.banco.entity.Transference;
import br.com.banco.entity.Type;
import br.com.banco.specifications.TransferenceSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransferenceRepositoryTest {
    private static final double TRANSFERENCE_VALUE = 50;
    private static final double WITHDRAW_VALUE = -50;

    @Autowired
    private TransferenceRepository transferenceRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Account account;
    private Account anotherAccount;
    private Transference transference;
    private Transference withdraw;

    @BeforeEach
    void setUp() {
        account = testEntityManager.persist(AccountRepositoryTest.buildAccount());
        anotherAccount = testEntityManager.persist(AccountRepositoryTest.buildAnotherAccount());
        transference = testEntityManager.persist(buildTransference());
        testEntityManager.persist(buildTransference2());
        withdraw = testEntityManager.persist(buildWithdraw());
    }

    @Test
    void findAll_NonFiltersPresents_ShouldReturnAllAccountTransference() {
        // given
        Specification<Transference> spec = buildSpecNonFilters();

        // when
        List<Transference> foundTransference = transferenceRepository.findAll(spec);

        // then
        assertFalse(foundTransference.isEmpty());
        assertEquals(2, foundTransference.size());
        assertTrue(foundTransference.contains(transference));
        assertTrue(foundTransference.contains(withdraw));
    }

    @Test
    void findAll_AllFiltersPresents_ShouldReturnAllMatchingTransference() {
        // given
        StatementFilter filter = buildFullFilter();
        Specification<Transference> spec = buildSpecAllFilters(filter);

        // when
        List<Transference> foundTransference = transferenceRepository.findAll(spec);

        // then
        assertFalse(foundTransference.isEmpty());
        assertEquals(1, foundTransference.size());
        assertEquals(transference, foundTransference.get(0));
    }

    @Test
    void findAll_TransactionOperatorNameFilterPresent_ShouldReturnAllMatchingTransference() {
        // given
        StatementFilter filter = buildTransactionOperatorNameFilter();
        Specification<Transference> spec = buildSpecTransactionOperatorNameFilter(filter);

        // when
        List<Transference> foundTransference = transferenceRepository.findAll(spec);

        // then
        assertFalse(foundTransference.isEmpty());
        assertEquals(1, foundTransference.size());
        assertTrue(foundTransference.contains(transference));
        assertFalse(foundTransference.contains(withdraw));
    }

    @Test
    void findAll_DatesFiltersPresents_ShouldReturnAllMatchingTransference() {
        // given
        StatementFilter filter = buildDatesFilters();
        Specification<Transference> spec = buildSpecDateFilter(filter);

        // when
        List<Transference> foundTransference = transferenceRepository.findAll(spec);

        // then
        assertFalse(foundTransference.isEmpty());
        assertEquals(2, foundTransference.size());
        assertTrue(foundTransference.contains(transference));
        assertTrue(foundTransference.contains(withdraw));
    }

    private Specification<Transference> buildSpecDateFilter(StatementFilter filter) {
        return Specification.where(TransferenceSpecifications
                .withAccountId(account.getId())
                .and(TransferenceSpecifications.withInitDateAndEndDate(filter.getInitDate(), filter.getEndDate())));
    }


    private Specification<Transference> buildSpecTransactionOperatorNameFilter(StatementFilter filter) {
        return Specification.where(TransferenceSpecifications
                .withAccountId(account.getId())
                .and(TransferenceSpecifications.withTransactionOperator(filter.getTransactionOperator())));
    }

    private Specification<Transference> buildSpecNonFilters() {
        return TransferenceSpecifications.withAccountId(account.getId());
    }

    private Specification<Transference> buildSpecAllFilters(StatementFilter filter) {
        return Specification.where(TransferenceSpecifications
                .withAccountId(account.getId())
                .and(TransferenceSpecifications.withInitDateAndEndDate(filter.getInitDate(), filter.getEndDate()))
                .and(TransferenceSpecifications.withTransactionOperator(filter.getTransactionOperator())));
    }

    private StatementFilter buildFullFilter() {
        return new StatementFilter(
                anotherAccount.getOwnerName(),
                transference.getTransferenceDate().minusDays(1L),
                LocalDateTime.now().plusDays(1L));
    }

    private StatementFilter buildTransactionOperatorNameFilter() {
        return new StatementFilter(
                anotherAccount.getOwnerName(),
                null,
                null);
    }

    private StatementFilter buildDatesFilters() {
        return new StatementFilter(
                anotherAccount.getOwnerName(),
                LocalDateTime.now().minusMinutes(1L),
                LocalDateTime.now().plusDays(1L));
    }

    private Transference buildTransference() {
        return new Transference(TRANSFERENCE_VALUE, Type.TRANSFERENCE, account, anotherAccount.getOwnerName());
    }

    private Transference buildTransference2() {
        return new Transference(TRANSFERENCE_VALUE, Type.TRANSFERENCE, anotherAccount, account.getOwnerName());
    }

    private Transference buildWithdraw() {
        return new Transference(WITHDRAW_VALUE, Type.WITHDRAW, account, null);
    }
}
