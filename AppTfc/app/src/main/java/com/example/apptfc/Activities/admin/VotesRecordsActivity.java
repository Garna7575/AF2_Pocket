package com.example.apptfc.Activities.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.Record;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.API.Vote;
import com.example.apptfc.Activities.ProfileActivity;
import com.example.apptfc.R;
import com.example.apptfc.adapters.RecordAdapter;
import com.example.apptfc.adapters.VoteAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VotesRecordsActivity extends AppCompatActivity {

    private static final String TAG = "VotesRecordsActivity";

    private RecyclerView votesRecyclerView;
    private RecyclerView recordsRecyclerView;
    private VoteAdapter voteAdapter;
    private RecordAdapter recordAdapter;
    private List<Vote> votes = new ArrayList<>();
    private List<Record> records = new ArrayList<>();
    private ProgressDialog progressDialog;
    private FloatingActionButton fabAddVote;
    private FloatingActionButton fabAddRecord;
    BottomNavigationView bottomNav;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_votes_records);

        setupViews();
        setupRecyclerViews();
        loadData();
        setupBottomNavigation();
        setupFABs();
    }

    private void setupViews() {
        votesRecyclerView = findViewById(R.id.recyclerViewVotes);
        recordsRecyclerView = findViewById(R.id.recyclerViewRecords);
        fabAddVote = findViewById(R.id.fabAddVote);
        fabAddRecord = findViewById(R.id.fabAddRecord);
        bottomNav = findViewById(R.id.bottomNavigationView);
    }

    private void setupRecyclerViews() {
        votesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        voteAdapter = new VoteAdapter(votes, this, false);
        votesRecyclerView.setAdapter(voteAdapter);
        votesRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        recordsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recordAdapter = new RecordAdapter(records, this);
        recordsRecyclerView.setAdapter(recordAdapter);
        recordsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void loadData() {
        showLoading("Cargando datos...");
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        int neighborhoodId = prefs.getInt("neighborhoodId", -1);

        Log.d(TAG, "loadData: neighborhoodId=" + neighborhoodId);

        if (neighborhoodId != -1) {
            loadVotes(neighborhoodId);
            loadRecords(neighborhoodId);
        } else {
            hideLoading();
            Log.e(TAG, "loadData: Datos de usuario no encontrados en SharedPreferences");
            Toast.makeText(this, "Error al identificar la comunidad", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadVotes(int neighborhoodId) {
        Log.d(TAG, "loadVotes: Iniciando carga de votos para userId=" + neighborhoodId);
        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        apiService.getVotesByNeighborhoodId(neighborhoodId).enqueue(new Callback<List<Vote>>() {
            @Override
            public void onResponse(Call<List<Vote>> call, Response<List<Vote>> response) {
                Log.d(TAG, "loadVotes: onResponse called");
                if (response.isSuccessful() && response.body() != null) {
                    votes.clear();
                    votes.addAll(response.body());
                    voteAdapter.notifyDataSetChanged();
                    Log.d(TAG, "loadVotes: votos recibidos = " + votes.size());
                } else {
                    Log.e(TAG, "loadVotes: respuesta inválida o vacía");
                }
                hideLoading();
            }

            @Override
            public void onFailure(Call<List<Vote>> call, Throwable t) {
                hideLoading();
                Log.e(TAG, "loadVotes: error en la petición", t);
                Toast.makeText(VotesRecordsActivity.this, "Error al cargar votaciones", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecords(int neighborhoodId) {
        Log.d(TAG, "loadRecords: Iniciando carga de actas para neighborhoodId=" + neighborhoodId);
        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        apiService.getRecords(neighborhoodId).enqueue(new Callback<List<Record>>() {
            @Override
            public void onResponse(Call<List<Record>> call, Response<List<Record>> response) {
                Log.d(TAG, "loadRecords: onResponse called");
                if (response.isSuccessful() && response.body() != null) {
                    records.clear();
                    records.addAll(response.body());
                    recordAdapter.notifyDataSetChanged();
                    Log.d(TAG, "loadRecords: actas recibidas = " + records.size());
                } else {
                    Log.e(TAG, "loadRecords: respuesta inválida o vacía");
                }
                hideLoading();
            }

            @Override
            public void onFailure(Call<List<Record>> call, Throwable t) {
                hideLoading();
                Log.e(TAG, "loadRecords: error en la petición", t);
                Toast.makeText(VotesRecordsActivity.this, "Error al cargar actas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_records);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, NeighborhoodDetailActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_records) {
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupFABs() {
        fabAddVote.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, AddVoteActivity.class), 101);
        });

        fabAddRecord.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, AddRecordActivity.class), 102);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bottomNav.setSelectedItemId(R.id.nav_records);
        if (resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: datos actualizados, recargando...");
            loadData();
        }
    }
}
