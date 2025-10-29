package com.felipejf.banktransactions.service;

import com.felipejf.banktransactions.domain.BankAccount;
import com.felipejf.banktransactions.enums.AccountType;
import com.felipejf.banktransactions.exception.AccountNotFoundException;
import com.felipejf.banktransactions.exception.BusinessException;
import com.felipejf.banktransactions.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    BankAccountRepository bankAccountRepository;

    @InjectMocks
    private BankAccountService bankAccountService;

    private BankAccount bankAccount;

    @BeforeEach
    void setup() {
        bankAccount = new BankAccount();
        bankAccount.setAccountId(UUID.randomUUID());
        bankAccount.setHolderName("Jorge Felipe");
        bankAccount.setBalance(BigDecimal.valueOf(1000));
        bankAccount.setAccountType(AccountType.CHECKING);
        bankAccount.setActive(true);
    }

    @Test
    void shouldCreateAccountSuccessfully() {
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(bankAccount);

            BankAccount created = bankAccountService.createAccount(bankAccount);

            assertNotNull(created);
            assertEquals("Jorge Felipe", created.getHolderName());
            assertEquals(BigDecimal.valueOf(1000), created.getBalance());
            assertTrue(created.isActive());
            verify(bankAccountRepository, times(1)).save(bankAccount);
    }

    @Test
    void shouldReturnListOfAccounts() {
        when(bankAccountRepository.findAll()).thenReturn(List.of(bankAccount));

        List<BankAccount> accounts = bankAccountService.getAccounts();

        assertEquals(1, accounts.size());
        verify(bankAccountRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnAccountById() {
        when(bankAccountRepository.findById(bankAccount.getAccountId())).thenReturn(Optional.of(bankAccount));

        BankAccount result = bankAccountService.getAccountId(bankAccount.getAccountId());

        assertNotNull(result);
        assertEquals(bankAccount.getAccountId(), result.getAccountId());
        verify(bankAccountRepository, times(1)).findById(bankAccount.getAccountId());
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundById(){
        UUID idFalse = UUID.randomUUID();
        when(bankAccountRepository.findById(idFalse)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> bankAccountService.getAccountId(idFalse));
    }

    @Test
    void shouldReturnTrueWhenAccountIsActive() {
        when(bankAccountRepository.findById(bankAccount.getAccountId())).thenReturn(Optional.of(bankAccount));

        boolean result = bankAccountService.isActiveAccount(bankAccount.getAccountId());

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenAccountIsInactive(){
        bankAccount.setActive(false);
        when(bankAccountRepository.findById(bankAccount.getAccountId())).thenReturn(Optional.of(bankAccount));

        boolean result = bankAccountService.isActiveAccount(bankAccount.getAccountId());

        assertFalse(result);
    }

    @Test
    void shouldUpdateAccountSuccessfully() {
        when(bankAccountRepository.findById(bankAccount.getAccountId())).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        BankAccount updated = bankAccountService.updateAccount(bankAccount.getAccountId(), "Jorge Felipe", true);

        assertEquals("Jorge Felipe", updated.getHolderName());
        verify(bankAccountRepository).save(bankAccount);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingInactiveAccount(){
        bankAccount.setActive(false);
        when(bankAccountRepository.findById(bankAccount.getAccountId())).thenReturn(Optional.of(bankAccount));

        assertThrows(BusinessException.class,
                () -> bankAccountService.updateAccount(bankAccount.getAccountId(), "Jorge Felipe", true));
    }

    @Test
    void shouldDeleteActiveAccount() {
        when(bankAccountRepository.findById(bankAccount.getAccountId())).thenReturn(Optional.of(bankAccount));

        bankAccountService.deleteAccount(bankAccount.getAccountId());

        verify(bankAccountRepository).delete(bankAccount);

    }

    @Test
    void shouldThrowExceptionWhenDeletingInactiveAccount(){
        bankAccount.setActive(false);
        when(bankAccountRepository.findById(bankAccount.getAccountId())).thenReturn(Optional.of(bankAccount));

        assertThrows(BusinessException.class,
                () -> bankAccountService.deleteAccount(bankAccount.getAccountId()));

        verify(bankAccountRepository, never()).delete(any());
    }
}