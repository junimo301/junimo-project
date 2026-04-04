package com.example.junimoapp.utils;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.junimoapp.R;
import com.example.junimoapp.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID) {
        //root layout
        ConstraintLayout fullLayout = new ConstraintLayout(this);
        fullLayout.setLayoutParams(new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        ));

        //inflate the actual activity layout into fullLayout
        View activityView = getLayoutInflater().inflate(layoutResID, fullLayout, false);
        fullLayout.addView(activityView);

        //add FAB programmatically
        FloatingActionButton fab = new FloatingActionButton(this);
        fab.setId(View.generateViewId());
        fab.setImageResource(R.drawable.ic_language);
        fab.setBackgroundTintList(ColorStateList.valueOf(0xFF2F2F32));
        fab.setImageTintList(ColorStateList.valueOf(0xFFFFFFFF));
        fab.setElevation(8f);

        //layout params for bottom-right corner
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                dpToPx(56), dpToPx(56)
        );
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        params.setMargins(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        fab.setLayoutParams(params);

        fab.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        fullLayout.addView(fab);

        super.setContentView(fullLayout);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
