package com.example.demo.Services;

import com.example.demo.DAOs.UserDAO;
import com.example.demo.Entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;

@Service
public class UserServiceImpl implements UserService{

    UserDAO userDAO;

    @Autowired
    public void setUserDAO(UserDAO userDAO){
        this.userDAO = userDAO;
    }

    public User getUserById(Integer id){
        //No business logic currently, just call delete function from DAO
        return userDAO.getById(id);
    }

    public void deleteUserById(Integer id){
        //No business logic currently, just call delete function from DAO
        userDAO.delete(id);
    }

    public Integer registerUser(User newUser) {
        //check if username already exists in database
        User preexistingUser = userDAO.findByUsername(newUser.getUsername());
        //if user by this username does not exist yet, create user
        if (preexistingUser == null) {
            User savedUser = userDAO.saveOrUpdate(newUser);
            return savedUser.getId();
        }
        //otherwise username is taken, return null
        else{
            return null;
        }

    }





}
