package com.finzsoft.model;

import java.util.List;

/**
 * Created by jasonwang on 9/07/13.
 */
public class TomcatInfo {

    private String port;

    private int maxThreads;
    private String version;
    private String windowsServiceName;
    private List<String> contextLists;
    private List<String> warFiles;

    public List<String> getWarFiles() {
        return warFiles;
    }

    public void setWarFiles(List<String> warFiles) {
        this.warFiles = warFiles;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getWindowsServiceName() {
        return windowsServiceName;
    }

    public void setWindowsServiceName(String windowsServiceName) {
        this.windowsServiceName = windowsServiceName;
    }

    public List<String> getContextLists() {
        return contextLists;
    }

    public void setContextLists(List<String> contextLists) {
        this.contextLists = contextLists;
    }


    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    @Override
    public String toString() {
        return "TomcatInfo{" +
                "port='" + port + '\'' +
                ", maxThreads=" + maxThreads +
                ", version='" + version + '\'' +
                ", windowsServiceName='" + windowsServiceName + '\'' +
                ", contextLists=" + contextLists +
                ", warFiles=" + warFiles +
                '}';
    }
}
