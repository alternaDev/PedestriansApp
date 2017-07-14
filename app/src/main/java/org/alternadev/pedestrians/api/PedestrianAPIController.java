package org.alternadev.pedestrians.api;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orm.SugarContext;
import com.orm.SugarTransactionHelper;

import org.alternadev.pedestrians.MainActivity;
import org.alternadev.pedestrians.PedaApplication;
import org.alternadev.pedestrians.db.Pedestrian;
import org.alternadev.pedestrians.db.PedestrianImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Julius on 29.06.2017.
 */

public class PedestrianAPIController {
    static final String BASE_URL = "http://192.168.178.75:8080/";
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

    PedestrianAPI getAPI() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(10 * 60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        final Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();

        return retrofit.create(PedestrianAPI.class);
    }

    private class GetImagesController implements Callback<List<ImageServerLocation>> {
        public void start() {


            Call<List<ImageServerLocation>> call = getAPI().getImages(MainActivity.USER);
            call.enqueue(this);

        }

        @Override
        public void onResponse(Call<List<ImageServerLocation>> call, Response<List<ImageServerLocation>> response) {
            if (response.isSuccessful()) {
                // Wait with downloading of images to give time for persitsting of persons.
                PedaApplication.JOB_MANAGER.stop();

                List<ImageServerLocation> images = response.body();

                for(final List<ImageServerLocation> i : Lists.partition(images, 300))
                    SugarTransactionHelper.doInTransaction(new SugarTransactionHelper.Callback() {
                        @Override
                        public void manipulateInTransaction() {
                            for (ImageServerLocation isl : i) {
                                isl.persist(PedestrianAPIController.this.context);
                            }
                        }
                    });


                PedaApplication.JOB_MANAGER.start();



            } else {
                System.out.println(response.errorBody());
            }
            FetchReadyEvent e = new FetchReadyEvent();
            e.isSuccess = response.isSuccessful();
            PedaApplication.BUS.post(e);

        }

        @Override
        public void onFailure(Call<List<ImageServerLocation>> call, Throwable t) {
            t.printStackTrace();
        }
    }

    private class GetNamesController implements Callback<List<String>> {
        public void start() {

            Call<List<String>> call = getAPI().getNames();
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

    private class SendResultsController implements Callback<ResponseBody> {
        public void start() {

            // TODO: Make this async!
            new Thread(new Runnable() {
                @Override
                public void run() {
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
                        r.setStatus(pi.getStatus());
                        if(pi.getPedestrian() != null)
                            r.setRecognizedPedestrian(pi.getPedestrian().getName());
                        results.add(r);
                    }
                    Log.d("asd", "asd1");

                    Call<ResponseBody> call = getAPI().sendResults(MainActivity.USER, results);
                    call.enqueue(SendResultsController.this);
                }
            });


        }

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            Log.d("asd", "asd");
            SendReadyEvent e = new SendReadyEvent();
            if (response.isSuccessful()) {
                String resp = null;
                try {
                    resp = response.body().string();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.out.println(resp);
            } else {
                System.out.println(response.errorBody());
            }
            e.isSuccess = response.isSuccessful();
            PedaApplication.BUS.post(e);
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
            SendReadyEvent e = new SendReadyEvent();
            e.isSuccess = false;
            PedaApplication.BUS.post(e);
        }
    }
}
