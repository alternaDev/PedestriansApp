package org.alternadev.pedestrians.db;

import com.orm.SugarRecord;

/**
 * Created by Julius on 28.06.2017.
 */

public class Pedestrian extends SugarRecord {
    public Pedestrian(){

    }
    private String name;



    public Pedestrian(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
