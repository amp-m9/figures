package xyz.andrick.figures;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;


public class SessionSceneController {
    private double startDragX, startDragY;
    private int imageIndex = 0;
    private SessionSettings settings;
    private ObservableTimer timer;
    private SimpleDoubleProperty timerArcLength;
    private ChangeListener<Number> timeListener;
    private Image colorImage;
    private WritableImage greyScaleImage;
    private Thread greyScaleThread;
    private Line[] verticalGridLines;
    private Line[] horizontalGridLines;
    @FXML
    private AnchorPane imageAnchorPane;
    @FXML
    private AnchorPane baseAnchorPane;
    @FXML
    private AnchorPane breakAnchorPane;
    @FXML
    private AnchorPane pauseAnchorPane;
    @FXML
    private ImageView imageView;
    @FXML
    private Button quitButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;
    @FXML
    private Button breakQuitButton;
    @FXML
    private Button breakResumeButton;
    @FXML
    private Button pauseQuitButton;
    @FXML
    private Button pauseResumeButton;
    @FXML
    private ToggleButton greyscaleButton;
    @FXML
    private ToggleButton gridButton;
    @FXML
    private Label breakTimeLabel;
    @FXML
    private Label pauseTimeLabel;
    @FXML
    private Label sessionProgressLabel;
    @FXML
    private Text pauseText;
    @FXML
    private Text breakText;
    @FXML
    private Arc timerArc;
    @FXML
    private SVGPath grayScaleSVG;
    @FXML
    private AnchorPane grayScaleButtonAnchorPane;
    @FXML
    public void initialize() {
        breakAnchorPane.setVisible(false);
        pauseAnchorPane.setVisible(false);
        timerArcLength = new SimpleDoubleProperty(360.0f);
        timerArc.lengthProperty().bind(timerArcLength);
        setUpImageView();
        setUpButtons();
        setUpGridLines();
        setUpToolTips();
    }

    private void setUpToolTips() {
        Tooltip grayscaleToolTip = new Tooltip("Toggle grayscale");
        Tooltip gridToolTip = new Tooltip("Toggle grid");
        greyscaleButton.setTooltip(grayscaleToolTip);
        gridButton.setTooltip(gridToolTip);
    }

    private void setUpButtons() {
        quitButton.setOnAction(event -> quitToSettings());
        nextButton.setOnAction(event -> nextImage());
        pauseButton.setOnAction(event -> pauseSession());
        previousButton.setOnAction(event -> previousImage());
        breakResumeButton.setOnAction(event -> resumeFromBreak());
        breakQuitButton.setOnAction(event -> {
            resumeFromBreak();
            quitToSettings();
        });

        pauseResumeButton.setOnAction(event -> resumeFromPause());
        pauseQuitButton.setOnAction(event -> {
            resumeFromPause();
            quitToSettings();
        });

        greyscaleButton.selectedProperty().addListener((obsVal, oldVal, newVal) -> toggleGreyscale(newVal));
        gridButton.selectedProperty().addListener((obsVal, oldVal, newVal) -> toggleGrid(newVal));
    }

    private void toggleGrid(Boolean selected) {
        for (Line line : horizontalGridLines) {
            line.setVisible(selected);
        }
        for (Line line : verticalGridLines) {
            line.setVisible(selected);
        }
    }

    private void setUpGridLines() {
        verticalGridLines = new Line[]{new Line(), new Line()};
        horizontalGridLines = new Line[]{new Line(), new Line()};

        addLineArrayToAnchorPane(horizontalGridLines);
        addLineArrayToAnchorPane(verticalGridLines);

        imageAnchorPane.heightProperty().addListener(observable -> updateGridLines());
        imageAnchorPane.widthProperty().addListener(observable -> updateGridLines());
    }

    private void addLineArrayToAnchorPane(Line[] lines) {
        for (Line line : lines) {
            line.setFill(Paint.valueOf("#ffffff"));
            line.setStroke(Paint.valueOf("#ffffff"));
            line.setBlendMode(BlendMode.DIFFERENCE);
            line.setVisible(false);
            imageAnchorPane.getChildren().add(line);
        }
    }

