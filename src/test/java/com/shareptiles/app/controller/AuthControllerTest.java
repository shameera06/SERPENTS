package com.shareptiles.app.controller;

import com.shareptiles.app.model.User;
import com.shareptiles.app.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthController controller = new AuthController();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
    }

    @Test
    void loginAcceptsBrowserAutofillUsernameParameterWithEmailAddress() {
        User user = new User("Keeper", "keeper@example.com", "password", "staff");
        when(userRepository.findByEmail("keeper@example.com")).thenReturn(Optional.of(user));
        HttpSession session = mock(HttpSession.class);

        String view = controller.login("keeper@example.com", null, "password", session, new ExtendedModelMap());

        assertEquals("redirect:/admin/dashboard", view);
        verify(session).setAttribute("currentUser", user);
    }

    @Test
    void loginContinuesToAcceptLegacyEmailParameter() {
        User user = new User("Keeper", "keeper@example.com", "password", "staff");
        when(userRepository.findByEmail("keeper@example.com")).thenReturn(Optional.of(user));
        HttpSession session = mock(HttpSession.class);

        String view = controller.login(null, "keeper@example.com", "password", session, new ExtendedModelMap());

        assertEquals("redirect:/admin/dashboard", view);
        verify(session).setAttribute("currentUser", user);
    }

    @Test
    void failedLoginKeepsIdentifierForBrowserRetryButDoesNotExposePassword() {
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.login("keeper", null, "wrong-password", mock(HttpSession.class), model);

        assertEquals("admin/auth/login", view);
        assertEquals("keeper", model.getAttribute("loginIdentifier"));
        assertFalse(model.containsAttribute("password"));
    }
}
