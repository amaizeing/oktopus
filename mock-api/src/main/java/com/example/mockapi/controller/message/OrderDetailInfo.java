package com.example.mockapi.controller.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailInfo {

    private long orderId;
    private long orderDetailId;
    private String description;

}
