package br.com.banco.repository;

import br.com.banco.entity.Transference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferenceRepository extends JpaRepository<Transference, Integer> {
}
