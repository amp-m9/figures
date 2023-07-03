package xyz.andrick.figures;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.synedra.validatorfx.Check;
import net.synedra.validatorfx.Validator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SettingsSceneController implements Initializable {
    private Stage stage;
    private Scene sessionScene;
    private SessionSceneController sessionSceneController;
    private final Validator validator = new Validator();
    private String toolTipPathString = "M61 122C94.6894 122 122 94.6894 122 61C122 27.3106 94.6894 0 61 0C27.3106 0 0 27.3106 0 61C0 94.6894 27.3106 122 61 122ZM52.746 80.8551V81.4091H63.8255V80.8551C63.8871 77.0388 64.2872 73.8535 65.0258 71.299C65.7645 68.7446 66.9494 66.544 68.5805 64.6974C70.2117 62.8201 72.3814 61.035 75.0898 59.3423C77.7981 57.6804 80.1063 55.7723 82.0145 53.6179C83.9534 51.4636 85.4307 49.0168 86.4463 46.2777C87.4927 43.5386 88.0159 40.4763 88.0159 37.0909C88.0159 32.2898 86.9079 28.0118 84.692 24.2571C82.5069 20.5024 79.3985 17.5478 75.3667 15.3934C71.3658 13.2391 66.657 12.1619 61.2403 12.1619C56.2545 12.1619 51.715 13.1468 47.6217 15.1165C43.5592 17.0862 40.2815 19.933 37.7886 23.657C35.3265 27.3809 33.9723 31.8589 33.7261 37.0909H45.3596C45.6059 33.4593 46.5138 30.5201 48.0834 28.2734C49.653 26.0267 51.6073 24.3802 53.9463 23.3338C56.2853 22.2874 58.7166 21.7642 61.2403 21.7642C64.1333 21.7642 66.7647 22.3643 69.1345 23.5646C71.5043 24.7649 73.397 26.473 74.8128 28.6889C76.2285 30.9048 76.9363 33.5208 76.9363 36.5369C76.9363 38.9683 76.5055 41.1842 75.6437 43.1846C74.8128 45.1851 73.674 46.9548 72.2275 48.4936C70.781 50.0016 69.1499 51.3096 67.3341 52.4176C64.318 54.2334 61.7327 56.2185 59.5784 58.3729C57.424 60.5272 55.7621 63.3433 54.5926 66.821C53.4231 70.2988 52.8076 74.9768 52.746 80.8551ZM52.7922 106.292C54.4233 107.923 56.3776 108.739 58.6551 108.739C60.1939 108.739 61.5789 108.369 62.8099 107.631C64.0718 106.861 65.072 105.846 65.8106 104.584C66.58 103.322 66.9648 101.937 66.9648 100.429C66.9648 98.1515 66.1492 96.1972 64.518 94.566C62.8869 92.9349 60.9326 92.1193 58.6551 92.1193C56.3776 92.1193 54.4233 92.9349 52.7922 94.566C51.161 96.1972 50.3454 98.1515 50.3454 100.429C50.3454 102.706 51.161 104.661 52.7922 106.292Z";
    @FXML
    private Button startSessionButton;
    @FXML
    private Button browseButton;
    @FXML
    private TextField imageDirectoryTextField;
    @FXML
    private Label shuffleLabel;
    @FXML
    private Label filesFoundLabel;
    @FXML
    private Label subDirectoryLabel;
    @FXML
    private Spinner<Double> imageDurationSpinner;
    @FXML
    private Spinner<Double> breakDurationSpinner;
    @FXML
    private Spinner<Integer> imagesBetweenBreaksSpinner;
    @FXML
    private Spinner<Integer> figureCountSpinner;
    @FXML
    private ToggleButton subDirectoryToggle;
    @FXML
    private ToggleButton breakMinutesToggle;
    @FXML
    private ToggleButton imageMinutesToggle;
    @FXML
    private ToggleButton shuffleButton;
    @FXML
    private ToggleGroup breakTimeToggleGroup;
    @FXML
    private ToggleGroup imageTimeToggleGroup;
    @FXML
    private AnchorPane directoryToolTip;
    @FXML
    private AnchorPane figuresBetweenBreaksToolTip;
    @FXML
    private AnchorPane figureCountToolTip;
    private SVGPath toolTipSvg;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        directoryValidation();
        setUpDoubleSpinner(imageDurationSpinner, 1);
        setUpDoubleSpinner(breakDurationSpinner, 5);
        setUpIntegerSpinner(imagesBetweenBreaksSpinner, 10);
        setUpIntegerSpinner(figureCountSpinner, 20);
        setUpToggles();
        setUpBrowseButton();
        setUpToolTips();
    }

    private void setUpToolTips() {
        toolTipSvg = new SVGPath();
        toolTipSvg.setContent(toolTipPathString);
        toolTipSvg.setFillRule(FillRule.EVEN_ODD);

        String directoryMessage = "Enter the path for a directory containing the images.\n" +
                "Figures supports the following image types:" +
                "\n\t- jpg/jpeg" +
                "\n\t- png" +
                "\n\t- bmp";

        String figureCountMessage = "Total images for this session";
        String figuresBetweenBreaksMessage = "How many images between breaks";

        setAnchorPaneToolTip(directoryToolTip, directoryMessage);
        setAnchorPaneToolTip(figureCountToolTip, figureCountMessage);
        setAnchorPaneToolTip(figuresBetweenBreaksToolTip, figuresBetweenBreaksMessage);
    }

    private void setAnchorPaneToolTip(AnchorPane pane, String message) {
        pane.shapeProperty().set(toolTipSvg);
        Tooltip tooltip = new Tooltip(message);
        Tooltip.install(pane, tooltip);
    }


    private void setUpBrowseButton() {
        browseButton.setOnAction(this::onBrowseButtonPress);
        startSessionButton.setOnAction(event -> {
            try {
                onStartPressed(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setUpToggles() {
        breakTimeToggleGroup.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null)
                oldVal.setSelected(true);
        });

        imageTimeToggleGroup.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null)
                oldVal.setSelected(true);
        });

        subDirectoryToggle.selectedProperty().addListener(((obsVal, oldVal, newVal) -> Platform.runLater(()-> {
            subDirectoryLabel.setText(newVal ? "on" : "off");
            onImageDirectoryChanged(imageDirectoryTextField.getText());
            allFieldsValid();
        })));
        subDirectoryToggle.setSelected(false);
        subDirectoryLabel.setText("off");

        shuffleButton.selectedProperty().addListener(((obsVal, oldVal, newVal) -> Platform.runLater(()->shuffleLabel.setText(newVal?"shuffle on":""))));
        shuffleButton.setSelected(true);

    }

    private void setUpIntegerSpinner(Spinner<Integer> spinner, int initialValue) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, initialValue, 1));

        validator.createCheck()
                .dependsOn("Duration", spinner.getEditor().textProperty())
                .withMethod(checkIntegerIsValid())
                .decorates(spinner)
                .immediate();
    }

    private Consumer<Check.Context> checkIntegerIsValid() {
        return c -> {
            String duration = c.get("Duration");
            if (!isValidNaturalNumber(duration))
                c.error("Number is invalid\n - Must be a whole number greater than 1");
            allFieldsValid();
        };
    }


    private void setUpDoubleSpinner(Spinner<Double> spinner, int initialValue) {
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, initialValue, 1));

        validator.createCheck()
                .dependsOn("Duration", spinner.getEditor().textProperty())
                .withMethod(checkDoubleIsValid())
                .decorates(spinner)
                .immediate();

        spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(".")) {
                spinner.getEditor().setText("0.");
            }
        });
    }

    private Consumer<Check.Context> checkDoubleIsValid() {
        return c -> {
            String duration = c.get("Duration");
            if (!isValidDouble(duration))
                c.error("Entry is invalid\n - Must be a number >0");

            allFieldsValid();
        };
    }

    private void directoryValidation() {
        validator.createCheck()
                .dependsOn("DirectoryField", imageDirectoryTextField.textProperty())
                .withMethod(checkDirectoryIsValid())
                .decorates(imageDirectoryTextField)
                .immediate();

        imageDirectoryTextField.textProperty().addListener((observableValue, s, t1) -> onImageDirectoryChanged(t1));
        onImageDirectoryChanged(imageDirectoryTextField.getText());
    }

    private Consumer<Check.Context> checkDirectoryIsValid() {
        return c -> {
            if (!doesDirectoryExist(c.get("DirectoryField"))) {
                c.error("Directory is invalid.");
            }
            allFieldsValid();
        };
    }


    public void onStartPressed(ActionEvent event) throws IOException {
        if (!allFieldsValid())
            return;
        if(sessionScene == null)
            setupSessionSceneAndController();

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(sessionScene);

        long breakDurationSpinnerValue = (long) Math.floor(breakDurationSpinner.getValue() * 1000);
        if(breakMinutesToggle.isSelected())
            breakDurationSpinnerValue = breakDurationSpinnerValue*60;

        long imageDurationSpinnerValue = (long) Math.floor(imageDurationSpinner.getValue() * 1000);
        if(imageMinutesToggle.isSelected())
            imageDurationSpinnerValue = imageDurationSpinnerValue*60;

        SessionSettings settings = new SessionSettings(
                imageDurationSpinnerValue,
                breakDurationSpinnerValue,
                imagesBetweenBreaksSpinner.getValue(),
                getFilePaths(),
                ((Node) event.getSource()).getScene(),
                stage
        );

        sessionSceneController.setupSession(settings);
        stage.setTitle("Figures: Drawing session");
        stage.show();
    }

    private String[] getFilePaths() {
        Integer maxFigures = figureCountSpinner.getValue();
        String[] filePathArray = new String[maxFigures];
        List<String> filePathList = getImagesInDirectory(imageDirectoryTextField.getText());

        if (!shuffleButton.isSelected()) {
            System.arraycopy(filePathList.toArray(new String[0]), 0, filePathArray, 0, maxFigures);
            return filePathArray;
        }

        Collections.shuffle(filePathList);
        filePathList.subList(0, maxFigures).toArray(filePathArray);
        return filePathArray;
    }

    private void setupSessionSceneAndController() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("session-view.fxml"));
        Parent root = loader.load();
        sessionScene = new Scene(root);
        sessionScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("session.css")).toExternalForm());

        if (sessionSceneController != loader.getController() ){
            sessionSceneController = loader.getController();
        }

        assert sessionScene!=null;
        assert sessionSceneController!=null;
    }

    private boolean allFieldsValid() {
        boolean validDirectory = doesDirectoryExist(imageDirectoryTextField.getText()) && getImagesInDirectory(imageDirectoryTextField.getText()).size()>0;
        boolean validImageDuration = isDouble(imageDurationSpinner.getEditor().getText());
        boolean validBreakDuration = isDouble(breakDurationSpinner.getEditor().getText());
        boolean validBreak = isValidNaturalNumber(imagesBetweenBreaksSpinner.getEditor().getText());
        boolean validFigureCount = isValidNaturalNumber(figureCountSpinner.getEditor().getText());
        boolean allValid = validDirectory && validImageDuration && validBreakDuration && validBreak && validFigureCount;
        startSessionButton.setDisable(!allValid);

        return allValid;
    }

    private boolean isValidNaturalNumber(String number) {
        try {
            int value = Integer.parseInt(number);
            return value >= 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidDouble(String number) {
        try{
            double value = Double.parseDouble(number);
            return value>0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private boolean isDouble(String number) {
        try {
            double value = Double.parseDouble(number);
            return !Double.isNaN(value);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void onImageDirectoryChanged(String newDirectory) {
        if (!doesDirectoryExist(newDirectory)) {
            filesFoundLabel.setText("Directory is invalid");
            return;
        }

        int imageCount = getImagesInDirectory(newDirectory).size();
        String imagesFoundString = "%d images found in directory".formatted(imageCount);
        filesFoundLabel.setText(imagesFoundString);

        if(imageCount<1) {
            return;
        }

        figureCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, imageCount, imageCount, 1));

        int imagesBetween = imagesBetweenBreaksSpinner.getValue()>imageCount? imageCount: imagesBetweenBreaksSpinner.getValue();
        imagesBetweenBreaksSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, imageCount, imagesBetween, 1));

    }

    public void onBrowseButtonPress(ActionEvent event) {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory == null)
            return;
        imageDirectoryTextField.setText(selectedDirectory.getAbsolutePath());
    }


    private boolean doesDirectoryExist(String directory) {
        return new File(directory).exists();
    }

    private List<String> getImagesInDirectory(String folder) {
        try {
            return getImagesInDirectory(new File(folder));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private List<String> getImagesInDirectory(File Directory) throws IOException {
        if (!Directory.exists())
            return new ArrayList<>();

        List<String> filePaths = new ArrayList<>();
        String regex = "([^\\s]+(\\.(?i)(jpg|png|bmp|jpeg))$)";
        Stream<Path> walkStream;

        if(subDirectoryToggle.selectedProperty().get())
            walkStream = Files.walk(Directory.toPath());
        else
            walkStream = Files.walk(Directory.toPath(), 1);

        walkStream.filter(p -> p.toFile().isFile())
            .forEach(f -> {
                if (f.toString().matches(regex)) {
                    filePaths.add(f.toString());
                }
            });
        return  filePaths;
    }


}