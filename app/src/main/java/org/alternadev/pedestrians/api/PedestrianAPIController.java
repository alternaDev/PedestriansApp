package org.alternadev.pedestrians.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.alternadev.pedestrians.MainActivity;
import org.alternadev.pedestrians.db.Pedestrian;
import org.alternadev.pedestrians.db.PedestrianImage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Julius on 29.06.2017.
 */

public class PedestrianAPIController {
    static final String BASE_URL = "http://192.168.178.77:8080/";
    private Context context;

    public PedestrianAPIController(Context c) {
        this.context = c;

    }

    public void fetchImages() {
        GetImagesController controller = new GetImagesController();
        controller.start();
    }

    public void sendResults() {
        SendResultsController controller = new SendResultsController();
        controller.start();
    }

    public void getNames(){
        GetNamesController controller = new GetNamesController();
        controller.start();
    }

    private class GetImagesController implements Callback<List<ImageServerLocation>> {
        public void start() {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            PedestrianAPI pedestrianAPI = retrofit.create(PedestrianAPI.class);

            Call<List<ImageServerLocation>> call = pedestrianAPI.getImages(MainActivity.USER);
            call.enqueue(this);

        }

        @Override
        public void onResponse(Call<List<ImageServerLocation>> call, Response<List<ImageServerLocation>> response) {
            if (response.isSuccessful()) {
                List<ImageServerLocation> images = response.body();
                for (ImageServerLocation isl : images) {
                    isl.persist(PedestrianAPIController.this.context);
                }

            } else {
                System.out.println(response.errorBody());
            }
        }

        @Override
        public void onFailure(Call<List<ImageServerLocation>> call, Throwable t) {
            t.printStackTrace();
        }
    }

    private class GetNamesController implements Callback<List<String>> {
        public void start() {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            PedestrianAPI pedestrianAPI = retrofit.create(PedestrianAPI.class);

            Call<List<String>> call = pedestrianAPI.getNames();
            call.enqueue(this);

        }

        @Override
        public void onResponse(Call<List<String>> call, Response<List<String>> response) {
            if (response.isSuccessful()) {
                List<String> names = response.body();
                Iterator<Pedestrian> it = Pedestrian.findAll(Pedestrian.class);
                ArrayList<Pedestrian> list = new ArrayList<Pedestrian>();
                while(it.hasNext()){
                    Pedestrian p = it.next();
                    list.add(p);
                }
                for(String s : names){
                    boolean existing = false;
                    for(Pedestrian p : list){
                        if(p.getName().equals(s))
                            existing = true;
                    }
                    if(!existing){
                        Pedestrian newPedestrian = new Pedestrian();
                        newPedestrian.setName(s);
                        newPedestrian.save();
                    }
                }
            } else {
                System.out.println(response.errorBody());
            }
        }

        @Override
        public void onFailure(Call<List<String>> call, Throwable t) {
            t.printStackTrace();
        }
    }

    private class SendResultsController implements Callback<String> {
        public void start() {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            PedestrianAPI pedestrianAPI = retrofit.create(PedestrianAPI.class);
            Iterator<PedestrianImage> it = PedestrianImage.findAll(PedestrianImage.class);
            ArrayList<PedestrianImage> list = new ArrayList<PedestrianImage>();
            while(it.hasNext()){
                PedestrianImage i = it.next();
                if(i.isAlreadyAnalyzed())
                    list.add(i);
            }
            ArrayList<PedestrianResult> results = new ArrayList<PedestrianResult>();
            for (PedestrianImage pi : list) {
                PedestrianResult r = new PedestrianResult();
                String fileName = pi.getPath().substring(pi.getPath().lastIndexOf('/')+1, pi.getPath().length() );
                r.setFileName(fileName);
                r.setPedestrian(!pi.isNoPedestrian());
                if(pi.getPedestrian() != null)
                    r.setRecognizedPedestrian(pi.getPedestrian().getName());
                results.add(r);
            }
            Call<String> call = pedestrianAPI.sendResults(MainActivity.USER, results);
            call.enqueue(this);

        }

        @Override
        public void onResponse(Call<String> call, Response<String> response) {
            if (response.isSuccessful()) {
                String resp = response.body();
                System.out.println(resp);

            } else {
                System.out.println(response.errorBody());
            }
        }

        @Override
        public void onFailure(Call<String> call, Throwable t) {

        }
    }
}
