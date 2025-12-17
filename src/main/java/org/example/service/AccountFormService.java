package org.example.service;
import org.example.dto.AccountRequestDTO;
import org.example.dto.AccountResponseDTO;
import org.example.model.Account;
import org.example.model.AccountTypeEnum;
import java.math.BigDecimal;
public class AccountFormService {
    private final AccountService accountService;
    public AccountFormService() {
        this.accountService = new AccountService();
    }
    public AccountResponseDTO createAccountFromForm(AccountRequestDTO accountDTO) {
        if (accountDTO == null) {
            throw new IllegalArgumentException("Account data cannot be null");
        }
        if (accountDTO.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (accountDTO.getAgency() == null || accountDTO.getAgency().trim().isEmpty()) {
            throw new IllegalArgumentException("Agency is required");
        }
        if (accountDTO.getAccountNumber() == null || accountDTO.getAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Account number is required");
        }
        AccountTypeEnum accountType = AccountTypeEnum.CHECKING; 
        if (accountDTO.getAccountType() != null && !accountDTO.getAccountType().trim().isEmpty()) {
            try {
                accountType = AccountTypeEnum.valueOf(accountDTO.getAccountType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid account type. Available types: CHECKING, SAVINGS");
            }
        }
        Account account = new Account();
        account.setUserId(accountDTO.getUserId());
        account.setBankCode(accountDTO.getBankCode());
        account.setBankName(accountDTO.getBankName());
        account.setAccountNumber(accountDTO.getAccountNumber());
        account.setAgencyNumber(accountDTO.getAgency());
        account.setAccountType(accountType);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus("ACTIVE");
        Account savedAccount = accountService.createAccount(account);
        return convertToResponseDTO(savedAccount);
    }
    private AccountResponseDTO convertToResponseDTO(Account account) {
        AccountResponseDTO dto = new AccountResponseDTO();
        dto.setAccountId(account.getAccountId());
        dto.setUserId(account.getUserId());
        dto.setBankCode(account.getBankCode());
        dto.setBankName(account.getBankName());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAgency(account.getAgencyNumber());
        dto.setAccountType(account.getAccountType() != null ? account.getAccountType().name() : null);
        dto.setBalance(account.getBalance());
        dto.setStatus(account.getStatus());
        dto.setCreatedAt(account.getCreatedAt());
        return dto;
    }
}