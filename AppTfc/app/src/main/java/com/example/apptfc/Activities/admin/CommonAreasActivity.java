package com.example.apptfc.Activities.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.models.CommonArea;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.Activities.general.AccountInfoActivity;
import com.example.apptfc.adapters.CommonAreaAdapter;
import com.example.apptfc.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommonAreasActivity extends AppCompatActivity implements CommonAreaAdapter.OnAreaSelectedListener {
    private RecyclerView rvCommonAreas;
    private CommonAreaAdapter adapter;
    private List<CommonArea> availableCommonAreas = new ArrayList<>();
    private ProgressDialog progressDialog;
    private FloatingActionButton fabAddArea;
    private BottomNavigationView bottomNav;
    private CommonArea selectedArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_areas);

        setupViews();
        setupRecyclerView();
        loadCommonAreas();
        setupBottomNavigation();

        fabAddArea.setOnClickListener(v -> {
            startActivity(new Intent(this, AddCommonAreaActivity.class));
        });
    }

    private void setupViews() {
        rvCommonAreas = findViewById(R.id.recyclerCommonAreas);
        fabAddArea = findViewById(R.id.fabAddCommonArea);
        bottomNav = findViewById(R.id.bottomNavigationView);
    }

    private void setupRecyclerView() {
        rvCommonAreas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommonAreaAdapter(availableCommonAreas, this, true);
        rvCommonAreas.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_commonAreas);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, NeighborhoodDetailActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            } else if (item.getItemId() == R.id.nav_records) {
                startActivity(new Intent(this, VotesRecordsActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(this, AccountInfoActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (item.getItemId() == R.id.nav_commonAreas) {
                return true;
            }
            return false;
        });
    }

    private void loadCommonAreas() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        int neighborhoodId = prefs.getInt("neighborhoodId", -1);

        if (neighborhoodId == -1) {
            Toast.makeText(this, "Error al identificar la comunidad", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        apiService.getCommonAreaByNeighborhood(neighborhoodId).enqueue(new Callback<List<CommonArea>>() {
            @Override
            public void onResponse(Call<List<CommonArea>> call, Response<List<CommonArea>> response) {
                if (response.isSuccessful()) {
                    List<CommonArea> data = response.body();
                    availableCommonAreas.clear();
                    if (data != null) {
                        availableCommonAreas.addAll(data);
                    }
                    adapter.updateData(availableCommonAreas);
                    findViewById(R.id.tvNoAreas).setVisibility(availableCommonAreas.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(CommonAreasActivity.this, "Error al cargar áreas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CommonArea>> call, Throwable t) {
                Toast.makeText(CommonAreasActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAreaSelected(CommonArea area) {
        selectedArea = area;
    }

    @Override
    public void onDeleteArea(CommonArea area) {
        showDeleteConfirmationDialog(area);
    }

    private void showDeleteConfirmationDialog(CommonArea area) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar área común")
                .setMessage("¿Estás seguro de que quieres eliminar " + area.getName() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteCommonArea(area))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteCommonArea(CommonArea area) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Eliminando área común...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        apiService.deleteCommonArea(area.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(CommonAreasActivity.this, "Área eliminada", Toast.LENGTH_SHORT).show();
                    loadCommonAreas(); // Recargar la lista
                } else {
                    Toast.makeText(CommonAreasActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CommonAreasActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCommonAreas();
        bottomNav.setSelectedItemId(R.id.nav_commonAreas);
    }
}