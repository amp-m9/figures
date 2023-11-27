package xyz.andrick.figures.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import xyz.andrick.figures.records.PexelImage;

import java.io.IOException;

import static xyz.andrick.figures.utilities.Helpers.getInputStreamFromUrl;

public class ImageSelectVbox extends VBox {
    public ImageSelectVbox(PexelImage photo) {
        super();
        try {
            Image image = new Image(getInputStreamFromUrl(photo.src().small()));
            if (image.isError()) {
                throw new RuntimeException("Cannot load image");
            }
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);

            BorderPane imagePane = new BorderPane();
            imagePane.setCenter(imageView);

            Label label = new Label(photo.alt());
            label.setAlignment(Pos.TOP_CENTER);
            label.setTextAlignment(TextAlignment.CENTER);
            label.prefWidthProperty().bind(widthProperty());
            getChildren().add(imagePane);
            getChildren().add(label);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
