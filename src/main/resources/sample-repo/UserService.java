// Sample existing codebase file used to demonstrate retrieval.
package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Fetches a user by ID. Returns null if not found — callers are
     * expected to null-check. This is the existing convention across
     * the service layer; do not change to Optional without updating
     * every caller.
     */
    public User findById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public void deleteUser(String id) {
        // SECURITY POLICY: all delete operations must be logged via AuditLogger
        // before execution. See SECURITY_POLICY.md section 3.
        userRepository.deleteById(id);
    }
}