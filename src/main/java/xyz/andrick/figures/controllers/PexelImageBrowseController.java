package xyz.andrick.figures.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import xyz.andrick.figures.records.PexelImage;
import xyz.andrick.figures.records.PexelResponse;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;


public class PexelImageBrowseController implements Initializable {
    int imageHeight = 250;
    @FXML
    TextField queryTextField;
    @FXML
    Button searchButton;
    @FXML
    HBox searchHbox;
    @FXML
    ScrollPane scrollPane;
    @FXML
    VBox galleryVbox;
    @FXML
    Pane imageBuffer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchButton.setOnAction(this::search);
    }

    private void search(ActionEvent actionEvent) {
        String query = queryTextField.getText();
        if (query.length() == 0)
            return;
        try {
            PexelResponse response = PexelSearchController.search(query);
            populateResults(response.photos());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public void init(String query) {
        queryTextField.setText(query);
    }

    public void populateResults(PexelImage[] photos) throws IOException {
        galleryVbox.getChildren().clear();
        for (PexelImage photo : photos
        ) {
            String url = photo.src().medium();
            URLConnection connection = new URL(url).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

            Image image = new Image(connection.getInputStream());
            if (image.isError()) {
                System.out.println("Error loading image from " + url);
                image.getException().printStackTrace();
                continue;
            }
            var imageView = new ImageView(image);
            imageView.setFitWidth(0);
            imageView.setFitHeight(imageHeight);
            imageView.preserveRatioProperty().set(true);
            imageView.styleProperty().set("-fx-background-color: red");
            imageBuffer.getChildren().add(imageView);
        }
        Platform.runLater(this::arrangeImages);
    }

    void arrangeImages() {
        final double maxWidth = galleryVbox.getWidth();
        var currentWidth = 0;
        var hBox = new HBox();
        var images = imageBuffer.getChildren().toArray();
        for (Object node : images) {
            var imageView = (ImageView) node;
            var imageWidth = imageView.layoutBoundsProperty().get().getWidth();
            if (currentWidth + imageWidth > maxWidth) {
                galleryVbox.getChildren().add(hBox);
                currentWidth = 0;
                hBox = new HBox();
            }
            hBox.getChildren().add(imageView);
            currentWidth += imageWidth;
        }
        galleryVbox.getChildren().add(hBox);
    }
}
