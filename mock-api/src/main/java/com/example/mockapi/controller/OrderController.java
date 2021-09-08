package com.example.mockapi.controller;

import com.example.mockapi.controller.message.OrderDetailInfo;
import com.example.mockapi.controller.message.OrderInfo;
import com.example.mockapi.controller.message.ResponseMessage;
import com.example.mockapi.controller.message.TokenResponse;
import com.example.mockapi.controller.message.UserInfo;
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
    public ResponseEntity<Object> login(@RequestBody UserInfo userInfo) {
        LOGGER.info("Receive login request");
        return ResponseEntity.ok(doLogin(userInfo));
    }

    @PostMapping("/wrap/login")
    public ResponseEntity<Object> wrapLogin(@RequestBody UserInfo userInfo) {
        LOGGER.info("Receive login request");
        return ResponseEntity.ok(new ResponseMessage<>(doLogin(userInfo), "ok", null));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Object> orderInfo(@PathVariable long orderId, @RequestHeader("x-token") String token) {
        LOGGER.info("Receive order request");
        final var orderInfo = getOrder(orderId, token);
        if (orderInfo == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderInfo);
    }

    @GetMapping("/wrap/orders/{orderId}")
    public ResponseEntity<Object> wrapOrderInfo(@PathVariable long orderId, @RequestHeader("x-token") String token) {
        LOGGER.info("Receive order request");
        final var orderInfo = getOrder(orderId, token);
        if (orderInfo == null) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(null, "invalid order id", null));
        }
        return ResponseEntity.ok(new ResponseMessage<>(orderInfo, "ok", null));
    }

    @GetMapping("/orders/{orderId}/details/{orderDetailId}")
    public ResponseEntity<Object> orderDetailInfo(@PathVariable long orderId, @PathVariable long orderDetailId, @RequestHeader("x-token") String token) {
        LOGGER.info("Receive order detail request");
        final var orderDetail = getOrderDetail(orderId, orderDetailId, token);
        if (orderDetail == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderDetail);
    }

    @GetMapping("/wrap/orders/{orderId}/details/{orderDetailId}")
    public ResponseEntity<Object> wrapOrderDetailInfo(@PathVariable long orderId, @PathVariable long orderDetailId, @RequestHeader("x-token") String token) {
        LOGGER.info("Receive order detail request");
        final var orderDetail = getOrderDetail(orderId, orderDetailId, token);
        if (orderDetail == null) {
            return ResponseEntity.badRequest().body(new ResponseMessage<>(null, "invalid order detail id", null));
        }
        return ResponseEntity.ok(new ResponseMessage<>(orderDetail, "ok", null));
    }

    private TokenResponse doLogin(UserInfo userInfo) {
        if (userInfo.getUserName().equals("invalid")) {
            throw new IllegalArgumentException();
        }
        final var token = userInfo.getUserName() + ":" + userInfo.getPassword();
        tokenToUser.put(token, userInfo.getUserName());
        return new TokenResponse(userInfo.getUserName() + ":" + userInfo.getPassword(), 60);
    }

    private OrderInfo getOrder(long orderId, String token) {
        final var user = tokenToUser.get(token);
        if (user == null) {
            return null;
        }
        return new OrderInfo(orderId, List.of(1L, 2L, 3L, 4L));
    }

    private OrderDetailInfo getOrderDetail(long orderId, long orderDetailId, String token) {
        final var user = tokenToUser.get(token);
        if (user == null) {
            return null;
        }
        return new OrderDetailInfo(orderId, orderDetailId, "Order detail");
    }


}
