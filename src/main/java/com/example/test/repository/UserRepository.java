package com.example.test.repository;

import com.example.test.dto.UserCreateRequest;
import com.example.test.dto.UserDTO;
import com.example.test.dto.UserUpdateRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private SimpleJdbcCall createUserCall;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -------------------------------------------------------
    // CREATE USING STORED FUNCTION
    // -------------------------------------------------------
    @PostConstruct
    public void init() {
        createUserCall = new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("public")
                .withFunctionName("create_user")
                .declareParameters(
                        new SqlParameter("p_first_name", Types.VARCHAR),
                        new SqlParameter("p_last_name", Types.VARCHAR),
                        new SqlParameter("p_email", Types.VARCHAR),
                        new SqlParameter("p_phone", Types.VARCHAR)
                );
    }

    public Long createUser(UserCreateRequest req) {
        Map<String, Object> params = Map.of(
                "p_first_name", req.getFirstName(),
                "p_last_name", req.getLastName(),
                "p_email", req.getEmail(),
                "p_phone", req.getPhone()
        );

        return createUserCall.executeFunction(Long.class, params);
    }

    // -------------------------------------------------------
    // GET ALL USERS (VIEW)
    // -------------------------------------------------------
    public List<UserDTO> getAllUsers() {
        String sql = "SELECT id, first_name, last_name, email, phone FROM view_all_users";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new UserDTO(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone")
                )
        );
    }

    // -------------------------------------------------------
    // GET USER BY ID (VIEW)
    // -------------------------------------------------------
    public UserDTO getUserById(Long id) {
        String sql = """
                SELECT id, first_name, last_name, email, phone
                FROM view_user_details
                WHERE id = ?
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new UserDTO(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone")
                ), id);
    }

    // -------------------------------------------------------
    // CHECK IF EMAIL ALREADY EXISTS FOR ANOTHER USER
    // -------------------------------------------------------
    public boolean emailExistsForAnotherUser(String email, Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND id <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email, id);
        return count != null && count > 0;
    }

    // -------------------------------------------------------
    // UPDATE USER (NORMAL UPDATE)
    // -------------------------------------------------------
    public boolean updateUser(Long id, UserUpdateRequest req) {
        String sql = """
                UPDATE users
                SET first_name = ?, last_name = ?, email = ?, phone = ?
                WHERE id = ?
                """;

        int rows = jdbcTemplate.update(sql,
                req.getFirstName(),
                req.getLastName(),
                req.getEmail(),
                req.getPhone(),
                id);

        return rows > 0;
    }

    // -------------------------------------------------------
    // DELETE USER
    // -------------------------------------------------------

    public boolean deleteUser(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);
        return rows > 0;
    }


}
