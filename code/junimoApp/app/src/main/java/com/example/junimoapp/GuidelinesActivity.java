package com.example.junimoapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.utils.BaseActivity;

/**
 * displays guidelines for a lottery
 * user stories implemented:
 *  - US 01.05.05: Entrant wants to know the criteria or guidelines for the lottery selection process.
 */

public class GuidelinesActivity extends BaseActivity {

    /**
     * Start activity
     * @param savedInstanceState saved instance state
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guidelines);

        TextView guidelinesText = findViewById(R.id.guidelinesText);
        TextView backButton = findViewById(R.id.guidelinesBackButton);
//
//        String guidelines =
//              <string name="app_rules">Event Lottery Guidelines\n\n1. Entrance must occur during the registration period.\n\n2. Each user may only enter once per event.\n\n" +
//        "3. After registration closes, winners are randomly selected.\n\n" +
//                "4. The number of winners depends on event capacity.\n\n" +
//                "5. Entrants may be placed on a waitlist.\n\n" +
//                "6. Selected users will be notified in the app.\n\n" +
//                "7. Winners must accept before the deadline.";</string>


        backButton.setOnClickListener(v -> finish());
    }
}
