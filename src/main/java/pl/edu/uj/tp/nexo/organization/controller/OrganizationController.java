package pl.edu.uj.tp.nexo.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import pl.edu.uj.tp.nexo.organization.dto.OrganizationResponse;
import pl.edu.uj.tp.nexo.organization.dto.UpdateOrganizationRequest;
import pl.edu.uj.tp.nexo.organization.service.OrganizationService;
import pl.edu.uj.tp.nexo.user.entity.User;

@RestController
@RequestMapping("/organizations/my")
@RequiredArgsConstructor
@Tag(name = "OrganizationController", description = "Organization management endpoints")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get my organization", description = "Retrieves details of the authenticated user's organization.")
    public OrganizationResponse getMyOrganization(@AuthenticationPrincipal User currentUser) {
        return organizationService.getOrganizationById(currentUser.getOrganization().getId());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update my organization", description = "Updates the name of the authenticated user's organization.")
    public OrganizationResponse updateMyOrganization(@RequestBody UpdateOrganizationRequest request, @AuthenticationPrincipal User currentUser) {
        return organizationService.updateOrganization(currentUser.getOrganization().getId(), request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete my organization", description = "Permanently deletes the authenticated user's organization.")
    public void deleteMyOrganization(@AuthenticationPrincipal User currentUser) {
        organizationService.deleteOrganization(currentUser.getOrganization().getId());
    }
}