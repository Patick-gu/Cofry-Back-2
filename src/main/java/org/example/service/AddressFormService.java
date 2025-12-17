package org.example.service;
import org.example.dao.UserDAO;
import org.example.dto.AddressRequestDTO;
import org.example.dto.AddressResponseDTO;
import org.example.model.Address;
import org.example.model.User;
import org.example.utils.ZipCodeValidator;
public class AddressFormService {
    private final AddressService addressService;
    private final UserDAO userDAO;
    public AddressFormService() {
        this.addressService = new AddressService();
        this.userDAO = new UserDAO();
    }
    public AddressResponseDTO createAddressFromForm(AddressRequestDTO addressDTO) {
        if (addressDTO == null) {
            throw new IllegalArgumentException("Address data cannot be null");
        }
        if (addressDTO.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        userDAO.findById(addressDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + addressDTO.getUserId()));
        if (addressDTO.getZipCode() == null || addressDTO.getZipCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Zip code is required");
        }
        if (!ZipCodeValidator.isValidFormat(addressDTO.getZipCode())) {
            throw new IllegalArgumentException("Invalid zip code format");
        }
        String formattedZipCode = ZipCodeValidator.format(addressDTO.getZipCode());
        ZipCodeValidator.ZipCodeInfo zipCodeInfo = ZipCodeValidator.lookupAddress(addressDTO.getZipCode());
        Address address = new Address();
        address.setUserId(addressDTO.getUserId());
        address.setZipCode(formattedZipCode);
        String houseNumber = (addressDTO.getHouseNumber() != null && !addressDTO.getHouseNumber().trim().isEmpty()) 
            ? addressDTO.getHouseNumber().trim() 
            : "S/N";
        address.setNumber(houseNumber);
        address.setCountry("Brazil");
        if (zipCodeInfo != null) {
            address.setStreet(zipCodeInfo.getStreet() != null ? zipCodeInfo.getStreet() : addressDTO.getStreet());
            address.setNeighborhood(zipCodeInfo.getDistrict() != null ? zipCodeInfo.getDistrict() : addressDTO.getDistrict());
            address.setCity(zipCodeInfo.getCity() != null ? zipCodeInfo.getCity() : addressDTO.getCity());
            address.setState(zipCodeInfo.getState() != null ? zipCodeInfo.getState() : addressDTO.getState());
        } else {
            address.setStreet(addressDTO.getStreet());
            address.setNeighborhood(addressDTO.getDistrict());
            address.setCity(addressDTO.getCity());
            address.setState(addressDTO.getState());
        }
        if (address.getStreet() == null || address.getStreet().trim().isEmpty()) {
            throw new IllegalArgumentException("Street is required");
        }
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
        if (address.getState() == null || address.getState().trim().isEmpty()) {
            throw new IllegalArgumentException("State is required");
        }
        address.setComplement(addressDTO.getComplement());
        Address savedAddress = addressService.createAddress(address);
        return convertToResponseDTO(savedAddress);
    }
    public ZipCodeValidator.ZipCodeInfo lookupZipCode(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Zip code cannot be empty");
        }
        if (!ZipCodeValidator.isValidFormat(zipCode)) {
            throw new IllegalArgumentException("Invalid zip code format");
        }
        return ZipCodeValidator.lookupAddress(zipCode);
    }
    private AddressResponseDTO convertToResponseDTO(Address address) {
        AddressResponseDTO dto = new AddressResponseDTO();
        dto.setAddressId(address.getAddressId());
        dto.setUserId(address.getUserId());
        User user = userDAO.findById(address.getUserId()).orElse(null);
        if (user != null) {
            dto.setPhoneNumber(user.getPhoneNumber());
        }
        dto.setZipCode(address.getZipCode());
        dto.setHouseNumber(address.getNumber());
        dto.setStreet(address.getStreet());
        dto.setDistrict(address.getNeighborhood());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setComplement(address.getComplement());
        dto.setCountry(address.getCountry());
        dto.setCreatedAt(address.getCreatedAt());
        return dto;
    }
}