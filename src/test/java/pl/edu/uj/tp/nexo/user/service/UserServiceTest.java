package pl.edu.uj.tp.nexo.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.edu.uj.tp.nexo.exception.ErrorInfo;
import pl.edu.uj.tp.nexo.organization.entity.Organization;
import pl.edu.uj.tp.nexo.user.dto.UpdateUserRequest;
import pl.edu.uj.tp.nexo.user.dto.UserResponse;
import pl.edu.uj.tp.nexo.user.entity.Role;
import pl.edu.uj.tp.nexo.user.entity.User;
import pl.edu.uj.tp.nexo.user.repository.UserRepository;
import pl.edu.uj.tp.nexo.validation.UserDataValidator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserDataValidator userDataValidator = mock(UserDataValidator.class);

    private final UserService userService = new UserService(
            userRepository,
            passwordEncoder,
            userDataValidator
    );

    @Test
    void updateUser_updatesFieldsValidatesDataEncodesPasswordAndReturnsResponse() {
        Organization organization = new Organization();
        organization.setId(10L);

        User user = User.builder()
                .id(1L)
                .email("old@example.com")
                .firstName("Old")
                .lastName("User")
                .password("old-password")
                .role(Role.USER)
                .organization(organization)
                .build();

        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Anna")
                .lastName("Nowak")
                .password("new-password")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(1L, request, 10L);

        assertEquals(1L, response.getId());
        assertEquals("old@example.com", response.getEmail());
        assertEquals("Anna", response.getFirstName());
        assertEquals("Nowak", response.getLastName());
        assertEquals(10L, response.getOrganizationId());
        assertEquals(Role.ADMIN, response.getRole());

        verify(userDataValidator).validateName("Anna", ErrorInfo.INVALID_FIRST_NAME);
        verify(userDataValidator).validateName("Nowak", ErrorInfo.INVALID_LAST_NAME);
        verify(userDataValidator).validatePassword("new-password");
        verify(passwordEncoder).encode("new-password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("Anna", savedUser.getFirstName());
        assertEquals("Nowak", savedUser.getLastName());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.ADMIN, savedUser.getRole());
    }

    @Test
    void updateUser_whenUserDoesNotExist_throwsUserNotFoundException() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Anna")
                .build();

        when(userRepository.findByIdAndOrganizationId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(99L, request, 10L)
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(userDataValidator);
    }

    @Test
    void deleteUser_whenUserExists_deletesUser() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("Anna")
                .lastName("Nowak")
                .password("password")
                .role(Role.USER)
                .build();

        when(userRepository.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L, 10L);

        verify(userRepository).delete(user);
    }
}