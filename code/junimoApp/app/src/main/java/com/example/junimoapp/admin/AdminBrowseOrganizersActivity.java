package com.example.junimoapp.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.R;

import com.example.junimoapp.firebase.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * activity for admin to browse/remove organizers
 * diaplays list of all organizers, allows admin to demote organizers to users
 * and deletes all events they made
 * Implements User story 03.05.01
 */
public class AdminBrowseOrganizersActivity extends AppCompatActivity {
    private static final String TAG = "AdminBrowseOrganizers";

    private RecyclerView recyclerView;
    private AdminOrganizerAdapter adapter;
    private List<AdminOrganizerAdapter.OrganizerItem> organizerList;
    private FirebaseFirestore db;

    private EditText searchInput;
    private TextView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //reuse profile browsing layout
        setContentView(R.layout.activity_admin_browse_profiles);

        db = FirebaseManager.getDB();
        recyclerView = findViewById(R.id.adminProfilesRecyclerView); //reusing recyclerview
        searchInput = findViewById(R.id.searchProfilesInput);
        backButton = findViewById(R.id.backToHomeText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        organizerList = new ArrayList<>();
        adapter = new AdminOrganizerAdapter(organizerList, this::onDemoteOrganizerClicked);
        recyclerView.setAdapter(adapter);

        //set up listeners
        setupBackButton();
        setupSearch();

        //load initial data
        loadOrganizers();
    }

    /**
     * fetches all organizers from Firestore, populated RecyclerView
     */
    private void loadOrganizers() {
        db.collection("users").whereEqualTo("organizer", true).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    organizerList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String docId = doc.getId();
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        //flagged field not in Firestore, pass false instead
                        organizerList.add(new AdminOrganizerAdapter.OrganizerItem(docId, name, email, false));
                    }
                    filterList(searchInput.getText().toString());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load organizers", e);
                    Toast.makeText(this, "Failed to load organizers", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * sets up back button listener
     */
    private void setupBackButton() {
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Sets up TextWatcher for search input to filter list
     */
    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                //do nothing
            }
        });
    }

    /**
     * filters list of organizers based on search
     *
     * @param query text to filter for organizer name
     */
    private void filterList(String query) {
        List<AdminOrganizerAdapter.OrganizerItem> filteredList;
        if (query.isEmpty()) {
            //empty search, full list
            filteredList = new ArrayList<>(organizerList);
        } else {
            //filter the list
            String lowerCaseQuery = query.toLowerCase();
            filteredList = organizerList.stream()
                    .filter(organizer -> organizer.name.toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }
        adapter.filterList(filteredList);
    }

    /**
     * displays a confirmation dialog when admin clicks demote button
     * US 03.05.01
     *
     * @param organizer OrganizerItem corresponding to clicked row
     */
    private void onDemoteOrganizerClicked(AdminOrganizerAdapter.OrganizerItem organizer) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Organizer Status")
                .setMessage("Are you sure you want to remove \"" + organizer.name + "\"'s Organizer status? This will delete all of their created events as well. (Permanent!)")
                .setPositiveButton("Confirm", (dialog, which) -> demoteOrganizer(organizer))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * updates user's organizer flag to false in Firestore
     * Part of US 03.05.01
     *
     * @param organizer the organizer to be demoted
     */
    private void demoteOrganizer(AdminOrganizerAdapter.OrganizerItem organizer) {
        //set organizer flag to false for the user
        db.collection("users").document(organizer.documentId)
                .update("organizer", false)
                .addOnSuccessListener(aVoid -> {
                    //find and delete their events
                    deleteOrganizerEvents(organizer);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to remove organizer status", e);
                    Toast.makeText(this, "Failed to remove organizer status.", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * queries for demoted organizer's events, deletes them as a batch
     *
     * @param organizer organizer who's events are to be deleted
     */
    private void deleteOrganizerEvents(AdminOrganizerAdapter.OrganizerItem organizer) {
        db.collection("events").whereEqualTo("organizerID", organizer.documentId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    //use a batch for efficiency of deleting all events at once
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Organizer status removed and events deleted.", Toast.LENGTH_LONG).show();
                        //remove user from local list, update UI
                        int position = organizerList.indexOf(organizer);
                        if (position != -1) {
                            organizerList.remove(position);
                            adapter.notifyItemRemoved(position);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to delete organizer's events", e);
                        Toast.makeText(this, "Removed organizer status, but failed to delete their events.", Toast.LENGTH_LONG).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to find organizer's events to delete", e);
                    Toast.makeText(this, "Removed organizer status, but could not find their events to delete.", Toast.LENGTH_LONG).show();
                });
    }

}