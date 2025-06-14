package com.example.apptfc.Activities.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.models.CommonArea;
import com.example.apptfc.API.RetrofitClient;
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
    private RecyclerView recyclerView;
    private CommonAreaAdapter adapter;
    private List<CommonArea> commonAreas = new ArrayList<>();
    private ProgressDialog progressDialog;
    private FloatingActionButton fabAddArea;
    private BottomNavigationView bottomNav;

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
        recyclerView = findViewById(R.id.recyclerCommonAreas);
        fabAddArea = findViewById(R.id.fabAddCommonArea);
        bottomNav = findViewById(R.id.bottomNavigationView);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommonAreaAdapter(commonAreas, this);
        recyclerView.setAdapter(adapter);
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
                return true;
            } else if (item.getItemId() == R.id.nav_commonAreas) {
                startActivity(new Intent(this, CommonAreasActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
                    Log.d("API_RESPONSE", "Tamaño: " + (data != null ? data.size() : "null"));

                    commonAreas.clear();
                    if (data != null) {
                        commonAreas.addAll(data);
                    }

                    adapter.updateData(commonAreas);
                    findViewById(R.id.tvNoAreas).setVisibility(commonAreas.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Log.e("getCommonAreaByNeighborhood", "Error: " + response.code());
                    Toast.makeText(CommonAreasActivity.this, "Error al cargar áreas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CommonArea>> call, Throwable t) {
                Log.e("API_FAILURE", "Excepción: ", t);
                Toast.makeText(CommonAreasActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onAreaSelected(CommonArea area) {
        Toast.makeText(this, "Seleccionado: " + area.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCommonAreas();
    }
}
