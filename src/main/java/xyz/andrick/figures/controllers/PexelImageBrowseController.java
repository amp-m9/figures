package xyz.andrick.figures.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class PexelImageBrowseController implements Initializable {
    int minImageWidth = 200;

    int spacing = 10;
    @FXML
    TextField queryTextField;
    @FXML
    Button searchButton;
    @FXML
    HBox searchHbox;
    @FXML
    HBox galleryHbox;
    @FXML
    VBox mainVbox;
    @FXML
    ScrollPane scrollPane;
    @FXML
    Pane imageBuffer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        galleryHbox.setSpacing(spacing);
        galleryHbox.paddingProperty().set(new Insets(0, 10, 10, 10));
        VBox.setMargin(scrollPane, new javafx.geometry.Insets(10, 0, 0, 0));

        searchButton.setOnAction(this::search);
    }

    private void search(ActionEvent actionEvent) {
        String query = queryTextField.getText();
        if (query.isEmpty())
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
        galleryHbox.getChildren().clear();
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
            imageView.setFitWidth(minImageWidth);
            imageView.setFitHeight(0);
            imageView.preserveRatioProperty().set(true);
            imageView.styleProperty().set("-fx-background-color: red");
            imageBuffer.getChildren().add(imageView);
        }
        Platform.runLater(this::arrangeImages);
    }

    void arrangeImages() {
        final double scrollbarWidth = scrollPane.lookup(".scroll-bar:vertical").getLayoutBounds().getWidth();
        double containerWidth = mainVbox.getLayoutBounds().getWidth() - 2 * spacing;
        containerWidth -= scrollbarWidth;
        final int maxCols = (int) Math.floor(containerWidth / (minImageWidth + spacing));
        final double colWidth = (containerWidth - (maxCols - 1) * spacing) / maxCols;
        final int containerCount = (int) (containerWidth / minImageWidth);

        var images = imageBuffer.getChildren().toArray();

        List<ImageColumn> containers = new ArrayList<>();
        for (int i = 0; i < containerCount; i++) {
            var container = new ImageColumn(new VBox(), 0);
            containers.add(container);
            container.Container.setSpacing(spacing);
            container.Container.setMinWidth(colWidth);
            container.Container.setPrefWidth(colWidth);
            galleryHbox.getChildren().add(container.Container);
        }

        Platform.runLater(() -> {
            for (Object node : images) {
                var imageView = (ImageView) node;
                imageView.setFitWidth(colWidth);
                var imageHeight = imageView.layoutBoundsProperty().get().getHeight();
                var container = containers.stream().min((p1, p2) -> Float.compare(p1.Height, p2.Height)).get();
                container.Container.getChildren().add(imageView);
                container.Height += imageHeight;
            }
        });
    }

    private class ImageColumn {
        public VBox Container;
        public float Height;

        ImageColumn(VBox container, float height) {
            Container = container;
            Height = height;
        }

    }
}
