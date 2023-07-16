package br.com.banco.service;

import br.com.banco.entity.Transference;

import java.util.List;

public interface ITransferenceService {
    Transference save(Transference transference);
    Transference findById(Integer id);
    List<Transference> findAll();
    void delete(Integer id);
}
