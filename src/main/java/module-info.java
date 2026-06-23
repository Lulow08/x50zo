module com.trifasico.x50zo {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.trifasico.x50zo to javafx.fxml;
    opens com.trifasico.x50zo.controller to javafx.fxml;
    exports com.trifasico.x50zo;
}