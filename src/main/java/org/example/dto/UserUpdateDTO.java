package org.example.dto;
import java.time.LocalDate;
public class UserUpdateDTO {
    private String cpf;
    private String email;
    private LocalDate dateOfBirth;
    public UserUpdateDTO() {
    }
    public UserUpdateDTO(String cpf, String email, LocalDate dateOfBirth) {
        this.cpf = cpf;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
    }
    public String getCpf() {
        return cpf;
    }
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}