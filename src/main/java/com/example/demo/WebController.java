package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() { return "redirect:/login"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String register() { return "register"; }

    @GetMapping("/dashboard")
    public String dashboard() { return "dashboard"; }

    @GetMapping("/incidents")
    public String incidents() { return "incidents"; }

    @GetMapping("/hospitals")
    public String hospitals() { return "hospitals"; }

    @GetMapping("/audit")
    public String audit() { return "audit"; }

    @GetMapping("/units")
    public String units() { return "units"; }
}