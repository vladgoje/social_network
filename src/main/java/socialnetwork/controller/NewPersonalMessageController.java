package socialnetwork.controller;

import javafx.fxml.FXML;

import javafx.scene.control.*;

import javafx.stage.Stage;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.repository.exceptions.RepositoryException;
import socialnetwork.service.UtilizatorService;

import java.util.ArrayList;
import java.util.List;


public class NewPersonalMessageController {
    UtilizatorService service;
    Stage dialogStage;
    Utilizator toUser;
    Utilizator userConnected;

    @FXML
    Label sendMessageLabel;
    @FXML
    TextArea textMessageArea;

    public void initialize(UtilizatorService UtilizatorService, Stage stage, Utilizator to, Utilizator userConnected) {
        service = UtilizatorService;
        this.userConnected = userConnected;
        this.dialogStage=stage;
        this.dialogStage.setMinHeight(400);
        this.dialogStage.setMinWidth(600);
        this.toUser = to;
        sendMessageLabel.setText("Send Message - " + to.getFirstName() + ' ' + to.getLastName());
    }

    @FXML
    public void handleNewPersonalMessage(){
        String messageText = textMessageArea.getText();
        try{
            List<String> to = new ArrayList<>();
            to.add(toUser.getId().toString());
            Message message = service.sendMessage(userConnected.getId().toString(), to, messageText);
            if(message == null){
                dialogStage.close();
            } else {
                MessageAlert.showErrorMessage(null, "Mesajul nu a fost trimis. Reincercati");
            }
        } catch(RepositoryException | ValidationException ex){
            MessageAlert.showErrorMessage(null, ex.getMessage());
        }
    }

}
