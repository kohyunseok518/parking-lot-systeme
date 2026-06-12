package com.parkinglot.policy;

import parkSystem.domain.ticket.ParkingTicket;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CommonPricingStrategy implements PricingStrategy {
    @Override
    public long calculatePrice(ParkingTicket parkingTicket, LocalDateTime outTime) {
        long minutes = ChronoUnit.MINUTES.between(parkingTicket.getInTime(), outTime);

        long price = minutes * 100;
        return price;
    }
}
