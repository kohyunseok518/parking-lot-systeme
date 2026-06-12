package com.parkinglot.domain.space;

import parkSystem.domain.vehicle.Size;
import parkSystem.domain.vehicle.Vehicle;

public class Spot {
    private Size size;
    private boolean isParked;

    public Spot(Size size) {
        this.size = size;
    }

    public boolean canPark(Vehicle vehicle) {
        // 1. 사이즈 및 크기 비교
        if(vehicle.getSize().ordinal() == size.ordinal() && isParked == false) {
            return true;
        } else {
            return false;
        }
    }

    public boolean canParkBigger(Vehicle vehicle) {
        // 1. 사이즈 및 크기 비교
        if(vehicle.getSize().ordinal() < size.ordinal() && isParked == false) {
            return true;
        } else {
            return false;
        }
    }

    // 주차 메서드
    public void parked() {
        isParked = true;
    }

    // 출차 메서드
    public void unParked() {
        isParked = false;
    }
}
