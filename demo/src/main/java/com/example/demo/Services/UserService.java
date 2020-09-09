package com.example.demo.Services;

import com.example.demo.DAOs.UserDAO;
import com.example.demo.Entities.User;

import java.sql.SQLIntegrityConstraintViolationException;

public interface UserService {

    public User getUserById(Integer id);

    public void deleteUserById(Integer id);

    public Integer registerUser(User newUser);
}
