package com.fakezone.fakezone.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        String email = null;
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            Object emailAttr = oAuth2User.getAttribute("email");
            if (emailAttr != null) {
                email = emailAttr.toString();
            }
        }
        model.addAttribute("email", email);
        return "home";
    }
}
