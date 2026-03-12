package com.example.junimoapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.User;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * user stories implemented:
 *  - US 01.07.01: Entrant wants to be identified by their device so they do not need a username or password.
 */

public class ProfileActivity extends AppCompatActivity {

    private TextInputLayout nameInputLayout;
    private EditText emailInput, phoneInput;
    private Button saveBtn;
    private String deviceId;
    private FirebaseFirestore db;
    private FirebaseManager firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //initialize firebase and device info
        db = FirebaseManager.getDB();
        firebase = new FirebaseManager();
        deviceId = DeviceUtils.getDeviceId(this);

        //initialize views
        nameInputLayout = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        saveBtn = findViewById(R.id.saveButton);

        //set save button listener
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get values from TextInputLayout and EditTexts
                String name = "";
                if (nameInputLayout.getEditText() != null) {
                    name = nameInputLayout.getEditText().getText().toString();
                }

                String email = emailInput.getText().toString();
                String phone = phoneInput.getText().toString();

                //create user object
                User user = new User(deviceId, name, email, phone);

                //save to Firestore
                CollectionReference usersRef = firebase.getDB().collection("users");
                firebase.addUser(user, usersRef);

                //give feedback to user
                saveBtn.setText("saved! :3");
            }
        });
    }
}