import groovy.sql.Sql


def CONFIG_FILE_SUFFIX = "-seek.properties";

def tomcatDir
String configDir
def allConfFiles = [];
def envs = [];
int indexEntered;
String userEnteredXMLLocation = ".";
System.in.withReader {
    def bufferedReader = new BufferedReader(it)
    println "Please provide tomcats installation directory, press enter to use the default one for Finzsoft TN/PN"
    def line = bufferedReader.readLine()
    if (line.trim().isEmpty()) {
        tomcatDir = "\\\\ak1vsvrpap01\\c\$\\servers\\apache-tomcat-6.0.32-windows-x64\\apache-tomcat-6.0.32"
    } else {
        tomcatDir = line.trim()
    }
    println "Using tomcat dir: " + tomcatDir
    configDir = tomcatDir + "\\lib";
    new File(configDir).eachFile { file ->
        if (file.name.endsWith(CONFIG_FILE_SUFFIX)) {
            allConfFiles << file;
            def env = file.name.replace(CONFIG_FILE_SUFFIX, "")
            envs << env;
        }
    }
    println "We have detected environments from the selected Tomcat environment: ";
    envs.eachWithIndex { def env, int i ->
        int plusOne = i + 1;
        println "   ($plusOne) $env"
    }
    println "Please enter the index of the environment to deploy to, press enter to deploy to all."
    def userEnteredEnv = bufferedReader.readLine()
    if (userEnteredEnv.isEmpty()) {
        println "Deploying to all..."
    } else {
        indexEntered = userEnteredEnv.toInteger()
        println "Deploying to (${indexEntered}) ${envs[indexEntered-1]}"
    }
    println "Please enter the location of the validation xmls.The location should contain no duplicated xmls "
    userEnteredXMLLocation = bufferedReader.readLine().trim();
    println "Loading validation definitions from ${userEnteredXMLLocation}"
}

def deployToAll = !indexEntered;
if (deployToAll) {
    allConfFiles.each { conf ->
        processConf(conf, userEnteredXMLLocation, CONFIG_FILE_SUFFIX)
    }
} else {
    def chosen = allConfFiles[indexEntered-1]
    processConf(chosen, userEnteredXMLLocation, CONFIG_FILE_SUFFIX)
}

private void processConf(conf, userEnteredXMLLocation, CONFIG_FILE_SUFFIX) {
    Properties p = new Properties();
    p.load(conf.newInputStream());
    //have to load into a properties first because ConfigSlurper does not handle comments in property files.
    def parsedConf = new ConfigSlurper().parse(p)
    Sql sql = Sql.newInstance(parsedConf.jdbc.connection.url, parsedConf.jdbc.connection.username,
            parsedConf.jdbc.connection.password, parsedConf.jdbc.connection.driver_class)

    new File(userEnteredXMLLocation).eachFileRecurse { vldXML ->
        if (vldXML.isFile() && vldXML.name.endsWith("VLD.xml")) {
            def validationRoot = new XmlParser().parse(vldXML)
            def pageCode = validationRoot.component_id.text()
            if (backupLastXmlIfDifferent(sql, pageCode, conf.name.replace(CONFIG_FILE_SUFFIX, ""), vldXML)) {
                deleteAllOldXmlsForThisPanel(sql, pageCode)
                insertNewXml(sql, vldXML, pageCode)
                println "Uploaded new ${vldXML.name}"
            } else {
                println "There is no change for ${vldXML.name}, so skipping the uploading."
            }

        }
    }
}
/**
 * Backup the existing xml stored in the database for the pageCode if and only if the newFile is different to the existing one.
 * Return true if different, otherwise false.
 * @param sql
 * @param pageCode
 * @param env
 * @param newFile
 * @return if different.
 */
private boolean backupLastXmlIfDifferent(sql, pageCode, env, newFile) {
    def date = new Date().format("yyyy-MM-dd");
    boolean different = false;
    sql.eachRow("select * from BOSS_SITE_PANEL_VALIDATION files " +
            " INNER JOIN BOSS_APPLICATION_PANEL_DEFN codes ON files.PANEL_ID = codes.PANEL_ID " +
            " WHERE files.EFFECTIVE_DATE <= \'${date}\' AND codes.PANEL_CODE = '${pageCode}' Order by files.EFFECTIVE_DATE desc") { row ->

        if (row == null || row.VALIDATION_XML == null) {
            println "cannot find validation xml for page code: ${pageCode}. So cannot backup."
            different = true;
            return;
        }
        def xml = clob2string(row.VALIDATION_XML.asciiStream)
        if (diff(newFile, xml)) {
            different = true;
            def backupDirPath = "./backup/validation/${env}/"
            def backupDir = new File(backupDirPath)
            backupDir.mkdirs();
            def backFilePath = backupDirPath + row.VALIDATION_XML_FILE_NAME.trim() + "." + row.EFFECTIVE_DATE
            def backupFile = new File(backFilePath)

            backupFile.createNewFile()
            backupFile << xml;

            println "creating backup file:${backFilePath}" + " for page: ${pageCode}"
        }
    }

    return different;
}

private String clob2string(inStream) {
    def buffer = new byte[1000]
    def out = new ByteArrayOutputStream()
    int num = 0
    while ((num = inStream.read(buffer)) > 0) {
        out.write(buffer, 0, num)
    }
    new String(out.toByteArray())
}

boolean diff(File file, String s) {
    return !file.getText().equals(s);
}


void deleteAllOldXmlsForThisPanel(Sql sql, String panelCode) {
    def affectedRows = sql.executeUpdate("delete from BOSS_SITE_PANEL_VALIDATION where PANEL_ID = ( Select PANEL_ID from BOSS_APPLICATION_PANEL_DEFN Where PANEL_CODE = ${panelCode} ) ")
    println "removed ; ${affectedRows} records for ${panelCode}"
}

void insertNewXml(Sql sql, File newXml, String panelCode) {
    def maxIdRow = sql.firstRow("SELECT MAX(SITE_PANEL_VALIDATION_ID) AS max_id FROM BOSS_SITE_PANEL_VALIDATION")
    if (maxIdRow == null) {
        return;
    }
    def maxId = maxIdRow.max_id.toInteger() + 1;

    def panelIdRow = sql.firstRow("select PANEL_ID from BOSS_APPLICATION_PANEL_DEFN Where PANEL_CODE = ${panelCode}")
    if (panelIdRow == null) {
        println "Cannot find panel ID for panel code:${panelCode}. Please check if the validation xml has the right component_id."
    }

    def panelId = panelIdRow.PANEL_ID

    def insert = "insert into BOSS_SITE_PANEL_VALIDATION (SITE_PANEL_VALIDATION_ID, PANEL_ID, EFFECTIVE_DATE, VALIDATION_XML, VALIDATION_XML_FILE_NAME) " +
            "values(?, ?, ?, ?, ?)"
    def insertStatement = sql.getConnection().prepareStatement(insert);
    insertStatement.setInt(1, maxId)
    insertStatement.setInt(2, panelId.toInteger())
    insertStatement.setDate(3, new java.sql.Date(new Date().getTime()))
    insertStatement.setCharacterStream(4, newXml.newReader());
    insertStatement.setString(5, newXml.name)
    insertStatement.executeUpdate()

}