package xyz.andrick.figures;

import javafx.scene.Scene;
import javafx.stage.Stage;

public record SessionSettings(long imageTimeMillis,
                              long breakTimeMillis,
                              String directory,
                              Integer picturesBetweenBreaks,
                              String[] imageSources,
                              Scene previousScene,
                              Stage stage) {
}