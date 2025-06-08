package com.example.apptfc.Activities.admin;

import android.app.ProgressDialog;
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
import com.example.apptfc.API.Incidence;
import com.example.apptfc.API.Neighbor;
import com.example.apptfc.API.Neighborhood;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.R;
import com.example.apptfc.adapters.IncidenceAdapter;
import com.example.apptfc.adapters.NeighborAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityNeighborhoodDetail extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private ApiService apiService;
    private Neighborhood neighborhood;

    private NeighborAdapter neighborsAdapter;
    private IncidenceAdapter incidenceAdapter;

    private TextView tvNeighborhoodName, tvTotalNeighbors, tvTotalIncidences;
    private RecyclerView rvNeighbors, rvIncidences;
    private TabHost tabHost;
    private SearchView searchView;

    private List<Neighbor> allNeighbors = new ArrayList<>();
    private List<Incidence> allIncidences = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neighborhood_detail);

        neighborhood = getIntent().getParcelableExtra("NEIGHBORHOOD");
        if (neighborhood == null) {
            Toast.makeText(this, "Error: No se encontraron datos de la comunidad", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupTabs();
        loadNeighbors();
        loadIncidences();
        setupSearch();
    }

    private void initializeViews() {
        tvNeighborhoodName = findViewById(R.id.tvNeighborhoodName);
        tvTotalNeighbors = findViewById(R.id.tvTotalNeighbors);
        tvTotalIncidences = findViewById(R.id.tvTotalIncidences);

        rvNeighbors = findViewById(R.id.rvNeighbors);
        rvNeighbors.setLayoutManager(new LinearLayoutManager(this));
        neighborsAdapter = new NeighborAdapter(new ArrayList<>());
        rvNeighbors.setAdapter(neighborsAdapter);

        rvIncidences = findViewById(R.id.rvIncidences);
        rvIncidences.setLayoutManager(new LinearLayoutManager(this));
        incidenceAdapter = new IncidenceAdapter(new ArrayList<>());
        rvIncidences.setAdapter(incidenceAdapter);

        tabHost = findViewById(R.id.tabHost);
        searchView = findViewById(R.id.searchView);

        tvNeighborhoodName.setText(neighborhood.getName());
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
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNeighbors(newText);
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

    private void loadNeighbors() {

        apiService = RetrofitClient.get().create(ApiService.class);
        Call<List<Neighbor>> call = apiService.getNeighborsByNeighborhood(neighborhood.getId());

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
        Call<List<Incidence>> call = apiService.getIncidencesByNeighborhoodId(neighborhood.getId());

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
}