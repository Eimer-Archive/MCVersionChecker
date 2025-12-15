package org.eimerarchive;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.eimerarchive.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class CheckUI extends Application {
    private List<File> fileList = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        Group group = new Group();
        Scene scene = new Scene(group, 400, 320);

        stage.setResizable(false);
        stage.setTitle("Version Checker");
        stage.setScene(scene);

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jar", "*.jar"));

        Button button = new Button();
        button.setText("Select file/s");
        button.setOnAction(e -> {
            fileList = fileChooser.showOpenMultipleDialog(stage);
            if (fileList == null) {
                return;
            }

            for (File file : fileList) {
                try {
                    FileUtil.getBukkitVersion(file);
                } catch (IOException | NoSuchAlgorithmException ex) {
                    System.err.println("Unable to get version from \"" + file.getAbsolutePath() + "\": " + ex.getMessage());
                }
            }
        });

        group.getChildren().add(button);
        stage.show();
    }
}