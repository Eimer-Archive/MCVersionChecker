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
import java.util.List;

public class CheckUI extends Application {

    @Override
    public void start(Stage stage) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jar", "*.jar"));

        Group group = new Group();
        Scene scene = new Scene(group, 400, 320);

        stage = new Stage();

        stage.setResizable(false);
        stage.setTitle("Version checking");
        stage.setScene(scene);

        Button button = new Button();
        button.setText("Select file/s");
        group.getChildren().add(button);

        Stage finalStage = stage;
        button.setOnAction(e -> {
            List<File> list = fileChooser.showOpenMultipleDialog(finalStage);
            if (list != null) {
                for (File file : list) {
                    try {
                        FileUtil.getBukkitVersion(file);
                    } catch (IOException | NoSuchAlgorithmException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        stage.show();
    }
}