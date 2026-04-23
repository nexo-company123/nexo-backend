package pl.edu.uj.tp.nexo.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import pl.edu.uj.tp.nexo.user.dto.UpdateUserRequest;
import pl.edu.uj.tp.nexo.user.dto.UserResponse;
import pl.edu.uj.tp.nexo.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "UserController", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users or filter by organization ID", description = "Retrieves a list of users. Can be optionally filtered by providing an organizationId query parameter.")
    public List<UserResponse> getUsers(@RequestParam(required = false) Long organizationId) {
        if (organizationId != null) {
            return userService.getUsersByOrganization(organizationId);
        }
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get user by ID", description = "Retrieves detailed information about a specific user by their ID.")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Update an existing user", description = "Updates a user's details, such as their name, password, or role.")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user by ID", description = "Permanently deletes a user from the system.")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}