package com.finzsoft

import com.finzsoft.model.TomcatInfo

/**
 * Created by jasonwang on 9/07/13.
 */
class TomcatHelper {

    TomcatInfo getTomcatInfo(String dir){

        TomcatInfo tomcatInfo = new TomcatInfo()
        //tomcatDir/conf/server.xml contains port info
        def serverDef = new File(dir+File.separator+"conf"+File.separator+"server.xml")
        def server = new XmlParser().parse(serverDef)
        //find the service with name="Catalina"
        def catalinaService = server.Service.find(){it.'@name'.contains("Catalina")}
        //find the http connector
        def httpConnector = catalinaService.Connector.find{it.'@protocol'.contains("HTTP")}
        tomcatInfo.setPort(httpConnector.'@port')
        def maxThreads = httpConnector.'@maxThreads'
        if(maxThreads == null){
            maxThreads = "200"; //default is 200
        }
        tomcatInfo.setMaxThreads( Integer.parseInt(maxThreads))
        def releaseNotes = new File(dir+File.separator+"RELEASE-NOTES")
        String version;
        releaseNotes.eachLine { if(it.contains("Apache Tomcat Version ")){
            version = it.replace("Apache Tomcat Version","")
            if(version!=null){
                version = version.trim()
            }
        }}
        tomcatInfo.setVersion(version)
        //All context files will be in tomcatDir/conf/Catalina/localhost/*.xml
        def contexts = new ArrayList<String>();
        def contextFolder = dir+File.separator+"conf"+File.separator+"Catalina"+File.separator+"localhost"
        if(new File(contextFolder).exists()){
            new File(contextFolder).
                    eachFileMatch(~/.*\.xml/){
                        contexts << it.getName().replace(".xml","")
                    }
            tomcatInfo.setContextLists(contexts)

        }else{
            println "Context folder does not exist."
        }
        //All war files will be under tomcatDir/webapps
        def wars = new ArrayList<String>()
        new File(dir+File.separator+"webapps").eachFileMatch(~/.*\.war/){
            wars<<it.getName().replace(".war","")
        }
        tomcatInfo.setWarFiles(wars)

        //tomcatDir/bin/tomcat7-bossw.exe
        new File(dir+File.separator+"bin").eachFile() {
            if(it.getName().endsWith("w.exe") || it.getName().endsWith("W.exe")){
                tomcatInfo.setWindowsServiceName(it.getName().replace(".exe",""))
            }
        }
        return tomcatInfo

    }

    String stopTomcat(String hostName, String serviceName){
        def reply = "scripts/sc-stop.bat ${hostName}${serviceName}".execute().text
    }

    String startTomcat(String hostName, String serviceName){
        "scripts/sc-start.bat ${hostName}${serviceName}".execute().text
    }
}
