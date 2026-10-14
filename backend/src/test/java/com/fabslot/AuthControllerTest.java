package com.fabslot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fabslot.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    void testRegisterStudentSuccess() throws Exception {
        String json = """
            {
                "username": "student1",
                "password": "Password123",
                "email": "student1@example.com",
                "fullName": "Student One",
                "role": "STUDENT"
            }
        """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("student1"))
                .andExpect(jsonPath("$.email").value("student1@example.com"))
                .andExpect(jsonPath("$.fullName").value("Student One"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testRegisterDuplicateUsernameReturns409() throws Exception {
        String json = """
            {
                "username": "dupeuser",
                "password": "Password123",
                "email": "email1@example.com",
                "fullName": "First User",
                "role": "STUDENT"
            }
        """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        String dupeJson = """
            {
                "username": "dupeuser",
                "password": "Password123",
                "email": "email2@example.com",
                "fullName": "Second User",
                "role": "STUDENT"
            }
        """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dupeJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Username already taken")));
    }

    @Test
    void testRegisterDuplicateEmailReturns409() throws Exception {
        String json = """
            {
                "username": "userone",
                "password": "Password123",
                "email": "same@example.com",
                "fullName": "First User",
                "role": "STUDENT"
            }
        """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        String dupeJson = """
            {
                "username": "usertwo",
                "password": "Password123",
                "email": "same@example.com",
                "fullName": "Second User",
                "role": "STUDENT"
            }
        """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dupeJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Email already registered")));
    }

    @Test
    void testRegisterValidationErrorsReturn400() throws Exception {
        String invalidJson = """
            {
                "username": "a",
                "password": "short",
                "email": "not-an-email",
                "fullName": ""
            }
        """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.fullName").exists());
    }

    @Test
    void testAdminRegistrationRequiresSecretCode() throws Exception {
        String invalidAdminJson = """
            {
                "username": "fakeadmin",
                "password": "Password123",
                "email": "fakeadmin@example.com",
                "fullName": "Fake Admin",
                "role": "LAB_ADMIN",
                "adminCode": "wrong-secret"
            }
        """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidAdminJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid admin secret code")));

        String validAdminJson = """
            {
                "username": "realadmin",
                "password": "Password123",
                "email": "realadmin@example.com",
                "fullName": "Real Admin",
                "role": "LAB_ADMIN",
                "adminCode": "fabslot-admin-secret-2026"
            }
        """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validAdminJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("LAB_ADMIN"));
    }

    @Test
    void testLoginSuccessAndFailure() throws Exception {
        // Register user
        String regJson = """
            {
                "username": "loginuser",
                "password": "SecretPassword123",
                "email": "loginuser@example.com",
                "fullName": "Login Tester",
                "role": "STUDENT"
            }
        """;
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(regJson))
                .andExpect(status().isCreated());

        // Correct credentials
        String loginJson = """
            {
                "username": "loginuser",
                "password": "SecretPassword123"
            }
        """;
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.username").value("loginuser"))
                .andExpect(jsonPath("$.fullName").value("Login Tester"))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        // Wrong password -> 401
        String wrongPassJson = """
            {
                "username": "loginuser",
                "password": "WrongPassword!"
            }
        """;
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(wrongPassJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));

        // Non-existent user -> 401
        String nonExistentJson = """
            {
                "username": "unknownuser",
                "password": "SecretPassword123"
            }
        """;
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nonExistentJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void testAuthMeAndLogout() throws Exception {
        // Register & login
        String regJson = """
            {
                "username": "tokenuser",
                "password": "SecretPassword123",
                "email": "tokenuser@example.com",
                "fullName": "Token User",
                "role": "STUDENT"
            }
        """;
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(regJson))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"tokenuser","password":"SecretPassword123"}
                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginNode.get("token").asText();

        // Calling /auth/me with Bearer token
        mockMvc.perform(get("/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tokenuser"))
                .andExpect(jsonPath("$.email").value("tokenuser@example.com"))
                .andExpect(jsonPath("$.fullName").value("Token User"));

        // Calling /auth/me without token -> 403 Forbidden
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isForbidden());

        // Calling /auth/logout
        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        // Now calling /auth/me with blacklisted token fails (403 Forbidden)
        mockMvc.perform(get("/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testStudentCannotAccessAdminEndpoint() throws Exception {
        // Register & login as STUDENT
        String regJson = """
            {
                "username": "studentaccess",
                "password": "Password123",
                "email": "studentaccess@example.com",
                "fullName": "Student Access",
                "role": "STUDENT"
            }
        """;
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(regJson))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"studentaccess","password":"Password123"}
                """))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Student tries to create a machine (requires LAB_ADMIN) -> 403
        String machineJson = """
            {
                "name": "Unauthorized CNC",
                "type": "CNC_ROUTER",
                "status": "AVAILABLE",
                "location": "Room 101"
            }
        """;
        mockMvc.perform(post("/api/machines")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(machineJson))
                .andExpect(status().isForbidden());
    }
}
