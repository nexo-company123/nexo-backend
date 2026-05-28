package pl.edu.uj.tp.nexo.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import pl.edu.uj.tp.nexo.exception.dto.ErrorResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAppException_returnsStatusAndCodeFromErrorInfo() {
        AppException exception = new AppException(ErrorInfo.BOARD_NOT_FOUND);

        ResponseEntity<ErrorResponse> response = handler.handleAppException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals(2002, response.getBody().errorCode());
        assertEquals("Board not found", response.getBody().message());
    }

    @Test
    void handleAccessDeniedException_returnsForbiddenWithUnauthorizedRoleCode() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDeniedException(new AccessDeniedException("nope"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(1003, response.getBody().errorCode());
        assertEquals("User does not have the required role", response.getBody().message());
    }

    @Test
    void handleAuthenticationException_returnsUnauthorizedInvalidCredentials() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAuthenticationException(new BadCredentialsException("bad"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(1002, response.getBody().errorCode());
        assertEquals("Invalid credentials", response.getBody().message());
    }

    @Test
    void handleDataIntegrityViolation_returnsConflictResourceInUse() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrityViolation(new DataIntegrityViolationException("fk constraint"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(3009, response.getBody().errorCode());
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(9999, response.getBody().errorCode());
        assertEquals("Internal server error", response.getBody().message());
    }
}
