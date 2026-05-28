package pl.edu.uj.tp.nexo.organization.service;

import org.junit.jupiter.api.Test;
import pl.edu.uj.tp.nexo.exception.AppException;
import pl.edu.uj.tp.nexo.exception.ErrorInfo;
import pl.edu.uj.tp.nexo.organization.dto.OrganizationResponse;
import pl.edu.uj.tp.nexo.organization.dto.UpdateOrganizationRequest;
import pl.edu.uj.tp.nexo.organization.entity.Organization;
import pl.edu.uj.tp.nexo.organization.repository.OrganizationRepository;
import pl.edu.uj.tp.nexo.validation.UserDataValidator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizationServiceTest {

    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final UserDataValidator userDataValidator = mock(UserDataValidator.class);

    private final OrganizationService organizationService = new OrganizationService(
            organizationRepository,
            userDataValidator
    );

    @Test
    void getOrganizationById_returnsOrganizationResponse() {
        Organization organization = new Organization();
        organization.setId(10L);
        organization.setName("Nexo Org");

        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));

        OrganizationResponse response = organizationService.getOrganizationById(10L);

        assertEquals(10L, response.getId());
        assertEquals("Nexo Org", response.getName());
    }

    @Test
    void getOrganizationById_whenMissing_throwsOrganizationNotFoundException() {
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrganizationNotFoundException.class,
                () -> organizationService.getOrganizationById(99L));
    }

    @Test
    void updateOrganization_validNewName_validatesAndUpdates() {
        Organization organization = new Organization();
        organization.setId(10L);
        organization.setName("Old Name");

        UpdateOrganizationRequest request = new UpdateOrganizationRequest("New Org Name");

        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrganizationResponse response = organizationService.updateOrganization(10L, request);

        assertEquals("New Org Name", response.getName());
        assertEquals("New Org Name", organization.getName());
        verify(userDataValidator).validateOrganizationName("New Org Name");
        verify(organizationRepository).save(organization);
    }

    @Test
    void updateOrganization_blankName_throwsAppExceptionAndDoesNotSave() {
        Organization organization = new Organization();
        organization.setId(10L);
        organization.setName("Old Name");

        UpdateOrganizationRequest request = new UpdateOrganizationRequest("  ");

        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        doThrow(new AppException(ErrorInfo.INVALID_ORGANIZATION_NAME))
                .when(userDataValidator).validateOrganizationName("  ");

        AppException ex = assertThrows(AppException.class,
                () -> organizationService.updateOrganization(10L, request));

        assertEquals(ErrorInfo.INVALID_ORGANIZATION_NAME, ex.getErrorInfo());
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void updateOrganization_whenNameIsNull_doesNotValidateAndDoesNotChangeName() {
        Organization organization = new Organization();
        organization.setId(10L);
        organization.setName("Old Name");

        UpdateOrganizationRequest request = new UpdateOrganizationRequest(null);

        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrganizationResponse response = organizationService.updateOrganization(10L, request);

        assertEquals("Old Name", response.getName());
        verify(userDataValidator, never()).validateOrganizationName(any());
    }

    @Test
    void deleteOrganization_existing_callsDeleteById() {
        when(organizationRepository.existsById(10L)).thenReturn(true);

        organizationService.deleteOrganization(10L);

        verify(organizationRepository).deleteById(10L);
    }

    @Test
    void deleteOrganization_missing_throwsOrganizationNotFoundException() {
        when(organizationRepository.existsById(99L)).thenReturn(false);

        assertThrows(OrganizationNotFoundException.class,
                () -> organizationService.deleteOrganization(99L));

        verify(organizationRepository, never()).deleteById(any());
    }
}
