package com.example.junimoapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.utils.LanguageHelper;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //find spinner
        Spinner languageSpinner = findViewById(R.id.spinnerLanguage);

        //define the languages
        String[] languages = {"English", "French", "Russian"};
        final String[] languageCodes = {"en", "fr", "ru"};

        //adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                languages
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        //set default to eng
        languageSpinner.setSelection(0);

        //handle selection events
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstSelection = true; //prevent immediate trigger on init

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstSelection) {
                    firstSelection = false;
                    return; //skip first automatic call
                }

                //get language code
                String langCode = languageCodes[position];

                //set app language
                LanguageHelper.setLanguage(langCode);

                recreate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });
    }
}
