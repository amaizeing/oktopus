package com.example.mockapi.controller.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDetailResponse {

    private long id;
    private String driverName;
    private String phoneNumber;
    private String licensePlate;

}
