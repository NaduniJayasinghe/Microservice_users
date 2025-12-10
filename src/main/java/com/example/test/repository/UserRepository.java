package com.example.test.repository;

import com.example.test.dto.UserCreateRequest;
import com.example.test.dto.UserDTO;
import com.example.test.dto.UserUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.sql.Types;
import java.util.*;

@Slf4j
@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    SimpleJdbcCall createUserProcedureCall;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -------------------------------------------------------
    // COMMON MAPPER FOR VIEWS
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
        log.info("Repository: Initializing stored procedure call for create_user_procedure");

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

        log.info("Repository: Stored procedure create_user_procedure initialized successfully");
    }

    // -------------------------------------------------------
    // CREATE USER (STORED PROCEDURE)
    // -------------------------------------------------------
    public Long createUserUsingProcedure(UserCreateRequest req) {

        log.info("Repository: Calling create_user_procedure for email={}", req.getEmail());

        Map<String, Object> params = Map.of(
                "p_first_name", req.getFirstName(),
                "p_last_name", req.getLastName(),
                "p_email", req.getEmail(),
                "p_phone", req.getPhone()
        );

        log.debug("Repository: Stored procedure input parameters = {}", params);

        Map<String, Object> result = createUserProcedureCall.execute(params);

        log.debug("Repository: Stored procedure result = {}", result);

        Object idObj = result.get("new_id");
        Long newId = (idObj == null) ? null : ((Number) idObj).longValue();

        log.info("Repository: Stored procedure completed — new user ID={}", newId);

        return newId;
    }

    // -------------------------------------------------------
    // GET ALL USERS (VIEW)
    // -------------------------------------------------------
    public List<UserDTO> getAllUsers() {
        String sql = "SELECT id, first_name, last_name, email, phone, created_at FROM view_all_users";

        log.info("Repository: Fetching all users (non-paginated)");

        List<UserDTO> list = jdbcTemplate.query(sql, USER_ROW_MAPPER);

        log.debug("Repository: getAllUsers returned {} users", list.size());
        return list;
    }

    // -------------------------------------------------------
    // GET USER BY ID (VIEW)
    // -------------------------------------------------------
    public Optional<UserDTO> getUserById(Long id) {

        log.info("Repository: Fetching user by ID={}", id);

        String sql = """
            SELECT id, first_name, last_name, email, phone, created_at
            FROM view_user_details
            WHERE id = ?
        """;

        List<UserDTO> list = jdbcTemplate.query(sql, USER_ROW_MAPPER, id);

        if (list.isEmpty()) {
            log.warn("Repository: No user found with ID={}", id);
        } else {
            log.debug("Repository: User found ID={} → {}", id, list.get(0));
        }

        return list.stream().findFirst();
    }

    // -------------------------------------------------------
    // CHECK DUPLICATE EMAIL (FOR UPDATE)
    // -------------------------------------------------------
    public boolean emailExistsForAnotherUser(String email, Long id) {
        log.info("Repository: Checking duplicate email={} excluding ID={}", email, id);

        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND id <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email, id);

        boolean exists = count != null && count > 0;
        log.debug("Repository: Duplicate email exists? {}", exists);

        return exists;
    }

    // -------------------------------------------------------
    // UPDATE USER
    // -------------------------------------------------------
    public boolean updateUser(Long id, UserUpdateRequest req) {

        log.info("Repository: Updating user ID={}", id);

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

        log.debug("Repository: Rows affected during update = {}", rows);

        return rows > 0;
    }

    // -------------------------------------------------------
    // DELETE USER
    // -------------------------------------------------------
    public boolean deleteUser(Long id) {
        log.info("Repository: Deleting user ID={}", id);

        String sql = "DELETE FROM users WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);

        log.debug("Repository: Rows affected during delete = {}", rows);

        return rows > 0;
    }


    // -------------------------------------------------------
    // SORTING HELPERS
    // -------------------------------------------------------
    private static final Map<String, String> SORTABLE_COLUMNS = Map.of(
            "id", "id",
            "firstName", "first_name",
            "lastName", "last_name",
            "email", "email",
            "phone", "phone",
            "createdAt", "created_at" // Added sorting for timestamp
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

        log.info("Repository: Paginated fetch page={}, size={}, sort={}, direction={}",
                page, size, sortBy, direction);

        String sortCol = validateSortBy(sortBy);
        String dir = validateDirection(direction);

        int offset = Math.max(0, page) * Math.max(1, size);

        String sql = """
            SELECT id, first_name, last_name, email, phone, created_at
            FROM view_all_users
            ORDER BY %s %s
            LIMIT ? OFFSET ?
        """.formatted(sortCol, dir);

        List<UserDTO> list = jdbcTemplate.query(sql, USER_ROW_MAPPER, size, offset);

        log.debug("Repository: Paginated users returned {}", list.size());
        return list;
    }

    // -------------------------------------------------------
    // SEARCH + SORT + PAGINATION
    // -------------------------------------------------------
    public List<UserDTO> searchUsers(String query, int limit, int offset, String sortBy, String sortDir) {

        log.info("Repository: Searching users query='{}', limit={}, offset={}, sort={}, direction={}",
                query, limit, offset, sortBy, sortDir);

        String sortCol = validateSortBy(sortBy);
        String dir = validateDirection(sortDir);

        String q = (query == null || query.isBlank()) ? null : "%" + query.toLowerCase() + "%";

        String where = (q != null)
                ? "WHERE lower(first_name) LIKE ? OR lower(last_name) LIKE ? OR lower(email) LIKE ? "
                : "";

        List<Object> params = new ArrayList<>();
        if (q != null) params.addAll(List.of(q, q, q));

        params.add(limit);
        params.add(offset);

        String sql = """
            SELECT id, first_name, last_name, email, phone, created_at
            FROM view_all_users
        """ + where +
                "ORDER BY " + sortCol + " " + dir + " LIMIT ? OFFSET ?";

        log.debug("Repository: Executing search SQL = {}", sql);

        List<UserDTO> result = jdbcTemplate.query(sql, params.toArray(), USER_ROW_MAPPER);

        log.debug("Repository: Search returned {} users", result.size());
        return result;
    }

}
