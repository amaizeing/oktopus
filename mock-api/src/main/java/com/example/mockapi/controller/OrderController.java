package com.example.mockapi.controller;

import com.example.mockapi.controller.message.OrderDetailResponse;
import com.example.mockapi.controller.message.OrderResponse;
import com.example.mockapi.controller.message.ResponseMessage;
import com.example.mockapi.controller.message.ShipmentDetailResponse;
import com.example.mockapi.controller.message.TokenRequest;
import com.example.mockapi.controller.message.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    private final Map<String, String> tokenToUser = new HashMap<>();

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody TokenRequest userInfo) {
        LOGGER.info("Receive login request");
        return ResponseEntity.ok(doLogin(userInfo));
    }

    @PostMapping("/wrap/login")
    public ResponseEntity<Object> wrapLogin(@RequestBody TokenRequest userInfo) {
        LOGGER.info("Receive login request");
        return ResponseEntity.ok(new ResponseMessage<>(doLogin(userInfo), "ok", null));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Object> orderInfo(@PathVariable long orderId, @RequestHeader("Authorization") String token) {
        LOGGER.info("Receive order request");
        final var orderInfo = getOrder(orderId, token);
        if (orderInfo == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderInfo);
    }

    @GetMapping("/wrap/orders/{orderId}")
    public ResponseEntity<Object> wrapOrderInfo(@PathVariable long orderId, @RequestHeader("Authorization") String token) {
        LOGGER.info("Receive order request");
        final var orderInfo = getOrder(orderId, token);
        if (orderInfo == null) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(null, "invalid order id", null));
        }
        return ResponseEntity.ok(new ResponseMessage<>(orderInfo, "ok", null));
    }

    @GetMapping("/order-details/{orderDetailId}")
    public ResponseEntity<Object> orderDetailInfo(@PathVariable long orderDetailId, @RequestHeader("Authorization") String token) {
        LOGGER.info("Receive order detail request");
        final var orderDetail = getOrderDetail(orderDetailId, token);
        if (orderDetail == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderDetail);
    }

    @GetMapping("/wrap/order-details/{orderDetailId}")
    public ResponseEntity<Object> wrapOrderDetailInfo(@PathVariable long orderDetailId, @RequestHeader("Authorization") String token) {
        LOGGER.info("Receive order detail request");
        final var orderDetail = getOrderDetail(orderDetailId, token);
        if (orderDetail == null) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(null, "invalid order detail id", null));
        }
        return ResponseEntity.ok(new ResponseMessage<>(orderDetail, "ok", null));
    }

    @GetMapping("/shipments/{shipmentId}")
    public ResponseEntity<Object> shipmentInfo(@PathVariable long shipmentId, @RequestHeader("Authorization") String token) {
        LOGGER.info("Receive shipment request");
        final var shipment = getShipment(shipmentId, token);
        if (shipment == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(shipment);
    }

    @GetMapping("/wrap/shipments/{shipmentId}")
    public ResponseEntity<Object> wrapShipmentInfo(@PathVariable long shipmentId, @RequestHeader("Authorization") String token) {
        LOGGER.info("Receive shipment request");
        final var shipment = getShipment(shipmentId, token);
        if (shipment == null) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(null, "invalid", null));
        }
        return ResponseEntity.ok(new ResponseMessage<>(shipment, "ok", null));
    }

    private TokenResponse doLogin(TokenRequest userInfo) {
        if (userInfo.getUserName().equals("invalid")) {
            throw new IllegalArgumentException();
        }
        final var token = userInfo.getUserName() + ":" + userInfo.getPassword();
        tokenToUser.put(token, userInfo.getUserName());
        return new TokenResponse(userInfo.getUserName() + ":" + userInfo.getPassword(), 60);
    }

    private OrderResponse getOrder(long orderId, String token) {
        final var user = tokenToUser.get(token);
        if (user == null) {
            return null;
        }
        return new OrderResponse(orderId, "STATUS", List.of(1L, 2L, 3L, 4L), List.of(1L, 2L, 3L, 4L));
    }

    private OrderDetailResponse getOrderDetail(long orderDetailId, String token) {
        final var user = tokenToUser.get(token);
        if (user == null) {
            return null;
        }
        return new OrderDetailResponse(orderDetailId, "item name", 1, "Order detail");
    }

    private ShipmentDetailResponse getShipment(long shipmentId, String token) {
        final var user = tokenToUser.get(token);
        if (user == null) {
            return null;
        }
        return new ShipmentDetailResponse(shipmentId, "Jack", "0123", "432");
    }

}
