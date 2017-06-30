package org.alternadev.pedestrians.org.alternadev.pedestrians.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Julius on 29.06.2017.
 */

public interface PedestrianAPI {

    @GET("getImages")
    Call<List<ImageServerLocation>> getImages(@Header("X-User") String user);

    @GET("getNames")
    Call<List<String>> getNames();

    @Headers("Content-Type: application/json")
    @POST("results")
    Call<String> sendResults(@Header("X-User") String user, @Body List<PedestrianResult> data);
}
