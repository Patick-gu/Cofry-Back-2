package org.example.service;
import org.example.dao.UserDAO;
import org.example.dao.SubscriptionPlanDAO;
import org.example.dto.UserCompleteDTO;
import org.example.model.User;
import org.example.model.SubscriptionPlan;
import org.example.service.SubscriptionPlanService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
public class UserService {
    private final UserDAO userDAO;
    private final SubscriptionPlanDAO planDAO;
    public UserService() {
        this.userDAO = new UserDAO();
        this.planDAO = new SubscriptionPlanDAO();
    }
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        validateUser(user);
        Optional<User> existingByEmail = userDAO.findByEmail(user.getEmail());
        if (existingByEmail.isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário com o email: " + user.getEmail());
        }
        Optional<User> existingByTaxId = userDAO.findByTaxId(user.getTaxId());
        if (existingByTaxId.isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário com o CPF: " + user.getTaxId());
        }
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        if (user.getPlanId() == null) {
            user.setPlanId(getOrCreateDefaultPlan());
        }
        return userDAO.save(user);
    }
    private Integer getOrCreateDefaultPlan() {
        Optional<SubscriptionPlan> freePlan = planDAO.findByName("Cofry Start");
        if (freePlan.isPresent()) {
            return freePlan.get().getPlanId();
        }
        Optional<SubscriptionPlan> defaultPlan = planDAO.findById(1);
        if (defaultPlan.isPresent()) {
            return 1;
        }
        SubscriptionPlan newPlan = new SubscriptionPlan();
        newPlan.setName("Cofry Start");
        newPlan.setPrice(BigDecimal.ZERO);
        newPlan.setDescription("Plano gratuito com funcionalidades básicas");
        SubscriptionPlan savedPlan = planDAO.save(newPlan);
        return savedPlan.getPlanId();
    }
    public User getUserById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return userDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));
    }
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }
    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode ser nulo ou vazio");
        }
        return userDAO.findByEmail(email);
    }
    public Optional<User> getUserByTaxId(String taxId) {
        if (taxId == null || taxId.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF não pode ser nulo ou vazio");
        }
        return userDAO.findByTaxId(taxId);
    }
    public List<User> getUsersByStatus(Boolean isActive) {
        return userDAO.findByStatus(isActive);
    }
    public List<User> getUsersByPlan(Integer planId) {
        if (planId == null) {
            throw new IllegalArgumentException("ID do plano não pode ser nulo");
        }
        return userDAO.findByPlanId(planId);
    }
    public User updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório para atualização");
        }
        User existingUser = getUserById(user.getUserId());
        if (user.getPlanId() == null) {
            user.setPlanId(existingUser.getPlanId());
        }
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            user.setFirstName(existingUser.getFirstName());
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            user.setLastName(existingUser.getLastName());
        }
        if (user.getTaxId() == null || user.getTaxId().trim().isEmpty()) {
            user.setTaxId(existingUser.getTaxId());
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            user.setEmail(existingUser.getEmail());
        }
        if (user.getPhoneNumber() == null) {
            user.setPhoneNumber(existingUser.getPhoneNumber());
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            user.setPasswordHash(existingUser.getPasswordHash());
        }
        if (user.getDateOfBirth() == null) {
            user.setDateOfBirth(existingUser.getDateOfBirth());
        }
        if (user.getIsActive() == null) {
            user.setIsActive(existingUser.getIsActive());
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(existingUser.getCreatedAt());
        }
        if (!existingUser.getEmail().equals(user.getEmail())) {
            Optional<User> userWithEmail = userDAO.findByEmail(user.getEmail());
            if (userWithEmail.isPresent() && !userWithEmail.get().getUserId().equals(user.getUserId())) {
                throw new IllegalArgumentException("Já existe um usuário com o email: " + user.getEmail());
            }
        }
        if (!existingUser.getTaxId().equals(user.getTaxId())) {
            Optional<User> userWithTaxId = userDAO.findByTaxId(user.getTaxId());
            if (userWithTaxId.isPresent() && !userWithTaxId.get().getUserId().equals(user.getUserId())) {
                throw new IllegalArgumentException("Já existe um usuário com o CPF: " + user.getTaxId());
            }
        }
        validateUser(user);
        return userDAO.update(user);
    }
    public User changeUserPlan(Integer userId, Integer planId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        if (planId == null) {
            throw new IllegalArgumentException("ID do plano é obrigatório");
        }
        User user = getUserById(userId);
        SubscriptionPlanService planService = new SubscriptionPlanService();
        planService.getPlanById(planId);
        user.setPlanId(planId);
        return userDAO.update(user);
    }
    public void deleteUser(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        boolean deleted = userDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + id);
        }
    }
    public UserCompleteDTO getUserCompleteInfo(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        User user = getUserById(userId);
        AddressService addressService = new AddressService();
        AccountService accountService = new AccountService();
        UserCompleteDTO dto = new UserCompleteDTO();
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
        dto.setUpdatedAt(user.getUpdatedAt());
        try {
            List<org.example.model.Address> addresses = addressService.getAddressesByUserId(userId);
            dto.setAddresses(addresses.stream()
                .map(addr -> {
                    org.example.dto.AddressResponseDTO addrDTO = new org.example.dto.AddressResponseDTO();
                    addrDTO.setAddressId(addr.getAddressId());
                    addrDTO.setUserId(addr.getUserId());
                    addrDTO.setPhoneNumber(user.getPhoneNumber());
                    addrDTO.setZipCode(addr.getZipCode());
                    addrDTO.setHouseNumber(addr.getNumber());
                    addrDTO.setStreet(addr.getStreet());
                    addrDTO.setDistrict(addr.getNeighborhood());
                    addrDTO.setCity(addr.getCity());
                    addrDTO.setState(addr.getState());
                    addrDTO.setComplement(addr.getComplement());
                    addrDTO.setCountry(addr.getCountry());
                    addrDTO.setCreatedAt(addr.getCreatedAt());
                    return addrDTO;
                })
                .collect(Collectors.toList()));
        } catch (Exception e) {
            dto.setAddresses(List.of());
        }
        try {
            List<org.example.model.Account> accounts = accountService.getAccountsByUserId(userId);
            dto.setAccounts(accounts.stream()
                .map(account -> {
                    org.example.dto.AccountResponseDTO accountDTO = new org.example.dto.AccountResponseDTO();
                    accountDTO.setAccountId(account.getAccountId());
                    accountDTO.setUserId(account.getUserId());
                    accountDTO.setBankCode(account.getBankCode());
                    accountDTO.setBankName(account.getBankName());
                    accountDTO.setAccountNumber(account.getAccountNumber());
                    accountDTO.setAgency(account.getAgencyNumber());
                    accountDTO.setAccountType(account.getAccountType() != null ? account.getAccountType().name() : null);
                    accountDTO.setBalance(account.getBalance());
                    accountDTO.setStatus(account.getStatus());
                    accountDTO.setCreatedAt(account.getCreatedAt());
                    return accountDTO;
                })
                .collect(Collectors.toList()));
        } catch (Exception e) {
            dto.setAccounts(List.of());
        }
        return dto;
    }
    private void validateUser(User user) {
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Sobrenome é obrigatório");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (user.getTaxId() == null || user.getTaxId().trim().isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }
        if (user.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória");
        }
    }
}