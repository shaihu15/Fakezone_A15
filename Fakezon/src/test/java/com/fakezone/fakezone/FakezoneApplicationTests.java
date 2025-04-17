package com.fakezone.fakezone;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FakezoneApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Application context loads successfully
    }

    @Test
    void whenGetRoot_thenShowHomePage() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Welcome to Our Site")));
    }

    @Test
    void whenGetLogin_thenShowLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Login with OAuth 2.0")));
    }

    @Test
    void whenGetProtectedEndpointWithoutAuth_thenRedirectToLogin() throws Exception {
        mockMvc.perform(get("/protected"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", containsString("/login")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenGetProtectedEndpointWithAuth_thenOk() throws Exception {
        mockMvc.perform(get("/secured"))
            .andExpect(status().isOk());
    }

}