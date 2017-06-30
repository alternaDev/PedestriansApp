package org.alternadev.pedestrians.api;

import android.content.Context;
import android.os.AsyncTask;

import org.alternadev.pedestrians.MainActivity;
import org.alternadev.pedestrians.db.Pedestrian;
import org.alternadev.pedestrians.db.PedestrianImage;

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

    public ImageServerLocation(String s) {
        this.url = s;
    }

    public void persist(Context c) {
        String fileName = url.substring(url.lastIndexOf('/')+1, url.length());
        String name = fileName.substring(0, fileName.lastIndexOf('.'));
        Iterator<PedestrianImage> it = PedestrianImage.findAll(PedestrianImage.class);
        boolean alreadyInDatabase = false;
        while(it.hasNext()){
            if(it.next().getName().equals(name))
                alreadyInDatabase = true;
        }

        if(!alreadyInDatabase) {
            final DownloadTask downloadTask = new DownloadTask(c);
            downloadTask.execute(url);
        }

    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPostExecute(String r){
            if(context instanceof MainActivity){
                ((MainActivity) context).loadImages();
                ((MainActivity) context).nextPicture();
            }
        }
        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                String fileName = sUrl[0].substring(sUrl[0].lastIndexOf('/')+1, sUrl[0].length() );
                output = context.openFileOutput(fileName, Context.MODE_PRIVATE);//new FileOutputStream(fileName);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);

                }

                //Finding the suggested Pedestrian:
                PedestrianImage image = new PedestrianImage();
                Iterator<Pedestrian> it  = Pedestrian.findAll(Pedestrian.class);
                while (it.hasNext() && ImageServerLocation.this.otherSuggestion != null) {
                        Pedestrian p = it.next();
                        if(ImageServerLocation.this.otherSuggestion.equals(p.getName())){
                            image.setSuggestion(p);
                        }
                    }

                //Saving it to the database

                image.setName(fileName.substring(0, fileName.lastIndexOf('.')));
                image.setPath(context.getFilesDir().toString() + "/" + fileName);
                image.save();
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }
}
