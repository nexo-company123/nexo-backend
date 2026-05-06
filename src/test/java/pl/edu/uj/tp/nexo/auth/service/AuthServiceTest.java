package pl.edu.uj.tp.nexo.auth.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.edu.uj.tp.nexo.auth.dto.AdminRegisterRequest;
import pl.edu.uj.tp.nexo.auth.dto.AuthenticationRequest;
import pl.edu.uj.tp.nexo.auth.dto.AuthenticationResponse;
import pl.edu.uj.tp.nexo.invitation.repository.InvitationRepository;
import pl.edu.uj.tp.nexo.organization.entity.Organization;
import pl.edu.uj.tp.nexo.organization.repository.OrganizationRepository;
import pl.edu.uj.tp.nexo.security.service.JwtService;
import pl.edu.uj.tp.nexo.user.entity.Role;
import pl.edu.uj.tp.nexo.user.entity.User;
import pl.edu.uj.tp.nexo.user.repository.UserRepository;
import pl.edu.uj.tp.nexo.validation.UserDataValidator;
import org.springframework.security.authentication.BadCredentialsException;
import pl.edu.uj.tp.nexo.auth.dto.InvitedRegisterRequest;
import pl.edu.uj.tp.nexo.exception.AppException;
import pl.edu.uj.tp.nexo.exception.ErrorInfo;
import pl.edu.uj.tp.nexo.invitation.entity.Invitation;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final InvitationRepository invitationRepository = mock(InvitationRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final UserDataValidator userDataValidator = mock(UserDataValidator.class);

    private final AuthService authService = new AuthService(
            userRepository,
            organizationRepository,
            invitationRepository,
            passwordEncoder,
            jwtService,
            authenticationManager,
            userDataValidator
    );

    @Test
    void registerAdmin_savesOrganizationAndAdminUserAndReturnsToken() {
        AdminRegisterRequest request = new AdminRegisterRequest(
                "Nexo Org",
                "Jan",
                "Kowalski",
                "jan@example.com",
                "password123"
        );

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthenticationResponse response = authService.registerAdmin(request);

        assertEquals("jwt-token", response.token());

        ArgumentCaptor<Organization> organizationCaptor = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepository).save(organizationCaptor.capture());
        assertEquals("Nexo Org", organizationCaptor.getValue().getName());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("jan@example.com", savedUser.getEmail());
        assertEquals("Jan", savedUser.getFirstName());
        assertEquals("Kowalski", savedUser.getLastName());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.ADMIN, savedUser.getRole());
        assertEquals(organizationCaptor.getValue(), savedUser.getOrganization());

        verify(userDataValidator).validateEmail(request.email());
        verify(userDataValidator).validatePassword(request.password());
        verify(jwtService).generateToken(savedUser);
    }

    @Test
    void registerInvited_withValidInvitation_savesUserMarksInvitationAsUsedAndReturnsToken() {
        InvitedRegisterRequest request = new InvitedRegisterRequest(
                "valid-token",
                "Anna",
                "Nowak",
                "password123"
        );

        Invitation invitation = new Invitation();
        invitation.setEmail("anna@example.com");
        invitation.setToken("valid-token");
        invitation.setOrganizationId(10L);
        invitation.setUsed(false);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(1));

        Organization organization = new Organization();
        organization.setId(10L);
        organization.setName("Nexo Org");

        when(invitationRepository.findByToken("valid-token")).thenReturn(Optional.of(invitation));
        when(userRepository.findByEmail("anna@example.com")).thenReturn(Optional.empty());
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthenticationResponse response = authService.registerInvited(request);

        assertEquals("jwt-token", response.token());
        assertTrue(invitation.isUsed());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("anna@example.com", savedUser.getEmail());
        assertEquals("Anna", savedUser.getFirstName());
        assertEquals("Nowak", savedUser.getLastName());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());
        assertEquals(organization, savedUser.getOrganization());

        verify(invitationRepository).save(invitation);
        verify(jwtService).generateToken(savedUser);
    }

    @Test
    void registerInvited_withExpiredInvitation_throwsAppException() {
        InvitedRegisterRequest request = new InvitedRegisterRequest(
                "expired-token",
                "Anna",
                "Nowak",
                "password123"
        );

        Invitation invitation = new Invitation();
        invitation.setToken("expired-token");
        invitation.setEmail("anna@example.com");
        invitation.setOrganizationId(10L);
        invitation.setUsed(false);
        invitation.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(invitationRepository.findByToken("expired-token")).thenReturn(Optional.of(invitation));

        AppException exception = assertThrows(
                AppException.class,
                () -> authService.registerInvited(request)
        );

        assertEquals(ErrorInfo.EXPIRED_INVITATION, exception.getErrorInfo());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void authenticate_withInvalidCredentials_throwsAppException() {
        AuthenticationRequest request = new AuthenticationRequest(
                "wrong@example.com",
                "bad-password"
        );

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        AppException exception = assertThrows(
                AppException.class,
                () -> authService.authenticate(request)
        );

        assertEquals(ErrorInfo.INVALID_CREDENTIALS, exception.getErrorInfo());
        verify(jwtService, never()).generateToken(any(User.class));
    }
}