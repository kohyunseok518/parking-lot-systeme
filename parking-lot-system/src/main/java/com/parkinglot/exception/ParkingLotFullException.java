package com.parkinglot.exception;

// 만차일시 발생하는 예외
public class ParkingLotFullException extends RuntimeException {

    public ParkingLotFullException(String message) {
        super(message);
    }
}
