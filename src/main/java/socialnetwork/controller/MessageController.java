package socialnetwork.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.repository.exceptions.RepositoryException;
import socialnetwork.service.UtilizatorService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageController {

    class UserCell extends ListCell<Utilizator> {
        HBox hbox = new HBox();
        Label label = new Label("(empty)");
        Pane pane1 = new Pane();
        Pane pane2= new Pane();
        Utilizator lastItem;
        ImageView imageView = new ImageView();

        public UserCell() {
            super();
            try {
                FileInputStream inputstream = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\account.png");
                Image image = new Image(inputstream);
                imageView.setImage(image);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            }
            imageView.setFitWidth(25);
            imageView.setFitHeight(25);
            pane1.setMinWidth(2);
            pane2.setMinWidth(2);

            hbox.getChildren().addAll(imageView, pane1, label, pane2);
            hbox.setAlignment(Pos.CENTER);
            HBox.setHgrow(pane2, Priority.ALWAYS);
        }

        @Override
        protected void updateItem(Utilizator item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (empty) {
                lastItem = null;
                setGraphic(null);
            } else {
                lastItem = item;
                label.setText(item != null ? item.getFirstName() + ' ' + item.getLastName() : "");
                setGraphic(hbox);
            }
        }
    }


    UtilizatorService service;
    Stage dialogStage;
    Utilizator userConnected;

    ObservableList<Utilizator> usersModel = FXCollections.observableArrayList();

    @FXML
    TextField searchTextField;
    @FXML
    ListView<Utilizator> usersListView;
    @FXML
    TextArea textMessageArea;


    public void initialize(UtilizatorService UtilizatorService, Stage stage, Utilizator userConnected) {
        service = UtilizatorService;
        this.dialogStage=stage;
        this.dialogStage.setMinHeight(400);
        this.dialogStage.setMinWidth(600);
        this.userConnected = userConnected;

        usersListView.setCellFactory(param -> new UserCell());

        usersListView.setItems(usersModel);
        searchTextField.textProperty().addListener(x -> handleFilter());
    }

    @FXML
    public void handleFilter(){
        String name = searchTextField.getText();
        Iterable<Utilizator> filtered = service.searchByName(name);
        if(name.equals("")){
            usersModel.setAll(new ArrayList<Utilizator>());
        } else {
            List<Utilizator> filtered2 = StreamSupport
                    .stream(filtered.spliterator(), false)
                    .collect(Collectors.toList());
            usersModel.setAll(filtered2.
                    stream().
                    filter(u -> !u.getId().equals(userConnected.getId()))
                    .collect(Collectors.toList()));
        }
    }

    @FXML
    public void handleNewMessage(){
        Utilizator selected = usersListView.getSelectionModel().getSelectedItem();
        String messageText = textMessageArea.getText();

        if(selected != null){
            try{
                List<String> to = new ArrayList<>();
                to.add(selected.getId().toString());
                Message message = service.sendMessage(userConnected.getId().toString(), to, messageText);
                if(message == null){
                    dialogStage.close();
                } else {
                    MessageAlert.showErrorMessage(null, "Mesajul nu a fost trimis. Reincercati");
                }
            } catch(RepositoryException | ValidationException ex){
                MessageAlert.showErrorMessage(null, ex.getMessage());
            }
        } else {
            MessageAlert.showErrorMessage(null, "Nu ai selectat nimic");
        }
    }

}
