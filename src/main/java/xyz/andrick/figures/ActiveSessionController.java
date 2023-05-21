package xyz.andrick.figures;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Timer;
import java.util.TimerTask;


public class ActiveSessionController {
    private double startDragX, startDragY;
    private final double maxZoom = 5;
    private int imageIndex = 0;
    private SessionSettings settings;
    private Timer imageTimer;
    private ObservableTimer timer;
    @FXML
    AnchorPane imageAnchorPane;
    @FXML
    AnchorPane baseAnchorPane;
    @FXML
    ImageView imageView;
    @FXML
    Button quitButton;
    @FXML
    Button playPauseButton;
    @FXML
    Button nextButton;
    @FXML
    Button previousButton;

    @FXML
    public void initialize()
    {
        imageView.setPreserveRatio(true);
        imageView.fitHeightProperty().bind(imageAnchorPane.heightProperty());
        imageView.fitWidthProperty().bind(imageAnchorPane.widthProperty());
        imageAnchorPane.setOnMousePressed(this::onMousePressed);
        imageAnchorPane.setOnMouseDragged(this::onMouseDragged);
        imageAnchorPane.setOnScroll(this::zoomOnScroll);
        quitButton.setOnAction(this::quitToSettings);
        nextButton.setOnAction(event->nextImage());
        previousButton.setOnAction(event -> previousImage());
    }

    public void initialiseSettings(SessionSettings _settings) {
        settings = _settings;
        System.out.println(settings.toString());
        displayImage(imageIndex);

        setUpTimers();
    }

