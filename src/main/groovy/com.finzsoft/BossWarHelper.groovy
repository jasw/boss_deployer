package com.finzsoft

import com.finzsoft.model.BossWarInfo

/**
 * Created by jasonwang on 9/07/13.
 */
class BossWarHelper {


    BossWarInfo getBossWarInfoFromFile(String path){

        def warFile = new File(path)
        def size = warFile.size()
        def unzippedLocation = Environment.TMP+File.pathSeparator+new Date().getTimeString()
        def version = ""
        def info = new BossWarInfo()
        info.setSizeInMB(size/(1024*1024))
        info.setOriginalPath(path)
        info.setUnzippedLocation(unzippedLocation)

        def ant = new AntBuilder()
        println "extracting $path into $unzippedLocation"
        ant.unzip(src:path,dest:unzippedLocation)
        new File(unzippedLocation).eachFileRecurse {file->
            if(file.name == "release.version"){
                version = file.getText("UTF-8");
            }
        }
        info.setVersion(version)
        return info;

    }
}
