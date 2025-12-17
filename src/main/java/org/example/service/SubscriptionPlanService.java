package org.example.service;
import org.example.dao.SubscriptionPlanDAO;
import org.example.model.SubscriptionPlan;
import java.util.List;
import java.util.Optional;
public class SubscriptionPlanService {
    private final SubscriptionPlanDAO planDAO;
    public SubscriptionPlanService() {
        this.planDAO = new SubscriptionPlanDAO();
    }
    public SubscriptionPlan createPlan(SubscriptionPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Plano não pode ser nulo");
        }
        if (plan.getName() == null || plan.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do plano é obrigatório");
        }
        Optional<SubscriptionPlan> existing = planDAO.findByName(plan.getName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Já existe um plano com o nome: " + plan.getName());
        }
        return planDAO.save(plan);
    }
    public SubscriptionPlan getPlanById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return planDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado com ID: " + id));
    }
    public List<SubscriptionPlan> getAllPlans() {
        return planDAO.findAll();
    }
    public Optional<SubscriptionPlan> getPlanByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }
        return planDAO.findByName(name);
    }
    public SubscriptionPlan updatePlan(SubscriptionPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Plano não pode ser nulo");
        }
        if (plan.getPlanId() == null) {
            throw new IllegalArgumentException("ID do plano é obrigatório para atualização");
        }
        getPlanById(plan.getPlanId());
        return planDAO.update(plan);
    }
    public void deletePlan(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        boolean deleted = planDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Plano não encontrado com ID: " + id);
        }
    }
}