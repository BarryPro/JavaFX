package sample.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class Controller {
    @FXML
    Button submit;
    @FXML
    TextArea B;
    @FXML
    TextArea A;

    @FXML
    public void onButtonClick() {
        submit.setOnAction((actionEvent)->submit.setText("gh"));
        B.setText("belong hello world");
    }
}
