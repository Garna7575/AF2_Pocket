package com.example.apptfc.Activities.Neighbor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.Incidence;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.Activities.BookingsActivity;
import com.example.apptfc.Activities.ProfileActivity;
import com.example.apptfc.Activities.incidences.PostIncidenceActivity;
import com.example.apptfc.R;
import com.example.apptfc.adapters.IncidenceAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IncidenceAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddIncidencia;
    private ApiService incidenceService;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        bottomNav = findViewById(R.id.bottomNavigationView);

        bottomNav.setSelectedItemId(R.id.nav_announcements);

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        incidenceService = RetrofitClient.get().create(ApiService.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        recyclerView = findViewById(R.id.recyclerViewIncidencias);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IncidenceAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadIncidences();
        });

        fabAddIncidencia = findViewById(R.id.fabAddIncidencia);
        fabAddIncidencia.setOnClickListener(view -> {
            long lastIncidenceTime = prefs.getLong("LAST_INCIDENCE_TIME_KEY", 0);
            long currentTime = System.currentTimeMillis();
            long cooldownPeriod = 15 * 60 * 1000;

            if (lastIncidenceTime == 0) {
                startActivity(new Intent(AnnouncementsActivity.this, PostIncidenceActivity.class));
            } else if (currentTime - lastIncidenceTime < cooldownPeriod) {
                long remainingMinutes = (cooldownPeriod - (currentTime - lastIncidenceTime)) / 60000;
                long remainingSeconds = ((cooldownPeriod - (currentTime - lastIncidenceTime)) % 60000) / 1000;

                Toast.makeText(AnnouncementsActivity.this,
                        String.format("Espere %d min %d seg antes de añadir otra incidencia", remainingMinutes, remainingSeconds),
                        Toast.LENGTH_LONG).show();
            } else {
                startActivity(new Intent(AnnouncementsActivity.this, PostIncidenceActivity.class));
            }
        });

        loadIncidences();
        updateFabState();

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_home){
                    startActivity(new Intent(AnnouncementsActivity.this, MainNeighborActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                } else if (item.getItemId() == R.id.nav_announcements){
                    startActivity(new Intent(AnnouncementsActivity.this, AnnouncementsActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_settings){
                    startActivity(new Intent(AnnouncementsActivity.this, ProfileActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                } else if (item.getItemId() == R.id.nav_bookings) {
                    startActivity(new Intent(AnnouncementsActivity.this, BookingsActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }

                return false;
            }
        });
    }

    private void loadIncidences() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        swipeRefreshLayout.setRefreshing(true);
        Log.d("incidences", String.valueOf(prefs.getInt("neighborhoodId", -1)));
        Call<List<Incidence>> call = incidenceService.getIncidencesByNeighborhoodId(prefs.getInt("neighborhoodId", -1));
        call.enqueue(new Callback<List<Incidence>>() {
            @Override
            public void onResponse(Call<List<Incidence>> call, Response<List<Incidence>> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Incidence> incidences = response.body();
                    if (!incidences.isEmpty()) {
                        adapter.updateData(incidences);
                    } else {
                        Toast.makeText(AnnouncementsActivity.this, "No hay incidencias para mostrar", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AnnouncementsActivity.this, "Error al cargar incidencias", Toast.LENGTH_SHORT).show();
                    Log.e("API Error", "Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Incidence>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(AnnouncementsActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Failure", t.getMessage(), t);
            }
        });
    }

    private void updateFabState() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        long lastIncidenceTime = prefs.getLong("LAST_INCIDENCE_TIME_KEY", 0);
        long currentTime = System.currentTimeMillis();
        long cooldownPeriod = 15 * 60 * 1000;

        if (lastIncidenceTime != 0 && currentTime - lastIncidenceTime < cooldownPeriod) {
            fabAddIncidencia.setAlpha(0.5f);
            fabAddIncidencia.setEnabled(false);
        } else {
            fabAddIncidencia.setAlpha(1.0f);
            fabAddIncidencia.setEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFabState();
        loadIncidences();
    }
}