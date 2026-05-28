package pl.edu.uj.tp.nexo.user.controller;

import org.junit.jupiter.api.Test;
import pl.edu.uj.tp.nexo.organization.entity.Organization;
import pl.edu.uj.tp.nexo.user.dto.UpdateUserRequest;
import pl.edu.uj.tp.nexo.user.dto.UserResponse;
import pl.edu.uj.tp.nexo.user.entity.Role;
import pl.edu.uj.tp.nexo.user.entity.User;
import pl.edu.uj.tp.nexo.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final UserController userController = new UserController(userService);

    private User makeCurrentUser(Long organizationId) {
        Organization organization = new Organization();
        organization.setId(organizationId);
        User user = new User();
        user.setOrganization(organization);
        return user;
    }

    @Test
    void getUsers_delegatesToServiceWithOrganizationId() {
        User currentUser = makeCurrentUser(10L);
        List<UserResponse> expected = List.of(
                UserResponse.builder().id(1L).email("a@b").firstName("A").lastName("B").role(Role.USER).build()
        );

        when(userService.getUsersByOrganization(10L)).thenReturn(expected);

        List<UserResponse> result = userController.getUsers(currentUser);

        assertSame(expected, result);
        verify(userService).getUsersByOrganization(10L);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getUserById_passesIdAndOrganizationToService() {
        User currentUser = makeCurrentUser(10L);
        UserResponse expected = UserResponse.builder().id(5L).build();

        when(userService.getUserByIdAndOrganization(5L, 10L)).thenReturn(expected);

        UserResponse result = userController.getUserById(5L, currentUser);

        assertSame(expected, result);
    }

    @Test
    void updateUser_passesIdRequestAndOrganizationToService() {
        User currentUser = makeCurrentUser(10L);
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Anna")
                .lastName("Nowak")
                .build();
        UserResponse expected = UserResponse.builder().id(5L).firstName("Anna").build();

        when(userService.updateUser(5L, request, 10L)).thenReturn(expected);

        UserResponse result = userController.updateUser(5L, request, currentUser);

        assertSame(expected, result);
        verify(userService).updateUser(5L, request, 10L);
    }

    @Test
    void deleteUser_callsServiceWithIdAndOrganization() {
        User currentUser = makeCurrentUser(10L);

        userController.deleteUser(5L, currentUser);

        verify(userService).deleteUser(5L, 10L);
        verifyNoMoreInteractions(userService);
    }
}
