package org.eimerarchive;

import javafx.application.Application;
import org.eimerarchive.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class MCVersionChecker {
    private MCVersionChecker() {}

    static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            if (arg.equals("--scan")) {
                for (String part : args[i + 1].split(",")) {
                    System.out.println(part);
                    File file = new File(part);
                    if (!file.isFile() || !file.getName().endsWith(".jar")) {
                        continue;
                    }

                    try {
                        FileUtil.getBukkitVersion(file);
                    } catch (IOException | NoSuchAlgorithmException ex) {
                        System.err.println("Unable to get version from \"" + file.getAbsolutePath() + "\": " + ex.getMessage());
                    }
                }
                return;
            }
        }
        Application.launch(CheckUI.class, args);
    }
}