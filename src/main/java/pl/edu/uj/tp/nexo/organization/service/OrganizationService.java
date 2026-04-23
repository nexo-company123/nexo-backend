package pl.edu.uj.tp.nexo.organization.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.uj.tp.nexo.organization.dto.OrganizationResponse;
import pl.edu.uj.tp.nexo.organization.dto.UpdateOrganizationRequest;
import pl.edu.uj.tp.nexo.organization.entity.Organization;
import pl.edu.uj.tp.nexo.organization.repository.OrganizationRepository;
import pl.edu.uj.tp.nexo.validation.UserDataValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserDataValidator userDataValidator;

    public List<OrganizationResponse> getOrganizations() {
        return organizationRepository.findAll().stream()
                .map(this::toOrganizationResponse)
                .collect(Collectors.toList());
    }

    public OrganizationResponse getOrganizationById(Long id) {
        return organizationRepository.findById(id)
                .map(this::toOrganizationResponse)
                .orElseThrow(() -> new OrganizationNotFoundException(id));
    }

    public OrganizationResponse updateOrganization(Long id, UpdateOrganizationRequest request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException(id));

        if (request.getName() != null) {
            userDataValidator.validateOrganizationName(request.getName());
            organization.setName(request.getName());
        }

        organization = organizationRepository.save(organization);
        return toOrganizationResponse(organization);
    }

    public void deleteOrganization(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new OrganizationNotFoundException(id);
        }
        organizationRepository.deleteById(id);
    }

    private OrganizationResponse toOrganizationResponse(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .createdAt(organization.getCreatedAt())
                .build();
    }
}