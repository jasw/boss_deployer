package com.finzsoft;

import com.finzsoft.model.BossWarInfo;
import com.finzsoft.model.TomcatInfo;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasonwang on 8/07/13.
 */
public class BossDeployerController {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);



    @FXML
    public TextField warLocationField;
    @FXML
    public Button chooseWarBtn;
    @FXML
    public TextField tomcatDirField;
    @FXML
    public Button chooseDirBtn;
    @FXML
    public CheckBox backUpOldWarCheck;
    @FXML
    public CheckBox removeBackupLogsCheck;
    @FXML
    public ToggleButton freshDeployToggle;
    @FXML
    public TextField contextNameField;
    @FXML
    public ComboBox contextCombo;
    @FXML
    public TextField warVersion;
    @FXML
    public TextField warSize;
    @FXML
    public TextField tomcatVersion;
    @FXML
    public TextField tomcatServiceName;
    @FXML
    public TextField tomcatPort;
    @FXML
    public Button confirmBtn;
    @FXML
    public TextArea summary;
    @FXML
    public TextArea logArea;
    @FXML
    public ProgressBar progressBar;
    @FXML
    public TitledPane configPane;
    @FXML
    public TitledPane executionPane;
    @FXML
    public Accordion accordion;
    @FXML
    public CheckBox remoteCheck;
    @FXML
    public TextField hostNameField;
    @FXML
    public Label hostNameLabel;


    @FXML
    void chooseWar(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        //Extention filter
        FileChooser.ExtensionFilter extentionFilter = new FileChooser.ExtensionFilter("WAR files (*.war)", "*.war");
        fileChooser.getExtensionFilters().add(extentionFilter);

        //Set to user directory or go to default if cannot access
        String userDirectoryString = System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        if (!userDirectory.canRead()) {
            userDirectory = new File(".");
        }
        fileChooser.setInitialDirectory(userDirectory);

        //Choose the file
        File chosenFile = fileChooser.showOpenDialog(null);
        //Make sure a file was selected, if not return default
        String path;
        if (chosenFile != null) {
            path = chosenFile.getPath();
            BossWarInfo warInfo = new BossWarHelper().getBossWarInfoFromFile(path);
            DecimalFormat df = new DecimalFormat("###.##");
            warSize.setText(df.format(warInfo.getSizeInMB())+" MB");
            warVersion.setText(warInfo.getVersion());
        } else {
            //default return value
            path = null;
        }
        warLocationField.setText(path);
    }

    @FXML
    public void chooseTCDir(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select where Tomcat is installed");
        File defaultDirectory = new File(".");
        chooser.setInitialDirectory(defaultDirectory);
        Window window = ((Node) (actionEvent.getTarget())).getScene().getWindow();
        File selectedDirectory = chooser.showDialog(window);
        if(selectedDirectory!=null){
            String absolutePath = selectedDirectory.getAbsolutePath();
            tomcatDirField.setText(absolutePath);
            TomcatInfo tomcatInfo = new TomcatHelper().getTomcatInfo(absolutePath);
            log.info("Tomcat has info:"+tomcatInfo);
            tomcatPort.setText(tomcatInfo.getPort());
            tomcatVersion.setText(tomcatInfo.getVersion());
            tomcatServiceName.setText(tomcatInfo.getWindowsServiceName());
            if(tomcatInfo.getContextLists() != null){
                List contexts = new ArrayList();
                contexts.add("None");
                contexts.add(tomcatInfo.getContextLists());
                contextCombo.setItems(FXCollections.observableArrayList(contexts));
                contextCombo.setVisible(true);
                contextCombo.setPromptText("Pick an existing context to deploy to");
            }else if(tomcatInfo.getWarFiles()!=null){
                List wars = new ArrayList();
                wars.add("None");
                wars.add(tomcatInfo.getWarFiles());
                contextCombo.setItems(FXCollections.observableArrayList(wars));
                contextCombo.setVisible(true);
                contextCombo.setPromptText("Pick an existing war to replace");
            }else{
                //if no context or wars found then it must be a fresh install. no need to backup war as there isn't one.
                freshDeployToggle.setSelected(true);
                freshDeployToggle.setDisable(true);
                backUpOldWarCheck.setSelected(false);
                backUpOldWarCheck.setDisable(true);
            }
        }
    }

    public void onContextSelection(ActionEvent actionEvent) {
        Object context = contextCombo.getValue();
        if(context !=null && !"None".equals(context) ){//chose a context meaning re-deploy
            freshDeployToggle.setSelected(false);
            contextNameField.setText(context.toString());
        }
    }


    public void showSummary(ActionEvent actionEvent) {
        String firstLineHolder = "You will deploy %s of version %s into Tomcat at machine %s, location %s under the context name: %s ";
        String warLoc = warLocationField.getText();
        String warName = "";
        if(warLoc!=null){
            warName = warLoc.substring(warLoc.lastIndexOf(File.separatorChar));
        }
        String firstLine = String.format(firstLineHolder, warName, warVersion.getText(), tomcatDirField.getText(), contextNameField.getText());
        summary.appendText(firstLine);
        if(backUpOldWarCheck.isSelected()){
            summary.appendText("Previous version of the war will be backed up in /backup/war folder.");
        }
        if(removeBackupLogsCheck.isSelected()){
            summary.appendText("Existing logs will be moved to /backup/logs");
        }
        if(freshDeployToggle.isSelected()){
            summary.appendText("A virtual context will be created along with all properties/jar/xml files. ");
        }

    }

    public void confirm(ActionEvent actionEvent) {
        accordion.setExpandedPane(executionPane);
        String serviceName = tomcatServiceName.getText();
        String hostName = hostNameField.getText();
        progressBar.setProgress(0.10);
        logArea.appendText("Stopping "+serviceName+" at machine "+hostName);
        TomcatHelper tomcatHelper = new TomcatHelper();
        String result = tomcatHelper.stopTomcat(hostName, serviceName);
        logArea.appendText(result);
        String warLoc = warLocationField.getText();
        logArea.appendText("Deploying "+  warLoc.substring(warLoc.lastIndexOf(File.separatorChar)));
        progressBar.setProgress(0.50);
        logArea.appendText("Backing up logs and existing war");
        progressBar.setProgress(0.80);
        logArea.appendText("Starting "+serviceName);
        String res = tomcatHelper.startTomcat(hostName, serviceName);
        logArea.appendText(res);

    }

    public void remoteDeployChecked(ActionEvent actionEvent) {
        hostNameField.setVisible(remoteCheck.isSelected());
        hostNameLabel.setVisible(remoteCheck.isSelected());
    }
}
