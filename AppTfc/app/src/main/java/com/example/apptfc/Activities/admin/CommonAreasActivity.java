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
import com.example.apptfc.API.CommonArea;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.adapters.CommonAreaAdapter;
import com.example.apptfc.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_areas);

        recyclerView = findViewById(R.id.recyclerCommonAreas);
        fabAddArea = findViewById(R.id.fabAddCommonArea);

        setupRecyclerView();
        loadCommonAreas();

        fabAddArea.setOnClickListener(v -> {
            startActivity(new Intent(this, AddCommonAreaActivity.class));
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommonAreaAdapter(commonAreas, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadCommonAreas() {

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        int neighborhoodId = prefs.getInt("neighborhoodId", -1);

        if (neighborhoodId == -1) {
            hideLoading();
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
                hideLoading();
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

    private void showLoading(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
