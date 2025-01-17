module com.artoch.tlm_app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.apache.commons.lang3;

    opens com.artoch.tlm_app to javafx.fxml;
    exports com.artoch.tlm_app;
}