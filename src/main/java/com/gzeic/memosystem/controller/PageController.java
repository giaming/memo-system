package com.gzeic.memosystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    @GetMapping("/memos")
    public String memos() {
        return "memos";
    }
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }
}
