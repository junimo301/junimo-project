package com.example.junimoapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.R;

/**
 * AdminHomeActivity is the main home page for admin to browse events/profiles/images
 * (and later notifications, too)
 * User stories: 03.01.01, 03.02.01, 03.03.01
 */
public class AdminHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        //initializing the buttons that are in the layout file
        Button browseEventsButton = findViewById(R.id.adminBrowseEventsButton);
        Button browseProfilesButton = findViewById(R.id.adminBrowseProfilesButton);
        //Button browseImagesButton = findViewById(R.id.adminBrowseImagesButton);

        //navigation for the event browsing button
        browseEventsButton.setOnClickListener(v -> startActivity(new Intent(this, AdminBrowseEventsActivity.class)));

        //navigation for the profile browsing button
        browseProfilesButton.setOnClickListener(v -> startActivity(new Intent(this, AdminBrowseProfilesActivity.class)));

        //navigation for the images browsing button
        //browseImagesButton.setOnClickListener(v -> startActivity(new Intent(this, AdminBrowseImagesActivity.class)));

    }
}
