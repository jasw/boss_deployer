package com.finzsoft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Created by jasonwang on 9/07/13.
 */
public class Environment {
    private static final Logger log = LoggerFactory.getLogger(Environment.class);

    public static final String TMP = "tmp";
    public static final String CONF = "conf";
    public static final String LOGS = "logs";
    public static final String DEFAULT_CONF = "default.properties";
    public static final String LAST_USED_CONF = "last_used.conf";

    private static Properties properties;

    static {
        File defaultConf = new File(CONF + File.separator + DEFAULT_CONF);
        File lastUsedConf = new File(CONF + File.separator + LAST_USED_CONF);


        if(defaultConf.exists()){
            try {
                BufferedReader defaultOne = new BufferedReader(new BufferedReader(new FileReader(defaultConf)));
                Properties properties = new Properties();
                properties.load(defaultOne);
                log.info("default properties: "+properties);
                if(lastUsedConf.exists()){

                    BufferedReader usedOne = new BufferedReader(new BufferedReader(new FileReader(lastUsedConf)));
                    properties.load(usedOne);
                    log.info("after merging in last used conf:"+properties);
                }else{
                    log.info("last used conf:"+LAST_USED_CONF+" does not exist");
                }
            } catch (FileNotFoundException e) {
                //ignored
            } catch (IOException e) {
                //ignored
            }

        }
    }

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

    public String getSetting(String property){

        return properties==null?"":properties.getProperty(property);
    }


}
