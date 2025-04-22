package com.example.apptfc.Activities.Neighbor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.Admin;
import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.Neighbor;
import com.example.apptfc.API.Neighborhood;
import com.example.apptfc.API.Record;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.API.Vote;
import com.example.apptfc.Activities.ProfileActivity;
import com.example.apptfc.R;
import com.example.apptfc.adapters.RecordAdapter;
import com.example.apptfc.adapters.VoteAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainNeighborActivity extends AppCompatActivity {
    private RecyclerView recordRecyclerView;
    private RecyclerView voteRecyclerView;
    private Button btnCallAdmin;
    private ImageView neighborhoodLogo;
    private TextView communityName;
    private ApiService apiService;
    private List<Vote> votes;
    private VoteAdapter voteAdapter;
    private RecyclerView recyclerViewActas;
    private RecordAdapter recordAdapter;
    private List<Record> records;
    private BottomNavigationView bottomNav;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_neighbor);

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        btnCallAdmin = findViewById(R.id.btnCallAdmin);
        voteRecyclerView = findViewById(R.id.recyclerViewVotaciones);
        recordRecyclerView = findViewById(R.id.recyclerViewActas);
        communityName = findViewById(R.id.communityName);
        neighborhoodLogo = findViewById(R.id.neighborhoodLogo);
        bottomNav = findViewById(R.id.bottomNavigationView);

        bottomNav.setSelectedItemId(R.id.nav_home);

        loadData(prefs);

        votes = new ArrayList<>();

        voteRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        voteAdapter = new VoteAdapter(votes, this);
        voteRecyclerView.setAdapter(voteAdapter);

        voteRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        btnCallAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+34" + prefs.getString("adminTlf", "90000000")));
            startActivity(intent);
        });

        recyclerViewActas = findViewById(R.id.recyclerViewActas);
        recyclerViewActas.setLayoutManager(new LinearLayoutManager(this));

        records = new ArrayList<>();
        recordAdapter = new RecordAdapter(records, this);
        recyclerViewActas.setAdapter(recordAdapter);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_home){
                    startActivity(new Intent(MainNeighborActivity.this, MainNeighborActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_announcements){
                    startActivity(new Intent(MainNeighborActivity.this, AnnouncementsActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                } else if (item.getItemId() == R.id.nav_settings){
                    startActivity(new Intent(MainNeighborActivity.this, ProfileActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                }

                return false;
            }
        });
    }

    private void loadData(SharedPreferences prefs){
        getNeighborhoodId(prefs);
        getNeighborId(prefs);
    }

    private void getNeighborhoodId(SharedPreferences prefs){
        SharedPreferences.Editor editor = prefs.edit();
        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        Call<Integer> call = apiService.getNeighborhoodId(prefs.getInt("id", -1));

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int neighborhoodId = response.body();
                    editor.putInt("neighborhoodId", neighborhoodId);
                    editor.apply();
                    getNeighborhoodData(neighborhoodId);
                    getAdminInfo(neighborhoodId, prefs);
                    getRecords(neighborhoodId);
                    Log.d("Votes", "Solicitando votaciones para el usuario ID: " + prefs.getInt("id", -1));
                    getVotes(prefs.getInt("id", -1));
                    Log.d("NeighborData", "El ID del vecindario es: " + neighborhoodId);
                } else {
                    Log.e("NeighborData", "Error al obtener el vecindario");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e("NeighborData", "Error de conexión: " + t.getMessage());
            }
        });
    }

    private void getNeighborhoodData(int id) {
        apiService = RetrofitClient.get().create(ApiService.class);

        Call<Neighborhood> call = apiService.getNeighborhoodById(id);
        call.enqueue(new Callback<Neighborhood>() {
            @Override
            public void onResponse(Call<Neighborhood> call, Response<Neighborhood> response) {
                if (response.isSuccessful() && response.body() != null) {
                    communityName.setText(communityName.getText() + response.body().getName());
                    Log.d("hoodData", response.body().getImage().toString());
                    formatImage(response.body().getImage());
                    Log.d("MainNeighborActivity", "Vecindario encontrado");
                } else {
                    Log.e("RegisterActivity", "Error en la respuesta: " + response.code());
                    Toast.makeText(MainNeighborActivity.this, "Error al cargar las comunidades", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Neighborhood> call, Throwable t) {
                Log.e("MainNeighborActivity", "Error de conexión: " + t.getMessage(), t);
                Toast.makeText(MainNeighborActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void getVotes(int id) {
        apiService = RetrofitClient.get().create(ApiService.class);
        Call<List<Vote>> call = apiService.getVotesByUser(id);
        Log.d("Votes", "URL de la petición: " + call.request().url());
        call.enqueue(new Callback<List<Vote>>() {
            @Override
            public void onResponse(Call<List<Vote>> call, Response<List<Vote>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    votes.clear();
                    votes.addAll(response.body());
                    voteAdapter.notifyDataSetChanged();
                } else {
                    Log.e("Votes", "Error al obtener votaciones: " + response.code() + " - " + response.message());
                    try {
                        Log.e("Votes", "Cuerpo de error: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e("Votes", "Error al leer el cuerpo de error", e);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Vote>> call, Throwable t) {
                Log.e("Votes", "Error de conexión: " + t.getMessage());
            }
        });
    }

    private void getNeighborId(SharedPreferences prefs){
        apiService = RetrofitClient.get().create(ApiService.class);

        apiService.getNeighborByUserId(prefs.getInt("id", -1)).enqueue(new Callback<Neighbor>() {
            @Override
            public void onResponse(Call<Neighbor> call, Response<Neighbor> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    Log.d("getNeighborId", "responseId: " + response.body().getId());
                    editor.putInt("neighborId", response.body().getId());
                    editor.commit();
                } else{
                    Log.d("getNeighborId", "NOT OK: " + response.isSuccessful());
                }
            }
            @Override
            public void onFailure(Call<Neighbor> call, Throwable t) {
                Log.e("API_ERROR", "Error: " + t.getMessage());
            }
        });
    }

    private void getRecords(int neighborhoodId) {
        apiService = RetrofitClient.get().create(ApiService.class);
        Call<List<Record>> call = apiService.getRecords(neighborhoodId);

        call.enqueue(new Callback<List<Record>>() {
            @Override
            public void onResponse(Call<List<Record>> call, Response<List<Record>> response) {
                if (response.isSuccessful()) {
                    try {
                        List<Record> recordsResponse = response.body();
                        if (recordsResponse != null) {
                            records.clear();
                            records.addAll(recordsResponse);
                            recordAdapter.notifyDataSetChanged();
                            Log.d("API_RESPONSE", "Records actualizados: " + records.size());
                        }
                    } catch (Exception e) {
                        Log.e("API_ERROR", "Error al procesar respuesta", e);
                    }
                } else {
                    try {
                        Log.e("API_ERROR", "Error: " + response.code() + " - " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("API_ERROR", "Error al leer errorBody", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Record>> call, Throwable t) {
                Log.e("API_ERROR", "Error de conexión: " + t.getMessage());
            }
        });
    }

    private void getAdminInfo(int neighborhoodId, SharedPreferences prefs) {
        apiService = RetrofitClient.get().create(ApiService.class);
        Call<Admin> call = apiService.getAdminByNeighborhoodId(neighborhoodId);

        call.enqueue(new Callback<Admin>() {
            @Override
            public void onResponse(Call<Admin> call, Response<Admin> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("adminTlf", response.body().getUser().getTlphNumber());
                    editor.commit();
                } else {
                    Log.e("adminInfo", "Error al obtener el admin: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Admin> call, Throwable t) {
                Log.e("adminInfo", "Error de conexión: " + t.getMessage());
            }
        });
    }

    private void formatImage(String image) {
        Log.d("Base64", "Imagen Base64 (primeros 50): " + image.substring(0, 50));
        if (image != null && !image.isEmpty()) {
            Log.d("ImageDebug", "Entrando a formatImage");
            try {
                byte[] imageBytes = android.util.Base64.decode(image, android.util.Base64.DEFAULT);
                Bitmap decodedImage = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                Log.d("ImageDecode", "Imagen decodificada correctamente.");
                neighborhoodLogo.setImageBitmap(decodedImage);

            } catch (IllegalArgumentException e) {
                Log.e("ImageDecode", "Error al decodificar la imagen Base64", e);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}