package com.example.apptfc.Activities.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.Admin;
import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.Neighborhood;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.Activities.AccountInfoActivity;
import com.example.apptfc.R;
import com.example.apptfc.adapters.AdminNeighborhoodAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainAdminActivity extends AppCompatActivity {

    private ListView listView;
    private AdminNeighborhoodAdapter adapter;
    private List<Neighborhood> neighborhoodList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);
        listView = findViewById(R.id.neighborhoodsListView);
        adapter = new AdminNeighborhoodAdapter(this, neighborhoodList);
        listView.setAdapter(adapter);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) return true;
            if (item.getItemId() == R.id.nav_settings) {
                startActivityForResult(new Intent(this, AccountInfoActivity.class), 100);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }
            return false;
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < neighborhoodList.size()) {
                Intent intent = new Intent(this, ActivityNeighborhoodDetail.class);
                intent.putExtra("NEIGHBORHOOD", neighborhoodList.get(position));
                startActivity(intent);
            } else {
                startActivityForResult(new Intent(this, AddNeighborhoodActivity.class), 101);
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override public void onScrollStateChanged(AbsListView view, int scrollState) { }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                updateFabVisibility();
            }
        });

        findViewById(R.id.fabAddCommunity).setOnClickListener(v -> {
            startActivityForResult(new Intent(this, AddNeighborhoodActivity.class), 101);
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}
            @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                updateFabVisibility();
            }
        });
        loadAdminDataAndNeighborhoods();
    }

    private void updateFabVisibility() {
        listView.post(() -> {
            boolean canScroll = listView.canScrollVertically(1);
            View fab = findViewById(R.id.fabAddCommunity);

            fab.setVisibility(canScroll ? View.VISIBLE : View.GONE);
            adapter.setShowAddItem(!canScroll);
        });
    }


    private void loadAdminDataAndNeighborhoods() {
        showLoading("Cargando datos...");
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        int userId = prefs.getInt("id", -1);
        RetrofitClient.get().create(ApiService.class).getAdminByUserId(userId).enqueue(new Callback<Admin>() {
            @Override public void onResponse(Call<Admin> call, Response<Admin> response) {
                if (response.isSuccessful() && response.body() != null) {
                    prefs.edit().putInt("adminId", response.body().getId()).apply();
                    loadAdminNeighborhoods();
                } else {
                    hideLoading();
                    showError("Error al obtener administrador");
                }
            }
            @Override public void onFailure(Call<Admin> call, Throwable t) {
                hideLoading();
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void loadAdminNeighborhoods() {
        int adminId = getSharedPreferences("UserData", MODE_PRIVATE).getInt("adminId", -1);
        if (adminId == -1) {
            hideLoading();
            showError("Admin no identificado");
            return;
        }
        RetrofitClient.get().create(ApiService.class).getNeighborhoodByAdminId(adminId).enqueue(new Callback<List<Neighborhood>>() {
            @Override public void onResponse(Call<List<Neighborhood>> call, Response<List<Neighborhood>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    neighborhoodList.clear();
                    neighborhoodList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateFabVisibility();
                } else {
                    showError("Error al cargar comunidades");
                }
            }
            @Override public void onFailure(Call<List<Neighborhood>> call, Throwable t) {
                hideLoading();
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void showLoading(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            loadAdminNeighborhoods();
        }
    }
}
