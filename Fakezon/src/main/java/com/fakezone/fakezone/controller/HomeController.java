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
        String email = extractEmail(authentication);
        model.addAttribute("email", email);
        return "home";
    }

    @GetMapping("/secured")
    public String secured(Authentication authentication, Model model) {
        String email = extractEmail(authentication);
        model.addAttribute("email", email);
        return "secured";
    }

    private String extractEmail(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            Object emailAttr = oAuth2User.getAttribute("email");
            if (emailAttr != null) {
                return emailAttr.toString();
            }
        }
        return null;
    }
}
