package com.example.test.controller;

import com.example.test.dto.UserCreateRequest;
import com.example.test.dto.UserDTO;
import com.example.test.dto.UserUpdateRequest;
import com.example.test.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // -------------------------------------------------------------
    // CREATE USER
    // -------------------------------------------------------------
    @PostMapping
    public ResponseEntity<Long> createUser(@RequestBody @Valid UserCreateRequest request) {

        log.info("Received CREATE USER request — email={}, firstName={}, lastName={}",
                request.getEmail(), request.getFirstName(), request.getLastName());
        log.debug("Full create request payload: {}", request);

        Long id = userService.createUser(request);

        log.info("User created successfully with ID={}", id);
        return ResponseEntity.ok(id);
    }

    // -------------------------------------------------------------
    // GET ALL USERS (non-paginated)
    // -------------------------------------------------------------
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Fetching ALL users (non-paginated)");
        List<UserDTO> list = userService.getAllUsers();

        log.debug("Total users fetched: {}", list.size());
        return ResponseEntity.ok(list);
    }

    // -------------------------------------------------------------
    // GET USER BY ID
    // -------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {

        log.info("Fetching USER by ID={}", id);

        UserDTO user = userService.getUserById(id);

        // Handle NOT FOUND
        if (user == null) {
            log.warn("No user found for ID={}", id);
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // Log success
        log.debug("Fetched user details for ID={} -> {}", id, user);
        return ResponseEntity.ok(user);
    }

    // -------------------------------------------------------------
    // UPDATE USER
    // -------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateRequest req
    ) {
        log.info("Received UPDATE USER request for ID={}", id);
        log.debug("Update request payload: {}", req);

        boolean updated = userService.updateUser(id, req);

        if (!updated) {
            log.warn("UPDATE FAILED — User not found for ID={}", id);
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        log.info("User updated successfully — ID={}", id);
        return ResponseEntity.ok(Map.of("message", "User updated successfully"));
    }

    // -------------------------------------------------------------
    // DELETE USER
    // -------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        log.info("Received DELETE USER request for ID={}", id);

        boolean deleted = userService.deleteUser(id);

        if (!deleted) {
            log.warn("DELETE FAILED — User not found for ID={}", id);
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        log.info("User deleted successfully — ID={}", id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // -------------------------------------------------------------
    // PAGINATION + SORTING + SEARCH
    // -------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false, name = "q") String query
    ) {

        log.info("Fetching PAGINATED USERS — page={}, size={}, sortBy={}, direction={}, query={}",
                page, size, sortBy, direction, query);

        // SEARCH MODE
        if (query != null && !query.isBlank()) {
            List<UserDTO> filtered = userService.getUsers(page, size, sortBy, direction, query);
            log.debug("Search results count: {}", filtered.size());
            return ResponseEntity.ok(filtered);
        }

        // PAGINATION ONLY
        List<UserDTO> list = userService.getUsersPaginated(page, size, sortBy, direction);
        log.debug("Pagination results count: {}", list.size());
        return ResponseEntity.ok(list);
    }

}
