package com.example.dolarya.API_SOA;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiClient apiClient;
    public static SoaService soaService;

    private ApiClient(){
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://so-unlam.net.ar/api/api/")
                .build();
        soaService = retrofit.create(SoaService.class);
    }

    public static ApiClient getInstance(){
        if(apiClient == null)
          apiClient = new ApiClient();
        return apiClient;
    }

    public void registrarUsuario(SoaRequest request, Callback<SoaResponse> callback) {
        Call<SoaResponse> userCall = soaService.registrarUsuario(request);
        userCall.enqueue(callback);
    }

    public void loginUsuario(SoaRequest request, Callback<SoaResponse> callback) {
        Call<SoaResponse> userCall = soaService.loginUsuario(request);
        userCall.enqueue(callback);
    }

    public void registrarEvento(String token, SoaEventoRequest request, Callback<SoaEventoResponse> callback) {
        Call<SoaEventoResponse> userCall = soaService.registrarEvento("Bearer " + token, request);
        userCall.enqueue(callback);
    }

    public void actualizarToken(String token, Callback<SoaResponse> callback) {
        Call<SoaResponse> userCall = soaService.actualizarToken("Bearer " + token);
        userCall.enqueue(callback);
    }
}
