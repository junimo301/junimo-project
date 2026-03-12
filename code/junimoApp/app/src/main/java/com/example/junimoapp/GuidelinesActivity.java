package com.example.junimoapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GuidelinesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guidelines);

        TextView guidelinesText = findViewById(R.id.guidelinesText);

        String guidelines =
                "Event Lottery Guidelines\n\n" +
                        "1. Entrance must occur during the registration period.\n\n" +
                        "2. Each user may only enter once per event.\n\n" +
                        "3. After registration closes, winners are randomly selected.\n\n" +
                        "4. The number of winners depends on event capacity.\n\n" +
                        "5. Entrants may be placed on a waitlist.\n\n" +
                        "6. Selected users will be notified in the app.\n\n" +
                        "7. Winners must accept before the deadline.";

        guidelinesText.setText(guidelines);
    }
}
