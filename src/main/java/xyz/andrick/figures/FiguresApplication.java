package xyz.andrick.figures;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class FiguresApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FiguresApplication.class.getResource("session-settings-view.fxml"));
        Parent root = fxmlLoader.load();
        AnchorPane settings = new AnchorPane();

//        String css = getClass().getResource("session-settings.css").toExternalForm();
//        scene.getStylesheets().add(css);

        Scene scene = new Scene(root);
        SessionSettingsSceneController sessionSettingsController = fxmlLoader.getController();

        primaryStage.setTitle("Figures: Create a session");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void makeSettings(){
    }

    public static void main(String[] args) {
        launch();
    }
}