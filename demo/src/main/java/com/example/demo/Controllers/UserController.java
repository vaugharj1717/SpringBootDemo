package com.example.demo.Controllers;

import com.example.demo.DAOs.UserDAO;
import com.example.demo.Entities.User;
import com.example.demo.Entities.UserInfo;
import com.example.demo.Services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    UserService userService;

    ObjectMapper mapper;    //Used to create JSON responses
    JsonParser parser = JsonParserFactory.getJsonParser();  //Used to parse JSON in request bodies

    @Autowired
    public void setUserService(UserService userService){
        this.userService = userService;
    }
    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) { this.mapper = objectMapper; }

    //GET USER BY ID
    @RequestMapping("/{id}")
    public ObjectNode getUser(@PathVariable int id){
        //ask userService to retrieve user
        User user = userService.getUserById(id);
        //create response JSON object
        ObjectNode response = mapper.createObjectNode();

        if(user != null) {  //success case
            response.put("success", true);

            //create user node
            ObjectNode userNode = mapper.createObjectNode();
            userNode.put("id", user.getId());
            userNode.put("firstName", user.getUserInfo().getFirstName());
            userNode.put("lastName", user.getUserInfo().getLastName());
            userNode.put("role", user.getRole());

            //add user node to response node
            response.put("user", userNode);
            return response;
        }
        else{ //failure case
            response.put("success", false);
            return response;
        }
    }

    //CREATE NEW USER
    @RequestMapping(method = RequestMethod.POST)
    public ObjectNode createUser(@RequestBody String body){
        //turn request JSON into map
        Map<String, Object> bodyMap = parser.parseMap(body);

        //create new User object by pulling data from map
        User newUser = new User();
        UserInfo newUserInfo = new UserInfo();
        newUser.setUsername((String) bodyMap.get("username"));
        newUser.setPassword((String) bodyMap.get("password"));
        newUser.setRole((String) bodyMap.get("role"));
        newUserInfo.setFirstName((String) bodyMap.get("firstName"));
        newUserInfo.setLastName((String) bodyMap.get("lastName"));
        newUser.setUserInfo(newUserInfo);

        //ask userService to register user
        Integer newUserId = userService.registerUser(newUser);

        //create JSON response object
        ObjectNode response = mapper.createObjectNode();
        if(newUserId == null){ //failure case
            response.put("success", false);
            response.put("errorMsg", "Username is taken.");
            return response;
        }
        else{ //success case
            response.put("success", true);
            response.put("id", newUserId);
            return response;
        }
    }

    //DELETE USER
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ObjectNode deleteUser(@PathVariable int id) {
        ObjectNode response = mapper.createObjectNode();
        try {
            //ask userService to delete user
            userService.deleteUserById(id);
            response.put("success", true);
            return response;
        }
        catch(Exception e){
            response.put("success", false);
            return response;
        }
    }
}
