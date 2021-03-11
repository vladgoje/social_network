package socialnetwork.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import socialnetwork.domain.*;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import socialnetwork.service.UtilizatorService;
import socialnetwork.utils.observer.Observer;

import java.util.ArrayList;


public class FriendRequestsController implements Observer{

    ObservableList<RequestDTO> model = FXCollections.observableArrayList();

    @FXML
    private ListView<RequestDTO> requestsListView;

    private UtilizatorService service;
    Stage dialogStage;
    Utilizator userConnected;

    @FXML
    public void initialize(UtilizatorService service, Stage stage, Utilizator userConnected) {
        this.service = service;
        this.userConnected = userConnected;
        this.dialogStage=stage;
        this.dialogStage.setMinHeight(400);
        this.dialogStage.setMinWidth(600);
        service.addObserver(this);
        initModel();
        requestsListView.setItems(model);
    }


    private void initModel() {
        ArrayList<RequestDTO> requestDTOS = new ArrayList<>();
        Iterable<CererePrietenie> requests = service.getUserFriendRequests(userConnected.getId().toString());
        requests.forEach(r->{
            if(r.getId().getRight().equals(userConnected.getId())){
                Utilizator user1 = service.getUser(r.getId().getLeft().toString());
                Utilizator user2 = service.getUser(r.getId().getRight().toString());
                requestDTOS.add(new RequestDTO(user1, user2, r.getDate(), r.getStatus()));
            }
        });
        model.setAll(requestDTOS);
    }

    @FXML
    public void handleAccept(){
        RequestDTO selected = requestsListView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            if(selected.getStatus().equals("pending")) {
                service.acceptFriendRequest(selected.getUser1().getId().toString(), selected.getUser2().getId().toString());
            }
        } else {
            MessageAlert.showErrorMessage(null, "Nu ai selectat nimic");
        }
    }

    @FXML
    public void handleDecline(){
        RequestDTO selected = requestsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if(selected.getStatus().equals("pending")) {
                service.rejectFriendRequest(selected.getUser1().getId().toString(), selected.getUser2().getId().toString());
            }
        } else {
            MessageAlert.showErrorMessage(null, "Nu ai selectat nimic");
        }
    }


    @Override
    public void update() {
        initModel();
    }
}
