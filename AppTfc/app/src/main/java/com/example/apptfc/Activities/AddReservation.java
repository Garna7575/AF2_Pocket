package com.example.apptfc.Activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.CommonArea;
import com.example.apptfc.API.PostAreaReservation;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.R;
import com.example.apptfc.adapters.CommonAreaAdapter;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddReservation extends AppCompatActivity {
    private TextView tvSelectedDate, tvStartHour, tvStartMinute, tvEndHour, tvEndMinute;
    private RecyclerView rvCommonAreas;
    private CommonAreaAdapter adapter;
    private List<CommonArea> availableCommonAreas = new ArrayList<>();
    private CommonArea selectedArea = null;
    private Calendar selectedDate;
    private int startHour = 9, startMinute = 0;
    private int endHour = 10, endMinute = 0;
    private SharedPreferences prefs;
    private int reservationId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reservation);

        prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        long dateInMillis = getIntent().getLongExtra("selectedDate", -1);
        selectedDate = Calendar.getInstance();

        if (dateInMillis != -1) {
            selectedDate.setTimeInMillis(dateInMillis);
            Log.d("FechaDebug", "Fecha recibida en AddReservation: " + selectedDate.getTime().toString());
        } else {
            Log.w("FechaDebug", "No se recibió fecha, usando fecha actual");
            Toast.makeText(this, "Usando fecha actual", Toast.LENGTH_SHORT).show();
        }

        initViews();
        setupTimeControls();
        setupRecyclerView();
        loadCommonAreas();

        MaterialButton actionButton = findViewById(R.id.btnCreateReservation);
        actionButton.setText("Crear Reserva");
        actionButton.setOnClickListener(v -> {
            createReservation();
        });
    }

    private void initViews() {
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvStartHour = findViewById(R.id.tvStartHour);
        tvStartMinute = findViewById(R.id.tvStartMinute);
        tvEndHour = findViewById(R.id.tvEndHour);
        tvEndMinute = findViewById(R.id.tvEndMinute);
        rvCommonAreas = findViewById(R.id.rvCommonAreas);

        updateDateDisplay();
        updateTimeDisplay();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        tvSelectedDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void setupTimeControls() {
        setupTimeButton(R.id.btnIncreaseStartHour, () -> {
            if (startHour < 23) startHour++;
            updateTimeDisplay();
        });

        setupTimeButton(R.id.btnDecreaseStartHour, () -> {
            if (startHour > 9) startHour--;
            updateTimeDisplay();
        });

        setupTimeButton(R.id.btnIncreaseStartMinute, () -> {
            startMinute = (startMinute + 10) % 60;
            updateTimeDisplay();
        });

        setupTimeButton(R.id.btnDecreaseStartMinute, () -> {
            startMinute = (startMinute - 10 + 60) % 60;
            updateTimeDisplay();
        });

        setupTimeButton(R.id.btnIncreaseEndHour, () -> {
            if (endHour < 23) endHour++;
            updateTimeDisplay();
        });

        setupTimeButton(R.id.btnDecreaseEndHour, () -> {
            if (endHour > startHour + 1 || (endHour == startHour + 1 && endMinute > startMinute)) {
                endHour--;
            }
            updateTimeDisplay();
        });

        setupTimeButton(R.id.btnIncreaseEndMinute, () -> {
            endMinute = (endMinute + 10) % 60;
            updateTimeDisplay();
        });

        setupTimeButton(R.id.btnDecreaseEndMinute, () -> {
            if (!(endHour == startHour && endMinute <= startMinute)) {
                endMinute = (endMinute - 10 + 60) % 60;
            }
            updateTimeDisplay();
        });
    }

    private void setupTimeButton(int buttonId, Runnable action) {
        MaterialButton button = findViewById(buttonId);
        button.setOnClickListener(v -> action.run());
    }

    private void updateTimeDisplay() {
        if (endHour < startHour || (endHour == startHour && endMinute <= startMinute)) {
            endHour = startHour;
            endMinute = (startMinute + 10) % 60;
            if (startMinute >= 50) endHour++;
        }

        tvStartHour.setText(String.format(Locale.getDefault(), "%02d", startHour));
        tvStartMinute.setText(String.format(Locale.getDefault(), "%02d", startMinute));
        tvEndHour.setText(String.format(Locale.getDefault(), "%02d", endHour));
        tvEndMinute.setText(String.format(Locale.getDefault(), "%02d", endMinute));

        if (!availableCommonAreas.isEmpty()) {
            checkAvailability();
        }
    }

    private void setupRecyclerView() {
        rvCommonAreas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommonAreaAdapter(availableCommonAreas, area -> {
            selectedArea = area;
            adapter.notifyDataSetChanged();
        });
        rvCommonAreas.setAdapter(adapter);
    }

    private void loadCommonAreas() {
        int neighborhoodId = prefs.getInt("neighborhoodId", -1);
        if (neighborhoodId == -1) {
            Toast.makeText(this, "Error: Vecindario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando zonas...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        apiService.getCommonAreaByNeighborhood(neighborhoodId)
                .enqueue(new Callback<List<CommonArea>>() {
                    @Override
                    public void onResponse(Call<List<CommonArea>> call, Response<List<CommonArea>> response) {
                        progressDialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            availableCommonAreas = response.body();
                            Collections.sort(availableCommonAreas, (a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));
                            checkAvailability();
                        } else {
                            Toast.makeText(AddReservation.this, "Error al cargar zonas", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CommonArea>> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(AddReservation.this,
                                "Error de conexión",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupEditMode() {
        reservationId = getIntent().getIntExtra("reservationId", -1);
        int commonAreaId = getIntent().getIntExtra("commonAreaId", -1);
        String startTime = getIntent().getStringExtra("startTime");
        String endTime = getIntent().getStringExtra("endTime");

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

            // Parsear hora de inicio
            Date startDate = sdf.parse(startTime);
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            startHour = startCal.get(Calendar.HOUR_OF_DAY);
            startMinute = startCal.get(Calendar.MINUTE);

            // Parsear hora de fin
            Date endDate = sdf.parse(endTime);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            endHour = endCal.get(Calendar.HOUR_OF_DAY);
            endMinute = endCal.get(Calendar.MINUTE);

            updateTimeDisplay();

            // Configurar el área común seleccionada
            selectedArea = new CommonArea();
            selectedArea.setId(commonAreaId);
            selectedArea.setName(getIntent().getStringExtra("commonAreaName"));

            // Forzar la selección en el RecyclerView
            if (adapter != null) {
                adapter.setSelectedArea(selectedArea);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar los datos de la reserva", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAvailability() {
        if (availableCommonAreas == null || availableCommonAreas.isEmpty()) {
            return;
        }

        String startTime = getDateTimeString(startHour, startMinute);
        String endTime = getDateTimeString(endHour, endMinute);
        Log.d("CheckAvailability", "Verificando: " + startTime + " - " + endTime);

        List<CommonArea> availableAreas = new ArrayList<>();
        AtomicInteger pendingRequests = new AtomicInteger(availableCommonAreas.size());

        for (CommonArea area : availableCommonAreas) {
            ApiService apiService = RetrofitClient.get().create(ApiService.class);
            apiService.checkIfAvailable(area.getId(), startTime, endTime)
                    .enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                            Log.d("API Request", "URL: " + call.request().url());
                            if (response.isSuccessful()) {
                                if (response.body() != null && response.body()) {
                                    availableAreas.add(area);
                                }
                            } else {
                                Log.e("API Error", "Código: " + response.code() + " - " + response.message());
                            }

                            if (pendingRequests.decrementAndGet() == 0) {
                                updateUIWithAvailableAreas(availableAreas);
                            }
                        }

                        @Override
                        public void onFailure(Call<Boolean> call, Throwable t) {
                            Log.e("API Failure", "Error: " + t.getMessage());
                            if (pendingRequests.decrementAndGet() == 0) {
                                updateUIWithAvailableAreas(availableAreas);
                            }
                        }
                    });
        }
    }

    private void updateUIWithAvailableAreas(List<CommonArea> availableAreas) {
        runOnUiThread(() -> {
            adapter.updateData(availableAreas);
            if (availableAreas.isEmpty()) {
                Toast.makeText(this,
                        "No hay zonas disponibles para este horario",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getDateTimeString(int hour, int minute) {
        Calendar calendar = (Calendar) selectedDate.clone();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(calendar.getTime());
    }

    private void createReservation() {
        if (selectedArea == null) {
            showError("Selecciona una zona común");
            return;
        }

        int neighborId = prefs.getInt("neighborId", -1);
        if (neighborId == -1) {
            showError("Usuario no identificado");
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creando reserva...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (selectedDate == null) {
            selectedDate = Calendar.getInstance();
        }

        PostAreaReservation reservation = new PostAreaReservation();
        reservation.setCommonAreaId(selectedArea.getId());
        reservation.setStartTime(getDateTimeString(startHour, startMinute));
        reservation.setEndTime(getDateTimeString(endHour, endMinute));
        reservation.setNeighborId(neighborId);

        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        Call<Void> call = apiService.createReservation(reservation);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    String message = "Reserva creada con éxito";
                    showSuccess(message);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                        if (errorBody.contains("no está disponible") || errorBody.contains("IllegalArgumentException")) {
                            showError("El área ya no está disponible para el horario seleccionado. Por favor, elige otro horario o área.");
                        } else {
                            showError("Error al procesar la reserva. Código: " + response.code());
                        }
                        
                        checkAvailability();
                    } catch (IOException e) {
                        showError("Error al procesar la respuesta del servidor");
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressDialog.dismiss();
                if (t instanceof IllegalArgumentException) {
                    showError("El área no está disponible en el horario solicitado. Por favor, elige otro horario.");
                } else {
                    showError("Error de conexión: " + t.getMessage());
                }
            }
        });
    }

    private void updateReservation() {
        createReservation();
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showSuccess(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Éxito")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }
}