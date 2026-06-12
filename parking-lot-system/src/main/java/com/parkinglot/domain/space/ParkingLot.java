package com.parkinglot.domain.space;


import com.parkinglot.domain.ticket.ParkingTicket;
import com.parkinglot.domain.vehicle.Vehicle;
import com.parkinglot.exception.ParkingLotFullException;
import com.parkinglot.policy.PricingStrategy;

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

        // Domain 클래스에서 시스템 호출로 답변하는 것이 아닌 사용자 정의 예외를 던짐
        throw new ParkingLotFullException("현재 주차 가능한 자리가 없습니다.");
    }

    public long out(ParkingTicket parkingTicket, PricingStrategy pricingStrategy) {
        LocalDateTime outTime = LocalDateTime.now();
        parkingTicket.getSpot().unParked();
        return pricingStrategy.calculatePrice(parkingTicket, outTime);
    }
}
