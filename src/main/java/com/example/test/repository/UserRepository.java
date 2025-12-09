package com.example.test.repository;

import com.example.test.dto.UserCreateRequest;
import com.example.test.dto.UserDTO;
import com.example.test.dto.UserUpdateRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.sql.Types;
import java.util.*;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private SimpleJdbcCall createUserProcedureCall;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -------------------------------------------------------
    // COMMON MAPPER
    // -------------------------------------------------------
    private static final RowMapper<UserDTO> USER_ROW_MAPPER = (rs, rowNum) ->
            new UserDTO(
                    rs.getLong("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getTimestamp("created_at").toLocalDateTime()
            );



    // -------------------------------------------------------
    // INIT STORED PROCEDURE CALL
    // -------------------------------------------------------
    @PostConstruct
    public void init() {
        createUserProcedureCall = new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("public")
                .withProcedureName("create_user_procedure")
                .declareParameters(
                        new SqlParameter("p_first_name", Types.VARCHAR),
                        new SqlParameter("p_last_name", Types.VARCHAR),
                        new SqlParameter("p_email", Types.VARCHAR),
                        new SqlParameter("p_phone", Types.VARCHAR),
                        new SqlOutParameter("new_id", Types.BIGINT)

                );
    }

    // -------------------------------------------------------
    // CREATE USER (STORED PROCEDURE)
    // -------------------------------------------------------
    public Long createUserUsingProcedure(UserCreateRequest req) {

        Map<String, Object> params = Map.of(
                "p_first_name", req.getFirstName(),
                "p_last_name", req.getLastName(),
                "p_email", req.getEmail(),
                "p_phone", req.getPhone()

        );

        Map<String, Object> result = createUserProcedureCall.execute(params);

        Object idObj = result.get("new_id");

        return (idObj == null) ? null : ((Number) idObj).longValue();
    }


    // -------------------------------------------------------
    // GET ALL USERS
    // -------------------------------------------------------
    public List<UserDTO> getAllUsers() {
        String sql = "SELECT id, first_name, last_name, email, phone, created_at FROM view_all_users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }


    // -------------------------------------------------------
    // GET USER BY ID (SAFE OPTIONAL)
    // -------------------------------------------------------
    public Optional<UserDTO> getUserById(Long id) {

        String sql = """
            SELECT id, first_name, last_name, email, phone,created_at 
            FROM view_user_details
            WHERE id = ?
        """;

        List<UserDTO> list = jdbcTemplate.query(sql, USER_ROW_MAPPER, id);

        return list.stream().findFirst();
    }


    // -------------------------------------------------------
    // EMAIL EXISTS FOR ANOTHER USER
    // -------------------------------------------------------
    public boolean emailExistsForAnotherUser(String email, Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND id <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email, id);
        return count != null && count > 0;
    }


    // -------------------------------------------------------
    // UPDATE USER
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

    // -------------------------------------------------------
    // ALLOWED SORT FIELDS
    // -------------------------------------------------------
    private static final Map<String, String> SORTABLE_COLUMNS = Map.of(
            "id", "id",
            "firstName", "first_name",
            "lastName", "last_name",
            "email", "email",
            "phone", "phone"
    );

    private String validateSortBy(String sortBy) {
        return SORTABLE_COLUMNS.getOrDefault(sortBy, "id");
    }

    private String validateDirection(String direction) {
        return "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";
    }


    // -------------------------------------------------------
    // PAGINATION + SORTING
    // -------------------------------------------------------
    public List<UserDTO> getUsersPaginated(int page, int size, String sortBy, String direction) {

        String sortCol = validateSortBy(sortBy);
        String dir = validateDirection(direction);

        int offset = Math.max(0, page) * Math.max(1, size);

        String sql = "SELECT id, first_name, last_name, email, phone,created_at  " +
                "FROM view_all_users " +
                "ORDER BY " + sortCol + " " + dir + " " +
                "LIMIT ? OFFSET ?";

        return jdbcTemplate.query(sql, USER_ROW_MAPPER, size, offset);
    }


    // -------------------------------------------------------
    // SEARCH + SORT + PAGINATION
    // -------------------------------------------------------

    public List<UserDTO> searchUsers(String query, int limit, int offset, String sortBy, String sortDir) {

        String sortCol = validateSortBy(sortBy);
        String dir = validateDirection(sortDir);

        String q = (query == null || query.isBlank()) ? null : "%" + query.toLowerCase() + "%";

        String where = (q != null)
                ? "WHERE lower(first_name) LIKE ? OR lower(last_name) LIKE ? OR lower(email) LIKE ? "
                : "";

        List<Object> params = new ArrayList<>();
        if (q != null) params.addAll(List.of(q, q, q));
        params.addAll(List.of(limit, offset));

        String sql = """
        SELECT id, first_name, last_name, email, phone,created_at 
        FROM view_all_users
        """ + where +
                "ORDER BY " + sortCol + " " + dir + " LIMIT ? OFFSET ?";

        return jdbcTemplate.query(sql, params.toArray(), USER_ROW_MAPPER);
    }


}
