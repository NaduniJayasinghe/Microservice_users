package com.example.test.service;

import com.example.test.dto.UserCreateRequest;
import com.example.test.dto.UserDTO;
import com.example.test.dto.UserUpdateRequest;
import com.example.test.exception.DuplicateEmailException;
import com.example.test.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;  // mock

    @InjectMocks
    private UserService userService;        // mock injected here

    // -------------------------------------------------------------
    // CREATE USER
    // -------------------------------------------------------------
    @Test
    void testCreateUser_success() {
        UserCreateRequest req = new UserCreateRequest();
        req.setEmail("test@example.com");

        when(userRepository.createUserUsingProcedure(req)).thenReturn(10L);

        Long id = userService.createUser(req);

        assertEquals(10L, id);
        verify(userRepository).createUserUsingProcedure(req);
    }

    @Test
    void testCreateUser_duplicateEmail() {
        UserCreateRequest req = new UserCreateRequest();
        req.setEmail("duplicate@example.com");

        when(userRepository.createUserUsingProcedure(req))
                .thenThrow(new DuplicateKeyException("Duplicate"));

        assertThrows(DuplicateKeyException.class, () -> userService.createUser(req));
    }

    // -------------------------------------------------------------
    // GET ALL USERS
    // -------------------------------------------------------------
    @Test
    void testGetAllUsers() {
        List<UserDTO> mockList = List.of(new UserDTO(1L, "A", "B", "a@b.com", "123", null));
        when(userRepository.getAllUsers()).thenReturn(mockList);

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(1, result.size());
        verify(userRepository).getAllUsers();
    }

    // -------------------------------------------------------------
    // GET USER BY ID
    // -------------------------------------------------------------
    @Test
    void testGetUserById_found() {
        UserDTO user = new UserDTO(1L, "A", "B", "a@b.com", "123", null);
        when(userRepository.getUserById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("A", result.getFirstName());
    }

    @Test
    void testGetUserById_notFound() {
        when(userRepository.getUserById(99L)).thenReturn(Optional.empty());

        UserDTO result = userService.getUserById(99L);

        assertNull(result);
    }

    // -------------------------------------------------------------
    // UPDATE USER
    // -------------------------------------------------------------
    @Test
    void testUpdateUser_success() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setEmail("ok@test.com");

        when(userRepository.emailExistsForAnotherUser("ok@test.com", 1L)).thenReturn(false);
        when(userRepository.updateUser(1L, req)).thenReturn(true);

        boolean result = userService.updateUser(1L, req);

        assertTrue(result);
        verify(userRepository).updateUser(1L, req);
    }

    @Test
    void testUpdateUser_duplicateEmail() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setEmail("dup@test.com");

        when(userRepository.emailExistsForAnotherUser("dup@test.com", 1L)).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.updateUser(1L, req));
    }

    // -------------------------------------------------------------
    // DELETE USER
    // -------------------------------------------------------------
    @Test
    void testDeleteUser() {
        when(userRepository.deleteUser(5L)).thenReturn(true);

        boolean result = userService.deleteUser(5L);

        assertTrue(result);
        verify(userRepository).deleteUser(5L);
    }

    // -------------------------------------------------------------
    // PAGINATION
    // -------------------------------------------------------------
    @Test
    void testGetUsersPaginated() {
        when(userRepository.getUsersPaginated(0, 10, "id", "asc"))
                .thenReturn(List.of());

        List<UserDTO> result = userService.getUsersPaginated(0, 10, "id", "asc");

        assertNotNull(result);
        verify(userRepository).getUsersPaginated(0, 10, "id", "asc");
    }

    // -------------------------------------------------------------
    // SEARCH
    // -------------------------------------------------------------
    @Test
    void testGetUsers_search() {
        when(userRepository.searchUsers("abc", 10, 0, "id", "asc"))
                .thenReturn(List.of());

        List<UserDTO> result = userService.getUsers(0, 10, "id", "asc", "abc");

        assertNotNull(result);
        verify(userRepository).searchUsers("abc", 10, 0, "id", "asc");
    }
}
