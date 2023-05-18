package xyz.andrick.figures;

import javafx.scene.Scene;

public record SessionSettings(Double ImageTime , Double BreakTime, String Directory, Integer PicturesBetweenBreaks, String[] ImageSources, Scene previousScene) {}
