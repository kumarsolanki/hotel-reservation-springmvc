package com.demo.domain;

import com.demo.util.Utils;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
public class MealPlan implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private UUID mealPlanId = UUID.randomUUID();

    @OneToOne
    private Guest guest;

    @OneToOne
    private Reservation reservation;

    // No CascadeType as Extra already has an id associated to it.
    @ManyToMany
    private List<Extra> foodExtras = new ArrayList<>();

    @ElementCollection
    private List<DietaryRequirement> dietaryRequirements = new ArrayList<>();

    public MealPlan() {
    }

    public MealPlan(Guest guest, Reservation reservation, List<Extra> foodExtras,
                    List<DietaryRequirement> dietaryRequirements) {
        this.guest = guest;
        this.reservation = reservation;
        this.foodExtras = foodExtras;
        this.dietaryRequirements = dietaryRequirements;
    }

    public UUID getMealPlanId() {
        return mealPlanId;
    }

    public Long getId() {
        return id;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public List<Extra> getFoodExtras() {
        return foodExtras;
    }

    public void setFoodExtras(List<Extra> foodExtras) {
        this.foodExtras = foodExtras;
    }

    public List<DietaryRequirement> getDietaryRequirements() {
        return dietaryRequirements;
    }

    public void setDietaryRequirements(List<DietaryRequirement> dietaryRequirements) {
        this.dietaryRequirements = dietaryRequirements;
    }

    public boolean isEmpty() {
        return foodExtras.isEmpty() && dietaryRequirements.isEmpty();
    }

    /**
     * @return The sum result of multiplying each food extra by total nights while applying
     * any child discounts to the total sum.
     */
    public BigDecimal getTotalMealPlanCost() {
        BigDecimal total = foodExtras.stream()
                .map(extra -> extra.getTotalPrice(reservation.getDates().totalNights()))
                .reduce(BigDecimal.ZERO, BigDecimal::add, BigDecimal::add);

        if (guest.isChild()) {
            BigDecimal discount = total.multiply(BigDecimal.valueOf(Reservation.CHILD_DISCOUNT_PERCENT));
            return total.subtract(discount);
        }
        return total;
    }

    public boolean hasFoodExtras() {
        return !foodExtras.isEmpty();
    }

    public boolean hasDietRequirements() {
        return !dietaryRequirements.isEmpty();
    }

    public String toFoodExtraCsv() {
        return Utils.toCsv(foodExtras, Extra::getDescription);
    }

    public String toDietRequirementsCsv() {
        return Utils.toCsv(dietaryRequirements, DietaryRequirement::getDescription);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MealPlan mealPlan = (MealPlan) o;
        return Objects.equals(mealPlanId, mealPlan.mealPlanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mealPlanId);
    }
}