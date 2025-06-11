package com.example.apptfc.API;


import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("users/{username}/{password}")
    Call<User> getUser(@Path("username") String username, @Path("password") String password);

    @GET("users/recovery/{email}")
    Call<User> getUserByEmail(@Path("email") String email);

    @GET("users/{username}")
    Call<User> getUserByUsername(@Path("username") String username);

    @GET("neighbor/{userId}")
    Call<Neighbor> getNeighborByUserId(@Path("userId") int userId);

    @GET("neighbor/by-neighborhood/{neighborhoodId}")
    Call<List<Neighbor>> getNeighborsByNeighborhood(@Path("neighborhoodId") int neighborhoodId);

    @GET("neighborhood")
    Call<List<Neighborhood>> getAllNeighborhoods();

    @GET("admin/{id}")
    Call<User> getAdminById(@Path("id") int id);

    @GET("admin/userId/{id}")
    Call<Admin> getAdminByUserId(@Path("id") int id);

    @GET("neighbor/neighborhood-id/{userId}")
    Call<Integer> getNeighborhoodId(@Path("userId") int userId);

    @GET("neighborhood/{id}")
    Call<Neighborhood> getNeighborhoodById(@Path("id") int id);

    @GET("neighborhood/admin/{id}")
    Call<List<Neighborhood>> getNeighborhoodByAdminId(@Path("id") int id);

    @GET("neighborhood/{neighborhoodId}/admin")
    Call<Admin> getAdminByNeighborhoodId(@Path("neighborhoodId") int neighborhoodId);

    @GET("vote/by-user/{userId}")
    Call<List<Vote>> getVotesByUser(@Path("userId") int userId);

    @GET("records/{neighborhoodId}")
    Call<List<Record>> getRecords(@Path("neighborhoodId") int neighborhoodId);

    @GET("incidences/{neighborhoodId}")
    Call<List<Incidence>> getIncidencesByNeighborhoodId(@Path("neighborhoodId") int neighborhoodId);

    @GET("reservations/date")
    Call<List<Reservation>> getReservationsByDate(@Query("date") String date, @Query("id") int userId);

    @GET("common-area/{id}")
    Call<List<CommonArea>> getCommonAreaByNeighborhood(@Path("id") int neighborhoodId);

    @GET("reservations/availability")
    Call<Boolean> checkIfAvailable(@Query("commonAreaId") int commonAreaId, @Query("startTime") String startTime, @Query("endTime") String endTime);

    @GET("receipts/{id}")
    Call<List<Receipt>> getReceiptsByNeighbor(@Path("id") int id);

    @GET("receipts/pending-receipts/{id}")
    Call<List<Receipt>> getPendingReceipts(@Path("id") int id);

    @POST("incidences/{neighborId}")
    Call<Void> postIncidence(@Path("neighborId") int neighborId, @Body Incidence incidence);

    @POST("users/neighbor")
    Call<Void> createNeighbor(@Body PostRequest request);

    @POST("users/forgot-password")
    Call<Void> forgotPassword(@Body ForgotPassword email);

    @POST("receipts/email")
    Call<Void> sendPaymentReminderEmail(@Body PaymentEmailDTO dto);

    @POST("users/change-password/{id}")
    Call<Void> changePassword(@Body PasswordChangeRequest request, @Path("id") int id);

    @POST("votes/vote/{neighborId}/{voteId}/{inFavor}")
    Call<ResponseBody> vote(@Path("neighborId") int neighborId, @Path("voteId") int voteId, @Path("inFavor") boolean inFavor);

    @POST("reservations")
    Call<Void> createReservation(@Body PostAreaReservation reservation);

    @POST("neighborhood")
    Call<Void> sendNeighborhoodCreationRequest(@Body creationNeighborhoods request);

    @PUT("users/{id}")
    Call<Void> updateUser(@Body User user, @Path("id") int id);

    @PUT("reservations/{id}")
    Call<Void> updateReservation(@Path("id") int id, @Body PostAreaReservation reservation);

    @PUT("receipts/payment/{id}")
    Call<Void> payment(@Path("id") int id);

    @DELETE("reservations/{id}")
    Call<Void> deleteReservation(@Path("id") int id);

    @DELETE("users/neighbor/{id}")
    Call<Void> deleteNeighbor(@Path("id") int id);
}