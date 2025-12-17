package org.example.service;
import org.example.dao.UserDAO;
import org.example.dto.ChangePasswordRequestDTO;
import org.example.dto.LoginRequestDTO;
import org.example.dto.LoginResponseDTO;
import org.example.model.User;
import org.example.utils.EncryptPassword;
import org.example.utils.Validations;
import org.example.utils.ValidateCPF;
import java.util.Optional;
public class AuthService {
    private final UserDAO userDAO;
    public AuthService() {
        this.userDAO = new UserDAO();
    }
    public LoginResponseDTO login(LoginRequestDTO loginDTO) {
        if (loginDTO == null) {
            throw new IllegalArgumentException("Dados de login não podem ser nulos");
        }
        String identifier = loginDTO.getEmail();
        String password = loginDTO.getPassword();
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Email ou CPF é obrigatório");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        Optional<User> userOpt = findUserByIdentifier(identifier);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("Email/CPF ou senha inválidos");
        }
        User user = userOpt.get();
        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new IllegalArgumentException("Usuário inativo. Entre em contato com o suporte");
        }
        String storedHash = user.getPasswordHash();
        if (storedHash == null || storedHash.isEmpty()) {
            throw new IllegalArgumentException("Usuário não possui senha cadastrada");
        }
        String simpleHash = EncryptPassword.encryptSimple(password);
        boolean passwordMatches = simpleHash.equals(storedHash);
        if (!passwordMatches) {
            throw new IllegalArgumentException("Email/CPF ou senha inválidos");
        }
        return createLoginResponse(user);
    }
    private Optional<User> findUserByIdentifier(String identifier) {
        Optional<User> userOpt = userDAO.findByEmail(identifier.trim());
        if (userOpt.isPresent()) {
            return userOpt;
        }
        String cleanCpf = ValidateCPF.unformat(identifier.trim());
        if (ValidateCPF.isValid(cleanCpf)) {
            String formattedCpf = ValidateCPF.format(cleanCpf);
            return userDAO.findByTaxId(formattedCpf);
        }
        return Optional.empty();
    }
    private LoginResponseDTO createLoginResponse(User user) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setUserId(user.getUserId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFirstName() + " " + user.getLastName());
        response.setEmail(user.getEmail());
        response.setCpf(user.getTaxId());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setIsActive(user.getIsActive());
        response.setPlanId(user.getPlanId());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
    public void changePassword(ChangePasswordRequestDTO changePasswordDTO) {
        if (changePasswordDTO == null) {
            throw new IllegalArgumentException("Dados de mudança de senha não podem ser nulos");
        }
        Integer userId = changePasswordDTO.getUserId();
        String currentPassword = changePasswordDTO.getCurrentPassword();
        String newPassword = changePasswordDTO.getNewPassword();
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha atual é obrigatória");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Nova senha é obrigatória");
        }
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("A nova senha deve ser diferente da senha atual");
        }
        Validations.validatePassword(newPassword);
        Optional<User> userOpt = userDAO.findById(userId);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        User user = userOpt.get();
        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new IllegalArgumentException("Usuário inativo. Entre em contato com o suporte");
        }
        String storedHash = user.getPasswordHash();
        if (storedHash == null || storedHash.isEmpty()) {
            throw new IllegalArgumentException("Usuário não possui senha cadastrada");
        }
        String currentPasswordHash = EncryptPassword.encryptSimple(currentPassword);
        if (!currentPasswordHash.equals(storedHash)) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        String newPasswordHash = EncryptPassword.encryptSimple(newPassword);
        user.setPasswordHash(newPasswordHash);
        userDAO.update(user);
    }
}