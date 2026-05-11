package pl.edu.uj.tp.nexo.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorInfo {

    // ------------------------------------------
    // 1000 - 1999: Users and Authentication (Auth/User)
    // ------------------------------------------
    USER_NOT_FOUND(1001, HttpStatus.NOT_FOUND, "User not found"),
    INVALID_CREDENTIALS(1002, HttpStatus.UNAUTHORIZED, "Invalid credentials"),
    USER_HAS_UNAUTHORIZED_ROLE(1003, HttpStatus.FORBIDDEN, "User does not have the required role"),
    EMAIL_ALREADY_EXISTS(1004, HttpStatus.CONFLICT, "User with this email already exists"),
    INVALID_AUTH_TOKEN(1005, HttpStatus.UNAUTHORIZED, "Invalid or expired authentication token"),

     // ------------------------------------------
     // 2000 - 2999: Issues, Boards, Stages
     // ------------------------------------------
     ISSUE_NOT_FOUND(2001, HttpStatus.NOT_FOUND, "Issue not found"),
     BOARD_NOT_FOUND(2002, HttpStatus.NOT_FOUND, "Board not found"),
     STAGE_NOT_FOUND(2003, HttpStatus.NOT_FOUND, "Stage not found"),
     EPIC_NOT_FOUND(2004, HttpStatus.NOT_FOUND, "Epic not found"),
     INVALID_EPIC_TYPE(2005, HttpStatus.BAD_REQUEST, "The referenced issue is not an epic"),
     EPIC_CIRCULAR_REFERENCE(2006, HttpStatus.BAD_REQUEST, "An epic cannot be linked to itself"),
     EPIC_INVALID_STAGE(2007, HttpStatus.BAD_REQUEST, "Epics can only be assigned to stages of type: PREPARATION, TO_DO, IN_PROGRESS, or DONE"),

    // ------------------------------------------
    // 3000 - 3999: General errors, Validation, Others
    // ------------------------------------------
    INVALID_INVITATION_TOKEN(3001, HttpStatus.BAD_REQUEST, "Invalid invitation invitationToken"),
    EXPIRED_INVITATION(3002, HttpStatus.BAD_REQUEST, "Invitation is expired or already used"),
    ORGANIZATION_NOT_FOUND(3003, HttpStatus.NOT_FOUND, "Organization not found"),

    INVALID_FIRST_NAME(3004, HttpStatus.BAD_REQUEST, "First name must be between 2 and 50 characters, and contain only letters, spaces, or hyphens"),
    INVALID_LAST_NAME(3005, HttpStatus.BAD_REQUEST, "Last name must be between 2 and 50 characters, and contain only letters, spaces, or hyphens"),
    INVALID_EMAIL(3006, HttpStatus.BAD_REQUEST, "Invalid email format. Email cannot be blank and must contain @ symbol"),
    INVALID_PASSWORD(3007, HttpStatus.BAD_REQUEST, "Password must be at least 6 characters long and cannot be blank"),
    INVALID_ORGANIZATION_NAME(3008, HttpStatus.BAD_REQUEST, "Organization name cannot be blank"),
    RESOURCE_IN_USE(3009, HttpStatus.CONFLICT, "Cannot delete or update this resource because it is still in use by other records (e.g., boards or issues)"),

    // ------------------------------------------
    // 9000 - 9999: System and Critical errors
    // ------------------------------------------
    INTERNAL_ERROR(9999, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final int code;
    private final HttpStatus status;
    private final String defaultMessage;
}