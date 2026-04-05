package com.example.junimoapp.Organizer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.example.junimoapp.OrganizerStartScreen;
import com.example.junimoapp.R;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;
import com.example.junimoapp.models.UserSession;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.example.junimoapp.firebase.FirebaseManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Organizer creates and edits events.
 *
 * User stories implemented here:
 *  - US 02.01.01 Create a new event and generate a unique promotional QR code
 *  - US 02.01.02 Create a private event (no public listing, no QR code)
 *  - US 02.01.03 Open invite screen after creating private event
 *  - US 02.01.04 Set a registration period
 *  - US 02.03.01 Optionally limit the number of entrants on the waiting list
 */
public class CreateEvent extends AppCompatActivity {

    EditText editTitle, editDescription, editStartDate, editEndDate, editDateEvent,
            editEventLocation, editMaxCapacity, editWaitingList, editPrice;

    android.widget.Spinner editTagSpinner;
    Button uploadNewEvent, previewButton, QRCodeButton, cancelButton, enableGeoLocationButton;
    TextView backButton;

    // ─────────────────────────────────────────────────────────────────────
    // US 02.01.02
    // Checkbox that lets the organizer mark this event as private.
    // When checked:
    //   - the QR code button is disabled (private events have no promo QR)
    //   - isPrivate is saved as true in Firestore
    //   - the event will NOT appear in the public event listing
    // ─────────────────────────────────────────────────────────────────────
    CheckBox checkPrivate;

    CheckBox check_coorganizer;
    Spinner spinner_coorganizer;

    private boolean geoLocation = false;
    private Event createdEvent = null;
    private String QRCodeString = null;
    private String eventID;
    private String organizerID;

    private ImageView eventPoster;
    private Uri imageUri;
    private Button pickImageButton;

