package com.finzsoft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by jasonwang on 9/07/13.
 */
public class Environment {
    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    public static final String TMP = "tmp";
    public static final String CONF = "conf";
    public static final String LOGS = "logs";

    private Environment(){}

    private static Environment instance = new Environment();

    public static Environment getInstance(){

        return instance;
    }

    public void prepareEnvironment() {
        log.info("Preparing environment");
        boolean tmp = new File(TMP).mkdirs();
        boolean conf = new File(CONF).mkdirs();
        boolean logs = new File(LOGS).mkdirs();

        if(tmp){
            log.info("Creating tmp folder succeeded");
        }else{
            log.info("tmp folder already exist");
        }
        if(conf){
            log.info("Creating conf folder succeeded");
        }else{
            log.info("conf folder already exist");
        }
        if(logs){
            log.info("Creating logs folder succeeded");
        }else{
            log.info("logs folder already exist");
        }

    }
}
