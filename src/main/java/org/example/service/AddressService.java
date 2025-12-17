package org.example.service;
import org.example.dao.AddressDAO;
import org.example.dao.UserDAO;
import org.example.model.Address;
import java.util.List;
public class AddressService {
    private final AddressDAO addressDAO;
    private final UserDAO userDAO;
    public AddressService() {
        this.addressDAO = new AddressDAO();
        this.userDAO = new UserDAO();
    }
    public Address createAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Endereço não pode ser nulo");
        }
        validateAddress(address);
        if (address.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        userDAO.findById(address.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + address.getUserId()));
        if (address.getCountry() == null || address.getCountry().trim().isEmpty()) {
            address.setCountry("Brazil");
        }
        return addressDAO.save(address);
    }
    public Address getAddressById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return addressDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Endereço não encontrado com ID: " + id));
    }
    public Address getAddressByIdAndUserId(Integer id, Integer userId) {
        if (id == null) {
            throw new IllegalArgumentException("ID do endereço não pode ser nulo");
        }
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        Address address = getAddressById(id);
        if (!address.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Endereço não encontrado ou não pertence ao usuário especificado");
        }
        return address;
    }
    public List<Address> getAllAddresses() {
        return addressDAO.findAll();
    }
    public List<Address> getAddressesByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return addressDAO.findByUserId(userId);
    }
    public List<Address> getAddressesByCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("Cidade não pode ser nula ou vazia");
        }
        return addressDAO.findByCity(city);
    }
    public List<Address> getAddressesByState(String state) {
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("Estado não pode ser nulo ou vazio");
        }
        return addressDAO.findByState(state);
    }
    public Address updateAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Endereço não pode ser nulo");
        }
        if (address.getAddressId() == null) {
            throw new IllegalArgumentException("ID do endereço é obrigatório para atualização");
        }
        getAddressById(address.getAddressId());
        if (address.getUserId() != null) {
            userDAO.findById(address.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + address.getUserId()));
        }
        validateAddress(address);
        return addressDAO.update(address);
    }
    public void deleteAddress(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        boolean deleted = addressDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Endereço não encontrado com ID: " + id);
        }
    }
    private void validateAddress(Address address) {
        if (address.getStreet() == null || address.getStreet().trim().isEmpty()) {
            throw new IllegalArgumentException("Rua é obrigatória");
        }
        if (address.getNumber() == null || address.getNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Número é obrigatório");
        }
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("Cidade é obrigatória");
        }
        if (address.getState() == null || address.getState().trim().isEmpty()) {
            throw new IllegalArgumentException("Estado é obrigatório");
        }
        if (address.getZipCode() == null || address.getZipCode().trim().isEmpty()) {
            throw new IllegalArgumentException("CEP é obrigatório");
        }
    }
}