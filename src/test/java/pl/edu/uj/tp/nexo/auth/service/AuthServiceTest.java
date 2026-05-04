package pl.edu.uj.tp.nexo.auth.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.edu.uj.tp.nexo.auth.dto.AdminRegisterRequest;
import pl.edu.uj.tp.nexo.auth.dto.AuthenticationResponse;
import pl.edu.uj.tp.nexo.invitation.repository.InvitationRepository;
import pl.edu.uj.tp.nexo.organization.entity.Organization;
import pl.edu.uj.tp.nexo.organization.repository.OrganizationRepository;
import pl.edu.uj.tp.nexo.security.service.JwtService;
import pl.edu.uj.tp.nexo.user.entity.Role;
import pl.edu.uj.tp.nexo.user.entity.User;
import pl.edu.uj.tp.nexo.user.repository.UserRepository;
import pl.edu.uj.tp.nexo.validation.UserDataValidator;

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
}