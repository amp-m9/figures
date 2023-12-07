package xyz.andrick.figures.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import xyz.andrick.figures.records.PexelImage;

import java.io.IOException;

import static xyz.andrick.figures.utilities.Helpers.getInputStreamFromUrl;

public class ImageSelectVbox extends VBox {
    boolean selected = false;
    ImageView imageView;
    BorderPane imagePane;
    Label label;
    int fontSize = 15;

    public ImageSelectVbox(PexelImage photo) {
        super();
        try {
            Image image = new Image(getInputStreamFromUrl(photo.src().medium()));
            if (image.isError()) {
                throw new RuntimeException("Cannot load image");
            }
            imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.fitWidthProperty().bind(widthProperty());


            label = new Label(photo.alt());
            label.setAlignment(Pos.TOP_CENTER);
            label.setTextAlignment(TextAlignment.CENTER);
            label.prefWidthProperty().bind(widthProperty());
            label.fontProperty().set(Font.font(fontSize));
            label.setPrefHeight(computeMinHeight(1));
            getChildren().add(imageView);
            getChildren().add(label);
            setVgrow(imageView, Priority.ALWAYS);
            setOnMouseClicked(this::onMouseClicked);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        selected = !selected;
        styleProperty().set(selected ? "-fx-background-color: blue" : "");
        label.styleProperty().set(selected ? "-fx-background-color: green" : "");
        imagePane.styleProperty().set(selected ? "-fx-background-color: red" : "");
    }
}
