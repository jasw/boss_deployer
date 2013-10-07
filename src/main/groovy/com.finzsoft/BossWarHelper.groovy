package com.finzsoft

import com.finzsoft.model.BossWarInfo

/**
 * Created by jasonwang on 9/07/13.
 */
class BossWarHelper {


    BossWarInfo getBossWarInfoFromFile(String path){

        def warFile = new File(path)
        def size = warFile.size()
        def unzippedLocation = Environment.TMP+File.separator+new Date().format("yyyy-MM-dd-HH-mm")
        def version = ""
        def info = new BossWarInfo()
        info.setSizeInMB(size/(1024*1024))
        info.setOriginalPath(path)
        info.setUnzippedLocation(unzippedLocation)

        def ant = new AntBuilder()
        println "extracting $path into $unzippedLocation"
        ant.unzip(src:path,dest:unzippedLocation)
        def unzippedCommonUtilDir;
        new File(unzippedLocation).eachFileRecurse {file->
            if(file.name.contains( "SovSE_Common_Util")){
                unzippedCommonUtilDir = unzippedLocation + File.separator + file.getName()
                ant.unzip(src:file.getAbsolutePath(), dest: unzippedCommonUtilDir)
            }
        }
        while(!new File(unzippedCommonUtilDir).exists()){
            //wait for half a sec.
            Thread.sleep(500)
        }
        def versionFileText = new File(unzippedCommonUtilDir+File.separator+"version.properties").text
        version = versionFileText.substring(versionFileText.lastIndexOf("=")+1)

        info.setVersion(version)
        return info;

    }
}
