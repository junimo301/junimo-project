package com.example.junimoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.junimoapp.utils.DeviceUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import com.example.junimoapp.Organizer.OrganizerStartScreen;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db;
    String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //get device id
        deviceId = DeviceUtils.getDeviceId(this);

        //test user document
        Map<String, Object> testUser = new HashMap<>();
        testUser.put("deviceId", deviceId);
        testUser.put("test", "connected");

        //write to firestore
        db.collection("users")
                .document(deviceId)
                .set(testUser)
                .addOnSuccessListener(unused -> {

                    //open user homepage when firebase succeeds
                    Intent intent = new Intent(MainActivity.this, UserHomeActivity.class);
                    startActivity(intent);

                    //close mainactivity
                    finish();
                });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button organizerButton = findViewById(R.id.organizer_button);

        organizerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OrganizerStartScreen.class);
            startActivity(intent);
        });

        //Admin button
        Button adminButton = findViewById(R.id.admin_button);
        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminHomeActivity.class);
            startActivity(intent);
        });
    }
}