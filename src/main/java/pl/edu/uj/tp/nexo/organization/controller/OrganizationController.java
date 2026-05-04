package pl.edu.uj.tp.nexo.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import pl.edu.uj.tp.nexo.exception.ErrorInfo;
import pl.edu.uj.tp.nexo.exception.annotation.ApiErrors;
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
    @ApiErrors({
            ErrorInfo.ORGANIZATION_NOT_FOUND,
            ErrorInfo.INVALID_AUTH_TOKEN,
            ErrorInfo.USER_HAS_UNAUTHORIZED_ROLE
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationResponse.class)))
    })
    public OrganizationResponse getMyOrganization(@AuthenticationPrincipal User currentUser) {
        return organizationService.getOrganizationById(currentUser.getOrganization().getId());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update my organization", description = "Updates the name of the authenticated user's organization.")
    @ApiErrors({
            ErrorInfo.ORGANIZATION_NOT_FOUND,
            ErrorInfo.INVALID_ORGANIZATION_NAME,
            ErrorInfo.INVALID_AUTH_TOKEN,
            ErrorInfo.USER_HAS_UNAUTHORIZED_ROLE
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationResponse.class)))
    })
    public OrganizationResponse updateMyOrganization(@RequestBody UpdateOrganizationRequest request, @AuthenticationPrincipal User currentUser) {
        return organizationService.updateOrganization(currentUser.getOrganization().getId(), request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete my organization", description = "Permanently deletes the authenticated user's organization.")
    @ApiErrors({
            ErrorInfo.ORGANIZATION_NOT_FOUND,
            ErrorInfo.RESOURCE_IN_USE,
            ErrorInfo.INVALID_AUTH_TOKEN,
            ErrorInfo.USER_HAS_UNAUTHORIZED_ROLE
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Organization deleted successfully")
    })
    public void deleteMyOrganization(@AuthenticationPrincipal User currentUser) {
        organizationService.deleteOrganization(currentUser.getOrganization().getId());
    }
}