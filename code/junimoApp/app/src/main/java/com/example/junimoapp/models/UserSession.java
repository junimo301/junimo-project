package com.example.junimoapp.models;

public class UserSession {
    /*
    * Tracking login
    *
    */
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        UserSession.currentUser = currentUser;
    }
}
