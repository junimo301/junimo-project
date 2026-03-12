package com.example.junimoapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.User;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.firebase.firestore.CollectionReference;

/**
 * user stories implemented:
 *  - US 01.07.01: Entrant wants to be identified by their device so they do not need a username or password.
 */

public class ProfileActivity extends AppCompatActivity {

    EditText nameInput, emailInput, phoneInput;
    Button saveBtn;
    String deviceId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        deviceId = DeviceUtils.getDeviceId(this);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        saveBtn = findViewById(R.id.saveButton);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name= nameInput.getText().toString();
                String email = emailInput.getText().toString();
                String phone = phoneInput.getText().toString();

                User user = new User(deviceId,name,email,phone);

                FirebaseManager firebase= new FirebaseManager();
                CollectionReference usersRef=firebase.getDB().collection("users");
                firebase.addUser(user,usersRef);            }
        });
    }
}