    public void updateGridLines(){
        if (imageAnchorPane == null)
            return;

        double width = imageAnchorPane.getWidth();
        double height = imageAnchorPane.getHeight();

        for (int i = 0; i< horizontalGridLines.length; i++){
            horizontalGridLines[i].setStartX(0);
            horizontalGridLines[i].setEndX(width);

            double yCord = (height / 3) + (height / 3) * i;
            horizontalGridLines[i].setStartY(yCord);
            horizontalGridLines[i].setEndY(yCord);
        }

        for (int i = 0; i< verticalGridLines.length; i++){
            verticalGridLines[i]. setStartY(0);
            verticalGridLines[i]. setEndY(height);

            double xCord = (width / 3) + (width / 3) * i;
            verticalGridLines[i].setStartX(xCord);
            verticalGridLines[i].setEndX(xCord);
        }
    }

    private void toggleGreyscale(boolean selected) {
        if (selected) {
            try{
                greyScaleThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            imageView.setImage(greyScaleImage);
            return;
        }
        imageView.setImage(colorImage);
    }

    private void setUpImageView() {
        imageView.setPreserveRatio(true);
        imageView.fitHeightProperty().bind(imageAnchorPane.heightProperty());
        imageView.fitWidthProperty().bind(imageAnchorPane.widthProperty());
        imageAnchorPane.widthProperty().addListener((obs, oldVal, newVal) -> keepImageInFrame());
        imageAnchorPane.heightProperty().addListener((obs, oldVal, newVal) -> keepImageInFrame());
        imageAnchorPane.setOnMousePressed(this::onMousePressed);
        imageAnchorPane.setOnMouseDragged(this::onMouseDragged);
        imageAnchorPane.setOnScroll(this::zoomOnScroll);
    }

    private void pauseSession() {
        timer.pause();
        int timeInSeconds = (int) (settings.imageTimeMillis() / 1000);
        int secondsElapsed = timer.timeElapsedProperty().intValue() / 1000;
        int minutesLeft = (int) Math.floor((double) (timeInSeconds - secondsElapsed) / 60);
        int secondsLeft = (timeInSeconds - secondsElapsed) % 60;
        pauseTimeLabel.setText("Time left on image: %02d:%02d".formatted(minutesLeft, secondsLeft));
        pauseAnchorPane.setVisible(true);
    }

    private void resumeFromPause() {
        pauseAnchorPane.setVisible(false);
        timer.resume();
    }

    public void setupSession(SessionSettings _settings) {
        imageIndex = 0;
        settings = _settings;
        settings.stage().setResizable(true);
        displayImage(imageIndex);
        setUpTimers();
    }

    private void setUpTimers() {
        Stage stage = (Stage) (imageView.getScene().getWindow());
        timer = new ObservableTimer(settings.imageTimeMillis(), -1, this::onTick);
        timer.start();
        timeListener = createSessionTimerListener();
        timer.timeElapsedProperty().addListener(timeListener);
        stage.setOnCloseRequest(windowEvent -> timer.stop());
    }

    private ChangeListener<Number> createSessionTimerListener() {
        return (observableValue, o, t1) -> {
            double fraction = (double) t1.intValue() / settings.imageTimeMillis();
            fraction = clamp(fraction, 0, 1);
            int length = (int) (fraction * 360);
            Platform.runLater(() -> timerArcLength.set(length));
        };
    }

    public void nextImage() {
        timer.stop();
        int endOfSources = settings.imageSources().length - 1;

        if (imageIndex == endOfSources) {
            Platform.runLater(this::quitToSettings);
            return;
        }

        if ((imageIndex + 1) % settings.picturesBetweenBreaks() == 0)
            beginBreak();

        imageIndex++;
        imageIndex = clampFromZero(imageIndex, settings.imageSources().length);
        displayImage(imageIndex);
        timer.start();
    }

    public void previousImage() {
        timer.stop();
        imageIndex--;
        imageIndex = clampFromZero(imageIndex, settings.imageSources().length);
        displayImage(imageIndex);
        timer.start();
    }

    public void displayImage(int index) {

        String fileString = settings.imageSources()[index % settings.imageSources().length];
        File imageFile = new File(fileString);
        if (!imageFile.exists()) {
            printImageError(fileString);
            return;
        }
        try {
            String imageURL = imageFile.toURI().toURL().toExternalForm();
            colorImage = new Image(imageURL);

            if (greyScaleThread != null)
                greyScaleThread.interrupt();

            // load greyscale on separate thread to not halt app. unless they wish to show greyscale image.
            greyScaleThread = new Thread(() -> makeGreyScaleCopy(colorImage));
            greyScaleThread.start();
            
            imageView.setImage(colorImage);
            Platform.runLater(() -> sessionProgressLabel.setText("%d / %d".formatted(index + 1, settings.imageSources().length)));
            resetImage();
            keepImageInFrame();
            greyscaleButton.setSelected(false);
        } catch (MalformedURLException e) {
            printImageError(fileString);
        }
    }

    private void makeGreyScaleCopy(Image image) {
        greyScaleImage = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = greyScaleImage.getPixelWriter();
        PixelReader pixelReader = greyScaleImage.getPixelReader();
        for (int i = 0; i < greyScaleImage.getHeight(); i++) {
            for (int j = 0; j < greyScaleImage.getWidth(); j++) {
                Color c = pixelReader.getColor(j, i);
                double grayVal = (c.getRed() * 0.3 + c.getGreen() * 0.59 + c.getBlue() * 0.11);
                Color greyColor = new Color(grayVal, grayVal, grayVal, c.getOpacity());
                pixelWriter.setColor(j, i, greyColor);
            }
        }
    }


    public void onMousePressed(MouseEvent mouseEvent) {
        startDragX = mouseEvent.getSceneX();
        startDragY = mouseEvent.getSceneY();
    }

    public void onMouseDragged(MouseEvent mouseEvent) {
        imageView.setX(imageView.getX() + (mouseEvent.getSceneX() - startDragX));
        imageView.setY(imageView.getY() + (mouseEvent.getSceneY() - startDragY));
        keepImageInFrame();
        startDragX = mouseEvent.getSceneX();
        startDragY = mouseEvent.getSceneY();
    }

    public void zoomOnScroll(ScrollEvent scrollEvent) {
        Integer direction = (int) (scrollEvent.getDeltaY());
        if (direction == 0) {
            return;
        }

        direction = direction.compareTo(0);
        double maxZoom = 5;
        if ((direction > 0 && imageView.getScaleX() >= maxZoom) || (direction < 0 && imageView.getScaleY() <= 1)) {
            return;
        }

        double scaleFactor = (float) (direction * .08);
        double newScale = clamp((imageView.getScaleX()) + scaleFactor, 1, maxZoom);

        double TargetCursorDeltaX = calculateMouseXAfterZoom(scrollEvent, newScale);
        double TargetMouseDeltaY = calculateMouseYAfterZoom(scrollEvent, newScale);

        imageView.setScaleX(newScale);
        imageView.setScaleY(newScale);

        adjustMouseDelta(scrollEvent, TargetCursorDeltaX, TargetMouseDeltaY);
        keepImageInFrame();
    }

    private void onTick() {
        nextImage();
    }

    private void beginBreak() {
        timer.timeElapsedProperty().removeListener(timeListener);
        timer.stop();
        breakAnchorPane.setVisible(true);
        timer.setTimeOutFunction(this::resumeFromBreak);
        timer.setInterval(settings.breakTimeMillis());
        timeListener = createBreakTimerListener();
        timer.timeElapsedProperty().addListener(timeListener);
        timer.start();
    }

    private ChangeListener<Number> createBreakTimerListener() {
        return (observableValue, number, t1) -> {
            int secondsElapsed = t1.intValue() / 1000;
            int breakInSeconds = (int) (settings.breakTimeMillis() / 1000);
            int minutesLeft = (int) Math.floor((double) (breakInSeconds - secondsElapsed) / 60);
            int secondsLeft = (breakInSeconds - secondsElapsed) % 60;
            Platform.runLater(() -> breakTimeLabel.setText("Time remaining: %02d:%02d".formatted(minutesLeft, secondsLeft)));
        };
    }

    private void resumeFromBreak() {
        timer.stop();
        timer.setTimeOutFunction(this::onTick);
        timer.setInterval(settings.imageTimeMillis());
        timer.timeElapsedProperty().removeListener(timeListener);
        timeListener = createSessionTimerListener();
        timer.timeElapsedProperty().addListener(timeListener);
        breakAnchorPane.setVisible(false);
        timer.start();
    }

    private void printImageError(String fileString) {
        System.out.printf("Cannot display image %s%n", fileString);
    }

    private void quitToSettings() {
        breakAnchorPane.setVisible(false);
        timer.stop();
        Stage stage = settings.stage();
        stage.setScene(settings.previousScene());
        stage.setTitle("Figures: Create a session");
        stage.show();
    }

    private void keepImageInFrame() {
        double imageHeight = imageView.getBoundsInParent().getHeight();
        double imageWidth = imageView.getBoundsInParent().getWidth();

        double LowerBoundX = Math.max((imageAnchorPane.getWidth() - imageWidth) / 2, 0);
        double UpperBoundX = Math.min(imageAnchorPane.getWidth(), (imageAnchorPane.getWidth() + imageWidth) / 2);
        double LowerBoundY = Math.max((imageAnchorPane.getHeight() - imageHeight) / 2, 0);
        double UpperBoundY = Math.min(imageAnchorPane.getHeight(), (imageAnchorPane.getHeight() + imageHeight) / 2);

        double imageMinY = imageView.getBoundsInParent().getMinY(); // top
        double imageMaxY = imageView.getBoundsInParent().getMaxY(); // bottom
        double imageMinX = imageView.getBoundsInParent().getMinX(); // lhs
        double imageMaxX = imageView.getBoundsInParent().getMaxX(); // rhs

        if (imageMinY > LowerBoundY)
            imageView.setY(imageView.getY() - (imageMinY - LowerBoundY));

        if (imageMaxY < UpperBoundY)
            imageView.setY(imageView.getY() + (UpperBoundY - imageMaxY));

        if (imageMinX > LowerBoundX)
            imageView.setX(imageView.getX() - (imageMinX - LowerBoundX));

        if (imageMaxX < UpperBoundX)
            imageView.setX(imageView.getX() + (UpperBoundX - imageMaxX));
    }

    private void resetImage() {
        imageView.setX(0);
        imageView.setY(0);
        imageView.setTranslateX(0);
        imageView.setTranslateY(0);
        imageView.setScaleX(1);
        imageView.setScaleY(1);
    }

    private double calculateMouseXAfterZoom(ScrollEvent scrollEvent, double scale) {
        double imageWidth = imageView.getBoundsInParent().getMaxX() - imageView.getBoundsInParent().getMinX();
        double imageCenterX = (imageView.getBoundsInParent().getMinX() + imageWidth / 2);
        double mouseDeltaXFromCenter = scrollEvent.getX() - imageCenterX;

        return mouseDeltaXFromCenter / imageView.getScaleX() * scale;
    }

    private double calculateMouseYAfterZoom(ScrollEvent scrollEvent, double scale) {
        double imageHeight = imageView.getBoundsInParent().getMaxY() - imageView.getBoundsInParent().getMinY();
        double imageCenterY = (imageView.getBoundsInParent().getMinY() + imageHeight / 2);
        double mouseDeltaYFromCenter = scrollEvent.getY() - imageCenterY;

        return mouseDeltaYFromCenter / imageView.getScaleY() * scale;
    }

    private void adjustMouseDelta(ScrollEvent scrollEvent, double TargetMouseDeltaX, double TargetMouseDeltaY) {
        double ImageWidth = imageView.getBoundsInParent().getMaxX() - imageView.getBoundsInParent().getMinX();
        double currentMouseDeltaX = scrollEvent.getX() - (imageView.getBoundsInParent().getMinX() + ImageWidth / 2);
        double mouseTranslateX = currentMouseDeltaX - TargetMouseDeltaX;
        imageView.setX(imageView.getX() + mouseTranslateX);

        double ImageHeight = imageView.getBoundsInParent().getMaxY() - imageView.getBoundsInParent().getMinY();
        double currentMouseDeltaY = scrollEvent.getY() - (imageView.getBoundsInParent().getMinY() + ImageHeight / 2);
        double mouseTranslateY = currentMouseDeltaY - TargetMouseDeltaY;
        imageView.setY(imageView.getY() + mouseTranslateY);
    }

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    private static int clampFromZero(int val, int max) {
        return Math.max(0, Math.min(max, val));
    }
}
