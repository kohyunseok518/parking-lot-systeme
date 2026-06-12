package com.parkinglot.domain.space;
import com.parkinglot.domain.vehicle.Vehicle;

import java.util.List;

public class ParkingFloor {
    private List<Spot> spots;

    public ParkingFloor(List<Spot> spots) {
        this.spots = spots;
    }

    public synchronized Spot findAvailableSpot(Vehicle vehicle) {
        // 1. 차체 크기에 해당 하는 주차 자리 먼저 찾기
        for (Spot spot : spots) {
            // 1. 자리 찾기
            if(spot.canPark(vehicle)) {
                // 2. 가능하면 주차
                spot.parked();
                return spot;
            }
        }
        return null;
    }

    public synchronized Spot findAvailableBiggerSpot(Vehicle vehicle) {
        // 1. 차체 크기에 해당 하는 주차 자리 먼저 찾기
        for (Spot spot : spots) {
            // 2. 본인보다 더 큰 자리 찾기
            if (spot.canParkBigger(vehicle)) {
                spot.parked();
                return spot;
            }
        }
        return null;
    }
}
