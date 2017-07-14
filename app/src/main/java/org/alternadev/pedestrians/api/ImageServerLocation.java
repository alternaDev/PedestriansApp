package org.alternadev.pedestrians.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.alternadev.pedestrians.MainActivity;
import org.alternadev.pedestrians.PedaApplication;
import org.alternadev.pedestrians.db.Pedestrian;
import org.alternadev.pedestrians.db.PedestrianImage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;


/**
 * Created by Julius on 29.06.2017.
 */

public class ImageServerLocation {
    String url;
    String otherSuggestion;

    private Context c;

    public ImageServerLocation(String s) {
        this.url = s;
    }

    public void persist(final Context c) {
        this.c = c;
        PedaApplication.JOB_MANAGER.addJobInBackground(new CacheImageJob(url));

        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        PedestrianImage image = new PedestrianImage();

        Iterator<Pedestrian> it = Pedestrian.findAll(Pedestrian.class);
        while (it.hasNext() && ImageServerLocation.this.otherSuggestion != null) {
            Pedestrian p = it.next();
            if (ImageServerLocation.this.otherSuggestion.equals(p.getName())) {
                image.setSuggestion(p);
            }
        }

        //Saving it to the database

        image.setName(fileName.substring(0, fileName.lastIndexOf('.')));
        image.setPath(ImageServerLocation.this.url);
        image.save();
    }

}
