package com.example.ePolan.Utils;

import com.example.ePolan.Model.Entities.User;

import java.util.List;

public class TestUsers {

    public static User user(String id) {
        User u = new User();
        u.setId(id);
        u.setFirstName("Test");
        u.setLastName("User");
        u.setEmail(id + "@test.com");
        return u;
    }
}
