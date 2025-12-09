package com.example.test.controller;

import com.example.test.dto.UserCreateRequest;
import com.example.test.dto.UserDTO;
import com.example.test.dto.UserUpdateRequest;
import com.example.test.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    // -------------------------------------------------------------
    // CREATE USER
    // -------------------------------------------------------------
    @Test
    void testCreateUser_success() throws Exception {
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(10L);

        String json = """
                {
                  "firstName":"John",
                  "lastName":"Doe",
                  "email":"john@test.com",
                  "phone":"123"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));

        verify(userService).createUser(any(UserCreateRequest.class));
    }

    // -------------------------------------------------------------
    // GET ALL USERS
    // -------------------------------------------------------------
    @Test
    void testGetAllUsers() throws Exception {

        UserDTO dto = new UserDTO(
                1L, "A", "B", "a@b.com", "999", LocalDateTime.now()
        );

        when(userService.getAllUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(userService).getAllUsers();
    }

    // -------------------------------------------------------------
    // GET BY ID (FOUND)
    // -------------------------------------------------------------
    @Test
    void testGetUserById_found() throws Exception {
        UserDTO dto = new UserDTO(
                1L, "A", "B", "a@b.com", "9876", LocalDateTime.now()
        );

        when(userService.getUserById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userService).getUserById(1L);
    }

    // -------------------------------------------------------------
    // GET BY ID (NOT FOUND)
    // -------------------------------------------------------------
    @Test
    void testGetUserById_notFound() throws Exception {
        when(userService.getUserById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));

        verify(userService).getUserById(99L);
    }

    // -------------------------------------------------------------
    // UPDATE USER (SUCCESS)
    // -------------------------------------------------------------
    @Test
    void testUpdateUser_success() throws Exception {

        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class)))
                .thenReturn(true);

        String json = """
                {
                  "firstName":"X",
                  "lastName":"Y",
                  "email":"xyz@test.com",
                  "phone":"12345"
                }
                """;

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully"));

        verify(userService).updateUser(eq(1L), any(UserUpdateRequest.class));
    }

    // -------------------------------------------------------------
    // UPDATE USER (NOT FOUND)
    // -------------------------------------------------------------
    @Test
    void testUpdateUser_notFound() throws Exception {

        when(userService.updateUser(eq(99L), any(UserUpdateRequest.class)))
                .thenReturn(false);

        String json = """
                {
                  "firstName":"X",
                  "lastName":"Y",
                  "email":"abc@test.com",
                  "phone":"111"
                }
                """;

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));

        verify(userService).updateUser(eq(99L), any(UserUpdateRequest.class));
    }

    // -------------------------------------------------------------
    // DELETE USER
    // -------------------------------------------------------------
    @Test
    void testDeleteUser_success() throws Exception {

        when(userService.deleteUser(5L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        verify(userService).deleteUser(5L);
    }

    @Test
    void testDeleteUser_notFound() throws Exception {

        when(userService.deleteUser(5L)).thenReturn(false);

        mockMvc.perform(delete("/api/users/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));

        verify(userService).deleteUser(5L);
    }

    // -------------------------------------------------------------
    // PAGINATION + SEARCH
    // -------------------------------------------------------------
    @Test
    void testGetUsersPagination() throws Exception {

        when(userService.getUsers(anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/users?page=0&size=10&sort=id&direction=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(userService).getUsers(0, 10, "id", "asc", null);
    }
}
