package com.example.apptfc.Activities.admin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.Neighbor;
import com.example.apptfc.API.Neighborhood;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.R;
import com.example.apptfc.adapters.NeighborAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityNeighborhoodDetail extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private ApiService apiService;
    private Neighborhood neighborhood;
    private NeighborAdapter neighborsAdapter;
    private TextView tvNeighborhoodName, tvTotalNeighbors;
    private ListView lvNeighbors;
    private TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neighborhood_detail);

        neighborhood = getIntent().getParcelableExtra("NEIGHBORHOOD");
        if (neighborhood == null) {
            Toast.makeText(this, "Error: No se encontraron datos de la comunidad", Toast.LENGTH_SHORT).show();
            return;
        }

        initializeViews();
        setupTabs();
        loadNeighbors();
    }

    private void initializeViews() {
        tvNeighborhoodName = findViewById(R.id.tvNeighborhoodName);
        lvNeighbors = findViewById(R.id.lvNeighbors);
        tvTotalNeighbors = findViewById(R.id.tvTotalNeighbors);
        tabHost = findViewById(R.id.tabHost);

        neighborhood = getIntent().getParcelableExtra("NEIGHBORHOOD");
        if (neighborhood == null) {
            Toast.makeText(this, "Error: Datos no encontrados", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            tvNeighborhoodName.setText(neighborhood.getName());
        }
    }

    private void setupTabs() {
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec("Neighbors");
        spec.setContent(R.id.tabNeighbors);
        spec.setIndicator("Vecinos");
        tabHost.addTab(spec);

    }

    private void loadNeighbors() {
        showLoadingDialog("Cargando vecinos...");

        apiService = RetrofitClient.get().create(ApiService.class);
        Call<List<Neighbor>> call = apiService.getNeighborsByNeighborhood(neighborhood.getId());
        Log.d("LoadNeighbors", "neighborhoodId: " + neighborhood.getId());

        call.enqueue(new Callback<List<Neighbor>>() {
            @Override
            public void onResponse(Call<List<Neighbor>> call, Response<List<Neighbor>> response) {
                Log.d("API Response", "Código: " + response.code() + " - Body: " + response.body());
                dismissLoadingDialog();

                if (response.isSuccessful() && response.body() != null) {
                    List<Neighbor> neighbors = response.body();
                    updateNeighborsList(neighbors);
                } else {
                    showError("Error al cargar vecinos");
                }
            }

            @Override
            public void onFailure(Call<List<Neighbor>> call, Throwable t) {
                dismissLoadingDialog();
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void updateNeighborsList(List<Neighbor> neighbors) {
        NeighborAdapter adapter = new NeighborAdapter(this, neighbors);
        lvNeighbors.setAdapter(adapter);

        Log.d("updateNeighborsList", String.valueOf(neighbors.size()));
        tvTotalNeighbors.setText("Total de vecinos: " + neighbors.size());
    }

    private void showLoadingDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}