    private Bitmap generateQRCode(String content, int size) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size);
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.createBitmap(matrix);
        } catch (Exception e) {
            Log.e("QR", "Failed to generate QR code", e);
            return null;
        }
    }

    private final ActivityResultLauncher<String>
            pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            imageUri = uri;
            String imageFile = uri.getLastPathSegment();
            if (imageFile != null && imageFile.contains("/")) {
                imageFile = imageFile.substring(imageFile.lastIndexOf("/") + 1);
            }
            pickImageButton.setText(imageFile);
            Glide.with(this).load(uri)
                    .placeholder(R.drawable.bg_event_tile)
                    .into(eventPoster);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        editTitle               = findViewById(R.id.edit_title);
        editDescription         = findViewById(R.id.edit_description);
        editStartDate           = findViewById(R.id.edit_start_date);
        editEndDate             = findViewById(R.id.edit_end_date);
        editDateEvent           = findViewById(R.id.edit_date);
        editEventLocation       = findViewById(R.id.edit_event_location);
        editMaxCapacity         = findViewById(R.id.edit_max_capacity);
        editWaitingList         = findViewById(R.id.edit_waiting_list);
        editPrice               = findViewById(R.id.edit_price);
        uploadNewEvent          = findViewById(R.id.upload_event_button);
        QRCodeButton            = findViewById(R.id.QR_code_button);
        enableGeoLocationButton = findViewById(R.id.enable_geoLocation_button);
        backButton              = findViewById(R.id.backButton);
        cancelButton            = findViewById(R.id.cancel_button);
        previewButton           = findViewById(R.id.preview_event_button);
        editTagSpinner          = findViewById(R.id.edit_tag_spinner);
        pickImageButton         = findViewById(R.id.pick_image_button);
        eventPoster             = findViewById(R.id.event_poster);
        CheckBox check_coorganizer = findViewById(R.id.check_coorganizer);
        Spinner spinner_coorganizer = findViewById(R.id.spinner_coorganizer);

        FirebaseManager firebase = new FirebaseManager();
        firebase.getDB().collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> userNames = new ArrayList<>();
                    List<String> userIds = new ArrayList<>();
                    User currentUser = UserSession.getCurrentUser();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String name = doc.getString("name");
                        String deviceId = doc.getString("deviceId");
                        if (!deviceId.equals(currentUser.getDeviceId())) {
                            userNames.add(name);
                            userIds.add(deviceId);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            R.layout.spinner_item_light,
                            userNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner_coorganizer.setAdapter(adapter);
                    spinner_coorganizer.setTag(userIds); //store IDs for later
                });

        check_coorganizer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            spinner_coorganizer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        Glide.with(this).load((String) null)
                .placeholder(R.drawable.bg_event_tile)
                .into(eventPoster);

        editDateEvent.setFocusable(false);
        editDateEvent.setFocusableInTouchMode(false);
        editDateEvent.setClickable(true);
        editDateEvent.setOnClickListener(view -> SelectDate(editDateEvent));

        editStartDate.setFocusable(false);
        editStartDate.setFocusableInTouchMode(false);
        editStartDate.setClickable(true);
        editStartDate.setOnClickListener(view -> SelectDate(editStartDate));

        editEndDate.setFocusable(false);
        editEndDate.setFocusableInTouchMode(false);
        editEndDate.setClickable(true);
        editEndDate.setOnClickListener(view -> SelectDate(editEndDate));

        // ─────────────────────────────────────────────────────────────────
        // US 02.01.02
        // Wire up the private event checkbox and disable QR when checked.
        // ─────────────────────────────────────────────────────────────────
        checkPrivate = findViewById(R.id.check_private_event);
        checkPrivate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            QRCodeButton.setEnabled(!isChecked);
            if (isChecked) {
                QRCodeString = null;
                Toast.makeText(this,
                        "Private event: no QR code will be generated.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Pre-fill fields if editing an existing event
        eventID = getIntent().getStringExtra("eventID");
        if (eventID != null) {
            createdEvent = EventData.searchEventID(eventID);
            if (createdEvent != null) {
                if (createdEvent.getQRCode() != null) {
                    QRCodeString = createdEvent.getQRCode();
                }
                editTitle.setText(createdEvent.getTitle());
                editDescription.setText(createdEvent.getDescription());
                editStartDate.setText(createdEvent.getStartDate());
                editEndDate.setText(createdEvent.getEndDate());
                editDateEvent.setText(createdEvent.getDateEvent());
                editEventLocation.setText(createdEvent.getEventLocation());
                editMaxCapacity.setText(String.valueOf(createdEvent.getMaxCapacity()));
                editWaitingList.setText(String.valueOf(createdEvent.getWaitingListLimit()));
                editPrice.setText(String.valueOf(createdEvent.getPrice()));
                if (createdEvent.getPoster() != null && !createdEvent.getPoster().isEmpty()) {
                    Glide.with(this).load(createdEvent.getPoster())
                            .placeholder(R.drawable.bg_event_tile)
                            .error(R.drawable.bg_event_tile)
                            .into(eventPoster);
                }
                if (createdEvent.getTag() != null && !createdEvent.getTag().isEmpty()) {
                    android.widget.ArrayAdapter<CharSequence> adapter =
                            (android.widget.ArrayAdapter<CharSequence>) editTagSpinner.getAdapter();
                    if (adapter != null) {
                        int position = adapter.getPosition(createdEvent.getTag());
                        if (position >= 0) editTagSpinner.setSelection(position);
                    }
                }
                // ─────────────────────────────────────────────────────────
                // US 02.01.02 — restore private flag when editing
                // ─────────────────────────────────────────────────────────
                checkPrivate.setChecked(createdEvent.isPrivate());
                geoLocation = createdEvent.isGeoLocation();
                enableGeoLocationButton.setText(geoLocation
                        ? getString(R.string.enabled)
                        : getString(R.string.disabled));
            }
        }

        enableGeoLocationButton.setOnClickListener(view -> {
            geoLocation = !geoLocation;
            enableGeoLocationButton.setText(geoLocation
                    ? getString(R.string.enabled)
                    : getString(R.string.disabled));
        });

        pickImageButton.setOnClickListener(view -> pickImage.launch("image/*"));

        // ─────────────────────────────────────────────────────────────────
        // US 02.01.01 / US 02.01.02 — QR code generation
        // ─────────────────────────────────────────────────────────────────
        QRCodeButton.setOnClickListener(view -> {
            if (checkPrivate.isChecked()) {
                Toast.makeText(this, "Private events do not use a QR code.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (QRCodeString == null) {
                if (eventID == null) eventID = UUID.randomUUID().toString();
                QRCodeString = "https://junimo.app/event?id=" + eventID;
                if (createdEvent != null) createdEvent.setQRCode(QRCodeString);
                Toast.makeText(this, "QR code generated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "QR already exists", Toast.LENGTH_SHORT).show();
            }

            Bitmap QRBitmap = generateQRCode(QRCodeString, 600);
            if (QRBitmap == null) return;

            View dialogView = getLayoutInflater().inflate(R.layout.pop_up_qr_code, null);
            ((ImageView) dialogView.findViewById(R.id.QR_image)).setImageBitmap(QRBitmap);
            ((TextView) dialogView.findViewById(R.id.event_title)).setText(editTitle.getText().toString());

            AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

            dialogView.findViewById(R.id.close_button).setOnClickListener(v -> dialog.dismiss());
            dialog.show();

            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setDimAmount(0.5f);

                window.setGravity(Gravity.CENTER);
                int screenWidth = view.getResources().getDisplayMetrics().widthPixels;
                int dialogWidth = (int) (screenWidth * 0.75);
                window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);

                ImageView QRCodeImageView = dialogView.findViewById(R.id.QR_image);
                int QRCodeSize = (int) (dialogWidth * 0.90);
                QRCodeImageView.getLayoutParams().width = QRCodeSize;
                QRCodeImageView.getLayoutParams().height = QRCodeSize;
                QRCodeImageView.requestLayout();
            }
        });

        previewButton.setOnClickListener(view -> {
            Intent previewEvent = new Intent(CreateEvent.this, EventPreview.class);
            previewEvent.putExtra("title", editTitle.getText().toString());
            previewEvent.putExtra("description", editDescription.getText().toString());
            previewEvent.putExtra("startDate", editStartDate.getText().toString());
            previewEvent.putExtra("endDate", editEndDate.getText().toString());
            previewEvent.putExtra("waitingListLimit", editWaitingList.getText().toString());
            previewEvent.putExtra("dateEvent", editDateEvent.getText().toString());
            previewEvent.putExtra("eventLocation", editEventLocation.getText().toString());
            previewEvent.putExtra("maxCapacity", editMaxCapacity.getText().toString());
            previewEvent.putExtra("price", editPrice.getText().toString());
            if (createdEvent != null && createdEvent.getPoster() != null && !createdEvent.getPoster().isEmpty()) {
                previewEvent.putExtra("poster", createdEvent.getPoster());
            } else if (imageUri != null) {
                previewEvent.putExtra("posterURI", imageUri.toString());
            }
            startActivity(previewEvent);
        });

        uploadNewEvent.setOnClickListener(v -> uploadNewEvent());
        backButton.setOnClickListener(view -> finish());
        cancelButton.setOnClickListener(view -> finish());
    }

    private void SelectDate(EditText target) {
        Calendar calendar = Calendar.getInstance();
        String dateExists = target.getText().toString();
        if (!dateExists.equals("")) {
            try {
                String[] separators = dateExists.split("-");
                calendar.set(Integer.parseInt(separators[0]),
                        Integer.parseInt(separators[1]) - 1,
                        Integer.parseInt(separators[2]));
            } catch (Exception ignored) {}
        }
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            target.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Validates all fields then uploads the event to Firebase.
     * US 02.01.02: saves private flag, skips QR for private events.
     * US 02.01.03: navigates to PrivateInviteActivity for private events.
     */
    private void uploadNewEvent() {
        if (createdEvent != null) {
            eventID = createdEvent.getEventID();
        } else if (eventID == null) {
            eventID = UUID.randomUUID().toString();
        }

        if (QRCodeString == null && !checkPrivate.isChecked()) {
            QRCodeButton.setText("Must Generate QR Code");
            Toast.makeText(this, "Must Generate QR Code", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = editDescription.getText().toString();
        String startDate   = editStartDate.getText().toString();
        String endDate     = editEndDate.getText().toString();

        if (!startDate.equals("") && !endDate.equals("")) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date start = format.parse(startDate);
                Date end   = format.parse(endDate);
                if (start.after(end)) {
                    editStartDate.setError("Start date must be before end date");
                    editStartDate.requestFocus();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final Integer finalWaitingListLimit;
        String waitingListLimit = editWaitingList.getText().toString();
        if (!waitingListLimit.equals("")) {
            int parsedLimit = Integer.parseInt(waitingListLimit);
            if (parsedLimit < 0) {
                editWaitingList.setError("Must be a positive integer");
                editWaitingList.requestFocus();
                return;
            }
            finalWaitingListLimit = parsedLimit;
        } else {
            finalWaitingListLimit = null;
        }

        String title = editTitle.getText().toString();
        if (title.equals("")) { editTitle.setError("*Field Required"); editTitle.requestFocus(); return; }

        String dateEvent = editDateEvent.getText().toString();
        if (dateEvent.equals("")) { editDateEvent.setError("*Field Required"); editDateEvent.requestFocus(); return; }

        String eventLocation = editEventLocation.getText().toString();
        if (eventLocation.equals("")) { editEventLocation.setError("*Field Required"); editEventLocation.requestFocus(); return; }

        if (editMaxCapacity.getText().toString().equals("")) { editMaxCapacity.setError("*Field Required"); editMaxCapacity.requestFocus(); return; }
        int maxCapacity = Integer.parseInt(editMaxCapacity.getText().toString());

        if (editPrice.getText().toString().equals("")) { editPrice.setError("*Field Required"); editPrice.requestFocus(); return; }
        double price = Double.parseDouble(editPrice.getText().toString());

        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            organizerID = currentUser.getDeviceId();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String tag = editTagSpinner.getSelectedItem().toString();
        if (tag.equals("None")) tag = "";

        boolean isPrivate = checkPrivate.isChecked();
        String finalTag = tag;
        boolean finalIsPrivate = isPrivate;
        String finalEventID = eventID;
        String finalTitle = title;

        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference("event_poster/" + eventID + ".jpg");

            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String poster = uri.toString();
                    Event saveEvent = new Event(title, description, startDate, endDate, dateEvent,
                            maxCapacity, finalWaitingListLimit, price, geoLocation,
                            poster, finalEventID, eventLocation, organizerID, finalTag);

                    saveEvent.setPrivate(finalIsPrivate);
                    if (!finalIsPrivate && QRCodeString != null) saveEvent.setQRCode(QRCodeString);

                    FirebaseManager firebase = new FirebaseManager();
                    firebase.addEvent(saveEvent, firebase.getDB().collection("events"));

                    Toast.makeText(CreateEvent.this, "Event Created", Toast.LENGTH_SHORT).show();
                    Log.d("createEvent", "Event created: " + saveEvent.getTitle());

                    navigateAfterSave(finalIsPrivate, finalEventID, finalTitle);

                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        } else {
            String poster = (createdEvent != null) ? createdEvent.getPoster() : "";
            Event saveEvent = new Event(title, description, startDate, endDate, dateEvent,
                    maxCapacity, finalWaitingListLimit, price, geoLocation,
                    poster, eventID, eventLocation, organizerID, tag);

            saveEvent.setPrivate(isPrivate);
            if (!isPrivate && QRCodeString != null) saveEvent.setQRCode(QRCodeString);

            if (check_coorganizer.isChecked() && spinner_coorganizer.getSelectedItem() != null) {
                int position = spinner_coorganizer.getSelectedItemPosition();
                List<String> userIds = (List<String>) spinner_coorganizer.getTag();
                String coOrganizerId = userIds.get(position);

                //mark the event as pending co-organizer invite for that user
                FirebaseManager firebase = new FirebaseManager();
                firebase.getDB().collection("users").document(coOrganizerId)
                        .update("coOrganizerInvites", FieldValue.arrayUnion(eventID))
                        .addOnSuccessListener(aVoid -> Log.d("CreateEvent", "Co-organizer invite sent"))
                        .addOnFailureListener(e -> Log.e("CreateEvent", "Failed to send co-organizer invite", e));
            }

            FirebaseManager firebase = new FirebaseManager();
            firebase.addEvent(saveEvent, firebase.getDB().collection("events"));

            Toast.makeText(this, "Event Created", Toast.LENGTH_SHORT).show();
            Log.d("createEvent", "Event created: " + saveEvent.getTitle());

            navigateAfterSave(isPrivate, eventID, title);
        }
    }

    /**
     * US 02.01.02 / US 02.01.03
     * After saving the event:
     * - If private: open PrivateInviteActivity then clear the back stack
     *   so returning lands on OrganizerStartScreen (triggers onResume reload).
     * - If public: go straight back to OrganizerStartScreen.
     *
     * FLAG_ACTIVITY_CLEAR_TOP ensures OrganizerStartScreen is brought to
     * the front and its onResume() fires, which reloads the event list.
     */
    private void navigateAfterSave(boolean isPrivate, String savedEventId, String savedTitle) {
        if (isPrivate) {
            // ─────────────────────────────────────────────────────────────
            // US 02.01.03
            // Open invite screen. When the organizer presses back there,
            // FLAG_ACTIVITY_CLEAR_TOP brings OrganizerStartScreen to the
            // front and triggers its onResume() to reload the event list.
            // ─────────────────────────────────────────────────────────────
            Intent invite = new Intent(CreateEvent.this, PrivateInviteActivity.class);
            invite.putExtra("eventId", savedEventId);
            invite.putExtra("eventTitle", savedTitle);
            // Clear the stack so PrivateInvite → back → OrganizerStartScreen
            invite.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(invite);
        } else {
            // Public event — go straight back to organizer home
            Intent home = new Intent(CreateEvent.this, OrganizerStartScreen.class);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(home);
        }
        finish();
    }
}