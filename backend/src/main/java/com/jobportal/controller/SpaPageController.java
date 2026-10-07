package com.jobportal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaPageController {

    @GetMapping({"/", "/sign-in", "/register", "/dashboard", "/dashboard/**"})
    public String forwardToApplication() {
        return "forward:/index.html";
    }
}