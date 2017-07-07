package org.alternadev.pedestrians.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.squareup.picasso.Picasso;

/**
 * Created by jhbru on 07.07.2017.
 */

public class CacheImageJob extends Job {
    private final String url;

    protected CacheImageJob(String url) {
        super(new Params(0).requireNetwork().persist());
        this.url = url;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        Picasso.with(getApplicationContext()).load(url).resize(800, 800).fetch();

    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }
}
