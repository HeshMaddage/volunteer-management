package com.volunteer.management.skill;

import com.volunteer.management.auth.Role;
import com.volunteer.management.auth.UserRepository;
import com.volunteer.management.common.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SkillControllerTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    void adminCanCreateAndDeleteSkill() throws Exception {
        String adminToken = registerAsAdmin("skilladmin@example.com");

        MvcResult createResult = mockMvc.perform(post("/api/v1/skills")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Driving"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Driving"))
                .andReturn();

        String skillId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/skills"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/skills/" + skillId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void nonAdminCannotCreateSkill() throws Exception {
        String volunteerToken = registerAsVolunteer("skillvolunteer@example.com");

        mockMvc.perform(post("/api/v1/skills")
                .header("Authorization", "Bearer " + volunteerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Teaching"}
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void skillListIsPubliclyReadable() throws Exception {
        mockMvc.perform(get("/api/v1/skills"))
                .andExpect(status().isOk());
    }

    // --- helpers ---

    private String registerAsAdmin(String email) throws Exception {
        String token = registerAndLogin(email);
        var user = userRepository.findByEmail(email).orElseThrow();
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        return loginOnly(email);
    }

    private String registerAsVolunteer(String email) throws Exception {
        return registerAndLogin(email);
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"password123","fullName":"Test User"}
                        """.formatted(email)))
                .andExpect(status().isOk());
        return loginOnly(email);
    }

    private String loginOnly(String email) throws Exception {
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
