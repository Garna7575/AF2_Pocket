package com.example.apptfc.Activities.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.apptfc.API.Admin;
import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.Neighborhood;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.Activities.BookingsActivity;
import com.example.apptfc.Activities.Neighbor.AnnouncementsActivity;
import com.example.apptfc.Activities.Neighbor.MainNeighborActivity;
import com.example.apptfc.Activities.ProfileActivity;
import com.example.apptfc.R;
import com.example.apptfc.adapters.AdminNeighborhoodAdapter;
import com.example.apptfc.adapters.NeighborhoodAdapter;
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

        initializeViews();
        loadAdminDataAndNeighborhoods();
    }

    private void initializeViews() {
        listView = findViewById(R.id.neighborhoodsListView);
        adapter = new AdminNeighborhoodAdapter(this, neighborhoodList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Neighborhood selectedNeighborhood = neighborhoodList.get(position);
            openNeighborhoodDetail(selectedNeighborhood);
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_home){
                    startActivity(new Intent(MainAdminActivity.this, MainAdminActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_settings){
                    startActivity(new Intent(MainAdminActivity.this, ProfileActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                }

                return false;
            }
        });
    }

    private void openNeighborhoodDetail(Neighborhood neighborhood) {
        Intent intent = new Intent(this, ActivityNeighborhoodDetail.class);
        intent.putExtra("NEIGHBORHOOD", neighborhood);
        startActivity(intent);
    }

    private void loadAdminDataAndNeighborhoods() {
        showLoading("Cargando datos del administrador...");

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        int userId = prefs.getInt("id", -1);

        Log.d("MainAdminActivity", "Llamando getAdminByUserId con userId=" + userId);
        getAdminByUserId(userId, new AdminCallback() {
            @Override
            public void onSuccess(int adminId) {
                // Guardar el adminId en SharedPreferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("adminId", adminId);
                editor.apply();

                loadAdminNeighborhoods();
            }

            @Override
            public void onFailure(String errorMessage) {
                hideLoading();
                showError(errorMessage);
                finish();
            }
        });
    }

    private void getAdminByUserId(int userId, AdminCallback callback) {
        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        Call<Admin> call = apiService.getAdminByUserId(userId);

        call.enqueue(new Callback<Admin>() {
            @Override
            public void onResponse(Call<Admin> call, Response<Admin> response) {
                Log.d("getAdminByUserId", "Código de respuesta: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Admin admin = response.body();
                    Log.d("getAdminByUserId", "Admin recibido: " + admin.getId());
                    callback.onSuccess(admin.getId());
                } else {
                    Log.e("getAdminByUserId", "Respuesta no exitosa o admin null");
                    callback.onFailure("No se pudo obtener el administrador");
                }
            }

            @Override
            public void onFailure(Call<Admin> call, Throwable t) {
                Log.e("getAdminByUserId", "Error en la llamada: " + t.getMessage());
                callback.onFailure("Error de conexión: " + t.getMessage());
            }

        });
    }

    private void loadAdminNeighborhoods() {
        showLoading("Cargando comunidades...");

        int adminId = getSharedPreferences("UserData", MODE_PRIVATE).getInt("adminId", -1);
        if (adminId == -1) {
            hideLoading();
            showError("Administrador no identificado");
            return;
        }

        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        apiService.getNeighborhoodByAdminId(adminId).enqueue(new Callback<List<Neighborhood>>() {
            @Override
            public void onResponse(Call<List<Neighborhood>> call, Response<List<Neighborhood>> response) {
                hideLoading();

                if (response.isSuccessful() && response.body() != null) {
                    neighborhoodList.clear();
                    neighborhoodList.addAll(response.body());


                    adapter.notifyDataSetChanged();
                } else {
                    showError("Error al cargar comunidades");
                    Log.e("API Error", "Código: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Neighborhood>> call, Throwable t) {
                hideLoading();
                showError("Error de conexión: " + t.getMessage());
                Log.e("API Error", "Error: " + t.getMessage());
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

    public interface AdminCallback {
        void onSuccess(int adminId);
        void onFailure(String errorMessage);
    }
}