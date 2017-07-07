package org.alternadev.pedestrians;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.squareup.okhttp.internal.Platform;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.alternadev.pedestrians.api.FetchReadyEvent;
import org.alternadev.pedestrians.api.PedestrianAPIController;
import org.alternadev.pedestrians.api.SendReadyEvent;
import org.alternadev.pedestrians.db.Pedestrian;
import org.alternadev.pedestrians.db.PedestrianImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.orm.SugarRecord.find;
import static com.orm.SugarRecord.findAll;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.imageView2)
    ImageView imageView;

    @BindView(R.id.delete)
    FloatingActionButton delete;

    @BindView(R.id.assign)
    FloatingActionButton assign;

    @BindView(R.id.skip)
    FloatingActionButton skip;

    @BindView(R.id.spinner)
    Spinner pedestrianNames;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.arriving)
    RadioButton arriving;

    @BindView(R.id.textView)
    TextView noImages;

    @BindView(R.id.radioGroup)
    RadioGroup group;

    @BindView(R.id.textView3)
    TextView remaining;

    private ArrayList<PedestrianImage> images;
    private PedestrianImage current;
    private PedestrianAPIController controller;

    private ArrayList<String> names;

    public static String USER = "NONE";
    private boolean autoSelect;

    ProgressDialog sendDialog;
    ProgressDialog fetchDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Pedestrian.deleteAll(Pedestrian.class);
        //PedestrianImage.deleteAll(PedestrianImage.class);
        autoSelect = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        PedaApplication.BUS.register(this);

        setSupportActionBar(toolbar);
        Stetho.initializeWithDefaults(this);

        images = new ArrayList<>();

        controller = new PedestrianAPIController(this);

        pedestrianNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!MainActivity.this.autoSelect && position == names.size() - 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Neue Person benennen");


                    final EditText input = new EditText(MainActivity.this);

                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);


                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Pedestrian newPedestrian = new Pedestrian();
                            newPedestrian.setName(input.getText().toString());
                            newPedestrian.save();
                            names.add(0, newPedestrian.getName());
                            pedestrianNames.setSelection(0);
                        }
                    });
                    builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();

                }
                MainActivity.this.autoSelect = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        final SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String user = sharedPref.getString("USER", null);
        if (user == null) {
            CharSequence userNames[] = {"Jean", "Julius"};
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Jean oder Christian?");
            builder.setItems(userNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    if (which == 0) {
                        editor.putString("USER", "A");
                        MainActivity.USER = "A";

                    } else {
                        editor.putString("USER", "B");
                        MainActivity.USER = "B";
                    }
                    editor.commit();
                }
            });
            builder.show();
        } else {
            MainActivity.USER = user;
        }

        loadData();
    }

    void loadData() {
        names = new ArrayList<>();
        ArrayList<Pedestrian> p = this.findAllPedestrians();
        for (Pedestrian pedestrian : p) {
            names.add(pedestrian.getName());
        }
        Collections.sort(names);
        names.add("Neue Person...");
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, names);
        pedestrianNames.setAdapter(spinnerArrayAdapter);

        this.loadImages();
        this.nextPicture();
    }

    @OnClick(R.id.skip)
    void onClickSkip() {
        skip();
    }

    @OnClick(R.id.delete)
    void onClickDelete() {
        this.current.setNoPedestrian(true);
        this.current.setAlreadyAnalyzed(true);
        this.current.save();
        this.nextPicture();
    }

    @OnClick(R.id.assign)
    void onClickAssign() {
        final PedestrianImage workingOn = MainActivity.this.current;

        Pedestrian newPedestrian = new Pedestrian();
        newPedestrian.setName(pedestrianNames.getSelectedItem().toString());
        newPedestrian.save();

        workingOn.setPedestrian(newPedestrian);
        workingOn.setAlreadyAnalyzed(true);

        if (arriving.isChecked())
            workingOn.setStatus("arriving");
        else
            workingOn.setStatus("leaving");
        workingOn.save();

        this.nextPicture();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void nextPicture() {
        this.images.remove(current);

        remaining.setText("Verbleibend:" + this.images.size());
        if (this.images.size() == 0) {
            this.imageView.setVisibility(View.INVISIBLE);
            noImages.setVisibility(View.VISIBLE);
            assign.setVisibility(View.INVISIBLE);
            delete.setVisibility(View.INVISIBLE);
            pedestrianNames.setVisibility(View.INVISIBLE);
            group.setVisibility(View.INVISIBLE);
            skip.setVisibility(View.INVISIBLE);

        } else {
            skip.setVisibility(View.VISIBLE);
            pedestrianNames.setVisibility(View.VISIBLE);
            group.setVisibility(View.VISIBLE);
            noImages.setVisibility(View.INVISIBLE);
            this.imageView.setVisibility(View.VISIBLE);
            assign.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);

            nextPic();


        }
    }

    void nextPic() {
        this.current = this.images.get(0);
        if (this.current.getSuggestion() != null) {
            this.autoSelect = true;
            this.pedestrianNames.setSelection(this.names.indexOf(this.current.getSuggestion().getName()));

            this.pedestrianNames.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            this.pedestrianNames.setBackgroundColor(getResources().getColor(android.R.color.background_light));
        }
        Picasso.with(this).load(current.getPath()).resize(800, 800)
                .centerInside().noFade().into(imageView);
    }

    public void skip() {
        if (this.images.size() > 1) {
            this.images.remove(current);
            this.images.add(current);
            nextPic();
        } else {
            Snackbar.make(this.imageView, "Keine andere Bilder vorhanden.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }


    public void loadImages() {

        this.images = new ArrayList<>();
        Iterator<PedestrianImage> it = PedestrianImage.findAll(PedestrianImage.class);
        while (it.hasNext()) {
            PedestrianImage next = it.next();
            if (!next.isAlreadyAnalyzed())
                this.images.add(next);
        }

        Collections.sort(this.images, PedestrianImage.DATE_COMPARATOR);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.send_data) {
            sendDialog = ProgressDialog.show(this, "",
                    "Sending Data. Please wait...", true);
            controller.sendResults();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<Pedestrian> findAllPedestrians() {
        Iterator<Pedestrian> it = findAll(Pedestrian.class);

        final ArrayList<Pedestrian> all = new ArrayList<Pedestrian>();

        while (it.hasNext()) {
            all.add(it.next());
        }
        return all;
    }

    @Subscribe
    public void fetchReady(FetchReadyEvent event) {
        if(fetchDialog != null)
            fetchDialog.hide();
        if(event.isSuccess) {
            this.loadData();
        } else {
            Toast.makeText(this, "Failed to fetch!", Toast.LENGTH_SHORT);
        }
    }

    @Subscribe
    public void sendReady(SendReadyEvent event) {
        if(sendDialog != null)
            sendDialog.hide();
        if(event.isSuccess) {
            Pedestrian.deleteAll(Pedestrian.class);
            PedestrianImage.deleteAll(PedestrianImage.class);
            controller.getNames();
            controller.fetchImages();
            fetchDialog = ProgressDialog.show(this, "",
                    "Fetching Data. Please wait...", true);
        } else {
            Toast.makeText(this, "Failed to send!", Toast.LENGTH_SHORT);
        }


    }
}
