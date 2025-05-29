package com.fakezone.fakezone.controller;

import ApplicationLayer.DTO.DiscountConditionDTO;
import ApplicationLayer.DTO.DiscountRequestDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Response;
import DomainLayer.Model.Cart;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discount")
public class DiscountController {
    private final ISystemService systemService;
    private final AuthenticatorAdapter authenticatorAdapter;
    private static final Logger logger = LoggerFactory.getLogger(DiscountController.class);

    @Autowired
    public DiscountController(ISystemService systemService, AuthenticatorAdapter authenticatorAdapter) {
        this.systemService = systemService;
        this.authenticatorAdapter = authenticatorAdapter;
    }

    private List<Predicate<Cart>> translateConditions(List<DiscountConditionDTO> conditions) {
        return conditions.stream()
            .<Predicate<Cart>>map(condition -> {
                switch (condition.getType().toUpperCase()) {
                    case "CONTAINS_PRODUCT":
                        return (Cart cart) -> cart.containsProduct(condition.getProductId());
                    case "PRODUCT_COUNT_ABOVE":
                        return (Cart cart) -> cart.getAllProducts().values().stream()
                            .mapToInt(basket -> basket.values().stream().mapToInt(Integer::intValue).sum())
                            .sum() >= condition.getThreshold();
                    case "ALWAYS_TRUE":
                        return (Cart cart) -> true;
                    default:
                        throw new IllegalArgumentException("Unknown condition type: " + condition.getType());
                }
            })
            .collect(Collectors.toList());
    }

    @PostMapping("/add/{storeId}/{requesterId}")
    public ResponseEntity<Response<Void>> addDiscount(
            @PathVariable("storeId") int storeId,
            @PathVariable("requesterId") int requesterId,
            @RequestBody DiscountRequestDTO request,
            @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to add discount to store {} by user {} with token {}", storeId, requesterId, token);
            
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }

            List<Predicate<Cart>> translatedConditions = null;
            if (request.getConditions() != null) {
                translatedConditions = translateConditions(request.getConditions());
            }

            Response<Void> response;
            switch (request.getDiscountType().toUpperCase()) {
                case "SIMPLE":
                    if ("PRODUCTS".equals(request.getScope())) {
                        response = systemService.addSimpleDiscountWithProductsScope(storeId, requesterId, request.getProductIds(), request.getPercentage());
                    } else {
                        response = systemService.addSimpleDiscountWithStoreScope(storeId, requesterId, request.getPercentage());
                    }
                    break;

                case "CONDITION":
                    if ("PRODUCTS".equals(request.getScope())) {
                        response = systemService.addConditionDiscountWithProductsScope(storeId, requesterId, 0, request.getProductIds(), translatedConditions, request.getPercentage());
                    } else {
                        response = systemService.addConditionDiscountWithStoreScope(storeId, requesterId, 0, translatedConditions, request.getPercentage());
                    }
                    break;

                case "AND":
                    if ("PRODUCTS".equals(request.getScope())) {
                        response = systemService.addAndDiscountWithProductsScope(storeId, requesterId, 0, request.getProductIds(), translatedConditions, request.getPercentage());
                    } else {
                        response = systemService.addAndDiscountWithStoreScope(storeId, requesterId, 0, translatedConditions, request.getPercentage());
                    }
                    break;

                case "OR":
                    if ("PRODUCTS".equals(request.getScope())) {
                        response = systemService.addOrDiscountWithProductsScope(storeId, requesterId, 0, request.getProductIds(), translatedConditions, request.getPercentage());
                    } else {
                        response = systemService.addOrDiscountWithStoreScope(storeId, requesterId, 0, translatedConditions, request.getPercentage());
                    }
                    break;

                case "XOR":
                    if ("PRODUCTS".equals(request.getScope())) {
                        response = systemService.addXorDiscountWithProductsScope(storeId, requesterId, 0, request.getProductIds(), translatedConditions, request.getPercentage());
                    } else {
                        response = systemService.addXorDiscountWithStoreScope(storeId, requesterId, 0, translatedConditions, request.getPercentage());
                    }
                    break;

                default:
                    response = new Response<>(null, "Invalid discount type", false, ErrorType.INVALID_INPUT, null);
            }

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);

        } catch (Exception e) {
            logger.error("Error in DiscountController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }
} 