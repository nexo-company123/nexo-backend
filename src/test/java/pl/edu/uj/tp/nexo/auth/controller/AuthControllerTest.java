package pl.edu.uj.tp.nexo.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import pl.edu.uj.tp.nexo.auth.dto.AdminRegisterRequest;
import pl.edu.uj.tp.nexo.auth.dto.AuthenticationRequest;
import pl.edu.uj.tp.nexo.auth.dto.AuthenticationResponse;
import pl.edu.uj.tp.nexo.auth.dto.InvitedRegisterRequest;
import pl.edu.uj.tp.nexo.auth.service.AuthService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final AuthController authController = new AuthController(authService);

    @Test
    void registerAdmin_returnsAuthenticationResponseFromService() {
        AdminRegisterRequest request = new AdminRegisterRequest(
                "Nexo Org",
                "Jan",
                "Kowalski",
                "jan@example.com",
                "password123"
        );
        AuthenticationResponse expectedResponse = new AuthenticationResponse("admin-token");

        when(authService.registerAdmin(request)).thenReturn(expectedResponse);

        ResponseEntity response = authController.registerAdmin(request);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expectedResponse, response.getBody());
        verify(authService).registerAdmin(request);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void registerInvited_returnsAuthenticationResponseFromService() {
        InvitedRegisterRequest request = new InvitedRegisterRequest(
                "invitation-token",
                "Anna",
                "Nowak",
                "password123"
        );
        AuthenticationResponse expectedResponse = new AuthenticationResponse("invited-token");

        when(authService.registerInvited(request)).thenReturn(expectedResponse);

        ResponseEntity response = authController.registerInvited(request);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expectedResponse, response.getBody());
        verify(authService).registerInvited(request);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void authenticate_returnsAuthenticationResponseFromService() {
        AuthenticationRequest request = new AuthenticationRequest(
                "anna@example.com",
                "password123"
        );
        AuthenticationResponse expectedResponse = new AuthenticationResponse("login-token");

        when(authService.authenticate(request)).thenReturn(expectedResponse);

        ResponseEntity response = authController.authenticate(request);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expectedResponse, response.getBody());
        verify(authService).authenticate(request);
        verifyNoMoreInteractions(authService);
    }
}