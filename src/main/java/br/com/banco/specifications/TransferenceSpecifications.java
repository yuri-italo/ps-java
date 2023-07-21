package br.com.banco.specifications;

import br.com.banco.entity.Transference;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TransferenceSpecifications {

    public static Specification<Transference> withAccountId(Integer accountId) {
        return (root, query, builder) -> builder.equal(root.get("account").get("id"), accountId);
    }

    public static Specification<Transference> withInitDateAndEndDate(LocalDateTime initDate, LocalDateTime endDate) {
        return (root, query, builder) -> builder.between(root.get("transferenceDate"), initDate, endDate);
    }

    public static Specification<Transference> withTransactionOperator(String transactionOperator) {
        return (root, query, builder) -> builder.equal(root.get("transactionOperatorName"), transactionOperator);
    }

}

