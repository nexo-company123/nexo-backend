package pl.edu.uj.tp.nexo.invitation.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pl.edu.uj.tp.nexo.invitation.dto.InvitationRequest;
import pl.edu.uj.tp.nexo.invitation.dto.InvitationResponse;
import pl.edu.uj.tp.nexo.invitation.entity.Invitation;
import pl.edu.uj.tp.nexo.invitation.repository.InvitationRepository;
import pl.edu.uj.tp.nexo.organization.entity.Organization;
import pl.edu.uj.tp.nexo.organization.repository.OrganizationRepository;
import pl.edu.uj.tp.nexo.validation.UserDataValidator;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvitationServiceTest {

    private final InvitationRepository invitationRepository = mock(InvitationRepository.class);
    private final UserDataValidator userDataValidator = mock(UserDataValidator.class);
    private final EmailService emailService = mock(EmailService.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);

    private final InvitationService invitationService = new InvitationService(
            invitationRepository,
            userDataValidator,
            emailService,
            organizationRepository
    );

    @Test
    void createInvitation_savesInvitationSendsEmailAndReturnsResponse() {
        InvitationRequest request = new InvitationRequest("user@example.com");

        Organization organization = new Organization();
        organization.setId(10L);
        organization.setName("Nexo Org");

        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));

        InvitationResponse response = invitationService.createInvitation(request, 10L);

        assertEquals("Invitation sent successfully to user@example.com", response.message());

        ArgumentCaptor<Invitation> invitationCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(invitationCaptor.capture());

        Invitation savedInvitation = invitationCaptor.getValue();

        assertEquals("user@example.com", savedInvitation.getEmail());
        assertEquals(10L, savedInvitation.getOrganizationId());
        assertNotNull(savedInvitation.getToken());
        assertFalse(savedInvitation.isUsed());
        assertTrue(savedInvitation.getExpiresAt().isAfter(LocalDateTime.now().plusDays(6)));

        verify(userDataValidator).validateEmail("user@example.com");
        verify(emailService).sendInvitationEmail(
                "user@example.com",
                savedInvitation.getToken(),
                "Nexo Org"
        );
    }
}