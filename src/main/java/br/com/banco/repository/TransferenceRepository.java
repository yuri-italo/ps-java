package br.com.banco.repository;

import br.com.banco.entity.Transference;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferenceRepository extends JpaRepository<Transference, Integer> {
    List<Transference> findAll(Specification<Transference> spec);
}
