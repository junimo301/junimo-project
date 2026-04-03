package com.example.junimoapp.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.stream.Collectors;

/**
 * Allows admin to browse images uploaded (event posters and profile pictures)
 * and remove any that violate app policy
 *
 * US 03.03.01 - As an admin, I want to be able to remove images
 * US 03.06.01 - As an admin, I want to browse images
 */
public class AdminBrowseImagesActivity extends AppCompatActivity {
    private static final String TAG = "AdminBrowseImages";
    private FirebaseFirestore db;
    private TextView backButton;
    private EditText searchInput;
    private RecyclerView recyclerView;
    private AdminImageAdapter adapter;

    private List<AdminImageAdapter.ImageItem> allImages;

    /**
     * Called when activity is created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_images);

        db = FirebaseManager.getDB();

        //wire up views
        backButton = findViewById(R.id.backToHomeText);
        searchInput = findViewById(R.id.searchImagesInput);
        recyclerView = findViewById(R.id.adminImagesRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        allImages = new ArrayList<>();
        adapter = new AdminImageAdapter(new ArrayList<>(), this::onDeleteImageClicked);
        recyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //intentionally blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterImages(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                //intentionally blank
            }
        });

        loadImages();
    }

    /**
     * Loads all images from Firestore in two passes
     * all event documents w/ non-empty poster field
     * all user documents w/ non-empty profilePhoto field
     */
    private void loadImages() {
        allImages.clear();

        db.collection("events").get()
                .addOnSuccessListener(eventSnaps -> {
                    for (DocumentSnapshot doc : eventSnaps) {
                        String poster = doc.getString("poster");
                        if (poster != null && !poster.isEmpty()) {
                            String title = doc.getString("title");
                            if (title == null || title.isEmpty()) title = "(no title)";

                            allImages.add(new AdminImageAdapter.ImageItem(
                                    doc.getId(),
                                    "events",
                                    "poster",
                                    poster,
                                    getString(R.string.image_type_poster),
                                    getString(R.string.event_title_label) + title
                            ));
                        }
                    }

                    db.collection("users").get()
                            .addOnSuccessListener(userSnaps -> {
                                for (DocumentSnapshot doc : userSnaps) {
                                    String photo = doc.getString("profilePhoto");
                                    if (photo != null && !photo.isEmpty()) {
                                        String name = doc.getString("name");
                                        if (name == null || name.isEmpty()) name = "(no name)";

                                        allImages.add(new AdminImageAdapter.ImageItem(
                                                doc.getId(),
                                                "users",
                                                "profilePhoto",
                                                photo,
                                                getString(R.string.image_type_profile),
                                                getString(R.string.user_label) + name
                                        ));
                                    }
                                }

                                filterImages(searchInput.getText().toString());
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to load user photos", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load event posters", e));
    }

    /**
     * Filters allImages by label
     * @param query
     */
    private void filterImages(String query) {
        List<AdminImageAdapter.ImageItem> filtered;

        if (query.isEmpty()) {
            filtered = new ArrayList<>(allImages);
        } else {
            String lower = query.toLowerCase();
            filtered = allImages.stream()
                    .filter(img -> img.label.toLowerCase().contains(lower))
                    .collect(Collectors.toList());
        }
        adapter.filterList(filtered);
    }

    /**
     * Called when admin taps remove button
     * @param image ImageItem to remove
     */
    private void onDeleteImageClicked(AdminImageAdapter.ImageItem image) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.remove_image))
                .setMessage(getString(R.string.remove_image_confirm) + getString(R.string.permanent))
                .setPositiveButton(getString(R.string.remove), (gialog, which) -> removeImageFromFirestore(image))
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /**
     * Clears image URL field in Firestore, removes item from list and re-filters
     * @param image the ImageItem to clear the URL of
     */
    private void removeImageFromFirestore(AdminImageAdapter.ImageItem image) {
        db.collection(image.collection)
                .document(image.documentId)
                .update(image.fieldName, "")
                .addOnSuccessListener(aVoid -> {
                    allImages.remove(image);
                    filterImages(searchInput.getText().toString());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to remove image", e));
    }
}
