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
        try {
            return userRepository.createUserUsingProcedure(request);
        } catch (DuplicateKeyException e) {
            throw new DuplicateKeyException("DUPLICATE_DATA_ERROR");
        }
    }


    // -------------------------------------------------------------
    // GET ALL USERS
    // -------------------------------------------------------------
    public List<UserDTO> getAllUsers() {
        return userRepository.getAllUsers();
    }


    // -------------------------------------------------------------
    // GET USER BY ID
    // -------------------------------------------------------------
    public UserDTO getUserById(Long id) {
        return userRepository.getUserById(id).orElse(null);
    }


    // -------------------------------------------------------------
    // UPDATE USER
    // -------------------------------------------------------------
    public boolean updateUser(Long id, UserUpdateRequest req) {

        // Check for duplicate email
        if (userRepository.emailExistsForAnotherUser(req.getEmail(), id)) {
            throw new DuplicateEmailException("Email already exists");
        }

        return userRepository.updateUser(id, req);
    }


    // -------------------------------------------------------------
    // DELETE USER
    // -------------------------------------------------------------
    public boolean deleteUser(Long id) {
        return userRepository.deleteUser(id);
    }


    // -------------------------------------------------------------
    // PAGINATION + SORTING
    // -------------------------------------------------------------
    public List<UserDTO> getUsersPaginated(int page, int size, String sortBy, String direction) {
        return userRepository.getUsersPaginated(page, size, sortBy, direction);
    }


    // -------------------------------------------------------------
    // SEARCH + PAGINATION
    // -------------------------------------------------------------
    public List<UserDTO> getUsers(int page, int size, String sortBy, String sortDir, String query) {
        int offset = page * size;
        return userRepository.searchUsers(query, size, offset, sortBy, sortDir);
    }

}
