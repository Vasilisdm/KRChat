package com.example.krchat.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HtmlController() {

    @GetMapping("/")
    fun index(): String {
        // implemented in src/main/resources/templates/chatrs.html
        return "chatrs"
    }
}