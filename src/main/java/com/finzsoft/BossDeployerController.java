package com.finzsoft;

import com.finzsoft.model.BossWarInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by jasonwang on 8/07/13.
 */
public class BossDeployerController {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);


    @FXML
    public TitledPane x1;
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
            warSize.setText(warInfo.getSizeInMB()+" MB");
            warVersion.setText(warInfo.getVersion());
        } else {
            //default return value
            path = null;
        }
        warLocationField.setText(path);
    }


}
