package pl.edu.uj.tp.nexo.validation;

import org.junit.jupiter.api.Test;
import pl.edu.uj.tp.nexo.exception.AppException;
import pl.edu.uj.tp.nexo.exception.ErrorInfo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserDataValidatorTest {

    private final UserDataValidator validator = new UserDataValidator();

    @Test
    void validateEmail_correctEmail_doesNotThrow() {
        assertDoesNotThrow(() -> validator.validateEmail("jan.kowalski@example.com"));
    }

    @Test
    void validateEmail_missingAtSign_throwsAppException() {
        AppException ex = assertThrows(AppException.class, () -> validator.validateEmail("nope-no-at-sign"));
        assertEquals(ErrorInfo.INVALID_EMAIL, ex.getErrorInfo());
    }

    @Test
    void validateEmail_blank_throwsAppException() {
        AppException ex = assertThrows(AppException.class, () -> validator.validateEmail("   "));
        assertEquals(ErrorInfo.INVALID_EMAIL, ex.getErrorInfo());
    }

    @Test
    void validateEmail_null_throwsAppException() {
        AppException ex = assertThrows(AppException.class, () -> validator.validateEmail(null));
        assertEquals(ErrorInfo.INVALID_EMAIL, ex.getErrorInfo());
    }

    @Test
    void validateName_correctName_doesNotThrow() {
        assertDoesNotThrow(() -> validator.validateName("Anna", ErrorInfo.INVALID_FIRST_NAME));
        assertDoesNotThrow(() -> validator.validateName("Anna-Maria", ErrorInfo.INVALID_FIRST_NAME));
        assertDoesNotThrow(() -> validator.validateName("Jan Adam", ErrorInfo.INVALID_LAST_NAME));
    }

    @Test
    void validateName_tooShort_throwsAppException() {
        AppException ex = assertThrows(AppException.class,
                () -> validator.validateName("A", ErrorInfo.INVALID_FIRST_NAME));
        assertEquals(ErrorInfo.INVALID_FIRST_NAME, ex.getErrorInfo());
    }

    @Test
    void validateName_withDigits_throwsAppException() {
        AppException ex = assertThrows(AppException.class,
                () -> validator.validateName("Jan123", ErrorInfo.INVALID_LAST_NAME));
        assertEquals(ErrorInfo.INVALID_LAST_NAME, ex.getErrorInfo());
    }

    @Test
    void validateName_tooLong_throwsAppException() {
        String tooLong = "a".repeat(51);
        AppException ex = assertThrows(AppException.class,
                () -> validator.validateName(tooLong, ErrorInfo.INVALID_FIRST_NAME));
        assertEquals(ErrorInfo.INVALID_FIRST_NAME, ex.getErrorInfo());
    }

    @Test
    void validatePassword_correctPassword_doesNotThrow() {
        assertDoesNotThrow(() -> validator.validatePassword("123456"));
        assertDoesNotThrow(() -> validator.validatePassword("longerPassword!"));
    }

    @Test
    void validatePassword_tooShort_throwsAppException() {
        AppException ex = assertThrows(AppException.class, () -> validator.validatePassword("12345"));
        assertEquals(ErrorInfo.INVALID_PASSWORD, ex.getErrorInfo());
    }

    @Test
    void validatePassword_null_throwsAppException() {
        AppException ex = assertThrows(AppException.class, () -> validator.validatePassword(null));
        assertEquals(ErrorInfo.INVALID_PASSWORD, ex.getErrorInfo());
    }

    @Test
    void validateOrganizationName_correctName_doesNotThrow() {
        assertDoesNotThrow(() -> validator.validateOrganizationName("Nexo Org"));
    }

    @Test
    void validateOrganizationName_blank_throwsAppException() {
        AppException ex = assertThrows(AppException.class, () -> validator.validateOrganizationName("  "));
        assertEquals(ErrorInfo.INVALID_ORGANIZATION_NAME, ex.getErrorInfo());
    }
}
