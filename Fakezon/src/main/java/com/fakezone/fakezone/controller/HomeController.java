package com.fakezone.fakezone.controller;

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
    public String home() {
        return "home";
    }

}
