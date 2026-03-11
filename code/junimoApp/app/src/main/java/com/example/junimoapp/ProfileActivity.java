package com.example.junimoapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    EditText nameInput, emailInput, phoneInput;
    Button saveBtn;

    FirebaseFirestore db;
    String deviceId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseManager.getDB();
        deviceId = DeviceUtils.getDeviceId(this);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        saveBtn = findViewById(R.id.saveButton);

        saveBtn.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {

        Map<String, Object> user = new HashMap<>();
        user.put("name", nameInput.getText().toString());
        user.put("email", emailInput.getText().toString());
        user.put("phone", phoneInput.getText().toString());

        db.collection("users")
                .document(deviceId)
                .set(user);
    }
}