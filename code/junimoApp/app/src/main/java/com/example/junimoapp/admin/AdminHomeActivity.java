package com.example.junimoapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.R;

/**
 * AdminHomeActivity is the main home page for admin to browse (and delete) events/profiles/images
 * (and later notifications, too)
 * User stories: 03.01.01, 03.02.01, 03.03.01
 */
public class AdminHomeActivity extends AppCompatActivity {

    /**
     * Called when activity is created - has the setup for all of the admin functions
     * and buttons and such
     * @param savedInstanceState typical var that's default to onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        //initializing the buttons that are in the layout file
        Button browseEventsButton = findViewById(R.id.adminBrowseEventsButton);
        Button browseProfilesButton = findViewById(R.id.adminBrowseProfilesButton);
        Button browseOrganizersButton = findViewById(R.id.adminBrowseOrganizersButton);

        //navigation for the event browsing button (click listener)
        browseEventsButton.setOnClickListener(v -> startActivity(new Intent(this, AdminBrowseEventsActivity.class)));

        //navigation for the profile browsing button (click listener)
        browseProfilesButton.setOnClickListener(v -> startActivity(new Intent(this, AdminBrowseProfilesActivity.class)));

        //navigation for the organizers browsing button (click listener)
        browseOrganizersButton.setOnClickListener(v -> startActivity(new Intent(this, AdminBrowseOrganizersActivity.class)));

    }
}
