package pl.edu.uj.tp.nexo.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import pl.edu.uj.tp.nexo.organization.dto.OrganizationResponse;
import pl.edu.uj.tp.nexo.organization.dto.UpdateOrganizationRequest;
import pl.edu.uj.tp.nexo.organization.service.OrganizationService;

import java.util.List;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
@Tag(name = "OrganizationController", description = "Organization management endpoints")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all organizations", description = "Retrieves a list of all registered organizations in the system.")
    public List<OrganizationResponse> getOrganizations() {
        return organizationService.getOrganizations();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get organization by ID", description = "Retrieves details of a specific organization by its ID.")
    public OrganizationResponse getOrganizationById(@PathVariable Long id) {
        return organizationService.getOrganizationById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing organization", description = "Updates the name of an existing organization.")
    public OrganizationResponse updateOrganization(@PathVariable Long id, @RequestBody UpdateOrganizationRequest request) {
        return organizationService.updateOrganization(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete organization by ID", description = "Permanently deletes an organization from the system.")
    public void deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
    }
}