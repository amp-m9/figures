package xyz.andrick.figures;

import javafx.scene.Scene;

public record SessionSettings(long ImageTimeMillis , long BreakTimeMillis, String Directory, Integer PicturesBetweenBreaks, String[] ImageSources, Scene previousScene) {}