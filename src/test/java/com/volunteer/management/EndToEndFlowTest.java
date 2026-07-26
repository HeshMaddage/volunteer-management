package com.volunteer.management;

import com.fasterxml.jackson.databind.JsonNode;
import com.volunteer.management.auth.Role;
import com.volunteer.management.auth.User;
import com.volunteer.management.auth.UserRepository;
import com.volunteer.management.common.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EndToEndFlowTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    /**
     * Replays the full manual test session: register -> promote to admin ->
     * create event -> create shift (capacity 1) -> volunteer registers ->
     * duplicate registration rejected -> second volunteer blocked by capacity
     * -> cancel frees the slot -> admin marks attendance -> hours appear.
     */
    @Test
    void fullVolunteerJourney() throws Exception {
        // --- Register admin, then promote directly (mirrors our manual DB step) ---
        String adminToken = registerAndLogin("admin@example.com", "password123", "Admin");
        User admin = userRepository.findByEmail("admin@example.com").orElseThrow();
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        adminToken = login("admin@example.com", "password123"); // re-login: token must reflect new role

        String volunteerAToken = registerAndLogin("volA@example.com", "password123", "Volunteer A");
        String volunteerBToken = registerAndLogin("volB@example.com", "password123", "Volunteer B");

        // --- Create event ---
        String eventBody = """
                {"title":"Beach Cleanup","description":"desc","location":"Colombo"}
                """;
        MvcResult eventResult = mockMvc.perform(post("/api/v1/events")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(eventBody))
                .andExpect(status().isOk())
                .andReturn();
        String eventId = jsonField(eventResult, "id");

        // --- Create shift with capacity 1 ---
        String shiftBody = """
                {"startTime":"2026-08-01T08:00:00Z","endTime":"2026-08-01T12:00:00Z","capacity":1,"requiredSkills":[]}
                """;
        MvcResult shiftResult = mockMvc.perform(post("/api/v1/events/" + eventId + "/shifts")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(shiftBody))
                .andExpect(status().isOk())
                .andReturn();
        String shiftId = jsonField(shiftResult, "id");

        // --- Volunteer A registers: 200 ---
        MvcResult regResult = mockMvc.perform(post("/api/v1/shifts/" + shiftId + "/register")
                .header("Authorization", "Bearer " + volunteerAToken))
                .andExpect(status().isOk())
                .andReturn();
        String registrationId = jsonField(regResult, "id");

        // --- Volunteer A registers again: 409 ---
        mockMvc.perform(post("/api/v1/shifts/" + shiftId + "/register")
                .header("Authorization", "Bearer " + volunteerAToken))
                .andExpect(status().isConflict());

        // --- Volunteer B tries: 409 (capacity full) ---
        mockMvc.perform(post("/api/v1/shifts/" + shiftId + "/register")
                .header("Authorization", "Bearer " + volunteerBToken))
                .andExpect(status().isConflict());

        // --- Cancel A's registration: 204 ---
        mockMvc.perform(delete("/api/v1/registrations/" + registrationId)
                .header("Authorization", "Bearer " + volunteerAToken))
                .andExpect(status().isNoContent());

        // --- Volunteer B registers again: 200 (slot freed) ---
        MvcResult regBResult = mockMvc.perform(post("/api/v1/shifts/" + shiftId + "/register")
                .header("Authorization", "Bearer " + volunteerBToken))
                .andExpect(status().isOk())
                .andReturn();
        String registrationBId = jsonField(regBResult, "id");

        // --- Admin marks B as ATTENDED: 200 ---
        mockMvc.perform(patch("/api/v1/registrations/" + registrationBId + "/attendance")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status":"ATTENDED"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATTENDED"));

        // --- Marking again: 400 ---
        mockMvc.perform(patch("/api/v1/registrations/" + registrationBId + "/attendance")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status":"ATTENDED"}
                        """))
                .andExpect(status().isBadRequest());

        // --- Volunteer B's hours reflect the 4-hour shift ---
        mockMvc.perform(get("/api/v1/volunteers/me/hours")
                .header("Authorization", "Bearer " + volunteerBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHours").value(4.0));

        // --- Admin roster shows both registrations ---
        mockMvc.perform(get("/api/v1/events/" + eventId + "/roster")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // --- helpers ---

    private String registerAndLogin(String email, String password, String fullName) throws Exception {
        String body = """
                {"email":"%s","password":"%s","fullName":"%s"}
                """.formatted(email, password, fullName);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk());

        return login(email, password);
    }

    private String login(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andReturn();

        return jsonField(result, "token");
    }

    private String jsonField(MvcResult result, String field) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(node.has(field)).as("expected field '%s' in response", field).isTrue();
        return node.get(field).asText();
    }
}
