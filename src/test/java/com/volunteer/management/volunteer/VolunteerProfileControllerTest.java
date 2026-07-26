package com.volunteer.management.volunteer;

import com.volunteer.management.common.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VolunteerProfileControllerTest extends IntegrationTestBase {

    @Test
    void volunteerCanUpdateOwnProfileWithSkills() throws Exception {
        String token = registerAndLogin("profile-update@example.com");

        mockMvc.perform(put("/api/v1/volunteers/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {"fullName":"Updated Name","phone":"0771234567","address":"Colombo","bio":"Loves cleanups","skills":["First Aid","Driving"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.skills.length()").value(2));
    }

    @Test
    void gettingProfileWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/volunteers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updatingProfileRejectsBlankFullName() throws Exception {
        String token = registerAndLogin("profile-blank@example.com");

        mockMvc.perform(put("/api/v1/volunteers/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"fullName":"","phone":null,"address":null,"bio":null,"skills":[]}
                        """))
                .andExpect(status().isBadRequest());
    }

    // --- helper ---

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"password123","fullName":"Original Name"}
                        """.formatted(email)))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"password123"}
                        """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
