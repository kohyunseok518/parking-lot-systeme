package com.parkinglot.domain.ticket;


import com.parkinglot.domain.space.Spot;
import com.parkinglot.domain.vehicle.Size;

import java.time.LocalDateTime;

public class ParkingTicket {
    private Size vehicleSize;
    private Spot spot;
    private LocalDateTime inTime;

    public ParkingTicket(Size vehicleSize, Spot spot, LocalDateTime inTime) {
        this.vehicleSize = vehicleSize;
        this.spot = spot;
        this.inTime = inTime;
    }

    public Spot getSpot() {
        return spot;
    }

    public LocalDateTime getInTime() {
        return inTime;
    }
}
