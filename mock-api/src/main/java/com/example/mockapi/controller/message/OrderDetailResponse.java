package com.example.mockapi.controller.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {

    private long id;
    private String name;
    private int quantity;
    private String description;

}
