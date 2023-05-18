package xyz.andrick.figures;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.synedra.validatorfx.Validator;
//import org.apache.commons.lang3.math.NumberUtils;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class SessionSettingsSceneController {
    private Stage stage;
    private Validator validator = new Validator();


    @FXML
    private Button startSessionButton;
    @FXML
    private TextField imageDirectoryTextField;
    @FXML
    private Label filesFoundLabel;
    @FXML
    private Spinner<Double> imageDurationSpinner;
    @FXML
    private Spinner<Double> breakDurationSpinner;
    @FXML
    private Spinner<Integer> imagesBetweenBreaksSpinner;

    @FXML
    private ToggleGroup imageTimeToggleGroup;
    @FXML
    private ToggleGroup breakTimeToggleGroup;

    @FXML
    public void initialize()
    {
        directoryValidation();
        setUpDoubleSpinner(imageDurationSpinner);
        setUpDoubleSpinner(breakDurationSpinner);
        setUpIntegerSpinner(imagesBetweenBreaksSpinner);
    }

    private void setUpIntegerSpinner(Spinner<Integer> spinner) {
        spinner.setValueFactory( new SpinnerValueFactory.IntegerSpinnerValueFactory(0,Integer.MAX_VALUE,10, 1));

        validator.createCheck()
                .dependsOn("Duration", spinner.getEditor().textProperty())
                .withMethod(c -> {
                    String duration = c.get("Duration");
                    if(!isNaturalNumber(duration))
                        c.error("Number is invalid\n - Must be a integer > 1");
                    allFieldsValid();
                })
                .decorates(spinner)
                .immediate();

        spinner.valueProperty().addListener((observableValue, integer, t1) ->{
            if(t1 == null)
                spinner.getValueFactory().setValue(0);
        });

        spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d*")){
                spinner.getEditor().setText("1");
            }
        });

    }


    private void setUpDoubleSpinner(Spinner<Double> spinner)
    {
        spinner.setValueFactory( new SpinnerValueFactory.DoubleSpinnerValueFactory(0,Double.MAX_VALUE,30, 5));

        validator.createCheck()
                .dependsOn("Duration", spinner.getEditor().textProperty())
                .withMethod(c -> {
                    String duration = c.get("Duration");
                    if(!isDouble(duration))
                        c.error("Number is invalid");
                    allFieldsValid();
                })
                .decorates(spinner)
                .immediate();

        ChangeListener<Double> Listener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                if(t1 == null)
                    spinner.getValueFactory().setValue(0d);
            }
        };

        spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.toLowerCase().matches("^(\\d+)*.?\\d*$") || !isDouble(newValue)){
                spinner.getEditor().setText(oldValue);
                allFieldsValid();
                return;
            }
            if(newValue.equals(".")){
                spinner.getEditor().setText("0.");
                return;
            }
        });
    }

    public void doubleSpinnerTextSanitiser(ObservableValue<? extends String> observable, String oldValue, String newValue){

    }

    private void directoryValidation()
    {
        validator.createCheck()
                .dependsOn("DirectoryField", imageDirectoryTextField.textProperty())
                .withMethod(c -> {
                    if(!doesDirectoryExist(c.get("DirectoryField")))
                    {
                        c.error("Directory is invalid.");
                    }
                    allFieldsValid();
                })
                .decorates(imageDirectoryTextField)
                .immediate();

//        ChangeListener<String> directoryListener = new ChangeListener<String>() {
//            @Override
//            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
//                onImageDirectoryChanged(t1);
//            }
//        };

        imageDirectoryTextField.textProperty().addListener((observableValue, s, t1) -> onImageDirectoryChanged(t1));
        onImageDirectoryChanged(imageDirectoryTextField.getText());
    }


    public void onStartPressed(ActionEvent event) throws IOException {
        if(!allFieldsValid())
        {
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("session-image.fxml"));
        Parent root = loader.load();

        ActiveSessionController SessionController = loader.getController();

        SessionSettings settings = new SessionSettings(
                imageDurationSpinner.getValue(),
                breakDurationSpinner.getValue(),
                imageDirectoryTextField.getText(),
                imagesBetweenBreaksSpinner.getValue(),
                getImagesInDirectory(imageDirectoryTextField.getText()),
                ((Node)event.getSource()).getScene()
        );


        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        SessionController.initialiseSettings(settings);

    }

    private boolean allFieldsValid()
    {
        boolean validDirectory = doesDirectoryExist(imageDirectoryTextField.getText());
        boolean validImageDuration = isDouble(imageDurationSpinner.getEditor().getText());
        boolean validBreakDuration = isDouble(breakDurationSpinner.getEditor().getText());
        boolean validBreak = isNaturalNumber(imagesBetweenBreaksSpinner.getEditor().getText());

        boolean all = validDirectory && validImageDuration && validBreakDuration &&validBreak;
        
        if(all)
            startSessionButton.setDisable(false);
        else
            startSessionButton.setDisable(true);

        return validDirectory && validImageDuration && validBreakDuration &&validBreak;
    }

    private boolean isNaturalNumber( String number) {
        try {
            Integer value = Integer.parseInt(number);
            if(value == null || value<1 )
                return false;

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isDouble(String number) {
        try {
            Double value = Double.parseDouble(number);
            //noinspection MalformedFormatString
            return !value.isNaN();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void onImageDirectoryChanged(String newDirectory)
    {
        if(!doesDirectoryExist(newDirectory))
        {
            filesFoundLabel.setText("Directory is invalid");
            filesFoundLabel.getStylesheets().removeIf(Predicate.isEqual("style for green text"));
            if( !filesFoundLabel.getStylesheets().contains("some style for red text"));
                filesFoundLabel.getStylesheets().add("some style for red text");
            return;
        }
        String imagesFoundString = "%d images found in directory".formatted(getImagesInDirectory(newDirectory).length);
        filesFoundLabel.setText(imagesFoundString);

    }

    public void onBrowseButtonPress(ActionEvent event)
    {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if(selectedDirectory == null)
            return;
        imageDirectoryTextField.setText(selectedDirectory.getAbsolutePath());
    }


    private boolean doesDirectoryExist(String directory) { return new File(directory).exists(); }

    private String[] getImagesInDirectory(String folder){ return getImagesInDirectory(new File(folder)); }

    private String[] getImagesInDirectory(File Directory)
    {
        if(!Directory.exists())
            return new String[]{};

        String[] fileList = Directory.list(new FilenameFilter() {
            static final String regex = "([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)";
            Pattern pattern = Pattern.compile(regex);

            @Override
            public boolean accept(File dir, String name) {
                return pattern.matcher(name.toLowerCase()).find();
            }
        });

        return fileList;
    }
}