package com.example.test.service;

import com.example.test.dto.UserCreateRequest;
import com.example.test.dto.UserDTO;
import com.example.test.dto.UserUpdateRequest;
import com.example.test.exception.DuplicateEmailException;
import com.example.test.exception.GlobalExceptionHandler;
import com.example.test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Long createUser(UserCreateRequest request) {

        try {
            return userRepository.createUser(request);
            
        }catch (DuplicateKeyException e){
            throw new DuplicateKeyException("DUPLICATE_DATA_ERROR");
        }
    }
    public List<UserDTO> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public UserDTO getUserById(Long id) {
        return userRepository.getUserById(id);
    }

    public boolean updateUser(Long id, UserUpdateRequest req) {

        // Check email duplication
        if (userRepository.emailExistsForAnotherUser(req.getEmail(), id)) {
            throw new DuplicateEmailException("Email already exists");
        }

        return userRepository.updateUser(id, req);
    }

    public boolean deleteUser(Long id) {
        return userRepository.deleteUser(id);
    }

    public List<UserDTO> getUsersPaginated(int page, int size, String sortBy, String direction) {

        // allowed sorting fields
        List<String> allowedSortFields = List.of("id", "first_name", "last_name", "email", "phone");

        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "id"; // default
        }

        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            direction = "asc"; // default
        }

        return userRepository.getUsersPaginated(page, size, sortBy, direction);
    }

    public List<UserDTO> getUsers(int page, int size, String sortBy, String sortDir, String query) {
        int offset = page * size;
        return userRepository.searchUsers(query, size, offset, sortBy, sortDir);
    }








}

