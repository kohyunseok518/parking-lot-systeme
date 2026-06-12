package com.parkinglot.domain.vehicle;

public class Vehicle {
    private Size size;

    Vehicle(Size size) {
        this.size = size;
    }

    public Size getSize() {
        return size;
    }
}
