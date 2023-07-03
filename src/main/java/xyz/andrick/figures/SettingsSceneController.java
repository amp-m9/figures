package xyz.andrick.figures;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        directoryValidation();
        setUpDoubleSpinner(imageDurationSpinner, 1);
        setUpDoubleSpinner(breakDurationSpinner, 5);
        setUpIntegerSpinner(imagesBetweenBreaksSpinner, 10);
        setUpIntegerSpinner(figureCountSpinner, 20);
        setUpToggles();
        setUpBrowseButton();
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