package com.example.apptfc.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.apptfc.Activities.Neighbor.AnnouncementsActivity;
import com.example.apptfc.Activities.Neighbor.MainNeighborActivity;
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
            startActivity(new Intent(ProfileActivity.this, AccountInfoActivity.class));
        });
//
//        optionReceipts.setOnClickListener(v -> {
//            startActivity(new Intent(ProfileActivity.this, ReceiptsActivity.class));
//        });

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_home){
                    startActivity(new Intent(ProfileActivity.this, MainNeighborActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                } else if (item.getItemId() == R.id.nav_announcements){
                    startActivity(new Intent(ProfileActivity.this, AnnouncementsActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                } else if (item.getItemId() == R.id.nav_settings){
                    startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                    return true;
                }

                return false;
            }
        });
    }
}