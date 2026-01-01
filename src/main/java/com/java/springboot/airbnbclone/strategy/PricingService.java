package com.java.springboot.airbnbclone.strategy;

import com.java.springboot.airbnbclone.entities.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {

    public BigDecimal calculateDynamicPricing(Inventory inventory) {

        PricingStrategy strategy = new BasePricingStrategy();

        // apply additional strategies

        strategy = new SurgePricingStrategy(strategy);
        strategy = new OccupancyPricingStrategy(strategy);
        strategy = new HolidayPricingStrategy(strategy);
        return strategy.calculatePrice(inventory);
    }
}
