package org.eimerarchive;

import javafx.application.Application;

public class MCVersionChecker {
    private MCVersionChecker() {}

    static void main(String[] args) {
        Application.launch(CheckUI.class, args);
    }
}