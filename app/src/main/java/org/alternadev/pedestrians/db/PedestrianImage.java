package org.alternadev.pedestrians.db;

import com.orm.SugarRecord;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Julius on 29.06.2017.
 */

public class PedestrianImage extends SugarRecord {

    public static final Comparator<PedestrianImage> DATE_COMPARATOR = new Comparator<PedestrianImage>() {
        DateFormat parser = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss", Locale.GERMAN);

        @Override
        public int compare(PedestrianImage o1, PedestrianImage o2) {
            try {
                Date d1 = parser.parse(o1.getName());
                Date d2 = parser.parse(o2.getName());
                return d1.compareTo(d2);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }
    };


    private String name;
    private boolean alreadyAnalyzed;
    private boolean noPedestrian;
    private Pedestrian pedestrian;
    private Pedestrian suggestion;
    private String status;
    private String path;

    public PedestrianImage(){
        this.alreadyAnalyzed = false;
        this.noPedestrian = false;
    }

    public PedestrianImage(String name, boolean alreadyAnalyzed, boolean noPedestrian, Pedestrian pedestrian, Pedestrian suggestion, String path, String status){
        this.alreadyAnalyzed = alreadyAnalyzed;
        this.name = name;
        this.noPedestrian = noPedestrian;
        this.pedestrian = pedestrian;
        this.path = path;
        this.status = status;
        this.suggestion = suggestion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Pedestrian getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(Pedestrian suggestion) {
        this.suggestion = suggestion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAlreadyAnalyzed() {
        return alreadyAnalyzed;
    }

    public void setAlreadyAnalyzed(boolean alreadyAnalyzed) {
        this.alreadyAnalyzed = alreadyAnalyzed;
    }

    public boolean isNoPedestrian() {
        return noPedestrian;
    }

    public void setNoPedestrian(boolean noPedestrian) {
        this.noPedestrian = noPedestrian;
    }

    public Pedestrian getPedestrian() {
        return pedestrian;
    }

    public void setPedestrian(Pedestrian pedestrian) {
        this.pedestrian = pedestrian;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
