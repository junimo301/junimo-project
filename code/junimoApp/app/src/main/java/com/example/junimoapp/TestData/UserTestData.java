package com.example.junimoapp.TestData;

import com.example.junimoapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserTestData {
    public static List<User> getUsers(){
        List<User> users = new ArrayList<>();
        users.add(new User(
                "id001",
                "Anica",
                "ahorsema@ualberta.ca",
                "780 555 5555"
        ));
        users.add(new User(
                "id002",
                "Ayema",
                "forgot@ualberta.ca",
                "780 555 5555"
        ));
        users.add(new User(
                "id003",
                "Farzana",
                "forgot@ualberta.ca",
                "780 555 5555"
        ));
        users.add(new User(
                "id004",
                "Treya",
                "forgot@ualberta.ca",
                "780 555 5555"
        ));

    return users;
    }
}
