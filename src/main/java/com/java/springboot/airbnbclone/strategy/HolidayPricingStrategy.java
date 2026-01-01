package com.java.springboot.airbnbclone.strategy;

import com.java.springboot.airbnbclone.entities.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy{

    private final PricingStrategy strategy;


    @Override
    public BigDecimal calculatePrice(Inventory inventory) {

        boolean isHoliday = true; // try to get this detail via 3rd party API or Local class having holidays defined

        BigDecimal price = strategy.calculatePrice(inventory);

        if(isHoliday){
            price = price.multiply(BigDecimal.valueOf(1.5));
        }

        return price;
    }
}
