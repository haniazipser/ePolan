package com.example.ePolan;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.ePolan.Model.Entities.User;
import com.example.ePolan.Repositories.UserRepository;
import com.example.ePolan.Services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private Jwt jwt;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getOrCreateUserFromJwt_UserExists_ReturnsUser() {
        String userId = "123";
        User existingUser = new User();
        existingUser.setId(userId);

        when(jwt.getSubject()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        User result = userService.getOrCreateUserFromJwt(jwt);

        assertEquals(existingUser, result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getOrCreateUserFromJwt_UserDoesNotExist_CreatesUser() {
        String userId = "456";
        User newUser = new User();
        newUser.setId(userId);

        when(jwt.getSubject()).thenReturn(userId);
        when(jwt.getClaimAsString("given_name")).thenReturn("John");
        when(jwt.getClaimAsString("family_name")).thenReturn("Doe");
        when(jwt.getClaimAsString("email")).thenReturn("john@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.getOrCreateUserFromJwt(jwt);

        assertEquals(userId, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john@example.com", result.getEmail());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void getLoggedUser_UserExists_ReturnsUser() {
        String userId = "789";
        User existingUser = new User();
        existingUser.setId(userId);

        when(jwt.getSubject()).thenReturn(userId);

        Authentication authMock = mock(Authentication.class);
        when(authMock.getPrincipal()).thenReturn(jwt);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authMock);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        User result = userService.getLoggedUser();

        assertEquals(existingUser, result);
    }

    @Test
    void getLoggedUser_UserDoesNotExist_ThrowsException() {
        String userId = "999";

        when(jwt.getSubject()).thenReturn(userId);

        Authentication authMock = mock(Authentication.class);
        when(authMock.getPrincipal()).thenReturn(jwt);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authMock);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.getLoggedUser());

        assertEquals("404 NOT_FOUND \"User not found\"", exception.getMessage());
    }

    @Test
    void getUserById_UserExists_ReturnsUser() {
        String userId = "321";
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertEquals(user, result);
    }

    @Test
    void getUserById_UserDoesNotExist_ThrowsException() {
        String userId = "654";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.getUserById(userId));

        assertEquals("404 NOT_FOUND \"User not found\"", exception.getMessage());
    }
}
