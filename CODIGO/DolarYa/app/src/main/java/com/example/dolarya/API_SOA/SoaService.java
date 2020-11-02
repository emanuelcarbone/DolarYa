package com.example.dolarya.API_SOA;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface SoaService {
    @POST("register")
    Call<SoaResponse> registrarUsuario(@Body SoaRequest request);

    @POST("login")
    Call<SoaResponse> loginUsuario(@Body SoaRequest request);

    @POST("event")
    Call<SoaEventoResponse> registrarEvento(@Header("Authorization") String token, @Body SoaEventoRequest request);

    @PUT("refresh")
    Call<SoaResponse> actualizarToken(@Header("Authorization") String tokenRefresh);
}
