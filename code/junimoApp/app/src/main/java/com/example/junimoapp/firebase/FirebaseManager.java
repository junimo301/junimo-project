package com.example.junimoapp.firebase;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {
    private FirebaseManager() {}

    //get firestore instance
    public static FirebaseFirestore getDB() {
        return FirebaseFirestore.getInstance();
    }
}