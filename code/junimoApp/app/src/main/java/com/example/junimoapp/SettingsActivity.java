package com.example.junimoapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.utils.LanguageHelper;

/**
 * Settings activity for the app
 * Allows the user to change the language of the app
 * Options: English (default), french, russian
 * */
public class SettingsActivity extends AppCompatActivity {

    /**
     * Start activity
     * @param savedInstanceState saved instance state
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //find spinner
        Spinner languageSpinner = findViewById(R.id.spinnerLanguage);

        //define the languages
        String[] languages = {"English", "French", "Russian"};
        final String[] languageCodes = {"en", "fr", "ru"};

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String savedLang = prefs.getString("language", "en"); // default English
        int savedIndex = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(savedLang)) {
                savedIndex = i;
                break;
            }
        }

        //adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_light,
                languages
        );
        adapter.setDropDownViewResource(R.layout.spinner_item_light);
        languageSpinner.setAdapter(adapter);

        //set spinner to saved language
        languageSpinner.setSelection(savedIndex, false);

        //handle selection events
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String langCode = languageCodes[position];

                //save selection
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("language", langCode)
                        .apply();

                //apply language
                LanguageHelper.setLanguage(langCode);

                recreate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button backButton = findViewById(R.id.back_sett_button);
        backButton.setOnClickListener(v -> finish());
    }
}
