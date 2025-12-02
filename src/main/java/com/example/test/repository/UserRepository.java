package com.example.test.repository;

import com.example.test.dto.UserCreateRequest;
import com.example.test.dto.UserDTO;
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

    // ============= CREATE USING STORED FUNCTION ==================
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


    // ================= GET ALL USERS (from view_all_users) ====================
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

    // ================= GET USER BY ID (from view_user_details) ====================
    public UserDTO getUserById(Long id) {
        String sql = "SELECT id, first_name, last_name, email, phone "
                + "FROM view_user_details WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new UserDTO(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone")
                ), id);
    }
}
