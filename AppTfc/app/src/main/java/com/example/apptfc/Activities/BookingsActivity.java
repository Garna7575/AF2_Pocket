package com.example.apptfc.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.Reservation;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.Activities.Neighbor.AnnouncementsActivity;
import com.example.apptfc.Activities.Neighbor.MainNeighborActivity;
import com.example.apptfc.R;
import com.example.apptfc.adapters.ReservationAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingsActivity extends AppCompatActivity {

    SharedPreferences prefs;

    CalendarView calendarView;
    RecyclerView reservationsRecyclerView;

    BottomNavigationView bottomNav;

    List<Reservation> reservations;

    ReservationAdapter adapter;

    FloatingActionButton fabAddReservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);

        prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fabAddReservation = findViewById(R.id.fabAddReservation);

        calendarView = findViewById(R.id.calendarView);
        reservationsRecyclerView = findViewById(R.id.reservationsRecyclerView);
        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_bookings);

        Calendar today = Calendar.getInstance();

        Calendar minDate = (Calendar) today.clone();
        minDate.add(Calendar.WEEK_OF_YEAR, 0);
        minDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        Calendar maxDate = (Calendar) today.clone();
        maxDate.add(Calendar.WEEK_OF_MONTH, 3);

        calendarView.setMinDate(minDate.getTimeInMillis());
        calendarView.setMaxDate(maxDate.getTimeInMillis());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            Toast.makeText(this, "Fecha seleccionada: " + dayOfMonth + "/" + (month + 1) + "/" + year, Toast.LENGTH_SHORT).show();
            loadReservationsForDate(selectedDate);
        });

        reservations = new ArrayList<>();
        reservationsRecyclerView = findViewById(R.id.reservationsRecyclerView);
        reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReservationAdapter(new ArrayList<>());
        reservationsRecyclerView.setAdapter(adapter);

        loadReservationsForDate(Calendar.getInstance());

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_home) {
                    startActivity(new Intent(BookingsActivity.this, MainNeighborActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                } else if (item.getItemId() == R.id.nav_announcements) {
                    startActivity(new Intent(BookingsActivity.this, AnnouncementsActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                } else if (item.getItemId() == R.id.nav_settings) {
                    startActivity(new Intent(BookingsActivity.this, ProfileActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                } else if (item.getItemId() == R.id.nav_bookings) {
                    startActivity(new Intent(BookingsActivity.this, BookingsActivity.class));
                }
                return false;
            }
        });

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(i, i1, i2);

                loadReservationsForDate(selectedDate);
            }
        });

        fabAddReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BookingsActivity.this, AddReservation.class));
            }
        });
    }

    private void loadReservationsForDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String formattedDate = sdf.format(calendar.getTime());

        Log.d("loadReservations", "loadReservationsForDate: " + formattedDate);

        ApiService apiService = RetrofitClient.get().create(ApiService.class);

        Call<List<Reservation>> call = apiService.getReservationsByDate(formattedDate, prefs.getInt("neighborId", -1));

        call.enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                Log.d("reservations", "onResponseSuccess: " + response.isSuccessful());
                Log.d("reservations", "onResponseBody: " + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateReservations(response.body());

                    if (response.body().isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                    }
                } else {
                    Toast.makeText(BookingsActivity.this, "Error al obtener reservas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Reservation>> call, Throwable t) {
                Log.d("LoadReservationsForDay", "onFailure: " + t.getMessage());
            }
        });
    }

    private void showEmptyState() {
        findViewById(R.id.emptyState).setVisibility(View.VISIBLE);
        reservationsRecyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        findViewById(R.id.emptyState).setVisibility(View.GONE);
        reservationsRecyclerView.setVisibility(View.VISIBLE);
    }
}
