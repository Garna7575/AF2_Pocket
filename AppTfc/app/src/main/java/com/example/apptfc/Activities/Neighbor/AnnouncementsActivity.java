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
import com.example.apptfc.API.models.Incidence;
import com.example.apptfc.API.RetrofitClient;
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
    private SharedPreferences prefs;
    private RecyclerView recyclerView;
    private IncidenceAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddIncidence;
    private ApiService apiService;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        apiService = RetrofitClient.get().create(ApiService.class);

        setupViews();
        setupAdapters();
        setupListeners();
        loadIncidences();
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadIncidences();
        });

        fabAddIncidence.setOnClickListener(view -> {
            startActivity(new Intent(AnnouncementsActivity.this, PostIncidenceActivity.class));
        });

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_home){
                    startActivity(new Intent(AnnouncementsActivity.this, MainNeighborActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                } else if (item.getItemId() == R.id.nav_announcements){
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

    private void setupAdapters() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IncidenceAdapter(new ArrayList<>(), new IncidenceAdapter.OnIncidenceClickListener() {
            @Override
            public void onIncidenceClick(Incidence incidence) {
            }

            @Override
            public void onResolveClick(Incidence incidence) {
            }
        }, false);

        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void setupViews() {
        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_announcements);
        recyclerView = findViewById(R.id.recyclerViewIncidencias);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fabAddIncidence = findViewById(R.id.fabAddIncidencia);
    }

    private void loadIncidences() {
        swipeRefreshLayout.setRefreshing(true);
        Call<List<Incidence>> call = apiService.getIncidencesByNeighborhoodId(prefs.getInt("neighborhoodId", -1));
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
        loadIncidences();
    }
}