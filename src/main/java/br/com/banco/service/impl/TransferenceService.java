package br.com.banco.service.impl;

import br.com.banco.entity.Transference;
import br.com.banco.repository.TransferenceRepository;
import br.com.banco.service.ITransferenceService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class TransferenceService implements ITransferenceService {
    private final TransferenceRepository transferenceRepository;
    private final MessageSource messageSource;

    public TransferenceService(TransferenceRepository transferenceRepository, MessageSource messageSource) {
        this.transferenceRepository = transferenceRepository;
        this.messageSource = messageSource;
    }

    @Override
    public Transference save(Transference transference) {
        return transferenceRepository.save(transference);
    }

    @Override
    public Transference findById(Integer id) {
        return transferenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("non-existing.id.error.message",null, Locale.getDefault())
                ));
    }

    @Override
    public List<Transference> findAll() {
        return transferenceRepository.findAll();
    }

    @Override
    public void delete(Integer id) {
        Transference transference = this.findById(id);
        transferenceRepository.delete(transference);
    }
}