package com.example.test.service;

import com.example.test.dto.UserCreateRequest;
import com.example.test.dto.UserDTO;
import com.example.test.dto.UserUpdateRequest;
import com.example.test.exception.DuplicateEmailException;
import com.example.test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // -------------------------------------------------------------
    // CREATE USER (Stored Procedure)
    // -------------------------------------------------------------
    public Long createUser(UserCreateRequest request) {

        log.info("Service: Creating user with email={}", request.getEmail());
        log.debug("UserCreateRequest payload: {}", request);

        try {
            Long id = userRepository.createUserUsingProcedure(request);
            log.info("Service: User created successfully with ID={}", id);
            return id;

        } catch (DuplicateKeyException e) {
            log.warn("Service: Duplicate email detected for email={}", request.getEmail());
            throw new DuplicateKeyException("DUPLICATE_DATA_ERROR");
        }
    }

    // -------------------------------------------------------------
    // GET ALL USERS
    // -------------------------------------------------------------
    public List<UserDTO> getAllUsers() {

        log.info("Service: Fetching ALL users");
        List<UserDTO> list = userRepository.getAllUsers();
        log.debug("Service: Total users returned={}", list.size());

        return list;
    }

    // -------------------------------------------------------------
    // GET USER BY ID
    // -------------------------------------------------------------
    public UserDTO getUserById(Long id) {

        log.info("Fetching user by ID={}", id);

        UserDTO user = userRepository.getUserById(id).orElse(null);

        if (user == null) {
            log.warn("No user found for ID={}", id);
        } else {
            log.debug("User found for ID={} -> {}", id, user);
        }

        return user;
    }


    // -------------------------------------------------------------
    // UPDATE USER
    // -------------------------------------------------------------
    public boolean updateUser(Long id, UserUpdateRequest req) {

        log.info("Service: Updating user ID={}", id);
        log.debug("UserUpdateRequest payload: {}", req);

        // Duplicate email check
        if (userRepository.emailExistsForAnotherUser(req.getEmail(), id)) {
            log.warn("Service: Cannot update. Email '{}' already exists for another user.", req.getEmail());
            throw new DuplicateEmailException("Email already exists");
        }

        boolean updated = userRepository.updateUser(id, req);

        if (updated) {
            log.info("Service: User updated successfully — ID={}", id);
        } else {
            log.warn("Service: Update failed. No user exists for ID={}", id);
        }

        return updated;
    }

    // -------------------------------------------------------------
    // DELETE USER
    // -------------------------------------------------------------
    public boolean deleteUser(Long id) {

        log.info("Service: Deleting user ID={}", id);

        boolean deleted = userRepository.deleteUser(id);

        if (deleted) {
            log.info("Service: User deleted successfully — ID={}", id);
        } else {
            log.warn("Service: Delete failed. No user exists for ID={}", id);
        }

        return deleted;
    }

    // -------------------------------------------------------------
    // PAGINATION + SORTING
    // -------------------------------------------------------------
    public List<UserDTO> getUsersPaginated(int page, int size, String sortBy, String direction) {

        log.info("Service: Fetching paginated users page={}, size={}, sort={}, direction={}",
                page, size, sortBy, direction);

        return userRepository.getUsersPaginated(page, size, sortBy, direction);
    }

    // -------------------------------------------------------------
    // SEARCH + PAGINATION
    // -------------------------------------------------------------
    public List<UserDTO> getUsers(int page, int size, String sortBy, String sortDir, String query) {

        int offset = page * size;

        log.info("Service: Searching users — page={}, size={}, sort={}, direction={}, query={}",
                page, size, sortBy, sortDir, query);

        return userRepository.searchUsers(query, size, offset, sortBy, sortDir);
    }
}
