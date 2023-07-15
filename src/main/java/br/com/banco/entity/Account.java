package br.com.banco.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Data
@NoArgsConstructor
@Entity
@Table(name = "conta")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conta", nullable = false)
    private Integer id;

    @Column(name = "nome_responsavel", length = 50)
    private String ownerName;

    public Account(String ownerName, Integer id) {
        Objects.requireNonNull(ownerName,"Owner name is mandatory.");
        Objects.requireNonNull(id, "ID is mandatory.");
        this.id = id;
        this.ownerName = ownerName;
    }
}
