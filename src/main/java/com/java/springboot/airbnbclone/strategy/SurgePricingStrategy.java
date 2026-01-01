package com.java.springboot.airbnbclone.strategy;

import com.java.springboot.airbnbclone.entities.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class SurgePricingStrategy implements PricingStrategy {

    private final PricingStrategy strategy;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = strategy.calculatePrice(inventory);
        price = price.multiply(inventory.getSurgeFactor());
        return price;
    }
}
