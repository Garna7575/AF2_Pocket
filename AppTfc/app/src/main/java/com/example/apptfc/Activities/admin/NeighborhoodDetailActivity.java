package com.example.apptfc.Activities.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.models.Incidence;
import com.example.apptfc.API.models.Neighbor;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.Activities.general.AccountInfoActivity;
import com.example.apptfc.R;
import com.example.apptfc.adapters.IncidenceAdapter;
import com.example.apptfc.adapters.NeighborAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NeighborhoodDetailActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    private ApiService apiService;
    private NeighborAdapter neighborsAdapter;
    private IncidenceAdapter incidenceAdapter;
    private TextView tvNeighborhoodName, tvTotalNeighbors, tvTotalIncidences;
    private RecyclerView rvNeighbors, rvIncidences;
    private TabHost tabHost;
    private SearchView searchView;
    private List<Neighbor> allNeighbors = new ArrayList<>();
    private List<Incidence> allIncidences = new ArrayList<>();
    private BottomNavigationView bottomNavigationView;
    private int neighborhoodId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neighborhood_detail);

        initializeViews();
        setupTabs();
        setupBottomNavigation();
        loadNeighbors();
        loadIncidences();
        setupSearch();
    }

    private void initializeViews() {
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        tvNeighborhoodName = findViewById(R.id.tvNeighborhoodName);
        tvTotalNeighbors = findViewById(R.id.tvTotalNeighbors);
        tvTotalIncidences = findViewById(R.id.tvTotalIncidences);

        rvNeighbors = findViewById(R.id.rvNeighbors);
        rvNeighbors.setLayoutManager(new LinearLayoutManager(this));
        neighborsAdapter = new NeighborAdapter(new ArrayList<>(), this);
        rvNeighbors.setAdapter(neighborsAdapter);

        rvIncidences = findViewById(R.id.rvIncidences);
        rvIncidences.setLayoutManager(new LinearLayoutManager(this));
        incidenceAdapter = new IncidenceAdapter(new ArrayList<>());
        rvIncidences.setAdapter(incidenceAdapter);

        tabHost = findViewById(R.id.tabHost);
        searchView = findViewById(R.id.searchView);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }
    private void setupTabs() {
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec("Neighbors");
        spec.setContent(R.id.tabNeighbors);
        spec.setIndicator("Vecinos");
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Incidences");
        spec.setContent(R.id.tabIncidences);
        spec.setIndicator("Incidencias");
        tabHost.addTab(spec);

        searchView.setQueryHint("Buscar vecinos...");

        tabHost.setOnTabChangedListener(tabId -> {
            if (tabId.equals("Neighbors")) {
                searchView.setQueryHint("Buscar vecinos...");
                filterNeighbors(searchView.getQuery().toString());
            } else if (tabId.equals("Incidences")) {
                searchView.setQueryHint("Buscar incidencias...");
                filterIncidences(searchView.getQuery().toString());
            }
        });
    }

    private void setupBottomNavigation(){
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) return true;
            if (item.getItemId() == R.id.nav_settings) {
                startActivityForResult(new Intent(this, AccountInfoActivity.class), 100);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (item.getItemId() == R.id.nav_records) {
                startActivity(new Intent(this, VotesRecordsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (item.getItemId() == R.id.nav_commonAreas){
                startActivity(new Intent(this, CommonAreasActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }
            return false;
        });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (tabHost.getCurrentTabTag().equals("Neighbors")) {
                    filterNeighbors(newText);
                } else if (tabHost.getCurrentTabTag().equals("Incidences")) {
                    filterIncidences(newText);
                }
                return true;
            }
        });
    }

    private void filterNeighbors(String text) {
        List<Neighbor> filteredList = new ArrayList<>();
        for (Neighbor neighbor : allNeighbors) {
            if (neighbor.getUser() != null && (
                    neighbor.getUser().getName().toLowerCase().contains(text.toLowerCase()) ||
                            neighbor.getUser().getSurname().toLowerCase().contains(text.toLowerCase()))) {
                filteredList.add(neighbor);
            }
        }
        neighborsAdapter.updateData(filteredList);
    }

    private void filterIncidences(String text) {
        List<Incidence> filteredList = new ArrayList<>();
        for (Incidence incidence : allIncidences) {
            if ((incidence.getTitle() != null && incidence.getTitle().toLowerCase().contains(text.toLowerCase()))) {
                filteredList.add(incidence);
            }
        }
        incidenceAdapter.updateData(filteredList);
    }

    private void loadNeighbors() {
        apiService = RetrofitClient.get().create(ApiService.class);
        Call<List<Neighbor>> call = apiService.getNeighborsByNeighborhood(neighborhoodId);
        Log.e("a", String.valueOf(neighborhoodId));
        call.enqueue(new Callback<List<Neighbor>>() {
            @Override
            public void onResponse(Call<List<Neighbor>> call, Response<List<Neighbor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allNeighbors = response.body();
                    neighborsAdapter.updateData(allNeighbors);
                    tvTotalNeighbors.setText("Total: " + allNeighbors.size());
                } else {
                    showError("Error al cargar vecinos");
                    Log.e("API_ERROR", "Error response: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Neighbor>> call, Throwable t) {
                showError("Error de conexión: " + t.getMessage());
                Log.e("API_FAILURE", "Error en la llamada API", t);
            }
        });
    }

    private void loadIncidences() {
        apiService = RetrofitClient.get().create(ApiService.class);
        Call<List<Incidence>> call = apiService.getIncidencesByNeighborhoodId(neighborhoodId);

        call.enqueue(new Callback<List<Incidence>>() {
            @Override
            public void onResponse(Call<List<Incidence>> call, Response<List<Incidence>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allIncidences = response.body();
                    incidenceAdapter.updateData(allIncidences);
                    tvTotalIncidences.setText("Total: " + allIncidences.size());
                } else {
                    showError("Error al cargar incidencias");
                }
            }

            @Override
            public void onFailure(Call<List<Incidence>> call, Throwable t) {
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            loadNeighbors();
            loadIncidences();
        }
    }
}