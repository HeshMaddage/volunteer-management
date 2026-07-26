package com.volunteer.management.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.volunteer.management.common.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventFilteringTest extends IntegrationTestBase {

    @Test
    void browseFiltersEventsBySkillAndStatus() throws Exception {
        String adminToken = registerAdmin("admin-filter@example.com");

        // Event A: shift requiring "First Aid"
        String eventAId = createEvent(adminToken, "Event A");
        createShift(adminToken, eventAId, "2026-08-01T08:00:00Z", "2026-08-01T12:00:00Z",
                java.util.Set.of("First Aid"));

        // Event B: shift requiring "Cooking"
        String eventBId = createEvent(adminToken, "Event B");
        createShift(adminToken, eventBId, "2026-08-02T08:00:00Z", "2026-08-02T12:00:00Z", java.util.Set.of("Cooking"));

        // Filtering by "First Aid" should return only Event A
        MvcResult result = mockMvc.perform(get("/api/v1/events")
                .header("Authorization", "Bearer " + adminToken)
                .param("skill", "First Aid"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = objectMapper.readTree(result.getResponse().getContentAsString()).get("content");
        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("title").asText()).isEqualTo("Event A");
    }

    @Test
    void browseFiltersEventsByStatus() throws Exception {
        String adminToken = registerAdmin("admin-status@example.com");
        String eventId = createEvent(adminToken, "Cancel Me");

        mockMvc.perform(patch("/api/v1/events/" + eventId + "/cancel")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/v1/events")
                .header("Authorization", "Bearer " + adminToken)
                .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = objectMapper.readTree(result.getResponse().getContentAsString()).get("content");
        boolean containsCancelled = false;
        for (JsonNode node : content) {
            if (node.get("title").asText().equals("Cancel Me"))
                containsCancelled = true;
            assertThat(node.get("status").asText()).isEqualTo("CANCELLED");
        }
        assertThat(containsCancelled).isTrue();
    }

    @Test
    void browseWithNoFiltersReturnsAllEvents() throws Exception {
        String adminToken = registerAdmin("admin-nofilter@example.com");
        createEvent(adminToken, "Unfiltered Event");

        mockMvc.perform(get("/api/v1/events")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // --- helpers ---

    private String registerAdmin(String email) throws Exception {
        String body = """
                {"email":"%s","password":"password123","fullName":"Admin"}
                """.formatted(email);
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        promoteToAdmin(email);

        String loginBody = """
                {"email":"%s","password":"password123"}
                """.formatted(email);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.volunteer.management.auth.UserRepository userRepository;

    private void promoteToAdmin(String email) {
        var user = userRepository.findByEmail(email).orElseThrow();
        user.setRole(com.volunteer.management.auth.Role.ADMIN);
        userRepository.save(user);
    }

    private String createEvent(String token, String title) throws Exception {
        String body = """
                {"title":"%s","description":"desc","location":"loc"}
                """.formatted(title);
        MvcResult result = mockMvc.perform(post("/api/v1/events")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private void createShift(String token, String eventId, String start, String end, java.util.Set<String> skills)
            throws Exception {
        String skillsJson = skills.stream()
                .map(s -> "\"" + s + "\"")
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
        String body = """
                {"startTime":"%s","endTime":"%s","capacity":5,"requiredSkills":%s}
                """.formatted(start, end, skillsJson);
        mockMvc.perform(post("/api/v1/events/" + eventId + "/shifts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }
}
