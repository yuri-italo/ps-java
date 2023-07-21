package br.com.banco.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "transferencia")
public class Transference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "data_transferencia", nullable = false)
    @CreationTimestamp
    private LocalDateTime transferenceDate;

    @Column(name = "valor", nullable = false)
    private Double value;

    @Column(name = "tipo", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "nome_operador_transacao", length = 50)
    private String transactionOperatorName;

    @ManyToOne
    @JoinColumn(name = "conta_id", referencedColumnName = "id_conta")
    private Account account;

    public Transference(Double value, Type type, Account account, String transactionOperatorName) {
        Objects.requireNonNull(value,"Value is mandatory.");
        Objects.requireNonNull(type,"Type is mandatory.");
        Objects.requireNonNull(account,"Account is mandatory.");
        Objects.requireNonNull(account,"Transaction Operator Name is mandatory.");
        this.value = value;
        this.type = type;
        this.account = account;
        this.transactionOperatorName = transactionOperatorName;
    }

    public String getOwnerName() {
        return this.getAccount().getOwnerName();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Transference that = (Transference) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
