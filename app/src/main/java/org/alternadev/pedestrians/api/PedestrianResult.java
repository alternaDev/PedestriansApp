package org.alternadev.pedestrians.api;

/**
 * Created by Julius on 29.06.2017.
 */

public class PedestrianResult {
    private String fileName;
    private String recognizedPedestrian;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;
    private boolean isPedestrian;

    public boolean isPedestrian() {
        return isPedestrian;
    }

    public void setPedestrian(boolean pedestrian) {
        isPedestrian = pedestrian;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRecognizedPedestrian() {
        return recognizedPedestrian;
    }

    public void setRecognizedPedestrian(String recognizedPedestrian) {
        this.recognizedPedestrian = recognizedPedestrian;
    }
}
