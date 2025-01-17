package com.artoch.tlm_app;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.SocketException;
import java.net.URL;
import java.util.ResourceBundle;

public class TLMController implements Initializable, Observer {

    TelemetryService telemetryService = new TelemetryService(this);
    private boolean receiving = false;

    @FXML
    private TableView<DataModel> tableView;

    @FXML
    private TableColumn<DataModel, Long> counter;
    @FXML
    private TableColumn<DataModel, String> time;
    @FXML
    private TableColumn<DataModel, Double> sinus;
    @FXML
    private TableColumn<DataModel, Integer> checksum;
    @FXML
    private TableColumn<DataModel, Boolean> checksumStatus;

    @FXML
    private ToggleButton startStopButton;

    public TLMController() throws SocketException {
    }


    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startStopButton.setStyle("-fx-background-color: palegreen;");

        counter.setCellValueFactory(new PropertyValueFactory<>("counter"));
        time.setCellValueFactory(new PropertyValueFactory<>("time"));
        sinus.setCellValueFactory(new PropertyValueFactory<>("sinus"));
        checksum.setCellValueFactory(new PropertyValueFactory<>("checksum"));
        checksumStatus.setCellValueFactory(new PropertyValueFactory<>("checksumStatus"));

        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(DataModel item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else if (item.getHighlightColour().equals("lightpink")) {
                    setStyle("-fx-background-color: lightpink;");
                } else if (item.getHighlightColour().equals("khaki")) {
                    setStyle("-fx-background-color: khaki;");
                } else {
                    setStyle("");
                }
            }
        });
    }


    @Override
    public void update(DataModel dataModel) {
        Platform.runLater(() -> {
            tableView.getItems().add(dataModel);
            tableView.scrollTo(tableView.getItems().size() - 1);
        });
    }


    @FXML
    public void onToggleReceiving() {
        if (receiving) {
            telemetryService.stopDataReceiving();
            startStopButton.setStyle("-fx-background-color: palegreen;");
            startStopButton.setText("Start Receiving");
        } else {
            telemetryService.startDataReceiving();
            startStopButton.setStyle("-fx-background-color: pink;");
            startStopButton.setText("Stop Receiving");
        }
        receiving = !receiving;
    }


}

