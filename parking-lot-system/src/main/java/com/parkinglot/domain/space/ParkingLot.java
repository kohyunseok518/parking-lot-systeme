package com.parkinglot.domain.space;

import parkSystem.domain.ticket.ParkingTicket;
import parkSystem.domain.vehicle.Vehicle;
import parkSystem.policy.PricingStrategy;

import java.time.LocalDateTime;
import java.util.List;

public class ParkingLot {
    private List<ParkingFloor> floors;

    public ParkingLot(List<ParkingFloor> floors) {
        this.floors = floors;
    }

    public ParkingTicket parked(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            // 1. 딱 맞는 자리 탐색
            Spot spot = floor.findAvailableSpot(vehicle);
            if(spot != null) {
                return new ParkingTicket(vehicle.getSize(), spot, LocalDateTime.now());
            }

            // 2. 더 큰 자리 탐색
            spot = floor.findAvailableBiggerSpot(vehicle);
            if(spot != null) {
                return new ParkingTicket(vehicle.getSize(), spot, LocalDateTime.now());
            }
        }

        System.out.println("만차입니다.");
        return null;
    }

    public void out(ParkingTicket parkingTicket, PricingStrategy pricingStrategy) {
        LocalDateTime outTime = LocalDateTime.now();
        parkingTicket.getSpot().unParked();
        System.out.println("주차 요금은 " + pricingStrategy.calculatePrice(parkingTicket, outTime));
    }
}
