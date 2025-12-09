package com.example.test.repository;

import com.example.test.dto.UserCreateRequest;
import com.example.test.dto.UserDTO;
import com.example.test.dto.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SimpleJdbcCall simpleJdbcCall;

    @InjectMocks
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        // inject our own SimpleJdbcCall mock into the repository
        userRepository = new UserRepository(jdbcTemplate);
        userRepository.init();
        userRepository.createUserProcedureCall = simpleJdbcCall;
    }

    // -------------------------------------------------------------
    // CREATE USER (Stored Procedure)
    // -------------------------------------------------------------
    @Test
    void testCreateUserUsingProcedure() {

        UserCreateRequest req = new UserCreateRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@test.com");
        req.setPhone("123");

        Map<String, Object> storedProcResult = Map.of("new_id", 10L);

        when(simpleJdbcCall.execute(anyMap())).thenReturn(storedProcResult);

        Long id = userRepository.createUserUsingProcedure(req);

        assertEquals(10L, id);
        verify(simpleJdbcCall).execute(anyMap());
    }

    // -------------------------------------------------------------
    // GET ALL USERS
    // -------------------------------------------------------------
    @Test
    void testGetAllUsers() {

        List<UserDTO> mockList = List.of(
                new UserDTO(1L, "A", "B", "a@b.com", "123", null)
        );

        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(mockList);

        List<UserDTO> result = userRepository.getAllUsers();

        assertEquals(1, result.size());
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class));
    }

    // -------------------------------------------------------------
    // GET USER BY ID
    // -------------------------------------------------------------
    @Test
    void testGetUserById_found() {

        UserDTO user = new UserDTO(1L, "A", "B", "a@b.com", "999", null);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1L)))
                .thenReturn(List.of(user));

        Optional<UserDTO> result = userRepository.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("A", result.get().getFirstName());
    }

    @Test
    void testGetUserById_notFound() {

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(99L)))
                .thenReturn(List.of());

        Optional<UserDTO> result = userRepository.getUserById(99L);

        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------
    // DUPLICATE EMAIL CHECK
    // -------------------------------------------------------------
    @Test
    void testEmailExistsForAnotherUser() {

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString(), anyLong()))
                .thenReturn(1);

        boolean exists = userRepository.emailExistsForAnotherUser("a@b.com", 1L);

        assertTrue(exists);
    }

    // -------------------------------------------------------------
    // UPDATE USER
    // -------------------------------------------------------------
    @Test
    void testUpdateUser() {

        UserUpdateRequest req = new UserUpdateRequest();
        req.setFirstName("A");
        req.setLastName("B");
        req.setEmail("a@b.com");
        req.setPhone("123");

        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), anyLong()))
                .thenReturn(1);

        boolean updated = userRepository.updateUser(1L, req);

        assertTrue(updated);
    }

    // -------------------------------------------------------------
    // DELETE USER
    // -------------------------------------------------------------
    @Test
    void testDeleteUser() {

        when(jdbcTemplate.update(anyString(), eq(5L))).thenReturn(1);

        boolean deleted = userRepository.deleteUser(5L);

        assertTrue(deleted);
    }

    // -------------------------------------------------------------
    // GET USERS PAGINATED
    // -------------------------------------------------------------
    @Test
    void testGetUsersPaginated() {

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyInt(), anyInt()))
                .thenReturn(List.of());

        List<UserDTO> result = userRepository.getUsersPaginated(0, 10, "id", "asc");

        assertNotNull(result);
    }

    // -------------------------------------------------------------
    // SEARCH USERS
    // -------------------------------------------------------------
    @Test
    void testSearchUsers() {

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(List.of());

        List<UserDTO> result = userRepository.searchUsers("abc", 10, 0, "id", "asc");

        assertNotNull(result);
    }
}
