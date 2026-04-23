package pl.edu.uj.tp.nexo.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import pl.edu.uj.tp.nexo.user.dto.UpdateUserRequest;
import pl.edu.uj.tp.nexo.user.dto.UserResponse;
import pl.edu.uj.tp.nexo.user.entity.User;
import pl.edu.uj.tp.nexo.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "UserController", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get all colleagues", description = "Retrieves all users belonging to the authenticated user's organization.")
    public List<UserResponse> getUsers(@AuthenticationPrincipal User currentUser) {
        return userService.getUsersByOrganization(currentUser.getOrganization().getId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get user by ID", description = "Retrieves information about a specific user, ensuring they belong to the same organization.")
    public UserResponse getUserById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return userService.getUserByIdAndOrganization(id, currentUser.getOrganization().getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Update an existing user", description = "Updates a user's details. Only permitted if they belong to the caller's organization.")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request, @AuthenticationPrincipal User currentUser) {
        return userService.updateUser(id, request, currentUser.getOrganization().getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user by ID", description = "Deletes a user. Admin can only delete users from their own organization.")
    public void deleteUser(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        userService.deleteUser(id, currentUser.getOrganization().getId());
    }
}