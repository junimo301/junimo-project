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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseOrganizersActivity extends AppCompatActivity {
    private static final String TAG = "AdminBrowseOrganizers";

    private RecyclerView recyclerView;
    private AdminOrganizerAdapter adapter;
    private List<AdminOrganizerAdapter.OrganizerItem> organizerList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //reuse profile browsing layout
        setContentView(R.layout.activity_admin_browse_profiles);

        db = FirebaseManager.getDB();
        recyclerView = findViewById(R.id.adminProfilesRecyclerView); //reusing recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        organizerList = new ArrayList<>();
        adapter = new AdminOrganizerAdapter(organizerList, this::onDemoteOrganizerClicked);
        recyclerView.setAdapter(adapter);

        loadOrganizers();
    }

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
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load organizers", e);
                    Toast.makeText(this, "Failed to load organizers", Toast.LENGTH_SHORT).show();
                });
    }

    private void onDemoteOrganizerClicked(AdminOrganizerAdapter.OrganizerItem organizer) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Organizer Status")
                .setMessage("Are you sure you want to remove \"" + organizer.name + "\"'s Organizer status? This will delete all of their created events as well. (Permanent!)")
                .setPositiveButton("Confirm", (dialog, which) -> demoteOrganizer(organizer))
                .setNegativeButton("Cancel", null)
                .show();
    }

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
