package xyz.andrick.figures.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.TilePane;
import xyz.andrick.figures.components.ImageSelectVbox;
import xyz.andrick.figures.records.PexelImage;
import xyz.andrick.figures.records.PexelResponse;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;


public class PexelImageBrowseController implements Initializable {
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
            String url = photo.src().small();
            URLConnection connection = new URL(url).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

            Image image = new Image(connection.getInputStream());
            if (image.isError()) {
                System.out.println("Error loading image from " + url);
                image.getException().printStackTrace();
                continue;
            }
            ImageSelectVbox imageSelectVbox = new ImageSelectVbox(photo);
            resultsTilePane.getChildren().add(imageSelectVbox);
        }
    }
}
