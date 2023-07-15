package br.com.banco.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Entity
@NoArgsConstructor
@Table(name = "transferencia")
public class Transference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "data_transferencia", nullable = false)
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

    public Transference(Integer id, LocalDateTime transferenceDate, Double value, Type type, Account account) {
        Objects.requireNonNull(id,"ID is mandatory.");
        Objects.requireNonNull(transferenceDate,"Transference date is mandatory.");
        Objects.requireNonNull(value,"Value is mandatory.");
        Objects.requireNonNull(type,"Type is mandatory.");
        Objects.requireNonNull(account,"Account is mandatory.");
        this.id = id;
        this.transferenceDate = transferenceDate;
        this.value = value;
        this.type = type;
        this.account = account;
    }
}
