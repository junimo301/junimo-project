package com.example.junimoapp.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.R;
import com.example.junimoapp.firebase.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity where admins can browse and remove user profiles
 * Implements user story 03.02.01
 */
public class AdminBrowseProfilesActivity extends AppCompatActivity {
    private static final String TAG = "AdminBrowseProfiles";

    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private List<AdminUserAdapter.UserItem> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_profiles);

        //get Firestore instance/singleton
        db = FirebaseManager.getDB();

        //set up the RecyclerView
        recyclerView = findViewById(R.id.adminProfilesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //init list and adapter
        userList = new ArrayList<>();
        //passed a lambda function to handle deleting clicks
        adapter = new AdminUserAdapter(userList, this::onDeleteUserClicked);
        recyclerView.setAdapter(adapter);

        //load users from Firestore
        loadUsers();
    }

    /**
     * fetch all documents from the Firestore users collection
     */
    private void loadUsers() {
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            userList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String docId = doc.getId();
                String name = doc.getString("name");
                String email = doc.getString("email");
                //missing profile info handling:
                if (name == null || name.isEmpty()) name = "(no name entered)";
                if (email == null || email.isEmpty()) email = "(no email entered)";
                userList.add(new AdminUserAdapter.UserItem(docId, name, email));
            }
            //notify adapter that data has changed
            adapter.notifyDataSetChanged();
        })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load users", e);
                    Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * show confirmation dialog when the admin clocks delete
     */
    private void onDeleteUserClicked(AdminUserAdapter.UserItem user) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Profile")
                .setMessage("Are you sure you want to remove \"" + user.name + "\"'s profile? (Permanent!)")
                .setPositiveButton("Remove", (dialog, which) -> deleteUserFromFirestore(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * deletes the user's document from the Firestore users collection
     */
    private void deleteUserFromFirestore(AdminUserAdapter.UserItem user) {
        db.collection("users").document(user.documentId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile removed!", Toast.LENGTH_SHORT).show();
                    //remove user from the local list too/update adapter
                    int position = userList.indexOf(user);
                    if (position != -1) {
                        userList.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting user from database", e);
                    Toast.makeText(this, "Failed to remove profile.", Toast.LENGTH_SHORT).show();
                });
    }
}
