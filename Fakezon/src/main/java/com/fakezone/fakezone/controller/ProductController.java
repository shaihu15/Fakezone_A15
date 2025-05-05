package com.fakezone.fakezone.controller;

import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Request;
import ApplicationLayer.Response;
import ApplicationLayer.Services.ProductService;
import ApplicationLayer.Services.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ISystemService systemService;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    public ProductController(ISystemService systemService){
        this.systemService = systemService;
    }
    @GetMapping("/getProduct/{id}")
    public ResponseEntity<Response<ProductDTO>> getProdcut(@PathVariable("id") int id, @RequestHeader("Authorization") String token) {
        try{
            logger.info("Received request to get product with ID: {} from user this request tocken of: {}", id, token);
            Response<ProductDTO> response = systemService.getProduct(id);
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            }
            if(response.getErrorType() == ErrorType.INTERNAL_ERROR){
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in ProductController: {}", e.getMessage());
            Response<ProductDTO> response = new Response<>(null, "An error occurred at the controller level", false);
            return ResponseEntity.status(500).body(response);
        }

    }

    @PutMapping("/updateProduct")
    public ResponseEntity<Response<Boolean>> updateProduct(@RequestBody Request<ProductDTO> updatedProduct){
        try{
            ProductDTO productDTO = updatedProduct.getData();
            logger.info("Received request to update product: {} from user this request tocken of: {}", productDTO.getName(), updatedProduct.getToken());

            Response<Boolean> response = systemService.updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoresIds());
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            }
            if(response.getErrorType() == ErrorType.INTERNAL_ERROR){
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        }
        catch (Exception e) {
            logger.error("Error in ProductController: {}", e.getMessage());
            Response<Boolean> response = new Response<>(null, "An error occurred at the controller level", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/deleteProduct")
    public ResponseEntity<Response<Boolean>> deleteProduct(@RequestBody Request<Integer> request) {
        try{
            logger.info("Received request to delete product with ID: {} from user this request tocken of: {}", request.getData(), request.getToken());
            Response<Boolean> response = systemService.deleteProduct(request.getData());
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            }
            if(response.getErrorType() == ErrorType.INTERNAL_ERROR){
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in ProductController: {}", e.getMessage());
            Response<Boolean> response = new Response<>(null, "An error occurred at the controller level", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/searchProducts/{keyword}")
    public ResponseEntity<Response<List<ProductDTO>>> searchProducts(@PathVariable("keyword") String keyword, @RequestHeader("Authorization") String token) {
        try{
            logger.info("Received request to search products with keyword: {} from user this request tocken of: {}", keyword, token);
            Response<List<ProductDTO>> response = systemService.searchByKeyword(keyword, token);
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            }
            if(response.getErrorType() == ErrorType.INTERNAL_ERROR){
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in ProductController: {}", e.getMessage());
            Response<List<ProductDTO>> response = new Response<>(null, "An error occurred at the controller level", false);
            return ResponseEntity.status(500).body(response);
        }
    }

}
