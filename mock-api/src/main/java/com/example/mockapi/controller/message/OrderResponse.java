package com.example.mockapi.controller.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private long id;
    private String status;
    private List<Long> shipmentIds;
    private List<Long> orderDetailIds;

}
