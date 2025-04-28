package com.fakezone.fakezone.controller;

import ApplicationLayer.Response;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import ApplicationLayer.Services.SystemService;

@Controller
public class HomeController {

    private final SystemService systemService;

    @Autowired
    public HomeController(SystemService systemService){
        this.systemService = systemService;
    }

    @GetMapping("/")
    public Response<String> home() {
        return new Response<String>("home", "Welcome to the home page!", true);
    }

}
