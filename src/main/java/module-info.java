module xyz.andrick.figures {
    requires javafx.controls;
    requires javafx.fxml;
    requires net.synedra.validatorfx;
    requires java.desktop;
    opens xyz.andrick.figures to javafx.fxml;
    exports xyz.andrick.figures;
}