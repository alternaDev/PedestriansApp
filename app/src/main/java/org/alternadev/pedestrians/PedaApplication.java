package org.alternadev.pedestrians;

import android.app.Application;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.orm.SugarApp;
import com.orm.SugarContext;
import com.squareup.otto.Bus;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by jhbru on 07.07.2017.
 */

public class PedaApplication extends SugarApp {
    public static JobManager JOB_MANAGER;
    public static final Bus BUS = new Bus();

    @Override
    public void onCreate() {
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(BuildConfig.DEBUG);
        built.setLoggingEnabled(BuildConfig.DEBUG);
        Picasso.setSingletonInstance(built);

        SugarContext.init(this);
        SugarContext.getSugarContext().getSugarDb().getDB(); // We have to call this to circumvent the stupid, non working lazy loading of sugarorm. thank your for nothing.
        super.onCreate();
        Log.d("ja", "moin");
        JOB_MANAGER = new JobManager(new Configuration.Builder(this).maxConsumerCount(2).build());
        PedaApplication.JOB_MANAGER.stop();

    }

    @Override
    public void onTerminate() {
        SugarContext.terminate();
        super.onTerminate();
        Log.d("ja", "asd");
    }
}
