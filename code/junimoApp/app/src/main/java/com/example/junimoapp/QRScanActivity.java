package com.example.junimoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.junimoapp.utils.BaseActivity;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

/**
 * US 01.06.01
 * Entrant wants to be able to view event details by scanning a QR code.
 *
 * How it works:
 *  1. This activity opens the camera using the ZXing library
 *     (already in the project — BarcodeEncoder is used in CreateEvent).
 *  2. When a QR code is scanned it reads the encoded string.
 *  3. QR codes are in the format: junimo://event?id=<eventID>
 *     (set by CreateEvent when the organizer taps Generate QR).
 *  4. We parse the eventID and pass it to EventDetailsActivity via
 *     the "eventID" intent extra — the same key EventDetailsActivity
 *     already uses to load event data from Firestore.
 *
 * Layout: activity_qr_scan.xml
 * Camera permission is declared in AndroidManifest.xml.
 */
public class QRScanActivity extends BaseActivity {

    // ─────────────────────────────────────────────────────────────────────
    // US 01.06.01
    // The ZXing DecoratedBarcodeView handles camera preview and scanning.
    // ─────────────────────────────────────────────────────────────────────
    private DecoratedBarcodeView barcodeView;
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    // Guard so we only navigate once even if multiple frames decode
    private boolean scanned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        barcodeView = findViewById(R.id.barcode_scanner);
        TextView backButton = findViewById(R.id.backToHomeText);
        backButton.setOnClickListener(v->{
            Intent intent = new Intent(this,UserHomeActivity.class);
            startActivity(intent);
        });

        // ─────────────────────────────────────────────────────────────────
        // US 01.06.01
        // Request camera permission at runtime if not already granted.
        // Android requires this for camera access on API 23+.
        // ─────────────────────────────────────────────────────────────────
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            startScanning();
        }
    }

    /**
     * US 01.06.01
     * Starts the continuous barcode scan.
     * On each successful decode, checks if it is a valid junimo event QR,
     * parses the eventID, and opens EventDetailsActivity.
     */
    private void startScanning() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                // Guard against processing the same scan multiple times
                if (scanned) return;

                String raw = result.getText();
                if (raw == null) return;

                // ─────────────────────────────────────────────────────────
                // US 01.06.01
                // Parse the eventID from the QR string.
                // Format set by CreateEvent: "junimo://event?id=<eventID>"
                // ─────────────────────────────────────────────────────────
                String eventID = parseEventId(raw);

                if (eventID == null) {
                    Toast.makeText(QRScanActivity.this,
                            "Invalid QR code — not a Junimo event code",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                scanned = true;

                // ─────────────────────────────────────────────────────────
                // US 01.06.01
                // Pass the parsed eventID to EventDetailsActivity using the
                // "eventID" key — the same key it already reads from in onCreate.
                // ─────────────────────────────────────────────────────────
                Intent intent = new Intent(QRScanActivity.this, EventDetailsActivity.class);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
                finish();
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Not needed — camera overlay handles visual feedback
            }
        });
    }

    /**
     * US 01.06.01
     * Parses "junimo://event?id=<eventID>" and returns just the eventID portion.
     * Returns null if the string is not in the expected format.
     */
    private String parseEventId(String raw) {
        String prefix = "https://junimo.app/event?id=";
        if (raw != null && raw.startsWith(prefix)) {
            String id = raw.substring(prefix.length()).trim();
            return id.equals("") ? null : id;
        }
        return null;
    }

    // ── Activity lifecycle — pause/resume scanner with the activity ────────

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    /**
     * US 01.06.01
     * Handle the result of the camera permission request.
     * If granted, start scanning. If denied, show a message and close.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this,
                        "Camera permission is needed to scan QR codes",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}

