package com.sarth.walletsim;

import com.sarth.walletsim.entity.Wallet;
import com.sarth.walletsim.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        if (walletRepository.findByUpiId("security.demo@upi").isPresent()) {
            return;
        }

        Wallet wallet = new Wallet();
        wallet.setUserEmail("security.demo@example.com");
        wallet.setMobileNumber("9000000002");
        wallet.setUpiId("security.demo@upi");
        wallet.setBalance(new BigDecimal("250.00"));
        walletRepository.save(wallet);
    }

    @Test
    void walletRoutesRequireJwtAndAdminRoutesRequireAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/wallet/{upiId}", "security.demo@upi"))
                .andExpect(status().isUnauthorized());

        String token = registerAndReadToken();

        mockMvc.perform(get("/api/v1/wallet/{upiId}", "security.demo@upi")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/admin/kyc/1/approve")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String registerAndReadToken() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Security Tester",
                                  "email": "security.tester@example.com",
                                  "password": "Tester@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("token").asText();
    }
}
