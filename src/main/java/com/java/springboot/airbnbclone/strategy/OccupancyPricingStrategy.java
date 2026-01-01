package com.java.springboot.airbnbclone.strategy;

import com.java.springboot.airbnbclone.entities.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class OccupancyPricingStrategy implements PricingStrategy{

    private final PricingStrategy strategy;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {

        BigDecimal price = strategy.calculatePrice(inventory);
        double occupancy = (double) (inventory.getBookedCount()) /(inventory.getTotalCount());

        if (occupancy > 0.8) {
            price = price.multiply(BigDecimal.valueOf(1.2));
        }

        return price;
    }
}
