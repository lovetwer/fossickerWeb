package com.fossicker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, FossickerWeb!";
    }

    @GetMapping("/")
    public String index() {
        return "Welcome to FossickerWeb!";
    }
}
