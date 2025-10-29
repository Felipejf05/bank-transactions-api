package com.felipejf.banktransactions.service;

import com.felipejf.banktransactions.domain.BankAccount;
import com.felipejf.banktransactions.exception.AccountNotFoundException;
import com.felipejf.banktransactions.exception.BusinessException;
import com.felipejf.banktransactions.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountService {

    private final BankAccountRepository repository;

    public BankAccount createAccount(BankAccount bankAccount) {
        log.info("Iniciando a criação da conta.. ");
        return repository.save(bankAccount);
    }

    public List<BankAccount> getAccounts() {
        log.info("Retornando a lista das contas..");
        return repository.findAll();
    }

    public BankAccount getAccountId(UUID uuid) {
        log.info("Buscando a conta pelo ID: {}", uuid);
        return repository.findById(uuid).orElseThrow(() -> new AccountNotFoundException("Conta com o ID: " + uuid + "não localizada"));
    }

    public boolean isActiveAccount(UUID accountId) {
        log.info("Verificando a situação da conta com o ID: {}", accountId);

        BankAccount account = repository.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Conta não encontrada: " + accountId));

        if (!account.isActive()) {
            log.info("A conta com o ID: {} está bloqueada", accountId);
            return false;
        }
        log.info("A conta com o ID: {} está ativa", accountId);
        return true;
    }

    public BankAccount updateAccount(UUID idAccount, String newHolderName, boolean newStatus){
        log.info("Atualizando conta com o ID: {}", idAccount);

        BankAccount account = repository.findById(idAccount)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada: " + idAccount));

        if(!account.isActive()){
            throw new BusinessException("Conta bloqueada não pode ser atualizada");
        }

        account.setHolderName(newHolderName);
        account.setActive(newStatus);

        BankAccount update = repository.save(account);
        log.info("Conta atualizada com sucesso: {}", update.getAccountId());

        return update;
    }

    public void deleteAccount(UUID uuid){
        log.info("Deletando conta de ID: {}", uuid);

        BankAccount account = repository.findById(uuid)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada: " + uuid));
        if(!account.isActive()){
            throw new BusinessException("Sua conta não pode ser encerrada porque está bloqueada. Por favor, entre em contato com seu gerente para mais informações.");
        }

        repository.delete(account);
        log.info("Conta {}", uuid, "encerrada com sucesso");
    }
}
