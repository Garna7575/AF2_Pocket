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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingsActivity extends AppCompatActivity implements ReservationAdapter.OnReservationActionListener {
    private SharedPreferences prefs;
    private CalendarView calendarView;
    private RecyclerView reservationsRecyclerView;
    private BottomNavigationView bottomNav;
    private ReservationAdapter adapter;
    private FloatingActionButton fabAddReservation;

    private Calendar currentSelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);

        prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        initializeViews();
        setupCalendar();
        setupRecyclerView();
        setupNavigation();
        loadReservationsForDate(Calendar.getInstance());
    }

    private void initializeViews() {
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
    }

    private void setupCalendar() {
        Calendar today = Calendar.getInstance();
        Calendar minDate = (Calendar) today.clone();
        minDate.add(Calendar.WEEK_OF_YEAR, 0);

        Calendar maxDate = (Calendar) today.clone();
        maxDate.add(Calendar.WEEK_OF_MONTH, 3);

        calendarView.setMinDate(minDate.getTimeInMillis());
        calendarView.setMaxDate(maxDate.getTimeInMillis());

        // Establecer fecha inicial
        currentSelectedDate = Calendar.getInstance();
        calendarView.setDate(currentSelectedDate.getTimeInMillis());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            currentSelectedDate = Calendar.getInstance();
            currentSelectedDate.set(year, month, dayOfMonth);
            loadReservationsForDate(currentSelectedDate);
        });
    }

    private void setupRecyclerView() {
        reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReservationAdapter(new ArrayList<>(), this, this);
        reservationsRecyclerView.setAdapter(adapter);

        fabAddReservation.setOnClickListener(view -> {
            if (currentSelectedDate != null) {
                Log.d("FechaDebug", "Fecha seleccionada para nueva reserva: " + currentSelectedDate.getTime().toString());

                Intent intent = new Intent(BookingsActivity.this, AddReservation.class);
                intent.putExtra("selectedDate", currentSelectedDate.getTimeInMillis());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Selecciona una fecha válida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation() {
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.nav_home){
                    startActivity(new Intent(BookingsActivity.this, MainNeighborActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                } else if (item.getItemId() == R.id.nav_announcements){
                    startActivity(new Intent(BookingsActivity.this, AnnouncementsActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                } else if (item.getItemId() == R.id.nav_bookings){
                    startActivity(new Intent(BookingsActivity.this, BookingsActivity.class));
                } else if (item.getItemId() == R.id.nav_settings){
                    startActivity(new Intent(BookingsActivity.this, ProfileActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void onReservationDeleted() {
        adapter.collapseAll();
        if (currentSelectedDate != null) {
            loadReservationsForDate(currentSelectedDate);
        }
    }

    @Override
    public void onReservationEdit(Reservation reservation) {
        Intent intent = new Intent(this, AddReservation.class);
        intent.putExtra("editMode", true);
        intent.putExtra("reservationId", reservation.getId());
        intent.putExtra("commonAreaId", reservation.getCommonArea().getId());
        intent.putExtra("commonAreaName", reservation.getCommonArea().getName());
        intent.putExtra("startTime", reservation.getStartTime());
        intent.putExtra("endTime", reservation.getEndTime());

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(reservation.getStartTime());
            intent.putExtra("selectedDate", date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            intent.putExtra("selectedDate", Calendar.getInstance().getTimeInMillis());
        }

        startActivity(intent);
    }

    private void loadReservationsForDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String formattedDate = sdf.format(calendar.getTime());

        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        apiService.getReservationsByDate(formattedDate, prefs.getInt("neighborId", -1))
                .enqueue(new Callback<List<Reservation>>() {
                    @Override
                    public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
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
                        Log.e("BookingsActivity", "Error: " + t.getMessage());
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

    @Override
    protected void onResume() {
        super.onResume();
        if (currentSelectedDate != null) {
            loadReservationsForDate(currentSelectedDate);
        } else {
            loadReservationsForDate(Calendar.getInstance());
        }
    }

}