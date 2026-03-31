package com.example.junimoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.User;
import com.example.junimoapp.models.UserSession;
import com.example.junimoapp.utils.DeviceUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * user stories implemented:
 *  - US 01.07.01: Entrant wants to be identified by their device so they do not need a username or password.
 */

/**
 * creates a profile for a user
 */
public class ProfileActivity extends AppCompatActivity {

    private EditText nameInputLayout;
    private EditText emailInput, phoneInput;
    private TextView backButton;
    private Button saveBtn;
    private Button deleteBtn;
    private String deviceId;
    private FirebaseFirestore db;
    private FirebaseManager firebase;
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        boolean newUser = getIntent().getBooleanExtra("new",true);
        boolean organizer = getIntent().getBooleanExtra("organizer",false);

        //initialize firebase and device info
        db = FirebaseManager.getDB();
        firebase = new FirebaseManager();
        deviceId = DeviceUtils.getDeviceId(this);

        //initialize views
        nameInputLayout = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        saveBtn = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backToHomeText);
        deleteBtn = findViewById(R.id.deleteButton);

        if(!newUser){
            user = UserSession.getCurrentUser();
            nameInputLayout.setText(user.getName());
            Log.d("profile activity",user.getName());
            emailInput.setText(user.getEmail());
            phoneInput.setText(user.getPhone());
        }
        else{
            user = new User(deviceId, "", "", "","","","");
        }


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(newUser) {
                    Intent intent= new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else if(organizer) {
                    Intent intent = new Intent(ProfileActivity.this, OrganizerStartScreen.class);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(ProfileActivity.this, UserHomeActivity.class);
                    startActivity(intent);
                }
            }
        });

        //set save button listener
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get values from EditTexts

                String name = nameInputLayout.getText().toString();
                String email = emailInput.getText().toString();
                String phone = phoneInput.getText().toString();

                user.setName(name);
                user.setEmail(email);
                user.setPhone(phone);

                //create user object
                //save to Firestore
                CollectionReference usersRef = firebase.getDB().collection("users");
                if (!newUser) {
                    firebase.updateUser(usersRef, user, "name", name);
                    firebase.updateUser(usersRef, user, "email", email);
                    firebase.updateUser(usersRef, user, "phone", phone);
                    user.initializeEvents();
                }
                else {
                    user.setInvitedEvents("");
                    user.setOrganizedEvents("");
                    user.setWaitlistedEvents("");
                    firebase.addUser(user,usersRef);
                }

                //give feedback to user
                saveBtn.setText("saved! :3");
                UserSession.setCurrentUser(user);
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!newUser){
                    User user = UserSession.getCurrentUser();
                    CollectionReference usersRef=firebase.getDB().collection("users");
                    firebase.deleteUser(user, usersRef);
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}