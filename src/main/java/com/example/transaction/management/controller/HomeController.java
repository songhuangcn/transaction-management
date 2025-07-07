package com.example.transaction.management.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@Hidden
public class HomeController {

    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("/swagger-ui");
    }
} 