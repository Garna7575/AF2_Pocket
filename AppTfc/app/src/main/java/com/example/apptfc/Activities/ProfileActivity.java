package com.example.apptfc.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        LinearLayout optionPersonalData = findViewById(R.id.option_personal_data);
        LinearLayout optionReceipts = findViewById(R.id.option_receipts);

        bottomNav = findViewById(R.id.bottomNavigationView);

        bottomNav.setSelectedItemId(R.id.nav_settings);


        optionPersonalData.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, PersonalDataActivity.class));
        });
//
//        optionReceipts.setOnClickListener(v -> {
//            startActivity(new Intent(ProfileActivity.this, ReceiptsActivity.class));
//        });
    }
}