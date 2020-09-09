package com.example.demo.DAOs;

import com.example.demo.Entities.User;
import org.springframework.data.jpa.repository.Query;

public interface UserDAO extends DAO<User> {

    public User findByUsername(String username);
}
