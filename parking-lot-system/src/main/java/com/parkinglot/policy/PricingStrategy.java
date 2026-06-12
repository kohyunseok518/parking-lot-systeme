package com.parkinglot.policy;

import parkSystem.domain.ticket.ParkingTicket;

import java.time.LocalDateTime;

public interface PricingStrategy {
    public long calculatePrice(ParkingTicket parkingTicket, LocalDateTime outTime);
}
