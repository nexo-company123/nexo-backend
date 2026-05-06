package pl.edu.uj.tp.nexo.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.edu.uj.tp.nexo.auth.dto.AuthenticationRequest;
import pl.edu.uj.tp.nexo.auth.dto.AuthenticationResponse;
import pl.edu.uj.tp.nexo.auth.service.AuthService;
import pl.edu.uj.tp.nexo.security.service.JwtService;
import pl.edu.uj.tp.nexo.auth.dto.AdminRegisterRequest;
import pl.edu.uj.tp.nexo.auth.dto.InvitedRegisterRequest;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void login_returns200AndTokenInJsonResponse() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest(
                "user@example.com",
                "password123"
        );

        when(authService.authenticate(request))
                .thenReturn(new AuthenticationResponse("jwt-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void registerAdmin_returns200AndTokenInJsonResponse() throws Exception {
        AdminRegisterRequest request = new AdminRegisterRequest(
                "Nexo Org",
                "Aleksander",
                "Bury",
                "aleksander.bury@example.com",
                "password123"
        );

        when(authService.registerAdmin(eq(request)))
                .thenReturn(new AuthenticationResponse("admin-token"));

        mockMvc.perform(post("/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin-token"));
    }

    @Test
    void registerInvited_returns200AndTokenInJsonResponse() throws Exception {
        InvitedRegisterRequest request = new InvitedRegisterRequest(
                "valid-invitation-token",
                "Mariola",
                "Kwasniak",
                "password123"
        );

        when(authService.registerInvited(eq(request)))
                .thenReturn(new AuthenticationResponse("invited-token"));

        mockMvc.perform(post("/auth/register-invited")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("invited-token"));
    }
}