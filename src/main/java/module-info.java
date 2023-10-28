module xyz.andrick.figures {
    requires javafx.controls;
    requires javafx.fxml;
    requires net.synedra.validatorfx;
    requires java.desktop;
    opens xyz.andrick.figures to javafx.fxml;
    exports xyz.andrick.figures;
    exports xyz.andrick.figures.controllers;
    opens xyz.andrick.figures.controllers to javafx.fxml;
    exports xyz.andrick.figures.records;
    opens xyz.andrick.figures.records to javafx.fxml;
    exports xyz.andrick.figures.utilities;
    opens xyz.andrick.figures.utilities to javafx.fxml;
}