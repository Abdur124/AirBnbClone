package com.java.springboot.airbnbclone.services;

import com.java.springboot.airbnbclone.entities.Hotel;
import com.java.springboot.airbnbclone.entities.HotelMinPrice;
import com.java.springboot.airbnbclone.entities.Inventory;
import com.java.springboot.airbnbclone.repos.HotelMinPriceRepository;
import com.java.springboot.airbnbclone.repos.HotelRepository;
import com.java.springboot.airbnbclone.repos.InventoryRepository;
import com.java.springboot.airbnbclone.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.PropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class PricingUpdateService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private PricingService pricingService;

    @Autowired
    private HotelMinPriceRepository hotelMinPriceRepository;

    //@Scheduled(cron = "0 0 * * * *")
    @Scheduled(cron = "0 */5 * * * *")
    public void updatePrice() {

        int page = 0;
        int batchSize = 100;

        while(true) {
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page, batchSize));

            if (hotelPage.isEmpty()) {
                break;
            }

            hotelPage.getContent().forEach(this::updateHotelPrices);

            page++;
        }
    }

    private void updateHotelPrices(Hotel hotel) {

        log.info("Updating hotel prices for a year for hotel with Id: {}", hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusYears(1);

        List<Inventory> inventories = inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);

        updateInventoryPrices(inventories);

        updateHotelMinPrice(hotel, inventories, startDate, endDate);
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventories, LocalDate startDate, LocalDate endDate) {

        Map<LocalDate, BigDecimal> dailyMinPrices = inventories.stream().collect(Collectors.groupingBy(Inventory::getDate, Collectors.mapping(Inventory::getPrice,
                Collectors.minBy(Comparator.naturalOrder())))).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(BigDecimal.ZERO)));

        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrices.forEach((date, price) -> {
            HotelMinPrice hotelMinPrice = hotelMinPriceRepository.findByHotelAndDate(hotel, date).orElse(new HotelMinPrice(hotel, date));
            hotelMinPrice.setPrice(price);
            hotelPrices.add(hotelMinPrice);
        });

        hotelMinPriceRepository.saveAll(hotelPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventories) {

        log.info("Updating Dynamic Pricing for List of Inventories received of size: {}", inventories.size());
        inventories.forEach(inventory -> {
            BigDecimal price = pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(price);
        });

        inventoryRepository.saveAll(inventories);
    }



}
