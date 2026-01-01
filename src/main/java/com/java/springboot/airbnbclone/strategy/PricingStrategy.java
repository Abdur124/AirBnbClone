package com.java.springboot.airbnbclone.strategy;

import com.java.springboot.airbnbclone.entities.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
