package com.example.junimoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class UserHomeActivity extends AppCompatActivity {

    Button eventsButton;
    Button invitationsButton;
    Button profileButton;
    Button guidelinesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        eventsButton = findViewById(R.id.eventsButton);
        invitationsButton = findViewById(R.id.invitationsButton);
        profileButton = findViewById(R.id.profileButton);
        guidelinesButton = findViewById(R.id.guidelinesButton);

        /**
         * open events page
         * eventsButton.setOnClickListener(v -> startActivity(new Intent(this, EventsActivity.class)));
         */
        //open invitations page
        invitationsButton.setOnClickListener(v ->
                startActivity(new Intent(this, InvitationsActivity.class)));

        //open profile page
        profileButton.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        //open guidelines page
        guidelinesButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, GuidelinesActivity.class);
            startActivity(intent);
        });
    }
}