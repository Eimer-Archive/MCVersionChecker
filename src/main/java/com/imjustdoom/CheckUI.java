package com.imjustdoom;

import com.imjustdoom.util.FileUtil;
import com.sun.scenario.Settings;
import com.sun.tools.javac.Main;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Stream;

public class CheckUI extends Application {

    @Override
    public void start(Stage stage) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jar", "*.jar"));

        DirectoryChooser directoryChooser = new DirectoryChooser();

        Group group = new Group();
        Scene scene = new Scene(group, 400, 320);

        stage = new Stage();

        stage.setResizable(false);
        stage.setTitle("Version checking");
        stage.setScene(scene);

        Button button = new Button();
        button.setText("Select file/s");

        Button scan = new Button();
        scan.setText("Select folder");
        scan.setLayoutY(25);

        group.getChildren().addAll(button, scan);

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

        scan.setOnAction(e -> {
            File directory = directoryChooser.showDialog(finalStage);
            if (directory == null) return;
            System.out.println("Started");
            this.e = 0;
            System.out.println(countFiles(directory.toPath()));
            scanDirectory(directory);
            System.out.println("Done - " + this.e);
        });

        stage.show();
    }

    public static int countFiles(Path directory) {
        try {
            FileCounterVisitor visitor = new FileCounterVisitor();
            Files.walkFileTree(directory, visitor);
            return visitor.getFileCount();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static class FileCounterVisitor extends SimpleFileVisitor<Path> {
        private int fileCount;

        public int getFileCount() {
            return fileCount;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (attrs.isRegularFile() && Files.isReadable(file)) {
                fileCount++;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
            // Handle specific exceptions here, if required
            e.printStackTrace();
            return FileVisitResult.CONTINUE;
        }
    }

    private int e = 0;

    private void scanDirectory(File directory) {
        if (directory == null || !directory.canRead() || !directory.isDirectory()) return;
        for (File folder : directory.listFiles()) {
            if (folder.isDirectory()) {
                scanDirectory(folder);
            } else if (folder.isFile() && folder.getName().contains(".jar")) {
                e++;
                System.out.println(folder.getAbsolutePath());
            }
        }
    }
}