package com.finzsoft.model;

/**
 * Created by jasonwang on 9/07/13.
 */
public class BossWarInfo {

    private double sizeInMB;
    private String version;
    private String originalPath;
    private String unzippedLocation;

    public double getSizeInMB() {
        return sizeInMB;
    }

    public void setSizeInMB(double sizeInMB) {
        this.sizeInMB = sizeInMB;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getUnzippedLocation() {
        return unzippedLocation;
    }

    public void setUnzippedLocation(String unzippedLocation) {
        this.unzippedLocation = unzippedLocation;
    }
}
