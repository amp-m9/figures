package xyz.andrick.figures.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import xyz.andrick.figures.records.PexelImage;
import xyz.andrick.figures.records.PexelResponse;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;


public class PexelImageBrowseController {
    @FXML
    TextField queryTextField;

    @FXML
    Button searchButton;
    @FXML
    Button confirmSelectionButton;
    @FXML
    TilePane resultsTilePane;

    PexelResponse lastSearch;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchButton.setOnAction(this::search);
        resultsTilePane.setPrefTileHeight(200);
        resultsTilePane.setPrefTileWidth(240);
        resultsTilePane.setHgap(10);
        resultsTilePane.setVgap(10);
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

    public void init(PexelResponse response, String query) {
        queryTextField.setText(query);

        try {
            populateResults(response.photos());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void populateResults(PexelImage[] photos) throws IOException {
        resultsTilePane.getChildren().clear();
        for (PexelImage photo : photos
        ) {
            String url = photo.src().tiny();
            URLConnection connection = new URL(url).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

            Image image = new Image(connection.getInputStream());
            if (image.isError()) {
                System.out.println("Error loading image from " + url);
                // if you need more details
                image.getException().printStackTrace();
                return;
            }
            ImageView imageView = new ImageView(image);
            Label label = new Label(photo.alt());
            label.setAlignment(Pos.TOP_CENTER);
            label.setMinHeight(80);
            label.setMaxHeight(80);

            VBox vbox = new VBox();
            vbox.maxWidth(200);
            vbox.minWidth(200);
            vbox.setPrefHeight(300);

            vbox.setAlignment(Pos.BOTTOM_CENTER);

            vbox.getChildren().add(imageView);
            vbox.getChildren().add(label);

            resultsTilePane.getChildren().add(vbox);
            resultsTilePane.setPrefTileHeight(300);
            resultsTilePane.setPrefTileWidth(200);

        }
    }
}
