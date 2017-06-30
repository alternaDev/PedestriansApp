package org.alternadev.pedestrians.db;

import com.orm.SugarRecord;

/**
 * Created by Julius on 29.06.2017.
 */

public class PedestrianImage extends SugarRecord<PedestrianImage>{
    private String name;
    private boolean alreadyAnalyzed;
    private boolean noPedestrian;
    private Pedestrian pedestrian;
    private Pedestrian suggestion;
    private String status;

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

    private String path;

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
}
