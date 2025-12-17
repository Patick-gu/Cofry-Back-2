package org.example.model;
public enum AccountPlan {
    BASIC("Basic", 0.00),
    PREMIUM("Premium", 29.90),
    ENTERPRISE("Enterprise", 99.90);
    private final String name;
    private final double price;
    AccountPlan(String name, double price) {
        this.name = name;
        this.price = price;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
    public static AccountPlan fromString(String planName) {
        if (planName == null) {
            return null;
        }
        for (AccountPlan plan : AccountPlan.values()) {
            if (plan.name.equalsIgnoreCase(planName) || 
                plan.name().equalsIgnoreCase(planName)) {
                return plan;
            }
        }
        return null;
    }
}