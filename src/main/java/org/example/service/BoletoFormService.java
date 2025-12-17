package org.example.service;
import org.example.dto.BoletoRequestDTO;
import org.example.dto.BoletoResponseDTO;
import org.example.model.Boleto;
import org.example.model.BoletoStatus;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class BoletoFormService {
    private final BoletoService boletoService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    public BoletoFormService() {
        this.boletoService = new BoletoService();
    }
    public BoletoResponseDTO createBoletoFromForm(BoletoRequestDTO boletoDTO) {
        if (boletoDTO == null) {
            throw new IllegalArgumentException("Dados do boleto não podem ser nulos");
        }
        String title = boletoDTO.getTitle();
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        BigDecimal amount = parseAmount(boletoDTO.getAmount());
        LocalDate dueDate = parseDueDate(boletoDTO.getDueDate());
        String bankCode = boletoDTO.getBankCode();
        if (bankCode == null || bankCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Código do banco é obrigatório");
        }
        String walletCode = boletoDTO.getWalletCode();
        if (walletCode == null || walletCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Código da carteira é obrigatório");
        }
        String ourNumber = boletoDTO.getOurNumber();
        if (ourNumber == null || ourNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Nosso número é obrigatório");
        }
        Boleto boleto = boletoService.createBoleto(
            title, amount, dueDate, bankCode, walletCode, ourNumber, boletoDTO.getUserId()
        );
        return convertBoletoToDTO(boleto);
    }
    public List<BoletoResponseDTO> listBoletos() {
        List<Boleto> boletos = boletoService.listBoletos();
        List<BoletoResponseDTO> dtos = new ArrayList<>();
        for (Boleto boleto : boletos) {
            dtos.add(convertBoletoToDTO(boleto));
        }
        return dtos;
    }
    public List<BoletoResponseDTO> listBoletosByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        List<Boleto> boletos = boletoService.listBoletosByUserId(userId);
        List<BoletoResponseDTO> dtos = new ArrayList<>();
        for (Boleto boleto : boletos) {
            dtos.add(convertBoletoToDTO(boleto));
        }
        return dtos;
    }
    public List<BoletoResponseDTO> listBoletosByCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }
        List<Boleto> boletos = boletoService.listBoletosByCpf(cpf);
        List<BoletoResponseDTO> dtos = new ArrayList<>();
        for (Boleto boleto : boletos) {
            dtos.add(convertBoletoToDTO(boleto));
        }
        return dtos;
    }
    public BoletoResponseDTO convertBoletoToDTO(Boleto boleto) {
        BoletoResponseDTO dto = new BoletoResponseDTO();
        dto.setId(boleto.getId());
        dto.setTitle(boleto.getTitle());
        dto.setAmount(boleto.getAmount());
        dto.setFormattedAmount(formatCurrency(boleto.getAmount()));
        dto.setDueDate(boleto.getDueDate());
        dto.setStatus(boleto.getStatus() != null ? boleto.getStatus().name() : null);
        dto.setStatusLabel(getStatusLabel(boleto.getStatus()));
        dto.setBankCode(boleto.getBankCode());
        dto.setWalletCode(boleto.getWalletCode());
        dto.setOurNumber(boleto.getOurNumber());
        dto.setBoletoCode(boleto.getBoletoCode());
        dto.setUserId(boleto.getUserId());
        dto.setPaidAt(boleto.getPaidAt());
        dto.setCreatedAt(boleto.getCreatedAt());
        dto.setUpdatedAt(boleto.getUpdatedAt());
        return dto;
    }
    private BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor é obrigatório");
        }
        try {
            String cleanAmount = amountStr.replace("R$", "")
                                         .replace(".", "")
                                         .replace(",", ".")
                                         .trim();
            BigDecimal amount = new BigDecimal(cleanAmount);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Valor deve ser maior que zero");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de valor inválido: " + amountStr);
        }
    }
    private LocalDate parseDueDate(String dueDateStr) {
        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Data de vencimento é obrigatória");
        }
        try {
            return LocalDate.parse(dueDateStr.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de data inválido. Use YYYY-MM-DD: " + dueDateStr);
        }
    }
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "R$ 0,00";
        }
        return CURRENCY_FORMATTER.format(amount);
    }
    private String getStatusLabel(BoletoStatus status) {
        if (status == null) {
            return "Desconhecido";
        }
        switch (status) {
            case OPEN:
                return "Em aberto";
            case OVERDUE:
                return "Vencido";
            case PAID:
                return "Pago";
            default:
                return status.name();
        }
    }
}