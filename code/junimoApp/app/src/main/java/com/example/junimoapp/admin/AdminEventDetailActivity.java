package com.example.junimoapp.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.R;
import com.example.junimoapp.firebase.FirebaseManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminEventDetailActivity shows details of an event when clicked on from the admin browsing
 * but only the relevant details and the comments (so comments can be removed)
 *
 * Only "relevant" details are the fields that the user can freely fill out, because
 * those ones could contain policy violations.
 *
 * US 03.10.01: As an administrator, I want to remove event comments that violate app policy
 */
public class AdminEventDetailActivity extends AppCompatActivity {
    //tag for Logcat
    private static final String TAG = "AdminEventDetail";

    private String eventId;

    private FirebaseFirestore db;

    // VIEWS
    private TextView backButton;
    private TextView titleText;
    private TextView descriptionText;
    private TextView locationText;
    private TextView dateText;
    private TextView organizerIdText;
    private RecyclerView commentsRecyclerView;
    private AdminCommentAdapter commentAdapter;

    // lists of all comments from Firestore
    private List<AdminCommentAdapter.CommentItem> commentList;

    /**
     * Called when Activity first created, reads event ID, sets up views, loads data
     * @param savedInstanceState previously saved state (unused)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_detail);

        db = FirebaseManager.getDB();

        //read event ID
        eventId = getIntent().getStringExtra("eventId");

        //wire up buttons
        backButton = findViewById(R.id.backToEventsText);
        titleText = findViewById(R.id.adminDetailEventTitle);
        descriptionText = findViewById(R.id.adminDetailDescription);
        locationText = findViewById(R.id.adminDetailLocation);
        dateText = findViewById(R.id.adminDetailDate);
        organizerIdText = findViewById(R.id.adminDetailOrganizerId);
        commentsRecyclerView = findViewById(R.id.adminCommentsRecyclerView);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        commentList = new ArrayList<>();
        commentAdapter = new AdminCommentAdapter(commentList, this::onDeleteCommentClicked);
        commentsRecyclerView.setAdapter(commentAdapter);

        backButton.setOnClickListener(v -> finish());

        loadEventDetails();
        loadComments();
    }

    /**
     * Loads event doc from firestore, populates detail view
     */
    private void loadEventDetails() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String title = doc.getString("title");
                    String description = doc.getString("description");
                    String location = doc.getString("eventLocation");
                    String date = doc.getString("dateEvent");
                    String organizerId = doc.getString("organizerID");

                    //placeholders for missing fields
                    if (title == null) title = "(no title)";
                    if (description == null) description = "(no description)";
                    if (location == null) location = "(no location)";
                    if (date == null) date = "(no date)";
                    if (organizerId == null) organizerId = "(unknown)";

                    //pupulate views
                    titleText.setText(title);
                    descriptionText.setText(getString(R.string.description_label) + description);
                    locationText.setText(getString(R.string.location_label) + location);
                    dateText.setText(getString(R.string.date_label) + date);
                    organizerIdText.setText(getString(R.string.organizer_id_label) + organizerId);
                })
                .addOnFailureListener(e -> Log.e(TAG, "failed to load event details", e));
    }

    /**
     * loads all comments from the event's sub-collection w/ text field and userID
     * fields so admin can see who wrote the comment
     */
    private void loadComments() {
        db.collection("events")
                .document(eventId)
                .collection("comments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    commentList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String commentId = doc.getId();
                        String text = doc.getString("text");
                        String userId = doc.getString("userId");

                        //skip weirdly formed docs
                        if (text == null || userId == null) continue;

                        commentList.add(new AdminCommentAdapter.CommentItem(commentId, userId, text));
                    }
                    commentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load comments", e));
    }

    /**
     * Permanently deletes the comment from Firestore, on success
     * removes it from the local list and updates the RecyclerView
     * @param comment the CommentItem to delete
     */
    private void deleteComment(AdminCommentAdapter.CommentItem comment) {
        db.collection("events")
                .document(eventId)
                .collection("comments")
                .document(comment.commentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    //remove from local list, refresh UI
                    int position = commentList.indexOf(comment);
                    if (position != -1) {
                        commentList.remove(position);
                        commentAdapter.notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete comment", e));
    }


    /**
     * Deletion confirmation thing
     */
    private void onDeleteCommentClicked(AdminCommentAdapter.CommentItem comment) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Comment")
                .setMessage("Are you sure you want to delete this comment? This action is permanent.")
                .setPositiveButton("Remove", (dialog, which) -> deleteComment(comment))
                .setNegativeButton("Cancel", null)
                .show();
    }

}
