package org.example.service;
import org.example.dto.UserRequestDTO;
import org.example.dto.UserResponseDTO;
import org.example.dto.UserUpdateDTO;
import org.example.model.User;
import org.example.model.Account;
import org.example.model.AccountTypeEnum;
import org.example.model.Card;
import org.example.model.CardTypeEnum;
import org.example.utils.EncryptPassword;
import org.example.utils.ValidateCPF;
import org.example.utils.Validations;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
public class UserFormService {
    private final UserService userService;
    private final AccountService accountService;
    private final CardService cardService;
    private final BoletoService boletoService;
    private static final Random random = new Random();
    public UserFormService() {
        this.userService = new UserService();
        this.accountService = new AccountService();
        this.cardService = new CardService();
        this.boletoService = new BoletoService();
    }
    public UserResponseDTO createUserFromForm(UserRequestDTO userDTO) {
        if (userDTO == null) {
            throw new IllegalArgumentException("User data cannot be null");
        }
        if (userDTO.getFullName() == null || userDTO.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        String[] nameParts = splitFullName(userDTO.getFullName());
        String firstName = nameParts[0];
        String lastName = nameParts[1];
        if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        Validations.validateEmail(userDTO.getEmail());
        if (userDTO.getCpf() == null || userDTO.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("CPF is required");
        }
        String cleanCpf = ValidateCPF.unformat(userDTO.getCpf());
        if (!ValidateCPF.isValid(cleanCpf)) {
            throw new IllegalArgumentException("Invalid CPF");
        }
        String formattedCpf = ValidateCPF.format(cleanCpf);
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        String passwordHash = EncryptPassword.encryptSimple(userDTO.getPassword());
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setTaxId(formattedCpf);
        user.setEmail(userDTO.getEmail());
        user.setPasswordHash(passwordHash);
        user.setDateOfBirth(LocalDate.now()); 
        user.setIsActive(true);
        User savedUser = userService.createUser(user);
        Account createdAccount = createInitialAccountForUser(savedUser.getUserId());
        createCofryCardForUser(savedUser, createdAccount);
        createInitialBoletosForUser(savedUser.getUserId());
        return convertToResponseDTO(savedUser);
    }
    private String[] splitFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        String trimmedName = fullName.trim();
        String[] parts = trimmedName.split("\\s+", 2);
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        }
        return new String[]{parts[0], parts[1]};
    }
    public UserResponseDTO updateUserFromForm(Integer userId, UserUpdateDTO updateDTO) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        if (updateDTO == null) {
            throw new IllegalArgumentException("Dados de atualização não podem ser nulos");
        }
        User existingUser = userService.getUserById(userId);
        boolean hasChanges = false;
        if (updateDTO.getCpf() != null && !updateDTO.getCpf().trim().isEmpty()) {
            String cleanCpf = ValidateCPF.unformat(updateDTO.getCpf());
            if (!ValidateCPF.isValid(cleanCpf)) {
                throw new IllegalArgumentException("CPF inválido");
            }
            String formattedCpf = ValidateCPF.format(cleanCpf);
            if (!existingUser.getTaxId().equals(formattedCpf)) {
                userService.getUserByTaxId(formattedCpf)
                    .ifPresent(user -> {
                        if (!user.getUserId().equals(userId)) {
                            throw new IllegalArgumentException("Já existe um usuário com o CPF: " + formattedCpf);
                        }
                    });
                existingUser.setTaxId(formattedCpf);
                hasChanges = true;
            }
        }
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().trim().isEmpty()) {
            Validations.validateEmail(updateDTO.getEmail());
            if (!existingUser.getEmail().equals(updateDTO.getEmail())) {
                userService.getUserByEmail(updateDTO.getEmail())
                    .ifPresent(user -> {
                        if (!user.getUserId().equals(userId)) {
                            throw new IllegalArgumentException("Já existe um usuário com o email: " + updateDTO.getEmail());
                        }
                    });
                existingUser.setEmail(updateDTO.getEmail());
                hasChanges = true;
            }
        }
        if (updateDTO.getDateOfBirth() != null) {
            if (!updateDTO.getDateOfBirth().equals(existingUser.getDateOfBirth())) {
                existingUser.setDateOfBirth(updateDTO.getDateOfBirth());
                hasChanges = true;
            }
        }
        if (!hasChanges) {
            return convertToResponseDTO(existingUser);
        }
        User updatedUser = userService.updateUser(existingUser);
        return convertToResponseDTO(updatedUser);
    }
    private Account createInitialAccountForUser(Integer userId) {
        try {
            Account initialAccount = new Account();
            initialAccount.setUserId(userId);
            initialAccount.setBankCode("999"); 
            initialAccount.setBankName("Cofry");
            initialAccount.setAccountNumber(generateAccountNumber(userId));
            initialAccount.setAgencyNumber("0001");
            initialAccount.setAccountType(AccountTypeEnum.CHECKING);
            initialAccount.setBalance(new BigDecimal("20000.00")); 
            initialAccount.setStatus("ACTIVE");
            return accountService.createAccount(initialAccount);
        } catch (Exception e) {
            System.err.println("Erro ao criar conta inicial para usuário " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    private void createCofryCardForUser(User user, Account account) {
        try {
            if (account == null) {
                System.err.println("Conta não encontrada para criar cartão. Tentando buscar...");
                List<Account> accounts = accountService.getAccountsByUserId(user.getUserId());
                if (accounts != null && !accounts.isEmpty()) {
                    account = accounts.get(0);
                } else {
                    System.err.println("Nenhuma conta encontrada para usuário " + user.getUserId());
                    return;
                }
            }
            String cardNumber = generateVisaCardNumber();
            String cvv = generateCVV();
            LocalDate expiryDate = LocalDate.now().plusYears(4);
            String cardHolderName = (user.getFirstName() + " " + user.getLastName()).toUpperCase().trim();
            String maskedCardNumber = maskCardNumber(cardNumber);
            Card cofryCard = new Card();
            cofryCard.setUserId(user.getUserId());
            cofryCard.setAccountId(account.getAccountId());
            cofryCard.setCardNumber(maskedCardNumber); 
            cofryCard.setCardHolderName(cardHolderName);
            cofryCard.setExpiryDate(expiryDate);
            cofryCard.setCvv(cvv);
            cofryCard.setCardType(CardTypeEnum.CREDIT); 
            cofryCard.setBrand("Visa");
            cofryCard.setLimitAmount(new BigDecimal("10000.00")); 
            cofryCard.setCurrentBalance(BigDecimal.ZERO);
            cofryCard.setStatus("ACTIVE");
            cardService.createCard(cofryCard);
            System.out.println("Cartão Cofry criado com sucesso para usuário " + user.getUserId());
        } catch (Exception e) {
            System.err.println("Erro ao criar cartão Cofry para usuário " + user.getUserId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    private String generateVisaCardNumber() {
        StringBuilder cardNumber = new StringBuilder("4"); 
        for (int i = 0; i < 14; i++) {
            cardNumber.append(random.nextInt(10));
        }
        String cardNumberStr = cardNumber.toString();
        int checkDigit = calculateLuhnCheckDigit(cardNumberStr);
        cardNumber.append(checkDigit);
        return cardNumber.toString();
    }
    private int calculateLuhnCheckDigit(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit;
    }
    private String generateCVV() {
        int cvv = 100 + random.nextInt(900); 
        return String.format("%03d", cvv);
    }
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
        if (cleanNumber.length() < 4) {
            return cardNumber;
        }
        String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }
    private String generateAccountNumber(Integer userId) {
        return String.format("COF-%05d-1", userId);
    }
    private void createInitialBoletosForUser(Integer userId) {
        try {
            List<org.example.model.Boleto> existingBoletos = boletoService.listBoletos();
            Set<String> existingOurNumbers = new HashSet<>();
            for (org.example.model.Boleto b : existingBoletos) {
                if (b.getOurNumber() != null) {
                    existingOurNumbers.add(b.getOurNumber());
                }
            }
            String[][] boletosData = {
                {"Claro Residencial", "149.90", "001", "17", "30"},
                {"Seguro Auto", "250.00", "341", "109", "45"},
                {"Internet Fibra", "89.90", "033", "26", "15"},
                {"Energia Elétrica", "180.50", "104", "109", "60"},
                {"Plano de Saúde", "350.00", "001", "17", "75"}
            };
            for (int i = 0; i < boletosData.length; i++) {
                String title = boletosData[i][0];
                BigDecimal amount = new BigDecimal(boletosData[i][1]);
                String bankCode = boletosData[i][2];
                String walletCode = boletosData[i][3];
                int daysToDue = Integer.parseInt(boletosData[i][4]);
                LocalDate dueDate = LocalDate.now().plusDays(daysToDue);
                String ourNumber = generateUniqueOurNumber(userId, i, existingOurNumbers);
                existingOurNumbers.add(ourNumber);
                try {
                    boletoService.createBoleto(title, amount, dueDate, bankCode, walletCode, ourNumber, userId);
                    System.out.println("Boleto criado com sucesso: " + title + " para usuário " + userId);
                } catch (Exception e) {
                    System.err.println("Erro ao criar boleto " + title + " para usuário " + userId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao criar boletos iniciais para usuário " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    private String generateUniqueOurNumber(Integer userId, int index, Set<String> existingOurNumbers) {
        String baseOurNumber = String.format("%05d%02d%010d", userId, index, System.currentTimeMillis() % 10000000000L);
        if (baseOurNumber.length() > 23) {
            baseOurNumber = baseOurNumber.substring(0, 23);
        }
        String ourNumber = baseOurNumber;
        int attempts = 0;
        while (existingOurNumbers.contains(ourNumber) && attempts < 100) {
            ourNumber = baseOurNumber + String.format("%03d", random.nextInt(1000));
            if (ourNumber.length() > 23) {
                ourNumber = ourNumber.substring(0, 23);
            }
            attempts++;
        }
        return ourNumber;
    }
    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(user.getFirstName() + " " + user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setCpf(user.getTaxId());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setIsActive(user.getIsActive());
        dto.setPlanId(user.getPlanId());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}