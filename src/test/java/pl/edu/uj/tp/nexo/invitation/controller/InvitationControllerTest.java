package pl.edu.uj.tp.nexo.invitation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import pl.edu.uj.tp.nexo.invitation.dto.InvitationRequest;
import pl.edu.uj.tp.nexo.invitation.dto.InvitationResponse;
import pl.edu.uj.tp.nexo.invitation.service.InvitationService;
import pl.edu.uj.tp.nexo.organization.entity.Organization;
import pl.edu.uj.tp.nexo.user.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class InvitationControllerTest {

    private final InvitationService invitationService = mock(InvitationService.class);
    private final InvitationController invitationController = new InvitationController(invitationService);

    @Test
    void createInvitation_returnsInvitationResponseFromService() {
        InvitationRequest request = new InvitationRequest("user@example.com");

        Organization organization = new Organization();
        organization.setId(10L);

        User admin = new User();
        admin.setOrganization(organization);

        InvitationResponse expectedResponse = new InvitationResponse(
                "Invitation sent successfully to user@example.com"
        );

        when(invitationService.createInvitation(request, 10L)).thenReturn(expectedResponse);

        ResponseEntity response = invitationController.createInvitation(request, admin);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expectedResponse, response.getBody());
        verify(invitationService).createInvitation(request, 10L);
        verifyNoMoreInteractions(invitationService);
    }
}