    private void setUpTimers() {
        Stage stage = (Stage)(imageView.getScene().getWindow());
        timer = new ObservableTimer(settings.ImageTimeMillis(), -1, this::onTick);
        timer.start();
        playPauseButton.setOnAction(event -> {
            if(timer.isRunning()) {
                timer.pause();
            }
            else {
                timer.resume();
            }
        });

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                timer.killAll();
            }
        });
    }

    public void nextImage(){
        imageIndex++;
        imageIndex = clamp(imageIndex, 0, settings.ImageSources().length);
        displayImage(imageIndex);
    }
    public void previousImage(){
        imageIndex--;
        imageIndex = clamp(imageIndex, 0, settings.ImageSources().length);
        displayImage(imageIndex);

    }
    public void displayImage(int index) {

        String fileString = settings.Directory() + '/' + settings.ImageSources()[index%settings.ImageSources().length];
        File imageFile = new File(fileString);
        if(!imageFile.exists()){
            displayImageError(fileString);
            return;
        }
        try {
            String imageURL = imageFile.toURI().toURL().toExternalForm();
            imageView.setImage(new Image(imageURL));
            resetImage();
            KeepImageInFrame();
        } catch (MalformedURLException e) {
            displayImageError(fileString);
        }
    }

    public void onMousePressed(MouseEvent mouseEvent){
            startDragX = mouseEvent.getSceneX();
            startDragY = mouseEvent.getSceneY();
    }

    public void onMouseDragged(MouseEvent mouseEvent){
        imageView.setX(imageView.getX() + (mouseEvent.getSceneX() - startDragX));
        imageView.setY(imageView.getY() + (mouseEvent.getSceneY() - startDragY));
        KeepImageInFrame();
        startDragX = mouseEvent.getSceneX();
        startDragY = mouseEvent.getSceneY();
    }

    public void zoomOnScroll(ScrollEvent scrollEvent){
        Integer direction = (int)(scrollEvent.getDeltaY());
        if(direction == 0) {
            return;
        }

        direction = direction.compareTo(0);
        if ((direction>0 && imageView.getScaleX()>=maxZoom) || (direction<0 && imageView.getScaleY()<=0)) {
            return;
        }

        double scaleFactor = (float) (direction * .05);
        double newScale = clamp( (imageView.getScaleX()) + scaleFactor, 1, maxZoom);

        double TargetCursorDeltaX = calculateMouseXAfterZoom(scrollEvent, newScale);
        double TargetMouseDeltaY = calculateMouseYAfterZoom(scrollEvent,newScale);

        imageView.setScaleX(newScale);
        imageView.setScaleY(newScale);

        adjustMouseDelta(scrollEvent, TargetCursorDeltaX, TargetMouseDeltaY);
        KeepImageInFrame();
    }

    private void onTick() {
        System.out.println("Time elapsed: %f".formatted(timer.getTimeElapsed()/1000));
        nextImage();
    }

    private void displayImageError(String fileString) {
        System.out.printf("Cannot display image %s%n", fileString);
    }

    private void quitToSettings(ActionEvent event) {
        timer.killAll();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(settings.previousScene());
        stage.show();
    }

    private void KeepImageInFrame()
    {
        double imageHeight = imageView.getBoundsInParent().getHeight();
        double imageWidth = imageView.getBoundsInParent().getWidth();

        double LowerBoundX = Math.max( (imageAnchorPane.getWidth()-imageWidth)/2, 0);
        double UpperBoundX = Math.min( imageAnchorPane.getWidth(),  (imageAnchorPane.getWidth()+imageWidth)/2);
        double LowerBoundY = Math.max( (imageAnchorPane.getHeight()-imageHeight)/2, 0);
        double UpperBoundY = Math.min( imageAnchorPane.getHeight() , (imageAnchorPane.getHeight()+imageHeight)/2);


        double imageMinY = imageView.getBoundsInParent().getMinY(); // top
        double imageMaxY = imageView.getBoundsInParent().getMaxY(); // bottom
        double imageMinX = imageView.getBoundsInParent().getMinX(); // lhs
        double imageMaxX = imageView.getBoundsInParent().getMaxX(); // rhs

        if(imageMinY>LowerBoundY){ imageView.setY(imageView.getY() - (imageMinY-LowerBoundY)); }

        if(imageMaxY<UpperBoundY){ imageView.setY(imageView.getY() + (UpperBoundY-imageMaxY)); }

        if(imageMinX>LowerBoundX){ imageView.setX(imageView.getX() - (imageMinX-LowerBoundX)); }

        if(imageMaxX<UpperBoundX){ imageView.setX(imageView.getX() + (UpperBoundX - imageMaxX)); }
    }

    private void resetImage() {
        imageView.setX(0);
        imageView.setY(0);
        imageView.setTranslateX(0);
        imageView.setTranslateY(0);
        imageView.setScaleX(1);
        imageView.setScaleY(1);
    }

    private double calculateMouseXAfterZoom(ScrollEvent scrollEvent, double scale){
        double imageWidth = imageView.getBoundsInParent().getMaxX() - imageView.getBoundsInParent().getMinX();
        double imageCenterX = (imageView.getBoundsInParent().getMinX() + imageWidth/2);
        double mouseDeltaXFromCenter = scrollEvent.getX() - imageCenterX;

        return mouseDeltaXFromCenter/imageView.getScaleX() * scale;
    }

    private double calculateMouseYAfterZoom(ScrollEvent scrollEvent, double scale){
        double imageHeight = imageView.getBoundsInParent().getMaxY() - imageView.getBoundsInParent().getMinY();
        double imageCenterY = (imageView.getBoundsInParent().getMinY() + imageHeight/2);
        double mouseDeltaYFromCenter = scrollEvent.getY() - imageCenterY;

        return mouseDeltaYFromCenter/imageView.getScaleY() * scale;
    }
    private void adjustMouseDelta(ScrollEvent scrollEvent, double TargetMouseDeltaX, double TargetMouseDeltaY) {
        double ImageWidth = imageView.getBoundsInParent().getMaxX() - imageView.getBoundsInParent().getMinX();
        double currentMouseDeltaX = scrollEvent.getX() - (imageView.getBoundsInParent().getMinX() + ImageWidth/2);
        double mouseTranslateX = currentMouseDeltaX - TargetMouseDeltaX;
        imageView.setX(imageView.getX() + mouseTranslateX);

        double ImageHeight = imageView.getBoundsInParent().getMaxY() - imageView.getBoundsInParent().getMinY();
        double currentMouseDeltaY = scrollEvent.getY() - (imageView.getBoundsInParent().getMinY() + ImageHeight/2);
        double mouseTranslateY = currentMouseDeltaY - TargetMouseDeltaY;
        imageView.setY(imageView.getY() + mouseTranslateY);
    }
    private static double clamp(double val, double min, double max) { return  Math.max(min, Math.min(max, val)); }
    private static int clamp(int val, int min, int max) { return  Math.max(min, Math.min(max, val)); }
}
