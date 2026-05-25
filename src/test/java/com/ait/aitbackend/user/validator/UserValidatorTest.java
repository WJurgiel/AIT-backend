package com.ait.aitbackend.user.validator;

import com.ait.aitbackend.auth.validator.UserValidator;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import com.ait.aitbackend.user.exceptions.UserDoesNotExistException;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserProfileRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserValidator userValidator;

    private final String username = "testUser";
    private final String email = "test@mail.com";
    private final String password = "password123";
    private final String encodedPassword = "encodedPassword";

    /**
     * Test sprawdza, czy rejestracja przebiega pomyślnie i nie wyrzuca błędów, gdy podana nazwa użytkownika i email nie istnieją jeszcze w bazie.
     */
    @Test
    void shouldPassValidationWhenUserDoesNotExist() {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertDoesNotThrow(() ->
                userValidator.validateUserRegister(username, email)
        );
    }

    /**
     * Weryfikuje, czy system rzuca wyjątek UserAlreadyExistsException, gdy próbuje się zarejestrować konto na zajętą już nazwę użytkownika.
     */
    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
                userValidator.validateUserRegister(username, email)
        );
    }

    /**
     * Sprawdza, czy próba rejestracji z użyciem zajętego adresu email kończy się rzuceniem błędu UserAlreadyExistsException.
     */
    @Test
    void shouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
                userValidator.validateUserRegister(username, email)
        );
    }

    /**
     * Upewnia się, że proces logowania przechodzi bez wyjątków, gdy podany użytkownik istnieje, a hasło zgadza się z tym w bazie.
     */
    @Test
    void shouldPassLoginWhenCredentialsAreCorrect() {
        UserProfile user = new UserProfile(username, email, encodedPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        assertDoesNotThrow(() ->
                userValidator.validateUserLogin(username, password)
        );
    }

    /**
     * Weryfikuje, czy podczas logowania rzucany jest UserDoesNotExistException, jeśli w bazie nie znaleziono podanego loginu.
     */
    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class, () ->
                userValidator.validateUserLogin(username, password)
        );
    }

    /**
     * Sprawdza mechanizm logowania pod kątem błędnego hasła – jeśli hasło nie pasuje, wyrzucany jest wyjątek BadCredentialsException.
     */
    @Test
    void shouldThrowExceptionWhenPasswordIsInvalid() {
        UserProfile user = new UserProfile(username, email, encodedPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () ->
                userValidator.validateUserLogin(username, password)
        );
    }
}